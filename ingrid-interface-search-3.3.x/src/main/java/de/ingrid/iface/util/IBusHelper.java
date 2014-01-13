package de.ingrid.iface.util;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.ingrid.ibus.client.BusClient;
import de.ingrid.ibus.client.BusClientFactory;
import de.ingrid.utils.IBus;
import de.ingrid.utils.IngridHit;
import de.ingrid.utils.IngridHits;
import de.ingrid.utils.query.IngridQuery;
import edu.emory.mathcs.backport.java.util.Arrays;

@Service
public class IBusHelper {

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

}
