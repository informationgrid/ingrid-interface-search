/*
 * **************************************************-
 * ingrid-interface-search
 * ==================================================
 * Copyright (C) 2014 - 2015 wemove digital solutions GmbH
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;

import de.ingrid.iface.atomDownloadService.om.ServiceFeed;
import de.ingrid.iface.atomDownloadService.om.ServiceFeedEntry;
import de.ingrid.iface.atomDownloadService.requests.ServiceFeedRequest;
import de.ingrid.iface.atomDownloadService.util.IngridQueryProducer;
import de.ingrid.iface.util.IBusHelper;
import de.ingrid.iface.util.IBusQueryResultIterator;
import de.ingrid.iface.util.IdfUtils;
import de.ingrid.iface.util.ServiceFeedUtils;
import de.ingrid.utils.IBus;
import de.ingrid.utils.IngridHit;
import de.ingrid.utils.queryparser.ParseException;

@Service
public class ServiceFeedProducer {

    private static final String[] REQUESTED_FIELDS = new String[] {};

    private IngridQueryProducer ingridQueryProducer;

    private List<ServiceFeedEntryProducer> serviceFeedEntryProducer;

    private ServiceFeedUtils serviceFeedUtils;

    private IBusHelper iBusHelper;

    private final static Log log = LogFactory.getLog(ServiceFeedProducer.class);

    public ServiceFeed produce(ServiceFeedRequest serviceFeedRequest) throws ParseException, Exception {

        if (log.isDebugEnabled()) {
            log.debug("Build service feed from IGC resource for service: " + serviceFeedRequest.getUuid());
        }

        ServiceFeed serviceFeed = null;
        IBus iBus = iBusHelper.getIBus();

        // create response header
        IBusQueryResultIterator serviceIterator = new IBusQueryResultIterator(ingridQueryProducer.createServiceFeedInGridQuery(serviceFeedRequest), REQUESTED_FIELDS, iBus);
        if (serviceIterator.hasNext()) {
            IngridHit hit = serviceIterator.next();
            if (log.isDebugEnabled()) {
                log.debug("Found valid service: " + hit.getHitDetail().getTitle());
            }

            Document idfDoc = IdfUtils.getIdfDocument(iBus.getRecord(hit));
            serviceFeed = serviceFeedUtils.createFromIdf(idfDoc);

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
    public void setiBusHelper(IBusHelper iBusHelper) {
        this.iBusHelper = iBusHelper;
    }

    @Autowired
    public void setServiceFeedUtils(ServiceFeedUtils serviceFeedUtils) {
        this.serviceFeedUtils = serviceFeedUtils;
    }

}
