/*
 * **************************************************-
 * ingrid-interface-search
 * ==================================================
 * Copyright (C) 2014 - 2019 wemove digital solutions GmbH
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

import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.detect.Detector;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import de.ingrid.iface.atomDownloadService.om.Category;
import de.ingrid.iface.atomDownloadService.om.DatasetFeedEntry;
import de.ingrid.iface.atomDownloadService.om.Link;
import de.ingrid.iface.util.StringUtils;
import de.ingrid.utils.xml.IDFNamespaceContext;
import de.ingrid.utils.xpath.XPathUtils;

@Service
public class DefaultDatasetFeedEntryProducer implements DatasetFeedEntryProducer {

    private static final XPathUtils XPATH = new XPathUtils(new IDFNamespaceContext());

    public static final String XPATH_DOWNLOAD_LINK = "//gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine[.//gmd:function/gmd:CI_OnLineFunctionCode/@codeListValue='Download of data' or .//gmd:function/gmd:CI_OnLineFunctionCode/@codeListValue='download' or .//gmd:function/gmd:CI_OnLineFunctionCode/@codeListValue='Datendownload']";

    private TikaConfig tikaConfig = TikaConfig.getDefaultConfig();
    private Detector detector = tikaConfig.getDetector();

    private final static Log log = LogFactory.getLog(DefaultDatasetFeedEntryProducer.class);

    public List<DatasetFeedEntry> produce(Document doc) throws Exception {
        List<DatasetFeedEntry> results = new ArrayList<DatasetFeedEntry>();

        NodeList nl = XPATH.getNodeList(doc, "//gmd:referenceSystemInfo/gmd:MD_ReferenceSystem/gmd:referenceSystemIdentifier/gmd:RS_Identifier");
        List<Category> catList = new ArrayList<Category>();
        for (int i = 0; i < nl.getLength(); i++) {
            String refSystemCode = XPATH.getString(nl.item(i), "gmd:code/gco:CharacterString");
            String epsgNumber = StringUtils.extractEpsgCodeNumber(refSystemCode);
            Category cat = new Category();
            cat.setLabel(refSystemCode);
            if (epsgNumber != null) {
                cat.setTerm("EPSG:" + epsgNumber);
            } else {
                cat.setTerm(XPATH.getString(nl.item(i), "gmd:codeSpace/gco:CharacterString"));
            }
            catList.add(cat);
        }

        NodeList linkages = XPATH.getNodeList(doc, XPATH_DOWNLOAD_LINK);
        for (int i = 0; i < linkages.getLength(); i++) {
            DatasetFeedEntry entry = new DatasetFeedEntry();

            String linkage = XPATH.getString(linkages.item(i), ".//gmd:linkage/gmd:URL");
            List<Link> links = new ArrayList<Link>();
            Link link = new Link();
            link.setHref(linkage);
            link.setRel("alternate");
            try {

                TikaInputStream stream = TikaInputStream.get(new URL(link.getHref()));

                Metadata metadata = new Metadata();
                metadata.add(Metadata.RESOURCE_NAME_KEY, link.getHref());
                MediaType mediaType = detector.detect(stream, metadata);
                link.setType(mediaType.toString());
            } catch (UnknownHostException e) {
                log.info("Invalid download url: " + link.getHref());
                continue;
            } catch (Exception e) {
                link.setType("application/octet-stream");
            }
            String name = XPATH.getString(linkages.item(i), ".//gmd:name//gco:CharacterString");
            if (name == null) {
                name = link.getHref();
            }
            link.setTitle(name);
            if (entry.getTitle() == null) {
                entry.setTitle(name);
            }
            links.add(link);
            entry.setLinks(links);
            entry.setId(link.getHref());
            entry.setCrs(catList);

            results.add(entry);
        }

        return results;
    }

}
