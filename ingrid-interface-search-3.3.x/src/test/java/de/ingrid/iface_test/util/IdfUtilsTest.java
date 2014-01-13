package de.ingrid.iface_test.util;

import java.io.StringReader;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import junit.framework.TestCase;

import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import de.ingrid.iface.util.IdfUtils;

public class IdfUtilsTest extends TestCase {

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
