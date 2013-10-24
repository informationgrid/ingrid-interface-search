package de.ingrid.iface.atomDownloadService;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import de.ingrid.iface.atomDownloadService.om.Author;
import de.ingrid.iface.atomDownloadService.om.Category;
import de.ingrid.iface.atomDownloadService.om.Link;
import de.ingrid.iface.atomDownloadService.om.ServiceFeed;
import de.ingrid.iface.atomDownloadService.om.ServiceFeedEntry;
import de.ingrid.iface.atomDownloadService.om.ServiceFeedEntry.EntryType;
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

@Service
public class IGCCoupledResourcesServiceFeedEntryProducer implements ServiceFeedEntryProducer {

    private static final XPathUtils XPATH = new XPathUtils(new IDFNamespaceContext());
    private static final String[] REQUESTED_FIELDS = new String[] {};

    @Autowired
    private IngridQueryProducer ingridQueryProducer;

    @Autowired
    private SearchInterfaceConfig config;

    private final static Log log = LogFactory.getLog(IGCCoupledResourcesServiceFeedEntryProducer.class);

    private String atomDownloadDatasetFeedUrlPattern = null;

    @PostConstruct
    public void init() {

        atomDownloadDatasetFeedUrlPattern = org.apache.commons.lang.StringUtils.stripEnd(config.getString(SearchInterfaceConfig.ATOM_DOWNLOAD_SERVICE_URL), "/");
        atomDownloadDatasetFeedUrlPattern += config.getString(SearchInterfaceConfig.ATOM_DOWNLOAD_DATASET_FEED_EXTENSION);
    }

    public List<ServiceFeedEntry> produce(Document idfDoc, ServiceFeed serviceFeed) throws Exception {

        if (log.isDebugEnabled()) {
            log.debug("Build service feed entries from IGC resource for service: " + serviceFeed.getUuid());
        }

        IBus iBus = IBusHelper.getIBus();

        List<ServiceFeedEntry> entryList = new ArrayList<ServiceFeedEntry>();
        String[] coupledUuids = XPATH.getStringArray(idfDoc, "//srv:operatesOn/@uuidref");
        IBusQueryResultIterator serviceEntryIterator = new IBusQueryResultIterator(ingridQueryProducer.createServiceFeedEntryInGridQuery(coupledUuids), REQUESTED_FIELDS, iBus);
        while (serviceEntryIterator.hasNext()) {
            IngridHit hit = serviceEntryIterator.next();
            if (log.isDebugEnabled()) {
                log.debug("Found coupled resource: " + hit.getHitDetail().getTitle());
            }
            idfDoc = IdfUtils.getIdfDocument(iBus.getRecord(hit));
            // check for data sets without data download links
            if (!XPATH.nodeExists(idfDoc, "//gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine[.//gmd:function/gmd:CI_OnLineFunctionCode/@codeListValue='Download of data']")) {
                if (log.isDebugEnabled()) {
                    log.debug("No Download Data Links found in coupled resource: " + hit.getHitDetail().getTitle());
                }
                continue;
            }

            ServiceFeedEntry entry = new ServiceFeedEntry();
            entry.setType(EntryType.IGC);
            entry.setUuid(XPATH.getString(idfDoc, "//gmd:fileIdentifier/gco:CharacterString"));
            entry.setTitle(XPATH.getString(idfDoc, "//gmd:identificationInfo//gmd:citation/gmd:CI_Citation/gmd:title/gco:CharacterString"));
            entry.setSummary(XPATH.getString(idfDoc, "//gmd:identificationInfo//gmd:abstract/gco:CharacterString"));

            Link link = new Link();
            link.setHref(config.getString(SearchInterfaceConfig.METADATA_ACCESS_URL).replace("{uuid}", entry.getUuid()));
            link.setRel("describedby");
            link.setType("application/vnd.ogc.csw.GetRecordByIdResponse_xml");
            entry.setDatasetMetadataRecord(link);

            link = new Link();
            link.setHref(atomDownloadDatasetFeedUrlPattern.replace("{uuid}", StringUtils.encodeForPath(entry.getUuid())).replace("{servicefeed-uuid}", StringUtils.encodeForPath(serviceFeed.getUuid())));
            link.setHrefLang("en");
            link.setType("application/atom+xml");
            entry.setDatasetFeed(link);

            String code = XPATH.getString(idfDoc, "//gmd:identificationInfo//gmd:citation//gmd:identifier/gmd:MD_Identifier/gmd:code/gco:CharacterString");
            if (code != null) {
                String[] codeParts = code.split("#");
                entry.setSpatialDatasetIdentifierCode(codeParts[1]);
                entry.setSpatialDatasetIdentifierNamespace(codeParts[0]);
            }
            entry.setUpdated(XPATH.getString(idfDoc, "//gmd:dateStamp/gco:DateTime | //gmd:dateStamp/gco:Date[not(../gco:DateTime)]"));

            entry.setRights(XPATH.getString(idfDoc, "//gmd:identificationInfo/*/gmd:resourceConstraints/*/gmd:accessConstraints/*/@codeListValue"));

            Author author = new Author();
            author.setName(XPATH.getString(idfDoc, "//gmd:identificationInfo//gmd:pointOfContact//gmd:organisationName/gco:CharacterString"));
            author.setEmail(XPATH.getString(idfDoc, "//gmd:identificationInfo//gmd:pointOfContact//gmd:contactInfo/gmd:CI_Contact/gmd:address/gmd:CI_Address/gmd:electronicMailAddress/gco:CharacterString"));
            entry.setAuthor(author);

            entry.setPolygon(IdfUtils.getEnclosingBoundingBoxAsPolygon(idfDoc));

            NodeList nl = XPATH.getNodeList(idfDoc, "//gmd:referenceSystemInfo/gmd:MD_ReferenceSystem/gmd:referenceSystemIdentifier/gmd:RS_Identifier");
            List<Category> catList = new ArrayList<Category>();
            for (int i = 0; i < nl.getLength(); i++) {
                Category cat = new Category();
                cat.setLabel(XPATH.getString(nl.item(i), "gmd:code/gco:CharacterString"));
                cat.setTerm(XPATH.getString(nl.item(i), "gmd:codeSpace/gco:CharacterString"));
                catList.add(cat);
            }
            entry.setCrs(catList);

            entryList.add(entry);
        }

        return entryList;
    }

}
