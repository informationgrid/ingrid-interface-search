/*
 * Copyright (c) 2006 wemove digital solutions. All rights reserved.
 */
package de.ingrid.opensearch.util;

import org.apache.commons.configuration.PropertiesConfiguration;
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

    public static synchronized OpensearchConfig getInstance() {
        if (instance == null) {
            try {
                instance = new OpensearchConfig();
            } catch (Exception e) {
                if (log.isFatalEnabled()) {
                    log.fatal(
                            "Error loading the portal config application config file. (ingrid-opensearch.properties)",
                            e);
                }
            }
        }
        return instance;
    }

    private OpensearchConfig() throws Exception {
        super("ingrid-opensearch.properties");
    }
}
