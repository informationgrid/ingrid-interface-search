package de.ingrid.iface.atomDownloadService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;

import de.ingrid.iface.atomDownloadService.om.ServiceFeed;
import de.ingrid.iface.atomDownloadService.om.ServiceFeedEntry;
import de.ingrid.iface.atomDownloadService.om.ServiceFeedEntry.EntryType;
import de.ingrid.iface.atomDownloadService.requests.DatasetFeedRequest;
import de.ingrid.iface.atomDownloadService.requests.ServiceFeedRequest;
import de.ingrid.iface.atomDownloadService.util.IngridQueryProducer;
import de.ingrid.iface.util.IBusHelper;
import de.ingrid.iface.util.IBusQueryResultIterator;
import de.ingrid.iface.util.IdfUtils;
import de.ingrid.iface.util.SearchInterfaceConfig;
import de.ingrid.iface.util.StringUtils;
import de.ingrid.iface.util.StringUtilsService;
import de.ingrid.utils.IBus;
import de.ingrid.utils.IngridHit;

@Service
public class DatasetFeedFactory {

    private IngridQueryProducer ingridQueryProducer;

    private ServiceFeedProducer serviceFeedProducer;

    private IBusHelper iBusHelper;

    private StringUtilsService stringUtilsService;
    
    private SearchInterfaceConfig config;

    private final static Log log = LogFactory.getLog(DatasetFeedFactory.class);

    private static final String[] REQUESTED_FIELDS = new String[] {};

    public Document getDatasetFeedDocument(DatasetFeedRequest datasetFeedRequest) throws Exception {

        Document doc = null;
        
        Integer connectionTimeout = config.getInt(SearchInterfaceConfig.ATOM_URL_CONNECTION_TIMEOUT, 1000);
        Integer readTimeout = config.getInt(SearchInterfaceConfig.ATOM_URL_READ_TIMEOUT, 1000);

        String datasetFeedUuid = datasetFeedRequest.getDatasetFeedUuid();
        if (datasetFeedUuid != null && datasetFeedUuid.toLowerCase().contains("request=getrecordbyid")) {
            if (log.isDebugEnabled()) {
                log.debug("Found external dataset: " + datasetFeedRequest.getDatasetFeedUuid());
            }
            // ISO Metadaten
            try {
                doc = stringUtilsService.urlToDocument(datasetFeedRequest.getDatasetFeedUuid(), connectionTimeout, readTimeout);
            } catch (Exception e) {
                log.warn("Unable to obtain XML document from " + datasetFeedRequest.getDatasetFeedUuid(), e);
            }
            // TODO: not nice to alter the request, change this 
            datasetFeedRequest.setType(EntryType.CSW);
        } else {
            // igc Metadaten
            IBus iBus = iBusHelper.getIBus();

            IBusQueryResultIterator datasetIterator = new IBusQueryResultIterator(ingridQueryProducer.createDatasetFeedInGridQuery(datasetFeedRequest), REQUESTED_FIELDS, iBus);
            IngridHit hit = null;
            if (datasetIterator.hasNext()) {
                hit = datasetIterator.next();
                if (log.isDebugEnabled()) {
                    log.debug("Found IGC dataset: " + hit.getHitDetail().getTitle());
                }
                doc = IdfUtils.getIdfDocument(iBus.getRecord(hit));
                datasetFeedRequest.setType(EntryType.IGC);
            } else {
                // no hit found, try all datasets related to the service
                ServiceFeedRequest sr = new ServiceFeedRequest();
                sr.setUuid(datasetFeedRequest.getServiceFeedUuid());
                ServiceFeed serviceFeed = serviceFeedProducer.produce(sr);
                for (ServiceFeedEntry entry : serviceFeed.getEntries()) {
                    if (entry.getType().equals(ServiceFeedEntry.EntryType.CSW)) {
                        if (entry.getSpatialDatasetIdentifierCode().equals(datasetFeedRequest.getSpatialDatasetIdentifierCode())
                                && entry.getSpatialDatasetIdentifierNamespace().equals(datasetFeedRequest.getSpatialDatasetIdentifierNamespace())) {
                            try {
                                doc = StringUtils.urlToDocument(entry.getDatasetMetadataRecord().getHref(), connectionTimeout, readTimeout);
                            } catch (Exception e) {
                                log.warn("Unable to obtain XML document from " + entry.getDatasetMetadataRecord().getHref(), e);
                            }
                            datasetFeedRequest.setType(EntryType.CSW);
                            datasetFeedRequest.setMetadataUrl(entry.getDatasetMetadataRecord().getHref());
                        }
                    }
                }
            }
        }
        return doc;
    }

    @Autowired
    public void setIngridQueryProducer(IngridQueryProducer ingridQueryProducer) {
        this.ingridQueryProducer = ingridQueryProducer;
    }

    @Autowired
    public void setServiceFeedProducer(ServiceFeedProducer serviceFeedProducer) {
        this.serviceFeedProducer = serviceFeedProducer;
    }

    @Autowired
    public void setiBusHelper(IBusHelper iBusHelper) {
        this.iBusHelper = iBusHelper;
    }

    @Autowired
    public void setStringUtilsService(StringUtilsService stringUtilsService) {
        this.stringUtilsService = stringUtilsService;
    }
    
    @Autowired
    public void setConfig(SearchInterfaceConfig config) {
        this.config = config;
    }

}
