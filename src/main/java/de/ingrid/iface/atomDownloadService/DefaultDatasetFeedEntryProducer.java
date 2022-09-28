/*
 * **************************************************-
 * ingrid-interface-search
 * ==================================================
 * Copyright (C) 2014 - 2022 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.1 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl5
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * **************************************************#
 */
package de.ingrid.iface.atomDownloadService;

import de.ingrid.iface.atomDownloadService.om.Category;
import de.ingrid.iface.atomDownloadService.om.DatasetFeedEntry;
import de.ingrid.iface.atomDownloadService.om.Link;
import de.ingrid.iface.util.StringUtils;
import de.ingrid.iface.util.URLUtil;
import de.ingrid.utils.xml.IDFNamespaceContext;
import de.ingrid.utils.xpath.XPathUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.detect.Detector;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

@Service
public class DefaultDatasetFeedEntryProducer implements DatasetFeedEntryProducer {

    private static final XPathUtils XPATH = new XPathUtils(new IDFNamespaceContext());

    public static final String XPATH_DOWNLOAD_LINK = "//gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine[.//gmd:function/gmd:CI_OnLineFunctionCode/@codeListValue='Download of data' or .//gmd:function/gmd:CI_OnLineFunctionCode/@codeListValue='download' or .//gmd:function/gmd:CI_OnLineFunctionCode/@codeListValue='Datendownload']";
    public static final String XPATH_SYSTEM_IDENTIFIER = "//gmd:referenceSystemInfo/gmd:MD_ReferenceSystem/gmd:referenceSystemIdentifier/gmd:RS_Identifier";

    private TikaConfig tikaConfig = TikaConfig.getDefaultConfig();
    private Detector detector = tikaConfig.getDetector();

    private final static Log log = LogFactory.getLog(DefaultDatasetFeedEntryProducer.class);

    public List<DatasetFeedEntry> produce(Document doc) throws Exception {
        ArrayList<DatasetFeedEntry> results = new ArrayList<>();

        NodeList linkages = XPATH.getNodeList(doc, XPATH_DOWNLOAD_LINK);
        for (int i = 0; i < linkages.getLength(); i++) {
            String url = XPATH.getString(linkages.item(i), ".//gmd:linkage/gmd:URL");
            String type = getTypeByLinkage(url, linkages.item(i));

            if (type == null) continue;
            if (type.equals("application/atom+xml")) {
                // produce multiple entries by atom feeds or relative urls
                ArrayList<DatasetFeedEntry> entries = produceEntriesByAtom(url);
                results.addAll(entries);
            } else {
                // produce a single entry by its type
                DatasetFeedEntry entry = produceEntry(url, type, linkages.item(i), doc);
                results.add(entry);
            }
        }

        return results;
    }

    private String getTypeByLinkage(String url, Object linkage) {
        try {
            String redirectedUrl = URLUtil.getRedirectedUrl(url);
            TikaInputStream stream = TikaInputStream.get(new URL(redirectedUrl));
            Metadata metadata = new Metadata();
            metadata.add(Metadata.RESOURCE_NAME_KEY, redirectedUrl);
            String type = detector.detect(stream, metadata).toString();

            // if application profile (Datentyp) is GMD and the file is considered a ZIP then set
            // link mime-type to application/x-gmz
            // see https://redmine.informationgrid.eu/issues/1306
            String applicationProfile = XPATH.getString(linkage, ".//gmd:applicationProfile/gco:CharacterString");
            if (applicationProfile != null && applicationProfile.equalsIgnoreCase("gml")) {
                if (type != null &&
                    (type.equalsIgnoreCase("application/zip") ||
                     type.equalsIgnoreCase("application/gzip") ||
                     type.equalsIgnoreCase("application/x-zip-compressed"))
                ) {
                    type = "application/x-gmz";
                }
            }

            return type;
        } catch (UnknownHostException e) {
            log.info("Invalid download url: " + url);
            return null;
        } catch (Exception e) {
            log.error(e);
            return "application/octet-stream";
        }
    }

