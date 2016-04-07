/*
 * **************************************************-
 * ingrid-interface-search
 * ==================================================
 * Copyright (C) 2014 - 2016 wemove digital solutions GmbH
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
package de.ingrid.iface.util;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.ingrid.ibus.client.BusClient;
import de.ingrid.ibus.client.BusClientFactory;
import de.ingrid.utils.IBus;
import de.ingrid.utils.IngridHit;
import de.ingrid.utils.IngridHits;
import de.ingrid.utils.PlugDescription;
import de.ingrid.utils.dsc.Record;
import de.ingrid.utils.query.FieldQuery;
import de.ingrid.utils.query.IngridQuery;

@Service
public class IBusHelper {

    private final static Log log = LogFactory.getLog(IBusHelper.class);

    @Autowired
    private SearchInterfaceConfig config;

    private IBus bus = null;

    private boolean cache = false;

    public IBus getIBus() throws Exception {
        BusClient client;

        if (bus == null) {
            client = BusClientFactory.createBusClient(IBusHelper.class.getResourceAsStream("/communication.xml"));

            SearchInterfaceConfig config = SearchInterfaceConfig.getInstance();

            if (config.getBoolean(SearchInterfaceConfig.ENABLE_CACHING, true) == true) {
                bus = client.getCacheableIBus();
                cache = true;
            } else {
                bus = client.getNonCacheableIBus();
            }
        }
        return bus;
    }

    public void injectCache(IngridQuery q) {
        if (!q.containsKey("cache")) {
            if (cache) {
                q.put("cache", "on");
            } else {
                q.put("cache", "off");
            }
        }
    }

    public String getPartnerName(String id, String defaultValue) {
        long start = 0;
        if (log.isDebugEnabled()) {
            start = System.currentTimeMillis();
        }

        String result = defaultValue;

        IngridHits hits;
        try {
            final IngridQuery ingridQuery = new IngridQuery();
            ingridQuery.addField(new FieldQuery(false, false, "datatype", "management"));
            ingridQuery.addField(new FieldQuery(false, false, "management_request_type", "1"));

            hits = this.getIBus().search(ingridQuery, 1000, 0, 0, 120000);

            if (hits.length() > 0) {
                final List<Object> partners = hits.getHits()[0].getArrayList("partner");
                for (final Object partner : partners) {
                    @SuppressWarnings("unchecked")
                    Map<String, String> partnerMap = (Map<String, String>) partner;
                    final String partnerId = partnerMap.get("partnerid");
                    if (partnerId.equals(id)) {
                        result = partnerMap.get("name");
                        break;
                    }
                }
            }

        } catch (Exception e) {
            log.info("Error obtaining partner name for id: " + id, e);
        }

        if (log.isDebugEnabled()) {
            log.debug( "Got partner name from id '" + id + "' within " + (System.currentTimeMillis() - start) + " ms.");
        }
        return result;

    }

    public String getProviderName(String id, String defaultValue) {
        long start = 0;
        if (log.isDebugEnabled()) {
            start = System.currentTimeMillis();
        }

        String result = defaultValue;

        IngridHits hits;
        try {
            final IngridQuery ingridQuery = new IngridQuery();
            ingridQuery.addField(new FieldQuery(false, false, "datatype", "management"));
            ingridQuery.addField(new FieldQuery(false, false, "management_request_type", "2"));

            hits = this.getIBus().search(ingridQuery, 1000, 0, 0, 120000);
            if (hits.length() > 0) {
                final List<Object> providerss = hits.getHits()[0].getArrayList("provider");
                for (final Object provider : providerss) {
                    @SuppressWarnings("unchecked")
                    Map<String, String> providerMap = (Map<String, String>) provider;
                    final String providerId = providerMap.get("providerid");
                    if (providerId.equals(id)) {
                        result = providerMap.get("name");
                        break;
                    }
                }
            }
        } catch (Exception e) {
            log.info("Error obtaining provider name for id: " + id, e);
        }

        if (log.isDebugEnabled()) {
            log.debug( "Got provider name from id '" + id + "' within " + (System.currentTimeMillis() - start) + " ms.");
        }
        return result;

    }
    
    public PlugDescription getPlugdescription(String plugId) throws Exception {
        long start = 0;
        if (log.isDebugEnabled()) {
            start = System.currentTimeMillis();
        }
        PlugDescription result = this.getIBus().getIPlug(plugId);
        if (log.isDebugEnabled()) {
            log.debug( "Got plugdescription from iplug '" + plugId + "' within " + (System.currentTimeMillis() - start) + " ms.");
        }
        return result;
    }
    
    public Record getRecord(IngridHit hit) throws Exception {
        long start = 0;
        if (log.isDebugEnabled()) {
            start = System.currentTimeMillis();
        }
        Record result = this.getIBus().getRecord(hit);
        if (log.isDebugEnabled()) {
            log.debug( "Got record from iplug '" + hit.getPlugId() + "' within " + (System.currentTimeMillis() - start) + " ms.");
        }
        return result;
    }
    

}
