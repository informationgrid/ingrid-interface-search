/*
 * **************************************************-
 * ingrid-interface-search
 * ==================================================
 * Copyright (C) 2014 - 2024 wemove digital solutions GmbH
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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.StringReader;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import de.ingrid.iface.util.IdfUtils;

public class IdfUtilsTest {

    @Test
    public void testEnclosingBoundingBoxAsPolygon() throws Exception {

        String data = IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream("data/idf_dataset_2.xml"));
        List<Double> results = IdfUtils.getEnclosingBoundingBoxAsPolygon(stringToDocument(data));
        assertEquals(50.323895, results.get(0));
        assertEquals(5.8665085, results.get(1));
        assertEquals(52.531437, results.get(2));
        assertEquals(5.8665085, results.get(3));
        assertEquals(52.531437, results.get(4));
        assertEquals(9.461479, results.get(5));
        assertEquals(50.323895, results.get(6));
        assertEquals(9.461479, results.get(7));
        assertEquals(50.323895, results.get(8));
        assertEquals(5.8665085, results.get(9));

    }

    public static Document stringToDocument(String string) throws Exception {
        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        domFactory.setNamespaceAware(true);
        DocumentBuilder builder = domFactory.newDocumentBuilder();
        Document doc = builder.parse(new InputSource(new StringReader(string)));
        return doc;
    }

}
