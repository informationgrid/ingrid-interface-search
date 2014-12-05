/*
 * **************************************************-
 * ingrid-interface-search
 * ==================================================
 * Copyright (C) 2014 wemove digital solutions GmbH
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
package de.ingrid.iface_test.util;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.NoSuchElementException;

import junit.framework.TestCase;
import de.ingrid.ibus.Bus;
import de.ingrid.iface.util.IBusQueryResultIterator;
import de.ingrid.iface.util.SearchInterfaceConfig;
import de.ingrid.utils.IngridHitDetail;
import de.ingrid.utils.IngridHits;
import de.ingrid.utils.query.IngridQuery;
import de.ingrid.utils.queryparser.QueryStringParser;

//Let's import Mockito statically so that the code looks clearer

public class IBusQueryResultIteratorTest extends TestCase {

    public void testIBusQueryResultIterator() throws Exception {
        Bus mockedIBus = mock(Bus.class);
        IngridQuery q = QueryStringParser.parse("datatype:idf t01_object.obj_uuid:1");

        IngridHitDetail[] details = new IngridHitDetail[10];
        for (int i = 0; i < 10; i++) {
            details[i] = new IngridHitDetail("plugid", i, 0, 1.0f, "title" + i, "summary" + i);
        }
        IngridHits hits_1_10 = new IngridHits(10, details);

        when(mockedIBus.searchAndDetail(q, 10, 1, 0, SearchInterfaceConfig.getInstance().getInt(SearchInterfaceConfig.IBUS_SEARCH_MAX_TIMEOUT, 30000), new String[] { "" })).thenReturn(hits_1_10);

        IBusQueryResultIterator it = new IBusQueryResultIterator(q, new String[] { "" }, mockedIBus);
        try {
            int cnt = 0;
            while (it.hasNext()) {
                assertEquals(cnt, it.next().getDocumentId());
                cnt++;
            }
        } catch (NoSuchElementException e) {
            assertTrue(false);
        }

        try {
            it.hasNext();
        } catch (NoSuchElementException e) {
            assertTrue(true);
        }

        try {
            it.next();
        } catch (NoSuchElementException e) {
            assertTrue(true);
        }

        details = new IngridHitDetail[10];
        for (int i = 0; i < 10; i++) {
            details[i] = new IngridHitDetail("plugid", i, 0, 1.0f, "title" + i, "summary" + i);
        }
        hits_1_10 = new IngridHits(19, details);

        details = new IngridHitDetail[9];
        for (int i = 10; i < 19; i++) {
            details[i - 10] = new IngridHitDetail("plugid", i, 0, 1.0f, "title" + i, "summary" + i);
        }
        IngridHits hits_11_19 = new IngridHits(19, details);

        when(mockedIBus.searchAndDetail(q, 10, 1, 0, SearchInterfaceConfig.getInstance().getInt(SearchInterfaceConfig.IBUS_SEARCH_MAX_TIMEOUT, 30000), new String[] { "" })).thenReturn(hits_1_10);
        when(mockedIBus.searchAndDetail(q, 10, 2, 10, SearchInterfaceConfig.getInstance().getInt(SearchInterfaceConfig.IBUS_SEARCH_MAX_TIMEOUT, 30000), new String[] { "" })).thenReturn(hits_11_19);

        it = new IBusQueryResultIterator(q, new String[] { "" }, mockedIBus);
        try {
            int cnt = 0;
            while (it.hasNext()) {
                assertEquals(cnt, it.next().getDocumentId());
                cnt++;
            }
            assertEquals(19, cnt);
        } catch (NoSuchElementException e) {
            assertTrue(false);
        }

        try {
            it.hasNext();
        } catch (NoSuchElementException e) {
            assertTrue(true);
        }

        try {
            it.next();
        } catch (NoSuchElementException e) {
            assertTrue(true);
        }


        // test results starting from other than page 1
        int startPage = 1;
        it = new IBusQueryResultIterator(q, new String[] { "" }, mockedIBus, 10, startPage, 1000);
        try {
            int cnt = 0;
            while (it.hasNext()) {
                assertEquals(startPage * 10 + cnt, it.next().getDocumentId());
                cnt++;
            }
            assertEquals(9, cnt);
        } catch (NoSuchElementException e) {
            assertTrue(false);
        }
    }
    
    public void testMultipleHasNextStability() throws Exception {
        Bus mockedIBus = mock(Bus.class);
        IngridQuery q = QueryStringParser.parse("datatype:idf t01_object.obj_uuid:1");

        IngridHitDetail[] details = new IngridHitDetail[10];
        for (int i = 0; i < 10; i++) {
            details[i] = new IngridHitDetail("plugid", i, 0, 1.0f, "title" + i, "summary" + i);
        }
        IngridHits hits_1_10 = new IngridHits(10, details);

        when(mockedIBus.searchAndDetail(q, 10, 1, 0, SearchInterfaceConfig.getInstance().getInt(SearchInterfaceConfig.IBUS_SEARCH_MAX_TIMEOUT, 30000), new String[] { "" })).thenReturn(hits_1_10);

        IBusQueryResultIterator it = new IBusQueryResultIterator(q, new String[] { "" }, mockedIBus);
        try {
            int cnt = 0;
            for (int i=0; i<100; i++) {
                assertTrue(it.hasNext());
            }
            
            while (it.hasNext()) {
                assertEquals(cnt, it.next().getDocumentId());
                cnt++;
            }
        } catch (NoSuchElementException e) {
            assertTrue(false);
        }
    }

}
