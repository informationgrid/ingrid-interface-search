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
package de.ingrid.iface.atomDownloadService.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.ingrid.iface.atomDownloadService.requests.DatasetFeedRequest;
import de.ingrid.iface.atomDownloadService.requests.ServiceFeedListRequest;
import de.ingrid.iface.atomDownloadService.requests.ServiceFeedRequest;
import de.ingrid.iface.util.IBusHelper;
import de.ingrid.iface.util.IBusQueryResultIterator;
import de.ingrid.iface.util.SearchInterfaceConfig;
import de.ingrid.iface.util.StringUtils;
import de.ingrid.utils.IBus;
import de.ingrid.utils.IngridHit;
import de.ingrid.utils.query.IngridQuery;
import de.ingrid.utils.queryparser.ParseException;
import de.ingrid.utils.queryparser.QueryStringParser;

@Service
public class IngridQueryProducer {

    private SearchInterfaceConfig config;

    private IBusHelper iBusHelper;

    private final static Log log = LogFactory.getLog(IngridQueryProducer.class);

    String queryExtension = null;

    public IngridQuery createServiceFeedInGridQuery(String uuid) throws ParseException {

        StringBuilder queryStr = new StringBuilder();
        queryStr.append("ranking:score (t01_object.obj_id:\"" + uuid + "\" OR t01_object.org_obj_id:\"" + uuid + "\")");
        addQueryExtension(queryStr);
        IngridQuery q = QueryStringParser.parse(queryStr.toString());
        return q;

    }

    /**
     * Build an InGrid query from ServiceFeedRequest parameter. Takes obj_id and
     * org_obj_id into account.
     * 
     * @param serviceFeedRequest
     * @return
     * @throws ParseException
     */
    public IngridQuery createServiceFeedInGridQuery(ServiceFeedRequest serviceFeedRequest) throws ParseException {

        StringBuilder queryStr = new StringBuilder();
        queryStr.append("ranking:score (t01_object.obj_id:\"").append(serviceFeedRequest.getUuid()).append("\" OR t01_object.org_obj_id:\"").append(serviceFeedRequest.getUuid()).append("\")");
        addQueryExtension(queryStr);
        if (log.isDebugEnabled()) {
            log.debug("Query string: " + queryStr);
        }
        IngridQuery q = QueryStringParser.parse(queryStr.toString());
        return q;
    }

    /**
     * Build an InGrid query from an UUID array and ServiceFeedRequest query.
     * Takes obj_id and org_obj_id into account.
     * 
     * @param uuids
     * @param serviceFeedRequest
     * @return
     * @throws ParseException
     */
    public IngridQuery createServiceFeedEntryInGridQuery(String[] uuids, ServiceFeedRequest serviceFeedRequest) throws ParseException {

        StringBuilder queryStr = new StringBuilder();
        if (serviceFeedRequest.getQuery() != null && serviceFeedRequest.getQuery().length() > 0) {
            queryStr.append("ranking:score ((" + StringUtils.join(uuids, "\") OR (", serviceFeedRequest.getQuery() + " t01_object.obj_id:\"") + "\") OR ("
                    + StringUtils.join(uuids, "\") OR (", serviceFeedRequest.getQuery() + " t01_object.org_obj_id:\"") + "\"))");
        } else {
            queryStr.append("ranking:score (" + StringUtils.join(uuids, "\" OR ", "t01_object.obj_id:\"") + "\" OR " + StringUtils.join(uuids, "\" OR ", "t01_object.org_obj_id:\"") + "\")");
        }
        addQueryExtension(queryStr);
        if (log.isDebugEnabled()) {
            log.debug("Query string: " + queryStr);
        }
        IngridQuery q = QueryStringParser.parse(queryStr.toString());
        return q;

    }

    public IngridQuery createDatasetFeedInGridQuery(String uuid) throws ParseException {

        StringBuilder queryStr = new StringBuilder();
        queryStr.append("ranking:score (t01_object.obj_id:\"" + uuid + "\" OR t01_object.org_obj_id:\"" + uuid + "\")");
        addQueryExtension(queryStr);
        if (log.isDebugEnabled()) {
            log.debug("Query string: " + queryStr);
        }
        IngridQuery q = QueryStringParser.parse(queryStr.toString());
        return q;
    }

