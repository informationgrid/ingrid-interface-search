/*
 * Copyright (c) 2006 wemove digital solutions. All rights reserved.
 */
package de.ingrid.iface.util;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

/**
 * Provides access to the opensearch preferences.
 * 
 * @author joachim@wemove.com
 */
@Service
public class SearchInterfaceConfig extends PropertiesConfiguration {

    // private stuff
    private static SearchInterfaceConfig instance = null;

    private final static Log log = LogFactory.getLog(SearchInterfaceConfig.class);

    public static final String SERVER_PORT = "server.port";

    public static final String OPENSEARCH_MAX_REQUESTED_HITS = "opensearch.max.requested.hits";

    public static final String OPENSEARCH_PROXY_URL = "opensearch.proxy.url";

    public static final String OPENSEARCH_METADATA_DETAILS_URL = "opensearch.metadata.details.url";

    public static final String DESCRIPTOR_FILE = "opensearch.descriptor.file";

    public static final String IBUS_SEARCH_MAX_TIMEOUT = "ibus.search.max.timeout";

    public static final String ENABLE_CACHING = "enable.caching";

    public static final String METADATA_ACCESS_URL = "metadata.access.url";

    public static final String ATOM_DOWNLOAD_SERVICE_URL = "atom.download.service.url";

    public static final String ATOM_DOWNLOAD_SERVICE_FEED_EXTENSION = "atom.download.service.feed.extension";

    public static final String ATOM_DOWNLOAD_DATASET_FEED_EXTENSION = "atom.download.dataset.feed.extension";

    public static final String ATOM_DOWNLOAD_OPENSEARCH_DESCRIBE_SPATIAL_DATASET_TEMPLATE = "atom.download.opensearch.describe.spatial.dataset.template";

    public static final String ATOM_DOWNLOAD_OPENSEARCH_GET_RESULTS_TEMPLATE = "atom.download.opensearch.get.results.template";
    
    public static final String ATOM_DOWNLOAD_OPENSEARCH_GET_SPATIAL_DATASET_TEMPLATE = "atom.download.opensearch.get.spatial.dataset.template";
    
    public static final String ATOM_DOWNLOAD_OPENSEARCH_DEFINITION_EXTENSION = "atom.download.opensearch.definition.extension";
    
    public static final String ATOM_DOWNLOAD_OPENSEARCH_SUPPORTED_LANGUAGES = "atom.download.opensearch.supported.languages";

    public static final String ATOM_URL_CONNECTION_TIMEOUT = "atom.url.connect.timeout";
    
    public static final String ATOM_URL_READ_TIMEOUT = "atom.url.read.timeout";
    
    public SearchInterfaceConfig() throws ConfigurationException {
        super("interface-search.properties");

        // reload File when changed
        this.setReloadingStrategy(new FileChangedReloadingStrategy());
    }

    public static synchronized SearchInterfaceConfig getInstance(String filePath) {
        if (instance == null || filePath != null) {
            try {
                if (filePath != null)
                    instance = new SearchInterfaceConfig(filePath);
                else
                    instance = new SearchInterfaceConfig("interface-search.properties");
            } catch (Exception e) {
                if (log.isFatalEnabled()) {
                    log.fatal("Error loading the portal config application config file. (ingrid-opensearch.properties)", e);
                }
            }
        }
        // reload File when changed
        instance.setReloadingStrategy(new FileChangedReloadingStrategy());

        return instance;
    }

    public static synchronized SearchInterfaceConfig getInstance() {
        return getInstance(null);
    }

    private SearchInterfaceConfig(String path) throws Exception {
        super(path);
    }
}
