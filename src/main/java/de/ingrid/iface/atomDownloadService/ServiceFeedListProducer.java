/*
 * **************************************************-
 * ingrid-interface-search
 * ==================================================
 * Copyright (C) 2014 - 2017 wemove digital solutions GmbH
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

import de.ingrid.iface.atomDownloadService.om.Link;
import de.ingrid.iface.atomDownloadService.om.ServiceFeed;
import de.ingrid.iface.atomDownloadService.om.ServiceFeedList;
import de.ingrid.iface.atomDownloadService.requests.ServiceFeedListRequest;
import de.ingrid.iface.atomDownloadService.util.IngridQueryProducer;
import de.ingrid.iface.util.IBusHelper;
import de.ingrid.iface.util.IBusQueryResultIterator;
import de.ingrid.iface.util.IdfUtils;
import de.ingrid.iface.util.SearchInterfaceConfig;
import de.ingrid.iface.util.ServiceFeedUtils;
import de.ingrid.iface.util.StringUtils;
import de.ingrid.iface.util.URLUtil;
import de.ingrid.utils.IBus;
import de.ingrid.utils.IngridHit;
import de.ingrid.utils.queryparser.ParseException;

@Service
public class ServiceFeedListProducer {

    private static final String[] REQUESTED_FIELDS = new String[] {};
    private IngridQueryProducer ingridQueryProducer;

    private IBusHelper iBusHelper;

    private final static Log log = LogFactory.getLog(ServiceFeedListProducer.class);

    private ServiceFeedUtils serviceFeedUtils;

    private SearchInterfaceConfig config;

    private String atomDownloadServiceFeedlistUrlPattern = null;

    @PostConstruct
    public void init() {
        atomDownloadServiceFeedlistUrlPattern = org.apache.commons.lang.StringUtils.stripEnd(config.getString(SearchInterfaceConfig.ATOM_DOWNLOAD_SERVICE_URL), "/");
        atomDownloadServiceFeedlistUrlPattern += config.getString(SearchInterfaceConfig.ATOM_DOWNLOAD_SERVICE_FEEDLIST_EXTENSION);
    }

    public ServiceFeedList produce(ServiceFeedListRequest serviceFeedListRequest) throws ParseException, Exception {

        if (log.isDebugEnabled()) {
            log.debug("Build service feed from IGC resource for query: " + serviceFeedListRequest.getQuery());
        }

        ServiceFeedList serviceFeedList = new ServiceFeedList();
        List<ServiceFeed> entryList = new ArrayList<ServiceFeed>();
        IBus iBus = iBusHelper.getIBus();

        Link link = new Link();
        String urlPattern = URLUtil.updateProtocol( atomDownloadServiceFeedlistUrlPattern, serviceFeedListRequest.getProtocol() );
        link.setHref(urlPattern.replace("{searchTerms}", StringUtils.encodeForPath(serviceFeedListRequest.getQuery())));
        link.setHrefLang("de");
        link.setType("application/atom+xml");
        link.setRel("self");
        link.setTitle("Feed containing the service list");
        serviceFeedList.setSelfReferencingLink(link);

        // create response header
        IBusQueryResultIterator serviceIterator = new IBusQueryResultIterator(ingridQueryProducer.createServiceFeedListInGridQuery(serviceFeedListRequest), REQUESTED_FIELDS, iBus);
        while (serviceIterator.hasNext()) {
            IngridHit hit = serviceIterator.next();
            Long startTimer = 0L;
            if (log.isDebugEnabled()) {
                log.debug("Found valid service '" + hit.getHitDetail().getTitle() + "' from iPlug '" + hit.getPlugId() + "'");
                startTimer = System.currentTimeMillis();
            }
            Document idfDoc = IdfUtils.getIdfDocument(iBus.getRecord(hit));
            if (log.isDebugEnabled()) {
                log.debug("Fetched IDF record within " + (System.currentTimeMillis() - startTimer) + " ms.");
            }
            

            ServiceFeed feed = serviceFeedUtils.createFromIdf(idfDoc, serviceFeedListRequest);

            entryList.add(feed);
        }

        serviceFeedList.setEntries(entryList);

        return serviceFeedList;

    }

    @Autowired
    public void setIngridQueryProducer(IngridQueryProducer ingridQueryProducer) {
        this.ingridQueryProducer = ingridQueryProducer;
    }

    @Autowired
    public void setiBusHelper(IBusHelper iBusHelper) {
        this.iBusHelper = iBusHelper;
    }

    @Autowired
    public void setServiceFeedUtils(ServiceFeedUtils serviceFeedUtils) {
        this.serviceFeedUtils = serviceFeedUtils;
    }

    @Autowired
    public void setConfig(SearchInterfaceConfig config) {
        this.config = config;
    }

}
