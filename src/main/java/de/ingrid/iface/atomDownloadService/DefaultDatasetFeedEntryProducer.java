/*
 * **************************************************-
 * ingrid-interface-search
 * ==================================================
 * Copyright (C) 2014 - 2025 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.2 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl
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
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaCoreProperties;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

@Service
public class DefaultDatasetFeedEntryProducer implements DatasetFeedEntryProducer {

    private static final XPathUtils XPATH = new XPathUtils(new IDFNamespaceContext());

    public static final String XPATH_DOWNLOAD_LINK = "//gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine[.//gmd:function/gmd:CI_OnLineFunctionCode/@codeListValue='Download of data' or .//gmd:function/gmd:CI_OnLineFunctionCode/@codeListValue='download' or .//gmd:function/gmd:CI_OnLineFunctionCode/@codeListValue='Datendownload']";
    public static final String XPATH_SYSTEM_IDENTIFIER = "//gmd:referenceSystemInfo/gmd:MD_ReferenceSystem/gmd:referenceSystemIdentifier/gmd:RS_Identifier";

    private final TikaConfig tikaConfig = TikaConfig.getDefaultConfig();
    private final Detector detector = tikaConfig.getDetector();

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
                ArrayList<DatasetFeedEntry> entries = produceEntriesByAtomOrXml(url);
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
            Metadata metadata = new Metadata();
            metadata.set(TikaCoreProperties.RESOURCE_NAME_KEY, redirectedUrl);
            String type = detector.detect(null, metadata).toString();

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

    // the given url of an atom or xml will be recursively dissolved until the downloadable entries
    // are found. All entries will be grouped in an array and get returned.
    // this is a special handling for atom or xml that have child atom(s) or xml(s).
    private ArrayList<DatasetFeedEntry> produceEntriesByAtomOrXml(String url) throws Exception {
        ArrayList<DatasetFeedEntry> entries = new ArrayList<>();
        ArrayList<String> urls = new ArrayList<>();
        urls.add(url);

        // when a child atom or xml is found, it will be added back to the urls for dissolving.
        while (!urls.isEmpty()) {
            String redirectedUrl = URLUtil.getRedirectedUrl(urls.get(0));
            Document doc = StringUtils.urlToDocument(redirectedUrl, 1000, 1000);
            NodeList nodeList = doc.getElementsByTagName("entry");
            for (int i = 0; i < nodeList.getLength(); i++) {
                try {
                    Element entryEl = (Element) nodeList.item(i);
                    Element linkEl = getLinkElByEntry(entryEl);
                    String type = linkEl.getAttributeNode("type").getValue();
                    String href = linkEl.getAttributeNode("href").getValue();
                    if (isRelativePath(href)) href = getBaseUrl(redirectedUrl) + href;

                    // check if entry is a child atom or xml
                    if (type.equals("application/atom+xml")) {
                        // add url back to dissolving process
                        urls.add(href);
                        continue;
                    }

                    // create downloadable entry
                    DatasetFeedEntry entry = new DatasetFeedEntry();
                    String title = getDownloadTitleByEntry(entryEl);
                    if (title == null) title = href;

                    // set link attributes
                    ArrayList<Link> links = new ArrayList<>();
                    Link link = new Link();
                    link.setHref(href);
                    link.setRel("alternate");
                    link.setType(type);
                    link.setTitle(title);
                    links.add(link);

                    // set attributes
                    entry.setTitle(title);
                    entry.setLinks(links);
                    entry.setId(href);

                    // set categories
                    NodeList catNodes = entryEl.getElementsByTagName("category");
                    ArrayList<Category> categories = getCategoriesByEl(catNodes);
                    if (!categories.isEmpty()) entry.setCrs(categories);

                    entries.add(entry);
                } catch (Exception e) {
                    log.error(e);
                }
            }

            // remove processed url
            urls.remove(0);
        }

        return entries;
    }

    // get download title by a given entry. target attributes can be optional,
    // title will be therefore extracted by priority.
    // priority: title > link > id -> unknown
    private String getDownloadTitleByEntry(Element entry) {
        // get from title
        String title = getValueFromFirstEntryByTag("title", entry);
        if (title != null) return title;

        // get from link
        NodeList links = entry.getElementsByTagName("link");
        for (int i = 0; i < links.getLength(); i++) {
            Element link = (Element) links.item(i);
            Node linkTitle = link.getAttributeNode("title");
            if (linkTitle != null) return linkTitle.getNodeValue();
        }

        // get from id
        String id = getValueFromFirstEntryByTag("id", entry);
        return id != null ? id : "unknown";
    }

    // first entry by tag from a given element will be extracted.
    // when nothing is found, it returns null.
    private String getValueFromFirstEntryByTag(String tag, Element el) {
        NodeList tags = el.getElementsByTagName(tag);
        for (int i = 0; i < tags.getLength(); i++) {
            Element title = (Element) tags.item(0);
            String text = title.getTextContent();
            if (text == null) continue;
            // remove cdata if present
            text = text.replace("<![CDATA[", "");
            text = text.replace("]]>", "");
            return text;
        }
        return null;
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

    private Element getLinkElByEntry(Element nodeEl) {
        Element downloadLink = null;
        NodeList links = nodeEl.getElementsByTagName("link");
        for (int i = 0; i < links.getLength(); i++) {
            Element link = (Element) links.item(i);
            String rel = link.getAttributeNode("rel").getValue();
            if (!rel.equals("alternate")) continue;
            downloadLink = link;
            break;
        }
        return downloadLink;
    }

    private boolean isRelativePath(String url) {
        return !url.contains("http://") && !url.contains("https://");
    }

    private String getBaseUrl(String url) {
        int breakpoint = url.lastIndexOf("/");
        return breakpoint != -1 ? url.substring(0, breakpoint + 1) : url;
    }
}
