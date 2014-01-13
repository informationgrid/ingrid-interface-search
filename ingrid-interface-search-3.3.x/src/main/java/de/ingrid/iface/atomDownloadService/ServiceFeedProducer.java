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
import de.ingrid.iface.atomDownloadService.om.Link;
import de.ingrid.iface.atomDownloadService.om.ServiceFeed;
import de.ingrid.iface.atomDownloadService.om.ServiceFeedEntry;
import de.ingrid.iface.atomDownloadService.requests.ServiceFeedRequest;
import de.ingrid.iface.atomDownloadService.util.IngridQueryProducer;
import de.ingrid.iface.util.IBusHelper;
import de.ingrid.iface.util.IBusQueryResultIterator;
import de.ingrid.iface.util.IdfUtils;
import de.ingrid.iface.util.SearchInterfaceConfig;
import de.ingrid.iface.util.StringUtils;
import de.ingrid.utils.IBus;
import de.ingrid.utils.IngridHit;
import de.ingrid.utils.queryparser.ParseException;
import de.ingrid.utils.xml.IDFNamespaceContext;
import de.ingrid.utils.xpath.XPathUtils;

@Service
public class ServiceFeedProducer {

    private static final String[] REQUESTED_FIELDS = new String[] {};
    private static final XPathUtils XPATH = new XPathUtils(new IDFNamespaceContext());

    private IngridQueryProducer ingridQueryProducer;

    private List<ServiceFeedEntryProducer> serviceFeedEntryProducer;

    private SearchInterfaceConfig config;

    private IBusHelper iBusHelper;

    private String atomDownloadServiceFeedUrlPattern = null;

    private String atomDownloadOpensearchDefinitionUrlPattern = null;

    private final static Log log = LogFactory.getLog(ServiceFeedProducer.class);

    @PostConstruct
    public void init() {

        atomDownloadServiceFeedUrlPattern = org.apache.commons.lang.StringUtils.stripEnd(config.getString(SearchInterfaceConfig.ATOM_DOWNLOAD_SERVICE_URL), "/");
        atomDownloadServiceFeedUrlPattern += config.getString(SearchInterfaceConfig.ATOM_DOWNLOAD_SERVICE_FEED_EXTENSION);

        atomDownloadOpensearchDefinitionUrlPattern = org.apache.commons.lang.StringUtils.stripEnd(config.getString(SearchInterfaceConfig.ATOM_DOWNLOAD_SERVICE_URL), "/");
        atomDownloadOpensearchDefinitionUrlPattern += config.getString(SearchInterfaceConfig.ATOM_DOWNLOAD_OPENSEARCH_DEFINITION_EXTENSION);
    }

