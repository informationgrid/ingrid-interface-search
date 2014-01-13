package de.ingrid.iface_test.util;

import de.ingrid.iface.util.StringUtils;
import junit.framework.TestCase;

public class StringUtilsTest extends TestCase {

    public void testEncodeForPath() {
        assertEquals("49C01584-4B3C-4F93-90CB-9830AFEA36F5", StringUtils.encodeForPath("49C01584-4B3C-4F93-90CB-9830AFEA36F5"));
        assertEquals("/49C01584-4B3C-4F93-90CB-9830AFEA36F5", StringUtils.encodeForPath("/49C01584-4B3C-4F93-90CB-9830AFEA36F5"));
        assertEquals("http://numis.niedersachsen.de/202/csw%3FREQUEST=GetRecordById&SERVICE=CSW&VERSION=2.0.2&id=28B5456A-AA9A-41F3-8EFA-27A0597A8FD9&iplug=ingrid-group:iplug-ouk-db-numis&elementSetName=full&elementSetName=full", StringUtils.encodeForPath("http://numis.niedersachsen.de/202/csw?REQUEST=GetRecordById&SERVICE=CSW&VERSION=2.0.2&id=28B5456A-AA9A-41F3-8EFA-27A0597A8FD9&iplug=ingrid-group:iplug-ouk-db-numis&elementSetName=full&elementSetName=full"));
        assertEquals("äöü&/$$%25@:%5C%5C", StringUtils.encodeForPath("äöü&/$$%@:\\\\"));
    }
    
    public void testExtractEpsgCodeNumber() {
        assertEquals("4326", StringUtils.extractEpsgCodeNumber("epsg: 4326 (WGS 84)"));
        assertEquals("4326", StringUtils.extractEpsgCodeNumber("das ist ein epsg:4326 (WGS 84 code)"));
        assertEquals("43264", StringUtils.extractEpsgCodeNumber("das ist ein epsg:43264 (WGS 84 code)"));
        assertEquals(null, StringUtils.extractEpsgCodeNumber("das ist ein epsg:432645 (WGS 84 code)"));
        assertEquals("4326", StringUtils.extractEpsgCodeNumber("das ist ein epsg4326 (WGS 84 code)"));
    }

}
