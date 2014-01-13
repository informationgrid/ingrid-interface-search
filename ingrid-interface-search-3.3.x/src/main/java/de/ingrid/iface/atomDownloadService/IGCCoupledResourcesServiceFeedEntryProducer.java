package de.ingrid.iface.atomDownloadService;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.ArrayUtils;
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
import de.ingrid.iface.atomDownloadService.util.IngridQueryProducer;
import de.ingrid.iface.util.IBusHelper;
import de.ingrid.iface.util.IBusQueryResultIterator;
import de.ingrid.iface.util.IdfUtils;
import de.ingrid.iface.util.SearchInterfaceConfig;
import de.ingrid.iface.util.StringUtils;
import de.ingrid.utils.IBus;
import de.ingrid.utils.IngridHit;
import de.ingrid.utils.xml.IDFNamespaceContext;
import de.ingrid.utils.xpath.XPathUtils;
import edu.emory.mathcs.backport.java.util.Arrays;

@Service
public class IGCCoupledResourcesServiceFeedEntryProducer implements ServiceFeedEntryProducer {

    private static final XPathUtils XPATH = new XPathUtils(new IDFNamespaceContext());
    private static final String[] REQUESTED_FIELDS = new String[] {};

    private IngridQueryProducer ingridQueryProducer;

    private SearchInterfaceConfig config;

    private IBusHelper iBusHelper;

    private final static Log log = LogFactory.getLog(IGCCoupledResourcesServiceFeedEntryProducer.class);

    private String atomDownloadDatasetFeedUrlPattern = null;

    @PostConstruct
    public void init() {

        atomDownloadDatasetFeedUrlPattern = org.apache.commons.lang.StringUtils.stripEnd(config.getString(SearchInterfaceConfig.ATOM_DOWNLOAD_SERVICE_URL), "/");
        atomDownloadDatasetFeedUrlPattern += config.getString(SearchInterfaceConfig.ATOM_DOWNLOAD_DATASET_FEED_EXTENSION);
    }

