package de.ingrid.opensearch.util;

import de.ingrid.ibus.client.BusClient;
import de.ingrid.ibus.client.BusClientFactory;
import de.ingrid.utils.IBus;
import de.ingrid.utils.query.IngridQuery;

public class IBusHelper {
	private static IBus bus = null;
	
	private static boolean cache = false;
	
	public static IBus getIBus() throws Exception {
		BusClient client;
		
		if (bus == null) {
			client = BusClientFactory.createBusClient(IBusHelper.class.getResourceAsStream("/communication.xml"));
		
	        OpensearchConfig config = OpensearchConfig.getInstance();
	        
	        if (config.getBoolean(OpensearchConfig.ENABLE_CACHING, true) == true) {
	        	bus = client.getCacheableIBus();
	        	cache = true;
	        } else {
	        	bus = client.getNonCacheableIBus();
	        }
		}
        return bus;
	}
	
    public static void injectCache(IngridQuery q) {
    	if (!q.containsKey("cache")) {
	    	if (cache) {
	    		q.put("cache", "on");
	        } else {
	        	q.put("cache", "off");
	        }
    	}
    }
}