    private DatasetFeedEntry produceEntry(String url, String type, Object linkage, Document doc) {
        DatasetFeedEntry entry = new DatasetFeedEntry();

        // set link attributes
        Link link = new Link();
        link.setHref(url);
        link.setType(type);
        link.setRel("alternate");

        // set link title
        String name = XPATH.getString(linkage, ".//gmd:name//gco:CharacterString");
        if (name == null) name = url;
        link.setTitle(name);

        // set entry attributes
        ArrayList<Link> links = new ArrayList<>();
        links.add(link);
        entry.setTitle(name);
        entry.setLinks(links);
        entry.setId(url);

        // set entry categories
        NodeList nl = XPATH.getNodeList(doc, XPATH_SYSTEM_IDENTIFIER);
        ArrayList<Category> catList = getCategories(nl);
        entry.setCrs(catList);
        return entry;
    }

    private ArrayList<Category> getCategories(NodeList list) {
        ArrayList<Category> catList = new ArrayList<>();
        for (int i = 0; i < list.getLength(); i++) {
            String refSystemCode = XPATH.getString(list.item(i), "gmd:code/gco:CharacterString|gmd:code/gmx:Anchor");
            String epsgNumber = StringUtils.extractEpsgCodeNumber(refSystemCode);
            Category cat = new Category();
            cat.setLabel(refSystemCode);
            if (epsgNumber != null) {
                cat.setTerm("EPSG:" + epsgNumber);
            } else {
                cat.setTerm(XPATH.getString(list.item(i), "gmd:codeSpace/gco:CharacterString"));
            }
            catList.add(cat);
        }
        return catList;
    }

    // should produce more entries
    private ArrayList<DatasetFeedEntry> produceEntriesByAtom(String url) throws Exception {
        ArrayList<DatasetFeedEntry> entries = new ArrayList<>();
        ArrayList<String> urls = new ArrayList<>();
        urls.add(url);

        // recursive dissolving atom or relative url
        while (urls.size() > 0) {
            String redirectedUrl = URLUtil.getRedirectedUrl(urls.get(0));
            Document doc = StringUtils.urlToDocument(redirectedUrl, 1000, 1000);
            NodeList nodeList = doc.getElementsByTagName("entry");
            for (int i = 0; i < nodeList.getLength(); i++) {
                Element nodeEl = (Element) nodeList.item(i);
                Element linkEl = (Element) nodeEl.getElementsByTagName("link").item(0);
                String type = linkEl.getAttributeNode("type").getValue();
                String href = linkEl.getAttributeNode("href").getValue();
                if (type.equals("application/atom+xml")) {
                    // add url back to dissolving process
                    if (isRelativePath(href)) href = getBaseUrl(redirectedUrl) + href;
                    urls.add(href);
                } else {
                    // create entry by download
                    DatasetFeedEntry entry = new DatasetFeedEntry();
                    String title = linkEl.getAttributeNode("title").getValue();

                    // set link attributes
                    ArrayList<Link> links = new ArrayList<>();
                    Link link = new Link();
                    link.setHref(href);
                    link.setRel("alternate");
                    link.setType(type);
                    link.setTitle(title);
                    links.add(link);

                    // set entry attributes
                    entry.setTitle(title);
                    entry.setLinks(links);
                    entry.setId(href);

                    // set entry category
                    NodeList catNodes = nodeEl.getElementsByTagName("category");
                    ArrayList<Category> categories = getCategoriesByEl(catNodes);
                    if (categories.size() > 0) entry.setCrs(categories);

                    entries.add(entry);
                }
            }
            urls.remove(0);
        }

        return entries;
    }

    private ArrayList<Category> getCategoriesByEl(NodeList list) {
        ArrayList<Category> catList = new ArrayList<>();
        for (int i = 0; i < list.getLength(); i++) {
            try {
                Element catEl = (Element) list.item(i);
                Category cat = new Category();
                String term = catEl.getAttributeNode("term").getValue();
                cat.setTerm(term);
                String label = catEl.getAttributeNode("label").getValue();
                cat.setLabel(label);
                catList.add(cat);
            } catch (Exception ignored) {
            }
        }
        return catList;
    }

    private boolean isRelativePath(String url) {
        return !url.contains("http://") && !url.contains("https://");
    }

    private String getBaseUrl(String url) {
        int breakpoint = url.lastIndexOf("/");
        return breakpoint != -1 ? url.substring(0, breakpoint + 1) : url;
    }
}