    public List<ServiceFeedEntry> produce(Document idfDoc, ServiceFeed serviceFeed, ServiceFeedRequest serviceFeedRequest) throws Exception {

        if (log.isDebugEnabled()) {
            log.debug("Build service feed entries from IGC resource for service: " + serviceFeed.getUuid());
        }

        IBus iBus = iBusHelper.getIBus();
        Document idfCoupledResourceDoc = null;

        List<ServiceFeedEntry> entryList = new ArrayList<ServiceFeedEntry>();
        String[] coupledDataUuids = XPATH.getStringArray(idfDoc, "//srv:operatesOn/@uuidref");
        String[] coupledOtherUuids = XPATH.getStringArray(idfDoc, "//idf:crossReference[./idf:attachedToField/@entry-id='9990' and ./idf:attachedToField/@list-id='2000']/@uuid");
        String[] coupledUuids = (String[]) ArrayUtils.addAll(coupledDataUuids, coupledOtherUuids);
        coupledUuids = (new HashSet<String>(Arrays.asList(coupledUuids))).toArray(new String[0]);
        
        
        if (coupledUuids.length == 0) {
            return entryList;
        }
        IBusQueryResultIterator serviceEntryIterator = new IBusQueryResultIterator(ingridQueryProducer.createServiceFeedEntryInGridQuery(coupledUuids, serviceFeedRequest), REQUESTED_FIELDS, iBus);
        List<String> coupledResourceUuids = new ArrayList<String>();
        while (serviceEntryIterator.hasNext()) {
            IngridHit hit = serviceEntryIterator.next();
            if (log.isDebugEnabled()) {
                log.debug("Found coupled resource: " + hit.getHitDetail().getTitle());
            }
            idfCoupledResourceDoc = IdfUtils.getIdfDocument(iBus.getRecord(hit));
            
            // do not process data with identical uuids (filter duplicates)
            String uuid = IdfUtils.getRecordId(idfCoupledResourceDoc).toString();
            if (coupledResourceUuids.contains(uuid)) {
                continue;
            }
            coupledResourceUuids.add(uuid);
            
            // check for data sets without data download links
            if (!XPATH.nodeExists(idfCoupledResourceDoc, "//gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine[.//gmd:function/gmd:CI_OnLineFunctionCode/@codeListValue='Download of data' or .//gmd:function/gmd:CI_OnLineFunctionCode/@codeListValue='download']")) {
                if (log.isDebugEnabled()) {
                    log.debug("No Download Data Links found in coupled resource: " + hit.getHitDetail().getTitle());
                }
                continue;
            }

            ServiceFeedEntry entry = new ServiceFeedEntry();
            entry.setType(EntryType.IGC);
            entry.setUuid(XPATH.getString(idfCoupledResourceDoc, "//gmd:fileIdentifier/gco:CharacterString"));
            entry.setTitle(XPATH.getString(idfCoupledResourceDoc, "//gmd:identificationInfo//gmd:citation/gmd:CI_Citation/gmd:title/gco:CharacterString"));
            entry.setSummary(XPATH.getString(idfCoupledResourceDoc, "//gmd:identificationInfo//gmd:abstract/gco:CharacterString"));

            Link link = new Link();
            link.setHref(config.getString(SearchInterfaceConfig.METADATA_ACCESS_URL).replace("{uuid}", entry.getUuid()));
            link.setRel("describedby");
            link.setType("application/vnd.ogc.csw.GetRecordByIdResponse_xml");
            entry.setDatasetMetadataRecord(link);

            link = new Link();
            link.setHref(atomDownloadDatasetFeedUrlPattern.replace("{datasetfeed-uuid}", StringUtils.encodeForPath(entry.getUuid())).replace("{servicefeed-uuid}", StringUtils.encodeForPath(serviceFeed.getUuid())));
            link.setHrefLang("de");
            link.setType("application/atom+xml");
            link.setRel("alternate");
            entry.setDatasetFeed(link);
            entry.setDatasetIdentifier(link.getHref());

            String code = XPATH.getString(idfCoupledResourceDoc, "//gmd:identificationInfo//gmd:citation//gmd:identifier/gmd:MD_Identifier/gmd:code/gco:CharacterString");
            if (code != null) {
                String[] codeParts = code.split("#");
                if (codeParts.length == 2) {
                    entry.setSpatialDatasetIdentifierCode(codeParts[1]);
                    entry.setSpatialDatasetIdentifierNamespace(codeParts[0]);
                } else {
                    entry.setSpatialDatasetIdentifierCode(codeParts[0]);
                }
            }
            entry.setUpdated(XPATH.getString(idfCoupledResourceDoc, "//gmd:dateStamp/gco:DateTime | //gmd:dateStamp/gco:Date[not(../gco:DateTime)]"));

            NodeList resourceConstraints = XPATH.getNodeList(idfCoupledResourceDoc, "//gmd:identificationInfo/*/gmd:resourceConstraints[*/gmd:accessConstraints]");
            StringBuilder copyRight = new StringBuilder();
            for (int i=0; i< resourceConstraints.getLength(); i++) {
                Node resourceConstraint = resourceConstraints.item(i);
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
            author.setName(XPATH.getString(idfCoupledResourceDoc, "//gmd:identificationInfo//gmd:pointOfContact//gmd:organisationName/gco:CharacterString"));
            author.setEmail(XPATH.getString(idfCoupledResourceDoc, "//gmd:identificationInfo//gmd:pointOfContact//gmd:contactInfo/gmd:CI_Contact/gmd:address/gmd:CI_Address/gmd:electronicMailAddress/gco:CharacterString"));
            entry.setAuthor(author);

            entry.setPolygon(IdfUtils.getEnclosingBoundingBoxAsPolygon(idfCoupledResourceDoc));

            NodeList nl = XPATH.getNodeList(idfCoupledResourceDoc, "//gmd:referenceSystemInfo/gmd:MD_ReferenceSystem/gmd:referenceSystemIdentifier/gmd:RS_Identifier");
            List<Category> catList = new ArrayList<Category>();
            for (int i = 0; i < nl.getLength(); i++) {
                String refSystemCode = XPATH.getString(nl.item(i), "gmd:code/gco:CharacterString");
                String epsgNumber = StringUtils.extractEpsgCodeNumber(refSystemCode);
                Category cat = new Category();
                cat.setLabel(refSystemCode);
                if (epsgNumber != null) {
                    cat.setTerm("EPSG:"+ epsgNumber);
                } else {
                    cat.setTerm(XPATH.getString(nl.item(i), "gmd:codeSpace/gco:CharacterString"));
                }
                catList.add(cat);
            }
            entry.setCrs(catList);

            entryList.add(entry);
        }

        return entryList;
    }

    @Autowired
    public void setIngridQueryProducer(IngridQueryProducer ingridQueryProducer) {
        this.ingridQueryProducer = ingridQueryProducer;
    }

    @Autowired
    public void setConfig(SearchInterfaceConfig config) {
        this.config = config;
    }

    @Autowired
    public void setiBusHelper(IBusHelper iBusHelper) {
        this.iBusHelper = iBusHelper;
    }

}
