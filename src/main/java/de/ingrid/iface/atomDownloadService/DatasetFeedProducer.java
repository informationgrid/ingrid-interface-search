package de.ingrid.iface.atomDownloadService;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;

import de.ingrid.iface.atomDownloadService.om.Author;
import de.ingrid.iface.atomDownloadService.om.DatasetFeed;
import de.ingrid.iface.atomDownloadService.om.DatasetFeedEntry;
import de.ingrid.iface.atomDownloadService.om.Link;
import de.ingrid.iface.atomDownloadService.requests.DatasetFeedRequest;
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
public class DatasetFeedProducer {

    private static final String[] REQUESTED_FIELDS = new String[] {};
    private static final XPathUtils XPATH = new XPathUtils(new IDFNamespaceContext());

    @Autowired
    private IngridQueryProducer ingridQueryProducer;

    @Autowired
    private SearchInterfaceConfig config;

    @Autowired
    private List<DatasetFeedEntryProducer> datasetFeedEntryProducer;

    private String atomDownloadDatasetFeedUrlPattern = null;

    private String atomDownloadServiceFeedUrlPattern = null;

    @PostConstruct
    public void init() {

        atomDownloadDatasetFeedUrlPattern = org.apache.commons.lang.StringUtils.stripEnd(config.getString(SearchInterfaceConfig.ATOM_DOWNLOAD_SERVICE_URL), "/");
        atomDownloadDatasetFeedUrlPattern += config.getString(SearchInterfaceConfig.ATOM_DOWNLOAD_DATASET_FEED_EXTENSION);

        atomDownloadServiceFeedUrlPattern = org.apache.commons.lang.StringUtils.stripEnd(config.getString(SearchInterfaceConfig.ATOM_DOWNLOAD_SERVICE_URL), "/");
        atomDownloadServiceFeedUrlPattern += config.getString(SearchInterfaceConfig.ATOM_DOWNLOAD_SERVICE_FEED_EXTENSION);
    }

    public DatasetFeed produce(DatasetFeedRequest datasetFeedRequest) throws ParseException, Exception {

        DatasetFeed datasetFeed = new DatasetFeed();

        Document doc = null;

        if (datasetFeedRequest.getUuid().toLowerCase().contains("request=getrecordbyid")) {
            // ISO Metadaten
            doc = StringUtils.urlToDocument(datasetFeedRequest.getUuid());
        } else {
            // igc Metadaten
            IBus iBus = IBusHelper.getIBus();

            // create response header
            IBusQueryResultIterator serviceIterator = new IBusQueryResultIterator(ingridQueryProducer.createDatasetFeedInGridQuery(datasetFeedRequest.getUuid()), REQUESTED_FIELDS, iBus);
            IngridHit hit = null;
            if (serviceIterator.hasNext()) {
                hit = serviceIterator.next();
                doc = IdfUtils.getIdfDocument(iBus.getRecord(hit));
            } else {
                throw new Exception("No dataset with uuid '" + datasetFeedRequest.getUuid() + "' found in IGC catalogs.");
            }
        }

        datasetFeed.setUuid(XPATH.getString(doc, "//gmd:fileIdentifier/gco:CharacterString"));
        datasetFeed.setTitle(XPATH.getString(doc, "//gmd:identificationInfo//gmd:citation/gmd:CI_Citation/gmd:title/gco:CharacterString"));
        datasetFeed.setSubTitle(XPATH.getString(doc, "//gmd:identificationInfo//gmd:abstract/gco:CharacterString"));

        Link link = new Link();
        link.setHref(atomDownloadDatasetFeedUrlPattern.replace("{uuid}", datasetFeed.getUuid()));
        link.setHrefLang("de");
        link.setType("application/atom+xml");
        link.setRel("self");
        datasetFeed.setSelfReferencingLink(link);
        datasetFeed.setIdentifier(link.getHref());

        link = new Link();
        link.setHref(atomDownloadServiceFeedUrlPattern.replace("{uuid}", datasetFeedRequest.getServiceFeedUuid()));
        link.setHrefLang("de");
        link.setType("application/atom+xml");
        link.setRel("up");
        link.setTitle("The parent service feed document.");
        datasetFeed.setDownloadServiceFeed(link);

        datasetFeed.setRights(XPATH.getString(doc, "//gmd:identificationInfo/*/gmd:resourceConstraints/*/gmd:accessConstraints/*/@codeListValue"));
        datasetFeed.setUpdated(XPATH.getString(doc, "//gmd:dateStamp/gco:DateTime | //gmd:dateStamp/gco:Date[not(../gco:DateTime)]"));

        Author author = new Author();
        author.setName(XPATH.getString(doc, "//gmd:identificationInfo//gmd:pointOfContact//gmd:organisationName/gco:CharacterString"));
        author.setEmail(XPATH.getString(doc, "//gmd:identificationInfo//gmd:pointOfContact//gmd:contactInfo/gmd:CI_Contact/gmd:address/gmd:CI_Address/gmd:electronicMailAddress/gco:CharacterString"));
        datasetFeed.setAuthor(author);

        List<DatasetFeedEntry> entryList = new ArrayList<DatasetFeedEntry>();
        for (DatasetFeedEntryProducer producer : datasetFeedEntryProducer) {
            entryList.addAll(producer.produce(doc));
        }

        datasetFeed.setEntries(entryList);

        return datasetFeed;

    }

}
