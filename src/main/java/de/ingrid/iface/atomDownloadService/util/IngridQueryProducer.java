package de.ingrid.iface.atomDownloadService.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.ingrid.iface.atomDownloadService.requests.DatasetFeedRequest;
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

    public IngridQuery createServiceFeedInGridQuery(String uuid) throws ParseException {

        IngridQuery q = QueryStringParser.parse("ranking:score (t01_object.obj_id:" + uuid + " OR t01_object.org_obj_id:" + uuid + ")");
        return q;

    }

    public IngridQuery createServiceFeedInGridQuery(ServiceFeedRequest serviceFeedRequest) throws ParseException {

        String queryStr = "ranking:score (t01_object.obj_id:" + serviceFeedRequest.getUuid() + " OR t01_object.org_obj_id:" + serviceFeedRequest.getUuid() + ")";
        if (log.isDebugEnabled()) {
            log.debug("Query string: " + queryStr);
        }
        IngridQuery q = QueryStringParser.parse(queryStr);
        return q;

    }

    public IngridQuery createServiceFeedEntryInGridQuery(String[] uuids, ServiceFeedRequest serviceFeedRequest) throws ParseException {

        String queryStr;
        if (serviceFeedRequest.getQuery() != null && serviceFeedRequest.getQuery().length() > 0) {
            queryStr = "ranking:score ((" + StringUtils.join(uuids, ") OR (", serviceFeedRequest.getQuery() + " t01_object.obj_id:") + ")) OR (("
                    + StringUtils.join(uuids, ") OR (", serviceFeedRequest.getQuery() + " t01_object.org_obj_id:") + "))";
        } else {
            queryStr = "ranking:score (" + StringUtils.join(uuids, " OR ", "t01_object.obj_id:") + ") OR (" + StringUtils.join(uuids, " OR ", "t01_object.org_obj_id:") + ")";
        }
        if (log.isDebugEnabled()) {
            log.debug("Query string: " + queryStr);
        }
        IngridQuery q = QueryStringParser.parse(queryStr);
        return q;

    }

    public IngridQuery createDatasetFeedInGridQuery(String uuid) throws ParseException {

        String queryStr = "ranking:score (t01_object.obj_id:" + uuid + " OR t01_object.org_obj_id:" + uuid + ")";
        if (log.isDebugEnabled()) {
            log.debug("Query string: " + queryStr);
        }
        IngridQuery q = QueryStringParser.parse(queryStr);
        return q;
    }

    public IngridQuery createDatasetFeedInGridQuery(DatasetFeedRequest datasetFeedRequest) throws Exception {

        if (datasetFeedRequest.getDatasetFeedUuid() != null && datasetFeedRequest.getDatasetFeedUuid().length() > 0) {
            IngridQuery q = QueryStringParser.parse("ranking:score (t01_object.obj_id:" + datasetFeedRequest.getDatasetFeedUuid() + " OR t01_object.org_obj_id:" + datasetFeedRequest.getDatasetFeedUuid() + ")");
            return q;
        } else {
            IngridQuery q = QueryStringParser.parse("ranking:score (t01_object.obj_id:" + datasetFeedRequest.getServiceFeedUuid() + " OR t01_object.org_obj_id:" + datasetFeedRequest.getServiceFeedUuid() + ")");
            IBus iBus = iBusHelper.getIBus();
            IBusQueryResultIterator queryIterator = new IBusQueryResultIterator(q, new String[] {}, iBus);
            IngridHit hit = null;
            if (queryIterator.hasNext()) {
                hit = queryIterator.next();
                String plugId = hit.getPlugId();
                String qStr = "ranking:score iplugs:\"" + plugId + "\" (t011_obj_geo.datasource_uuid:\"" + datasetFeedRequest.getSpatialDatasetIdentifierCode() + "\" OR t011_obj_geo.datasource_uuid:\""
                        + datasetFeedRequest.getSpatialDatasetIdentifierNamespace() + "#" + datasetFeedRequest.getSpatialDatasetIdentifierCode() + "\")";
                if (datasetFeedRequest.getCrs() != null && datasetFeedRequest.getCrs().length() > 0) {
                    qStr = qStr + " t011_obj_geo.referencesystem_id:" + (datasetFeedRequest.getCrs().contains(":") ? "\"" + datasetFeedRequest.getCrs() + "\"" : datasetFeedRequest.getCrs());
                }
                if (datasetFeedRequest.getLanguage() != null && datasetFeedRequest.getLanguage().length() > 0) {
                    String[] supportedLanguages = config.getStringArray(SearchInterfaceConfig.ATOM_DOWNLOAD_OPENSEARCH_SUPPORTED_LANGUAGES);
                    for (String supportedLanguage : supportedLanguages) {
                        if (supportedLanguage.equals(datasetFeedRequest.getLanguage())) {
                            qStr = qStr + " lang:" + datasetFeedRequest.getLanguage();
                            break;
                        }
                    }
                }
                if (log.isDebugEnabled()) {
                    log.debug("Query string: " + qStr);
                }
                q = QueryStringParser.parse(qStr);
                return q;
            }
            throw new Exception("Cannot create InGrid query from dataset feed request: " + datasetFeedRequest);
        }
    }

    @Autowired
    public void setiBusHelper(IBusHelper iBusHelper) {
        this.iBusHelper = iBusHelper;
    }

    @Autowired
    public void setConfig(SearchInterfaceConfig config) {
        this.config = config;
    }

}
