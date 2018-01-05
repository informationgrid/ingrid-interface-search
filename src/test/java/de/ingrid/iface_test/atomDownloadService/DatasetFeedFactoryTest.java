/*
 * **************************************************-
 * ingrid-interface-search
 * ==================================================
 * Copyright (C) 2014 - 2018 wemove digital solutions GmbH
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
package de.ingrid.iface_test.atomDownloadService;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import de.ingrid.ibus.Bus;
import de.ingrid.iface.atomDownloadService.DatasetFeedFactory;
import de.ingrid.iface.atomDownloadService.IGCCoupledResourcesServiceFeedEntryProducer;
import de.ingrid.iface.atomDownloadService.ServiceFeedEntryProducer;
import de.ingrid.iface.atomDownloadService.ServiceFeedProducer;
import de.ingrid.iface.atomDownloadService.requests.DatasetFeedRequest;
import de.ingrid.iface.atomDownloadService.util.IngridQueryProducer;
import de.ingrid.iface.util.IBusHelper;
import de.ingrid.iface.util.SearchInterfaceConfig;
import de.ingrid.iface.util.ServiceFeedUtils;
import de.ingrid.iface.util.StringUtils;
import de.ingrid.iface.util.StringUtilsService;
import de.ingrid.utils.IBus;
import de.ingrid.utils.IngridHit;
import de.ingrid.utils.IngridHitDetail;
import de.ingrid.utils.IngridHits;
import de.ingrid.utils.dsc.Record;
import de.ingrid.utils.query.IngridQuery;
import de.ingrid.utils.queryparser.QueryStringParser;
import de.ingrid.utils.xml.IDFNamespaceContext;
import de.ingrid.utils.xpath.XPathUtils;

public class DatasetFeedFactoryTest {

    private DatasetFeedFactory datasetFeedFactory;

    private static final XPathUtils XPATH = new XPathUtils(new IDFNamespaceContext());

    @Test
    public void testGetDatasetFeedDocument() throws Exception {

        DatasetFeedRequest datasetFeedRequest = new DatasetFeedRequest();
        datasetFeedRequest.setServiceFeedUuid("SERVICE_FEED_UUID_1");
        datasetFeedRequest.setDatasetFeedUuid("DATASET_FEED_UUID_1");

        Document doc = datasetFeedFactory.getDatasetFeedDocument(datasetFeedRequest);

        assertEquals("DATASET_FEED_UUID_1", XPATH.getString(doc, "//gmd:fileIdentifier/gco:CharacterString"));

        datasetFeedRequest = new DatasetFeedRequest();
        datasetFeedRequest.setServiceFeedUuid("SERVICE_FEED_UUID_1");
        datasetFeedRequest
                .setDatasetFeedUuid("http://numis.niedersachsen.de/202/csw?REQUEST=GetRecordById&SERVICE=CSW&VERSION=2.0.2&id=28B5456A-AA9A-41F3-8EFA-27A0597A8FD9&iplug=/ingrid-group:iplug-ouk-db-numis&elementSetName=full&elementSetName=full");

        doc = datasetFeedFactory.getDatasetFeedDocument(datasetFeedRequest);

        assertEquals("28B5456A-AA9A-41F3-8EFA-27A0597A8FD9", XPATH.getString(doc, "//gmd:fileIdentifier/gco:CharacterString"));

        datasetFeedRequest = new DatasetFeedRequest();
        datasetFeedRequest.setServiceFeedUuid("SERVICE_FEED_UUID_1");
        datasetFeedRequest.setSpatialDatasetIdentifierCode("0e416521-9974-455e-9a49-538dca0546d6");
        datasetFeedRequest.setSpatialDatasetIdentifierNamespace("http://portalu.de/igc_testNS");

        doc = datasetFeedFactory.getDatasetFeedDocument(datasetFeedRequest);

        assertEquals("DATASET_FEED_UUID_2", XPATH.getString(doc, "//gmd:fileIdentifier/gco:CharacterString"));
        
        
    }

    @Before
    public void prepareMockedData() throws Exception {

        IBus mockedIbus = mock(Bus.class);
        IBusHelper iBusHelper = mock(IBusHelper.class);
        StringUtilsService stringUtilsService = mock(StringUtilsService.class);

        datasetFeedFactory = new DatasetFeedFactory();
        datasetFeedFactory.setiBusHelper(iBusHelper);
        datasetFeedFactory.setConfig(SearchInterfaceConfig.getInstance());

        IngridQueryProducer ingridQueryProducer = new IngridQueryProducer();
        ingridQueryProducer.setiBusHelper(iBusHelper);
        ingridQueryProducer.setConfig( SearchInterfaceConfig.getInstance() );
        datasetFeedFactory.setIngridQueryProducer(ingridQueryProducer);

        ServiceFeedUtils serviceFeedUtils = new ServiceFeedUtils();
        serviceFeedUtils.setConfig(SearchInterfaceConfig.getInstance());
        serviceFeedUtils.init();
        
        ServiceFeedProducer serviceFeedProducer = new ServiceFeedProducer();
        serviceFeedProducer.setServiceFeedUtils(serviceFeedUtils);
        serviceFeedProducer.setiBusHelper(iBusHelper);
        serviceFeedProducer.setIngridQueryProducer(ingridQueryProducer);
        List<ServiceFeedEntryProducer> serviceFeedEntryProducers = new ArrayList<ServiceFeedEntryProducer>();

        IGCCoupledResourcesServiceFeedEntryProducer igcServiceFeedEntryProducer = new IGCCoupledResourcesServiceFeedEntryProducer();
        igcServiceFeedEntryProducer.setConfig(SearchInterfaceConfig.getInstance());
        igcServiceFeedEntryProducer.setiBusHelper(iBusHelper);
        igcServiceFeedEntryProducer.setIngridQueryProducer(ingridQueryProducer);
        
        serviceFeedEntryProducers.add(igcServiceFeedEntryProducer);

        serviceFeedProducer.setServiceFeedEntryProducer(serviceFeedEntryProducers);

        datasetFeedFactory.setServiceFeedProducer(serviceFeedProducer);

        datasetFeedFactory.setStringUtilsService(stringUtilsService);

        IngridQuery q = QueryStringParser.parse("ranking:score (t01_object.obj_id:DATASET_FEED_UUID_1 OR t01_object.org_obj_id:DATASET_FEED_UUID_1) datatype:dsc_ecs");

        IngridHit[] hits = new IngridHit[2];
        for (int i = 0; i < hits.length; i++) {
            hits[i] = new IngridHit("plugid", i+"", 0, 1.0f);
            hits[i].setHitDetail(new IngridHitDetail("plugid", i+"", 0, 1.0f, "title" + i, "summary" + i));
            Record record = new Record();
            String data = IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream("data/idf_dataset_" + (i + 1) + ".xml"));
            record.put("compressed", "false");
            record.put("data", data);
            when(mockedIbus.getRecord(hits[i])).thenReturn(record);
        }
        IngridHits hitsObject = new IngridHits(hits.length, hits);

        when(
                mockedIbus.searchAndDetail(Matchers.eq(q), Matchers.eq(10), Matchers.eq(1), Matchers.eq(0), Matchers.eq(SearchInterfaceConfig.getInstance().getInt(SearchInterfaceConfig.IBUS_SEARCH_MAX_TIMEOUT, 30000)),
                        Matchers.any(String[].class))).thenReturn(hitsObject);

        q = QueryStringParser.parse("ranking:score (t01_object.obj_id:SERVICE_FEED_UUID_1 OR t01_object.org_obj_id:SERVICE_FEED_UUID_1) datatype:dsc_ecs");

        hits = new IngridHit[1];
        for (int i = 0; i < hits.length; i++) {
            hits[i] = new IngridHit("plugid", (i+2)+"", 0, 1.0f);
            hits[i].setHitDetail(new IngridHitDetail("plugid", (i+2)+"", 0, 1.0f, "service feed " + i, "summary service feed " + i));
            Record record = new Record();
            String data = IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream("data/idf_service_" + (i + 1) + ".xml"));
            record.put("compressed", "false");
            record.put("data", data);
            when(mockedIbus.getRecord(hits[i])).thenReturn(record);
        }
        hitsObject = new IngridHits(hits.length, hits);

        when(
                mockedIbus.searchAndDetail(Matchers.eq(q), Matchers.eq(10), Matchers.eq(1), Matchers.eq(0), Matchers.eq(SearchInterfaceConfig.getInstance().getInt(SearchInterfaceConfig.IBUS_SEARCH_MAX_TIMEOUT, 30000)),
                        Matchers.any(String[].class))).thenReturn(hitsObject);
        
        q = QueryStringParser.parse("ranking:score iplugs:\"plugid\" (t011_obj_geo.datasource_uuid:\"0e416521-9974-455e-9a49-538dca0546d6\" OR t011_obj_geo.datasource_uuid:\"http://portalu.de/igc_testNS#0e416521-9974-455e-9a49-538dca0546d6\") datatype:dsc_ecs");

        hits = new IngridHit[1];
        for (int i = 0; i < hits.length; i++) {
            hits[i] = new IngridHit("plugid", (i+3)+"", 0, 1.0f);
            hits[i].setHitDetail(new IngridHitDetail("plugid", (i+3)+"", 0, 1.0f, "service feed " + i, "summary service feed " + i));
            Record record = new Record();
            String data = IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream("data/idf_dataset_2.xml"));
            record.put("compressed", "false");
            record.put("data", data);
            when(mockedIbus.getRecord(hits[i])).thenReturn(record);
        }
        hitsObject = new IngridHits(hits.length, hits);
        
        when(
                mockedIbus.searchAndDetail(Matchers.eq(q), Matchers.eq(10), Matchers.eq(1), Matchers.eq(0), Matchers.eq(SearchInterfaceConfig.getInstance().getInt(SearchInterfaceConfig.IBUS_SEARCH_MAX_TIMEOUT, 30000)),
                        Matchers.any(String[].class))).thenReturn(hitsObject);
        
        
        when(iBusHelper.getIBus()).thenReturn(mockedIbus);

        when(
                stringUtilsService
                        .urlToDocument("http://numis.niedersachsen.de/202/csw?REQUEST=GetRecordById&SERVICE=CSW&VERSION=2.0.2&id=28B5456A-AA9A-41F3-8EFA-27A0597A8FD9&iplug=/ingrid-group:iplug-ouk-db-numis&elementSetName=full&elementSetName=full", 1000, 1000))
                .thenReturn(StringUtils.inputSourceToDocument(new InputSource(this.getClass().getClassLoader().getResourceAsStream("data/csw_numis.xml"))));

    }

}
