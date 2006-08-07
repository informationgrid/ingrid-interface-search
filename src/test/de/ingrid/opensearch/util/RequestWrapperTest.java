package de.ingrid.opensearch.util;

import junit.framework.TestCase;
import de.ingrid.opensearch.test.ServletRequestMockObject;

public class RequestWrapperTest extends TestCase {

    /*
     * Test method for
     * 'de.ingrid.opensearch.util.RequestWrapper.RequestWrapper(HttpServletRequest)'
     */
    public void testRequestWrapper() {

        ServletRequestMockObject r = new ServletRequestMockObject();

        // check the normal use
        r.getParameterMap().put("p", "1");
        r.getParameterMap().put("q", "Wasser");
        r.getParameterMap().put("h", "10");
        r.getParameterMap().put("d", "1");

        RequestWrapper w = null;
        try {
            w = new RequestWrapper(r);
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertNotNull(w);
        assertEquals(w.getRequestedPage(), 1);
        assertEquals(w.getHitsPerPage(), 10);
        assertEquals(w.getRequestedDetails(), true);

        // check out of bounds parameters
        r.getParameterMap().put("p", "0");
        r.getParameterMap().put("q", "Wasser");
        r.getParameterMap().put("h", "10000");
        r.getParameterMap().remove("d");

        w = null;
        try {
            w = new RequestWrapper(r);
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertNotNull(w);
        assertEquals(w.getRequestedPage(), 1);
        assertEquals(w.getHitsPerPage(), 10);
        assertEquals(w.getRequestedDetails(), false);

        // check wrong query
        r.getParameterMap().put("q", "Wasser ((*");

        w = null;
        try {
            w = new RequestWrapper(r);
        } catch (Throwable t) {
            t.printStackTrace();
        }
        assertNull(w);
    }

}