    public ServiceFeed produce(ServiceFeedRequest serviceFeedRequest) throws ParseException, Exception {

        if (log.isDebugEnabled()) {
            log.debug("Build service feed from IGC resource for service: " + serviceFeedRequest.getUuid());
        }

        ServiceFeed serviceFeed = null;
        IBus iBus = iBusHelper.getIBus();

        // create response header
        IBusQueryResultIterator serviceIterator = new IBusQueryResultIterator(ingridQueryProducer.createServiceFeedInGridQuery(serviceFeedRequest), REQUESTED_FIELDS, iBus);
        if (serviceIterator.hasNext()) {
            serviceFeed = new ServiceFeed();
            IngridHit hit = serviceIterator.next();
            if (log.isDebugEnabled()) {
                log.debug("Found valid service: " + hit.getHitDetail().getTitle());
            }

            Document idfDoc = IdfUtils.getIdfDocument(iBus.getRecord(hit));
            serviceFeed.setUuid(XPATH.getString(idfDoc, "//gmd:fileIdentifier/gco:CharacterString"));
            serviceFeed.setTitle(XPATH.getString(idfDoc, "//gmd:identificationInfo//gmd:citation/gmd:CI_Citation/gmd:title/gco:CharacterString"));
            serviceFeed.setSubTitle(XPATH.getString(idfDoc, "//gmd:identificationInfo//gmd:abstract/gco:CharacterString"));
            serviceFeed.setUpdated(XPATH.getString(idfDoc, "//gmd:dateStamp/gco:DateTime | //gmd:dateStamp/gco:Date[not(../gco:DateTime)]"));

            NodeList resourceConstraints = XPATH.getNodeList(idfDoc, "//gmd:identificationInfo/*/gmd:resourceConstraints[*/gmd:accessConstraints]");
            StringBuilder copyRight = new StringBuilder();
            for (int i = 0; i < resourceConstraints.getLength(); i++) {
                Node resourceConstraint = resourceConstraints.item(i);
                String restrictionCode = XPATH.getString(resourceConstraint, "*/gmd:accessConstraints/*/@codeListValue");
                if (copyRight.length() > 0) {
                    copyRight.append("; ");
                }
                copyRight.append(restrictionCode);
                if (restrictionCode.equalsIgnoreCase("otherRestrictions")) {
                    String otherRestrictions = XPATH.getString(resourceConstraint, "*/gmd:otherConstraints/gco:CharacterString");
                    if (otherRestrictions != null && otherRestrictions.length() > 0) {
                        copyRight.append(": ").append(otherRestrictions);
                    }
                }
            }
            serviceFeed.setCopyright(copyRight.toString());

            Link link = new Link();
            link.setHref(config.getString(SearchInterfaceConfig.METADATA_ACCESS_URL).replace("{uuid}", serviceFeed.getUuid()));
            link.setRel("describedby");
            link.setType("application/vnd.ogc.csw.GetRecordByIdResponse_xml");
            serviceFeed.setMetadataAccessUrl(link);

            link = new Link();
            link.setHref(atomDownloadServiceFeedUrlPattern.replace("{servicefeed-uuid}", StringUtils.encodeForPath(serviceFeed.getUuid())));
            link.setHrefLang("de");
            link.setType("application/atom+xml");
            link.setRel("self");
            link.setTitle("Feed containing the dataset (in one or more downloadable formats)");
            serviceFeed.setSelfReferencingLink(link);
            serviceFeed.setIdentifier(link.getHref());

            link = new Link();
            link.setHref(atomDownloadOpensearchDefinitionUrlPattern.replace("{servicefeed-uuid}", StringUtils.encodeForPath(serviceFeed.getUuid())));
            link.setHrefLang("de");
            link.setType("application/opensearchdescription+xml");
            link.setTitle("Open Search Description");
            link.setRel("search");
            serviceFeed.setOpenSearchDefinitionLink(link);

            Author author = new Author();
            author.setName(XPATH.getString(idfDoc, "//gmd:identificationInfo//gmd:pointOfContact//gmd:organisationName/gco:CharacterString"));
            author.setEmail(XPATH.getString(idfDoc, "//gmd:identificationInfo//gmd:pointOfContact//gmd:contactInfo/gmd:CI_Contact/gmd:address/gmd:CI_Address/gmd:electronicMailAddress/gco:CharacterString"));
            serviceFeed.setAuthor(author);

            List<ServiceFeedEntry> entryList = new ArrayList<ServiceFeedEntry>();
            for (ServiceFeedEntryProducer producer : serviceFeedEntryProducer) {
                entryList.addAll(producer.produce(idfDoc, serviceFeed, serviceFeedRequest));
            }

            serviceFeed.setEntries(entryList);
        }

        return serviceFeed;

    }

    @Autowired
    public void setIngridQueryProducer(IngridQueryProducer ingridQueryProducer) {
        this.ingridQueryProducer = ingridQueryProducer;
    }

    @Autowired
    public void setServiceFeedEntryProducer(List<ServiceFeedEntryProducer> serviceFeedEntryProducer) {
        this.serviceFeedEntryProducer = serviceFeedEntryProducer;
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
