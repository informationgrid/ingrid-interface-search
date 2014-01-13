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
import de.ingrid.iface.atomDownloadService.om.Category;
import de.ingrid.iface.atomDownloadService.om.Link;
import de.ingrid.iface.atomDownloadService.om.ServiceFeed;
import de.ingrid.iface.atomDownloadService.om.ServiceFeedEntry;
import de.ingrid.iface.atomDownloadService.om.ServiceFeedEntry.EntryType;
import de.ingrid.iface.atomDownloadService.requests.ServiceFeedRequest;
import de.ingrid.iface.util.IdfUtils;
import de.ingrid.iface.util.SearchInterfaceConfig;
import de.ingrid.iface.util.StringUtils;
import de.ingrid.utils.xml.IDFNamespaceContext;
import de.ingrid.utils.xpath.XPathUtils;

@Service
public class CSWGetRecordByIdServiceFeedEntryProducer implements ServiceFeedEntryProducer {

    private static final XPathUtils XPATH = new XPathUtils(new IDFNamespaceContext());

    private SearchInterfaceConfig config;

    private String atomDownloadDatasetFeedUrlPattern = null;

    private final static Log log = LogFactory.getLog(CSWGetRecordByIdServiceFeedEntryProducer.class);

    @PostConstruct
    public void init() {

        atomDownloadDatasetFeedUrlPattern = org.apache.commons.lang.StringUtils.stripEnd(config.getString(SearchInterfaceConfig.ATOM_DOWNLOAD_SERVICE_URL), "/");
        atomDownloadDatasetFeedUrlPattern += config.getString(SearchInterfaceConfig.ATOM_DOWNLOAD_DATASET_FEED_EXTENSION);
    }

