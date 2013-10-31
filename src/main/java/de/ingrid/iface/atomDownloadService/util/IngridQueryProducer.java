package de.ingrid.iface.atomDownloadService.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.ingrid.iface.atomDownloadService.requests.DatasetFeedRequest;
import de.ingrid.iface.atomDownloadService.requests.ServiceFeedRequest;
import de.ingrid.iface.util.IBusHelper;
import de.ingrid.iface.util.IBusQueryResultIterator;
import de.ingrid.iface.util.StringUtils;
import de.ingrid.utils.IBus;
import de.ingrid.utils.IngridHit;
import de.ingrid.utils.query.IngridQuery;
import de.ingrid.utils.queryparser.ParseException;
import de.ingrid.utils.queryparser.QueryStringParser;

@Service
public class IngridQueryProducer {

    private IBusHelper iBusHelper;

    public IngridQuery createServiceFeedInGridQuery(String uuid) throws ParseException {

        IngridQuery q = QueryStringParser.parse("ranking:score t01_object.obj_id:" + uuid);
        return q;

    }

    public IngridQuery createServiceFeedInGridQuery(ServiceFeedRequest serviceFeedRequest) throws ParseException {

        String queryStr = "ranking:score t01_object.obj_id:" + serviceFeedRequest.getUuid();
        IngridQuery q = QueryStringParser.parse(queryStr);
        return q;

    }

    public IngridQuery createServiceFeedEntryInGridQuery(String[] uuids, ServiceFeedRequest serviceFeedRequest) throws ParseException {

        String queryStr = "ranking:score (" + StringUtils.join(uuids, " OR ", "t01_object.obj_id:") + ")";
        if (serviceFeedRequest.getQuery() != null) {
            queryStr += " " + serviceFeedRequest.getQuery();
        }
        IngridQuery q = QueryStringParser.parse(queryStr);
        return q;

    }

    public IngridQuery createDatasetFeedInGridQuery(String uuid) throws ParseException {

        IngridQuery q = QueryStringParser.parse("ranking:score t01_object.obj_id:" + uuid);
        return q;
    }

    public IngridQuery createDatasetFeedInGridQuery(DatasetFeedRequest datasetFeedRequest) throws Exception {

        if (datasetFeedRequest.getDatasetFeedUuid() != null && datasetFeedRequest.getDatasetFeedUuid().length() > 0) {
            IngridQuery q = QueryStringParser.parse("ranking:score t01_object.obj_id:" + datasetFeedRequest.getDatasetFeedUuid());
            return q;
        } else {
            IngridQuery q = QueryStringParser.parse("ranking:score t01_object.obj_id:" + datasetFeedRequest.getServiceFeedUuid());
            IBus iBus = iBusHelper.getIBus();
            IBusQueryResultIterator queryIterator = new IBusQueryResultIterator(q, new String[] {}, iBus);
            IngridHit hit = null;
            if (queryIterator.hasNext()) {
                hit = queryIterator.next();
                String plugId = hit.getPlugId();
                q = QueryStringParser.parse("ranking:score iplugs:\"" + plugId + "\" (t011_obj_geo.datasource_uuid:\"" + datasetFeedRequest.getSpatialDatasetIdentifierCode() + "\" OR t011_obj_geo.datasource_uuid:\""
                        + datasetFeedRequest.getSpatialDatasetIdentifierNamespace() + "#" + datasetFeedRequest.getSpatialDatasetIdentifierCode() + "\")");
                return q;
            }
            throw new Exception("Cannot create InGrid query from dataset feed request: " + datasetFeedRequest);
        }
    }

    @Autowired
    public void setiBusHelper(IBusHelper iBusHelper) {
        this.iBusHelper = iBusHelper;
    }

}