    public IngridQuery createDatasetFeedInGridQuery(DatasetFeedRequest datasetFeedRequest) throws Exception {

        StringBuilder queryStr = new StringBuilder();
        if (datasetFeedRequest.getDatasetFeedUuid() != null && datasetFeedRequest.getDatasetFeedUuid().length() > 0) {
            queryStr.append("ranking:score (t01_object.obj_id:\"" + datasetFeedRequest.getDatasetFeedUuid() + "\" OR t01_object.org_obj_id:\"" + datasetFeedRequest.getDatasetFeedUuid() + "\")");
            addQueryExtension(queryStr);
            IngridQuery q = QueryStringParser.parse(queryStr.toString());
            return q;
        } else {
            queryStr.append("ranking:score (t01_object.obj_id:\"" + datasetFeedRequest.getServiceFeedUuid() + "\" OR t01_object.org_obj_id:\"" + datasetFeedRequest.getServiceFeedUuid() + "\")");
            addQueryExtension(queryStr);
            IngridQuery q = QueryStringParser.parse(queryStr.toString());
            IBus iBus = iBusHelper.getIBus();
            IBusQueryResultIterator queryIterator = new IBusQueryResultIterator(q, new String[] {}, iBus);
            IngridHit hit = null;
            if (queryIterator.hasNext()) {
                hit = queryIterator.next();
                String plugId = hit.getPlugId();
                queryStr.setLength(0);
                queryStr.append("ranking:score iplugs:\"" + plugId + "\" (t011_obj_geo.datasource_uuid:\"" + datasetFeedRequest.getSpatialDatasetIdentifierCode() + "\" OR t011_obj_geo.datasource_uuid:\""
                        + datasetFeedRequest.getSpatialDatasetIdentifierNamespace() + "#" + datasetFeedRequest.getSpatialDatasetIdentifierCode() + "\")");
                if (datasetFeedRequest.getCrs() != null && datasetFeedRequest.getCrs().length() > 0) {
                    queryStr.append(" t011_obj_geo.referencesystem_id:" + (datasetFeedRequest.getCrs().contains(":") ? "\"" + datasetFeedRequest.getCrs() + "\"" : datasetFeedRequest.getCrs()));
                }
                if (datasetFeedRequest.getLanguage() != null && datasetFeedRequest.getLanguage().length() > 0) {
                    String[] supportedLanguages = config.getStringArray(SearchInterfaceConfig.ATOM_DOWNLOAD_OPENSEARCH_SUPPORTED_LANGUAGES);
                    for (String supportedLanguage : supportedLanguages) {
                        if (supportedLanguage.equals(datasetFeedRequest.getLanguage())) {
                            queryStr.append(" lang:" + datasetFeedRequest.getLanguage());
                            break;
                        }
                    }
                }
                addQueryExtension(queryStr);
                if (log.isDebugEnabled()) {
                    log.debug("Query string: " + queryStr);
                }
                q = QueryStringParser.parse(queryStr.toString());
                return q;
            }
            throw new Exception("Cannot create InGrid query from dataset feed request: " + datasetFeedRequest);
        }
    }

    /**
     * Creates a ingrid query to get all atom download feed services.
     * 
     * @param serviceFeedListRequest
     * @return
     * @throws ParseException
     */
    public IngridQuery createServiceFeedListInGridQuery(ServiceFeedListRequest serviceFeedListRequest) throws ParseException {
        StringBuilder queryStr = new StringBuilder();
        queryStr.append("ranking:score t011_obj_serv.has_atom_download:Y");
        if (serviceFeedListRequest.getQuery() != null && !serviceFeedListRequest.getQuery().isEmpty()) {
            queryStr.append(" ");
            queryStr.append(serviceFeedListRequest.getQuery());
        }
        addQueryExtension(queryStr);
        IngridQuery q = QueryStringParser.parse(queryStr.toString());
        return q;
    }

    @Autowired
    public void setiBusHelper(IBusHelper iBusHelper) {
        this.iBusHelper = iBusHelper;
    }

    @Autowired
    public void setConfig(SearchInterfaceConfig config) {
        this.config = config;
    }
    
    /**
     * Lazy load query extension from config file. Apply if exists.
     * 
     * @param qryStr
     */
    private void addQueryExtension(StringBuilder qryStr) {
        
        if (queryExtension == null) {
            String s = config.getString(SearchInterfaceConfig.ATOM_DOWNLOAD_QUERY_EXTENSION, null);
            if (s != null && s.length() > 0) {
                queryExtension = s;
            } else {
                queryExtension = "";
            }
        }
        
        if (queryExtension.length() > 0) {
            qryStr.append(" " + queryExtension);
        }
    }

}
