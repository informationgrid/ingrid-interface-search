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

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.ingrid.iface.atomDownloadService.om.Author;
import de.ingrid.iface.atomDownloadService.om.DatasetFeed;
import de.ingrid.iface.atomDownloadService.om.DatasetFeedEntry;
import de.ingrid.iface.atomDownloadService.om.Link;
import de.ingrid.iface.atomDownloadService.om.ServiceFeedEntry.EntryType;
import de.ingrid.iface.atomDownloadService.requests.DatasetFeedRequest;
import de.ingrid.iface.util.SearchInterfaceConfig;
import de.ingrid.iface.util.StringUtils;
import de.ingrid.iface.util.URLUtil;
import de.ingrid.utils.queryparser.ParseException;
import de.ingrid.utils.xml.IDFNamespaceContext;
import de.ingrid.utils.xpath.XPathUtils;

@Service
public class DatasetFeedProducer {

    private static final XPathUtils XPATH = new XPathUtils(new IDFNamespaceContext());

    private DatasetFeedFactory datasetFeedFactory;

    private SearchInterfaceConfig config;

    private List<DatasetFeedEntryProducer> datasetFeedEntryProducer;

    private String atomDownloadDatasetFeedUrlPattern = null;

    private String atomDownloadServiceFeedUrlPattern = null;

    private final static Log log = LogFactory.getLog(DatasetFeedProducer.class);

    @PostConstruct
    public void init() {

        atomDownloadDatasetFeedUrlPattern = org.apache.commons.lang.StringUtils.stripEnd(config.getString(SearchInterfaceConfig.ATOM_DOWNLOAD_SERVICE_URL), "/");
        atomDownloadDatasetFeedUrlPattern += config.getString(SearchInterfaceConfig.ATOM_DOWNLOAD_DATASET_FEED_EXTENSION);

        atomDownloadServiceFeedUrlPattern = org.apache.commons.lang.StringUtils.stripEnd(config.getString(SearchInterfaceConfig.ATOM_DOWNLOAD_SERVICE_URL), "/");
        atomDownloadServiceFeedUrlPattern += config.getString(SearchInterfaceConfig.ATOM_DOWNLOAD_SERVICE_FEED_EXTENSION);
    }

