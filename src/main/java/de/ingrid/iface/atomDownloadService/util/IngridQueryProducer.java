package de.ingrid.iface.atomDownloadService.util;

import org.springframework.stereotype.Service;

import de.ingrid.iface.util.StringUtils;
import de.ingrid.utils.query.IngridQuery;
import de.ingrid.utils.queryparser.ParseException;
import de.ingrid.utils.queryparser.QueryStringParser;

@Service
public class IngridQueryProducer {

    public IngridQuery createServiceFeedInGridQuery(String uuid) throws ParseException {

        IngridQuery q = QueryStringParser.parse("ranking:score t01_object.obj_id:" + uuid);
        return q;

    }

    public IngridQuery createServiceFeedEntryInGridQuery(String[] uuids) throws ParseException {

        IngridQuery q = QueryStringParser.parse("ranking:score (" + StringUtils.join(uuids, " OR ", "t01_object.obj_id:") + ")");
        return q;

    }

    public IngridQuery createDatasetFeedInGridQuery(String uuid) throws ParseException {

        IngridQuery q = QueryStringParser.parse("ranking:score t01_object.obj_id:" + uuid);
        return q;
    }

}
