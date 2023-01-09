/*
 * **************************************************-
 * ingrid-interface-search
 * ==================================================
 * Copyright (C) 2014 - 2022 wemove digital solutions GmbH
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
package de.ingrid.iface_test.opensearch.util;

import static org.junit.jupiter.api.Assertions.*;


import org.junit.jupiter.api.Test;

import de.ingrid.iface.opensearch.util.RequestWrapper;
import de.ingrid.iface_test.opensearch.test.ServletRequestMockObject;

public class RequestWrapperTest {

    /*
     * Test method for
     * 'de.ingrid.opensearch.util.RequestWrapper.RequestWrapper(HttpServletRequest)'
     */
    @Test
    public void testRequestWrapper() {

        ServletRequestMockObject r = new ServletRequestMockObject();

        // check the normal use
        r.getParameterMap().put("p", "1");
        r.getParameterMap().put("q", "Wasser");
        r.getParameterMap().put("h", "10");
        r.getParameterMap().put("plugid", "ingrid:test/test:ingrid");
        r.getParameterMap().put("docid", "1234567");
        r.getParameterMap().put("altdocid", "1234567QWERTA");

        RequestWrapper w = null;
        try {
            w = new RequestWrapper(r);
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertNotNull(w);
        assertEquals(w.getRequestedPage(), 1);
        assertEquals(w.getHitsPerPage(), 10);
        assertEquals(w.getPlugId(), "ingrid:test/test:ingrid");
        assertEquals(w.getDocId(), 1234567);
        assertEquals(w.getAltDocId(), "1234567QWERTA");
        assertEquals(w.getQueryString(), "Wasser");

        // check out of bounds parameters
        r.getParameterMap().put("p", "0");
        r.getParameterMap().put("q", "Wasser");
        r.getParameterMap().put("h", "10000");

        w = null;
        try {
            w = new RequestWrapper(r);
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertNotNull(w);
        assertEquals(w.getRequestedPage(), 1);
        assertEquals(w.getHitsPerPage(), 10000);
        assertEquals(w.getPlugId(), "ingrid:test/test:ingrid");
        assertEquals(w.getDocId(), 1234567);
        assertEquals(w.getAltDocId(), "1234567QWERTA");
        assertEquals(w.getQueryString(), "Wasser");

        // check wrong query
        r.getParameterMap().put("q", "Wasser ((*");

        w = null;
        try {
            w = new RequestWrapper(r);
        } catch (Throwable t) {
            t.printStackTrace();
        }
        assertNotNull(w);
        assertEquals(w.getRequestedPage(), 1);
        assertEquals(w.getHitsPerPage(), 10000);
        assertEquals(w.getPlugId(), "ingrid:test/test:ingrid");
        assertEquals(w.getDocId(), 1234567);
        assertEquals(w.getAltDocId(), "1234567QWERTA");
        assertEquals(w.getQueryString(), "Wasser ((*");

        // check missing query (get details)
        r.getParameterMap().remove("q");

        w = null;
        try {
            w = new RequestWrapper(r);
        } catch (Throwable t) {
            t.printStackTrace();
        }
        assertNotNull(w);
        assertEquals(w.getRequestedPage(), 1);
        assertEquals(w.getHitsPerPage(), 10000);
        assertEquals(w.getPlugId(), "ingrid:test/test:ingrid");
        assertEquals(w.getDocId(), 1234567);
        assertEquals(w.getAltDocId(), "1234567QWERTA");
        assertEquals(w.getQueryString(), "");
    }

}
