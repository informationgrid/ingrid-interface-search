/*-
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
package de.ingrid.iface.util;

import static org.junit.Assert.*;

import java.net.MalformedURLException;

import org.junit.Test;

public class URLUtilTest {

    @Test
    public void testUpdateProtocol() throws MalformedURLException {
        
        assertEquals( "https://domain/path/", URLUtil.updateProtocol( "http://domain/path/", "https" ));
        assertEquals( "https://domain/path/", URLUtil.updateProtocol( "//domain/path/", "https" ));
        assertEquals( "http://domain/path/", URLUtil.updateProtocol( "https://domain/path/", "http" ));
        assertEquals( "https://192.168.0.228:8181/dls/dataset/{servicefeed-uuid}/?spatial_dataset_identifier_code={inspire_dls:spatial_dataset_identifier_code?}&amp;spatial_dataset_identifier_namespace={inspire_dls:spatial_dataset_identifier_namespace?}&amp;language={language?}", URLUtil.updateProtocol( "http://192.168.0.228:8181/dls/dataset/{servicefeed-uuid}/?spatial_dataset_identifier_code={inspire_dls:spatial_dataset_identifier_code?}&amp;spatial_dataset_identifier_namespace={inspire_dls:spatial_dataset_identifier_namespace?}&amp;language={language?}", "https" ));
    }

}