    public List<ServiceFeedEntry> produce(Document idfDoc, ServiceFeed serviceFeed, ServiceFeedRequest serviceFeedRequest) throws Exception {

        if (log.isDebugEnabled()) {
            log.debug("Build service feed entries from IGC resource for service: " + serviceFeed.getUuid());
        }

        List<ServiceFeedEntry> entryList = new ArrayList<ServiceFeedEntry>();

        NodeList linkages = XPATH.getNodeList(idfDoc, "//gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine/idf:idfOnlineResource/gmd:linkage/gmd:URL");
        for (int i = 0; i < linkages.getLength(); i++) {
            String linkage = XPATH.getString(linkages.item(i), ".");
            if (linkage.toLowerCase().contains("request=getrecordbyid")) {
                if (log.isDebugEnabled()) {
                    log.debug("Found external coupled resource: " + linkage);
                }

                Integer connectionTimeout = config.getInt(SearchInterfaceConfig.ATOM_URL_CONNECTION_TIMEOUT, 1000);
                Integer readTimeout = config.getInt(SearchInterfaceConfig.ATOM_URL_READ_TIMEOUT, 1000);

                Document isoDoc = null;
                try {
                    isoDoc = StringUtils.urlToDocument(linkage, connectionTimeout, readTimeout);
                } catch (Exception e) {
                    log.error("Unable to obtain XML document from " + linkage, e);
                    continue;
                }
                    
                // check for data sets without data download links
                if (!XPATH.nodeExists(isoDoc,
                        "//gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine[.//gmd:function/gmd:CI_OnLineFunctionCode/@codeListValue='Download of data' or .//gmd:function/gmd:CI_OnLineFunctionCode/@codeListValue='download']")) {
                    if (log.isDebugEnabled()) {
                        log.debug("No Download Data Links found in coupled resource: " + linkage);
                    }
                    continue;
                }

                ServiceFeedEntry entry = new ServiceFeedEntry();
                entry.setType(EntryType.CSW);
                entry.setUuid(XPATH.getString(isoDoc, "//gmd:fileIdentifier/gco:CharacterString"));
                entry.setTitle(XPATH.getString(isoDoc, "//gmd:identificationInfo//gmd:citation/gmd:CI_Citation/gmd:title/gco:CharacterString"));
                entry.setSummary(XPATH.getString(isoDoc, "//gmd:identificationInfo//gmd:abstract/gco:CharacterString"));

                Link link = new Link();
                link.setHref(linkage);
                link.setRel("describedby");
                link.setType("application/vnd.ogc.csw.GetRecordByIdResponse_xml");
                entry.setDatasetMetadataRecord(link);

                link = new Link();
                link.setHref(atomDownloadDatasetFeedUrlPattern.replace("{datasetfeed-uuid}", StringUtils.encodeForPath(linkage)).replace("{servicefeed-uuid}", StringUtils.encodeForPath(serviceFeed.getUuid())));
                link.setHrefLang("de");
                link.setType("application/atom+xml");
                link.setRel("alternate");
                entry.setDatasetFeed(link);
                entry.setDatasetIdentifier(link.getHref());

                String code = XPATH.getString(isoDoc, "//gmd:identificationInfo//gmd:citation//gmd:identifier/gmd:MD_Identifier/gmd:code/gco:CharacterString");
                if (code != null) {
                    String[] codeParts = code.split("#");
                    if (codeParts.length == 2) {
                        entry.setSpatialDatasetIdentifierCode(codeParts[1]);
                        entry.setSpatialDatasetIdentifierNamespace(codeParts[0]);
                    } else {
                        entry.setSpatialDatasetIdentifierCode(codeParts[0]);
                    }
                }
                entry.setUpdated(XPATH.getString(isoDoc, "//gmd:dateStamp/gco:DateTime | //gmd:dateStamp/gco:Date[not(../gco:DateTime)]"));

                NodeList resourceConstraints = XPATH.getNodeList(isoDoc, "//gmd:identificationInfo/*/gmd:resourceConstraints[*/gmd:accessConstraints]");
                StringBuilder copyRight = new StringBuilder();
                for (int j = 0; j < resourceConstraints.getLength(); j++) {
                    Node resourceConstraint = resourceConstraints.item(j);
                    String restrictionCode = XPATH.getString(resourceConstraint, "*/gmd:accessConstraints/*/@codeListValue");
                    if (copyRight.length() > 0) {
                        copyRight.append("; ");
                    }
                    if (restrictionCode.equalsIgnoreCase("otherRestrictions")) {
                        String otherRestrictions = XPATH.getString(resourceConstraint, "*/gmd:otherConstraints/gco:CharacterString");
                        if (otherRestrictions != null && otherRestrictions.length() > 0) {
                            copyRight.append(otherRestrictions);
                        }
                    } else {
                        copyRight.append(restrictionCode);
                    }
                }
                entry.setRights(copyRight.toString());

                Author author = new Author();
                author.setName(XPATH.getString(isoDoc, "//gmd:identificationInfo//gmd:pointOfContact//gmd:organisationName/gco:CharacterString"));
                author.setEmail(XPATH.getString(isoDoc, "//gmd:identificationInfo//gmd:pointOfContact//gmd:contactInfo/gmd:CI_Contact/gmd:address/gmd:CI_Address/gmd:electronicMailAddress/gco:CharacterString"));
                entry.setAuthor(author);

                entry.setPolygon(IdfUtils.getEnclosingBoundingBoxAsPolygon(isoDoc));

                NodeList nl = XPATH.getNodeList(isoDoc, "//gmd:referenceSystemInfo/gmd:MD_ReferenceSystem/gmd:referenceSystemIdentifier/gmd:RS_Identifier");
                List<Category> catList = new ArrayList<Category>();
                for (int j = 0; j < nl.getLength(); j++) {
                    String refSystemCode = XPATH.getString(nl.item(j), "gmd:code/gco:CharacterString");
                    String epsgNumber = StringUtils.extractEpsgCodeNumber(refSystemCode);
                    Category cat = new Category();
                    cat.setLabel(refSystemCode);
                    if (epsgNumber != null) {
                        cat.setTerm("EPSG:" + epsgNumber);
                    } else {
                        cat.setTerm(XPATH.getString(nl.item(j), "gmd:codeSpace/gco:CharacterString"));
                    }
                    catList.add(cat);
                }
                entry.setCrs(catList);

                entryList.add(entry);
            }
        }

        return entryList;
    }

    @Autowired
    public void setConfig(SearchInterfaceConfig config) {
        this.config = config;
    }

}
