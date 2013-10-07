/*
 * Copyright (c) 2006 wemove digital solutions. All rights reserved.
 */
package de.ingrid.opensearch.util;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Provides access to the opensearch preferences.
 * 
 * @author joachim@wemove.com
 */
public class OpensearchConfig extends PropertiesConfiguration {

    // private stuff
    private static OpensearchConfig instance = null;

    private final static Log log = LogFactory.getLog(OpensearchConfig.class);

    public static final String SERVER_PORT = "server.port";

    public static final String MAX_REQUESTED_HITS = "max.requested.hits";
    
    public static final String PROXY_URL = "proxy.url";

    public static final String METADATA_DETAILS_URL = "metadata.details.url";
    
    public static final String DESCRIPTOR_FILE = "descriptor.file";
    
    public static final String IBUS_SEARCH_MAX_TIMEOUT = "ibus.search.max.timeout";
    
    public static final String ENABLE_CACHING = "enable.caching";
    
    
    public static synchronized OpensearchConfig getInstance(String filePath) {
        if (instance == null || filePath != null) {
            try {
                if (filePath != null)
                    instance = new OpensearchConfig(filePath);
                else 
                    instance = new OpensearchConfig("ingrid-opensearch.properties");
            } catch (Exception e) {
                if (log.isFatalEnabled()) {
                    log.fatal(
                            "Error loading the portal config application config file. (ingrid-opensearch.properties)",
                            e);
                }
            }
        }
        // reload File when changed
        instance.setReloadingStrategy(new FileChangedReloadingStrategy());

        return instance;
    }
    
    public static synchronized OpensearchConfig getInstance() {
        return getInstance(null);
    }

    private OpensearchConfig(String path) throws Exception {
        super(path);
    }
}