    public DatasetFeed produce(DatasetFeedRequest datasetFeedRequest) throws ParseException, Exception {

        if (log.isDebugEnabled()) {
            log.debug("Build dataset feed for dataset: " + datasetFeedRequest.toString());
        }

        DatasetFeed datasetFeed = null;

        Document doc = datasetFeedFactory.getDatasetFeedDocument(datasetFeedRequest);
        if (doc != null) {

            datasetFeed = new DatasetFeed();

            datasetFeed.setUuid(XPATH.getString(doc, "//gmd:fileIdentifier/gco:CharacterString"));
            datasetFeed.setTitle(XPATH.getString(doc, "//gmd:identificationInfo//gmd:citation/gmd:CI_Citation/gmd:title/gco:CharacterString"));
            datasetFeed.setSubTitle(XPATH.getString(doc, "//gmd:identificationInfo//gmd:abstract/gco:CharacterString"));

            Link link = new Link();
            String urlPattern = URLUtil.updateProtocol( atomDownloadDatasetFeedUrlPattern, datasetFeedRequest.getProtocol() );
            link.setHref(urlPattern.replace("{datasetfeed-uuid}", StringUtils.encodeForPath(datasetFeedRequest.getDatasetFeedUuid())).replace("{servicefeed-uuid}", StringUtils.encodeForPath(datasetFeedRequest.getServiceFeedUuid())));
            link.setHrefLang("de");
            link.setType("application/atom+xml");
            link.setRel("self");
            datasetFeed.setSelfReferencingLink(link);
            datasetFeed.setIdentifier(link.getHref());

            link = new Link();
            if (datasetFeedRequest.getType().equals(EntryType.CSW)) {
                link.setHref(datasetFeedRequest.getMetadataUrl());
            } else {
                link.setHref(config.getString(SearchInterfaceConfig.METADATA_ACCESS_URL).replace("{uuid}", datasetFeed.getUuid()));
            }
            link.setRel("describedby");
            link.setType("application/vnd.ogc.csw.GetRecordByIdResponse_xml");
            datasetFeed.setDescribedBy(new ArrayList<Link>());
            datasetFeed.getDescribedBy().add(link);
            
            link = new Link();
            urlPattern = URLUtil.updateProtocol( atomDownloadServiceFeedUrlPattern, datasetFeedRequest.getProtocol() );
            link.setHref(urlPattern.replace("{servicefeed-uuid}", StringUtils.encodeForPath(datasetFeedRequest.getServiceFeedUuid())));
            link.setHrefLang("de");
            link.setType("application/atom+xml");
            link.setRel("up");
            link.setTitle("The parent service feed document.");
            datasetFeed.setDownloadServiceFeed(link);

            // Only use useLimitations and ignore other restrictions!
            // -> REDMINE-348
            //NodeList resourceConstraints = XPATH.getNodeList(doc, "//gmd:identificationInfo/*/gmd:resourceConstraints[*/gmd:accessConstraints]");
            NodeList resourceConstraints = XPATH.getNodeList(doc, "//gmd:identificationInfo/*/gmd:resourceConstraints");
            StringBuilder copyRight = new StringBuilder();
            for (int i=0; i< resourceConstraints.getLength(); i++) {
                Node resourceConstraint = resourceConstraints.item(i);
                
                String useLimitations = XPATH.getString(resourceConstraint, "*/gmd:useLimitation/gco:CharacterString");
                if (useLimitations != null && useLimitations.length() > 0) {
                    if (copyRight.length() > 0) {
                        copyRight.append("; ");
                    }
                    copyRight.append(useLimitations);
                }
                // String restrictionCode = XPATH.getString(resourceConstraint, "*/gmd:accessConstraints/*/@codeListValue");
                // if (copyRight.length() > 0) {
                //     copyRight.append("; ");
                // }
                // if (restrictionCode.equalsIgnoreCase("otherRestrictions")) {
                //     String otherRestrictions = XPATH.getString(resourceConstraint, "*/gmd:otherConstraints/gco:CharacterString");
                //     if (otherRestrictions != null && otherRestrictions.length() > 0) {
                //         copyRight.append(otherRestrictions);
                //     }
                // } else {
                //     copyRight.append(restrictionCode);
                // }
            }
            datasetFeed.setRights(copyRight.toString());
            datasetFeed.setUpdated(XPATH.getString(doc, "//gmd:dateStamp/gco:DateTime | //gmd:dateStamp/gco:Date[not(../gco:DateTime)]"));

            Author author = new Author();
            author.setName(XPATH.getString(doc, "//gmd:identificationInfo//gmd:pointOfContact//gmd:organisationName/gco:CharacterString"));
            author.setEmail(XPATH.getString(doc, "//gmd:identificationInfo//gmd:pointOfContact//gmd:contactInfo/gmd:CI_Contact/gmd:address/gmd:CI_Address/gmd:electronicMailAddress/gco:CharacterString"));
            datasetFeed.setAuthor(author);

            List<DatasetFeedEntry> entryList = new ArrayList<DatasetFeedEntry>();
            for (DatasetFeedEntryProducer producer : datasetFeedEntryProducer) {
                entryList.addAll(producer.produce(doc));
            }
            
            // add link to portal detail page if requested
            if (datasetFeedRequest.isDetail()) {
                String detailUrl = SearchInterfaceConfig.getInstance().getString( SearchInterfaceConfig.METADATA_DETAILS_URL );
                Link detailLink = new Link();
                detailLink.setRel( "detail" );
                detailLink.setHref( detailUrl + "?docuuid=" + datasetFeed.getUuid() );
                datasetFeed.setDetailLink(detailLink);
            }

            datasetFeed.setEntries(entryList);
        }

        return datasetFeed;

    }

    @Autowired
    public void setDatasetFeedFactory(DatasetFeedFactory datasetFeedFactory) {
        this.datasetFeedFactory = datasetFeedFactory;
    }

    @Autowired
    public void setConfig(SearchInterfaceConfig config) {
        this.config = config;
    }

    @Autowired
    public void setDatasetFeedEntryProducer(List<DatasetFeedEntryProducer> datasetFeedEntryProducer) {
        this.datasetFeedEntryProducer = datasetFeedEntryProducer;
    }

}
