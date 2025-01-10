/*
 * **************************************************-
 * ingrid-interface-search
 * ==================================================
 * Copyright (C) 2014 - 2025 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.2 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * **************************************************#
 */
package de.ingrid.iface_test.util;

import de.ingrid.iface.util.StringUtils;


import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class StringUtilsTest {

    @Test
    public void testEncodeForPath() {
        assertEquals("49C01584-4B3C-4F93-90CB-9830AFEA36F5", StringUtils.encodeForPath("49C01584-4B3C-4F93-90CB-9830AFEA36F5"));
        assertEquals("/49C01584-4B3C-4F93-90CB-9830AFEA36F5", StringUtils.encodeForPath("/49C01584-4B3C-4F93-90CB-9830AFEA36F5"));
        assertEquals("http://numis.niedersachsen.de/202/csw%3FREQUEST=GetRecordById&SERVICE=CSW&VERSION=2.0.2&id=28B5456A-AA9A-41F3-8EFA-27A0597A8FD9&iplug=ingrid-group:iplug-ouk-db-numis&elementSetName=full&elementSetName=full", StringUtils.encodeForPath("http://numis.niedersachsen.de/202/csw?REQUEST=GetRecordById&SERVICE=CSW&VERSION=2.0.2&id=28B5456A-AA9A-41F3-8EFA-27A0597A8FD9&iplug=ingrid-group:iplug-ouk-db-numis&elementSetName=full&elementSetName=full"));
        assertEquals("äöü&/$$%25@:%5C%5C", StringUtils.encodeForPath("äöü&/$$%@:\\\\"));
    }

    @Test
    public void testExtractEpsgCodeNumber() {
        assertEquals("4326", StringUtils.extractEpsgCodeNumber("epsg: 4326 (WGS 84)"));
        assertEquals("4326", StringUtils.extractEpsgCodeNumber("das ist ein epsg:4326 (WGS 84 code)"));
        assertEquals("43264", StringUtils.extractEpsgCodeNumber("das ist ein epsg:43264 (WGS 84 code)"));
        assertNull(StringUtils.extractEpsgCodeNumber("das ist ein epsg:432645 (WGS 84 code)"));
        assertEquals("4326", StringUtils.extractEpsgCodeNumber("das ist ein epsg4326 (WGS 84 code)"));
    }

}
