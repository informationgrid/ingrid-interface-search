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
        
	    if (log.isDebugEnabled()) {
            log.debug("Get single dataset feed document from dataset feed request.");
        }

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
                Long startTimer = 0L;
                if (log.isDebugEnabled()) {
                    startTimer = System.currentTimeMillis();
                }
                doc = stringUtilsService.urlToDocument(datasetFeedRequest.getDatasetFeedUuid(), connectionTimeout, readTimeout);
                if (log.isDebugEnabled()) {
                    log.debug("Fetched ISO record from '" + datasetFeedRequest.getDatasetFeedUuid() + "' within " + (System.currentTimeMillis() - startTimer) + " ms.");
                }
            } catch (Exception e) {
                log.warn("Unable to obtain XML document from " + datasetFeedRequest.getDatasetFeedUuid(), e);
            }
            // TODO: not nice to alter the request, change this 
            datasetFeedRequest.setType(EntryType.CSW);
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Found IGC dataset UUID: " + datasetFeedRequest.getDatasetFeedUuid());
            }
            // igc Metadaten
            IBus iBus = iBusHelper.getIBus();

            IBusQueryResultIterator datasetIterator = new IBusQueryResultIterator(ingridQueryProducer.createDatasetFeedInGridQuery(datasetFeedRequest), REQUESTED_FIELDS, iBus);
            IngridHit hit = null;
            if (datasetIterator.hasNext()) {
                hit = datasetIterator.next();
                Long startTimer = 0L;
                if (log.isDebugEnabled()) {
                    log.debug("Found IGC dataset: " + hit.getHitDetail().getTitle() + "' from iPlug '" + hit.getPlugId() + "'");
                    startTimer = System.currentTimeMillis();
                }
                doc = IdfUtils.getIdfDocument(iBus.getRecord(hit));
                if (log.isDebugEnabled()) {
                    log.debug("Fetched IDF record within " + (System.currentTimeMillis() - startTimer) + " ms.");
                }
                
                datasetFeedRequest.setType(EntryType.IGC);
            } else {
                // no hit found, try all datasets related to the service
        	    if (log.isDebugEnabled()) {
                    log.debug("No IGC found from UUID. Try to get ISO document from related CSW GetRecordById Links referenced from the service.");
                }
                ServiceFeedRequest sr = new ServiceFeedRequest();
                sr.setUuid(datasetFeedRequest.getServiceFeedUuid());
                sr.setProtocol(datasetFeedRequest.getProtocol());
                ServiceFeed serviceFeed = serviceFeedProducer.produce(sr);
                for (ServiceFeedEntry entry : serviceFeed.getEntries()) {
                    if (entry.getType().equals(ServiceFeedEntry.EntryType.CSW)) {
                        if (entry.getSpatialDatasetIdentifierCode().equals(datasetFeedRequest.getSpatialDatasetIdentifierCode())
                                && entry.getSpatialDatasetIdentifierNamespace().equals(datasetFeedRequest.getSpatialDatasetIdentifierNamespace())) {
                            try {
                                Long startTimer = 0L;
                                if (log.isDebugEnabled()) {
                                    startTimer = System.currentTimeMillis();
                                }
                                doc = StringUtils.urlToDocument(entry.getDatasetMetadataRecord().getHref(), connectionTimeout, readTimeout);
                                if (log.isDebugEnabled()) {
                                    log.debug("Fetched ISO record from '" + entry.getDatasetMetadataRecord().getHref() + "' within " + (System.currentTimeMillis() - startTimer) + " ms.");
                                }
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
