/*
 * **************************************************-
 * ingrid-interface-search
 * ==================================================
 * Copyright (C) 2014 - 2021 wemove digital solutions GmbH
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
/*
 * Copyright (c) 2006 wemove digital solutions. All rights reserved.
 */
package de.ingrid.iface.util;

import org.apache.commons.configuration.CombinedConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.tree.OverrideCombiner;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

/**
 * Provides access to the opensearch preferences.
 * 
 * @author joachim@wemove.com
 */
@Service
public class SearchInterfaceConfig extends CombinedConfiguration {

    // private stuff
    private static SearchInterfaceConfig instance = null;

    private final static Log log = LogFactory.getLog(SearchInterfaceConfig.class);

    public static final String SERVER_PORT = "server.port";

    public static final String OPENSEARCH_MAX_REQUESTED_HITS = "opensearch.max.requested.hits";

    public static final String OPENSEARCH_PROXY_URL = "opensearch.proxy.url";

    public static final String METADATA_DETAILS_URL = "metadata.details.url";

    public static final String DESCRIPTOR_FILE = "opensearch.descriptor.file";

    public static final String IBUS_SEARCH_MAX_TIMEOUT = "ibus.search.max.timeout";

    public static final String ENABLE_CACHING = "enable.caching";

    public static final String METADATA_ACCESS_URL = "metadata.access.url";

    public static final String ATOM_DOWNLOAD_SERVICE_URL = "atom.download.service.url";

    public static final String ATOM_DOWNLOAD_SERVICE_FEED_EXTENSION = "atom.download.service.feed.extension";

    public static final String ATOM_DOWNLOAD_SERVICE_FEEDLIST_EXTENSION = "atom.download.service.feedlist.extension";
    
    public static final String ATOM_DOWNLOAD_DATASET_FEED_EXTENSION = "atom.download.dataset.feed.extension";

    public static final String ATOM_DOWNLOAD_OPENSEARCH_DESCRIBE_SPATIAL_DATASET_TEMPLATE = "atom.download.opensearch.describe.spatial.dataset.template";

    public static final String ATOM_DOWNLOAD_OPENSEARCH_GET_RESULTS_TEMPLATE = "atom.download.opensearch.get.results.template";
    
    public static final String ATOM_DOWNLOAD_OPENSEARCH_GET_SPATIAL_DATASET_TEMPLATE = "atom.download.opensearch.get.spatial.dataset.template";
    
    public static final String ATOM_DOWNLOAD_OPENSEARCH_DEFINITION_EXTENSION = "atom.download.opensearch.definition.extension";
    
    public static final String ATOM_DOWNLOAD_OPENSEARCH_SUPPORTED_LANGUAGES = "atom.download.opensearch.supported.languages";

    public static final String ATOM_URL_CONNECTION_TIMEOUT = "atom.url.connect.timeout";
    
    public static final String ATOM_URL_READ_TIMEOUT = "atom.url.read.timeout";

    public static final String WEBAPP_DIR = "jetty.webapp";

    public static final String ATOM_DOWNLOAD_QUERY_EXTENSION = "atom.download.service.search.extension";

    public static final String  OPENSEARCH_CHANNEL_TITLE = "opensearch.channel.title";

    public static final String  OPENSEARCH_CHANNEL_DESCRIPTION = "opensearch.channel.description";

    public static final String  OPENSEARCH_CHANNEL_LINK = "opensearch.channel.link";

    public static final String  OPENSEARCH_CHANNEL_LANGUAGE = "opensearch.channel.language";

    public static final String  OPENSEARCH_CHANNEL_COPYRIGHT = "opensearch.channel.copyright";

    
    public SearchInterfaceConfig() throws ConfigurationException {
        super(new OverrideCombiner());
        try {
            this.addConfiguration( new PropertiesConfiguration( "interface-search-user.properties" ) );
        } catch (ConfigurationException e) {}
        this.addConfiguration( new PropertiesConfiguration( "interface-search.properties" ) );
    }

    /**
     * This creates an instance of SearchInterfaceConfig initialized by a given
     * property file if given or a default file "interface-search.properties". This
     * was introduced especially for tests where a different configuration file is
     * loaded.
     * 
     * @param filePath is the path to the property file to be loaded
     * @return the instance of the configuration object
     */
    public static synchronized SearchInterfaceConfig getInstance(String filePath) {
        if (instance == null || filePath != null) {
            try {
                if (filePath != null)
                    instance = new SearchInterfaceConfig(filePath);
                else
                    instance = new SearchInterfaceConfig("interface-search.properties");
            } catch (Exception e) {
                if (log.isFatalEnabled()) {
                    log.fatal("Error loading the portal config application config file. (ingrid-search.properties)", e);
                }
            }
        }

        return instance;
    }

    public static synchronized SearchInterfaceConfig getInstance() {
        return getInstance(null);
    }

    private SearchInterfaceConfig(String path) throws Exception {
        super(new OverrideCombiner());
        try {
            this.addConfiguration( new PropertiesConfiguration( "interface-search-user.properties" ) );
        } catch (ConfigurationException e) {}
        this.addConfiguration( new PropertiesConfiguration( path ) );
    }
}
