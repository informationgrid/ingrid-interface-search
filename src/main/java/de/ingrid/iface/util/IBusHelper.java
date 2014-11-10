/*
 * **************************************************-
 * ingrid-interface-search
 * ==================================================
 * Copyright (C) 2014 wemove digital solutions GmbH
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

import java.util.ArrayList;
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
import de.ingrid.utils.query.FieldQuery;
import de.ingrid.utils.query.IngridQuery;
import edu.emory.mathcs.backport.java.util.Arrays;

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

    public List<IngridHit> getAllResults(IngridQuery q) throws Exception {

        List<IngridHit> results = null;

        long cnt = 0;
        IngridHits hits;
        do {
            hits = bus.search(q, 10, 1, 10, 30000);
            cnt += hits.getHits().length;
            results.addAll(Arrays.asList(hits.getHits()));
        } while (cnt <= hits.length());

        return results;

    }

    public String getPartnerName(String id, String defaultValue) {

        String result = defaultValue;

        IngridHits hits;
        try {
            final IngridQuery ingridQuery = new IngridQuery();
            ingridQuery.addField(new FieldQuery(false, false, "datatype", "management"));
            ingridQuery.addField(new FieldQuery(false, false, "management_request_type", "1"));
            ingridQuery.addField(new FieldQuery(false, false, "cache", "off"));

            hits = this.getIBus().search(ingridQuery, 1000, 0, 0, 120000);

            if (hits.length() > 0) {
                final ArrayList<Map<String, String>> partners = hits.getHits()[0].getArrayList("partner");
                for (final Map<String, String> partner : partners) {
                    final String partnerId = partner.get("partnerid");
                    if (partnerId.equals(id)) {
                        result = partner.get("name");
                        break;
                    }
                }
            }

        } catch (Exception e) {
            log.info("Error obtaining partner name for id: " + id, e);
        }

        return result;

    }

    public String getProviderName(String id, String defaultValue) {

        String result = defaultValue;

        IngridHits hits;
        try {
            final IngridQuery ingridQuery = new IngridQuery();
            ingridQuery.addField(new FieldQuery(false, false, "datatype", "management"));
            ingridQuery.addField(new FieldQuery(false, false, "management_request_type", "2"));
            ingridQuery.addField(new FieldQuery(false, false, "cache", "off"));

            hits = this.getIBus().search(ingridQuery, 1000, 0, 0, 120000);
            if (hits.length() > 0) {
                final ArrayList<Map<String, String>> providerss = hits.getHits()[0].getArrayList("provider");
                for (final Map<String, String> provider : providerss) {
                    final String providerId = provider.get("providerid");
                    if (providerId.equals(id)) {
                        result = provider.get("name");
                        break;
                    }
                }
            }
        } catch (Exception e) {
            log.info("Error obtaining provider name for id: " + id, e);
        }

        return result;

    }

}
