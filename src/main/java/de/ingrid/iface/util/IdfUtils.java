/*
 * **************************************************-
 * ingrid-interface-search
 * ==================================================
 * Copyright (C) 2014 - 2015 wemove digital solutions GmbH
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
/*
 * Copyright (c) 2012 wemove digital solutions. All rights reserved.
 */
package de.ingrid.iface.util;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;

import de.ingrid.utils.dsc.Record;
import de.ingrid.utils.idf.IdfTool;
import de.ingrid.utils.xml.IDFNamespaceContext;
import de.ingrid.utils.xpath.XPathUtils;

public class IdfUtils {

    private static final String ID_XPATH = "idf:html/idf:body/idf:idfMdMetadata/gmd:fileIdentifier/gco:CharacterString";

    final protected static Log log = LogFactory.getLog(IdfUtils.class);

    /**
     * Extract the idf document from the given record. Throws an exception if
     * there is not idf content.
     * 
     * @param record
     * @return Document
     * @throws Exception
     */
    public static Document getIdfDocument(Record record) throws Exception {
        String content = IdfTool.getIdfDataFromRecord(record);
        if (content != null) {
            try {
                return StringUtils.stringToDocument(content);
            } catch (Throwable t) {
                log.error("Error transforming record to DOM: " + content, t);
                throw new Exception(t);
            }
        } else {
            throw new IOException("Document contains no IDF data.");
        }
    }

    /**
     * Extract the id from the idf document.
     * 
     * @param document
     * @return Serializable
     * @throws Exception
     */
    public static Serializable getRecordId(Document document) throws Exception {
        XPathUtils xpath = new XPathUtils(new IDFNamespaceContext());
        Serializable id = xpath.getString(document, ID_XPATH);
        return id;
    }

    /**
     * Extract the id from the idf content of the given record. Throws an
     * exception if there is not idf content.
     * 
     * @param record
     * @return Serializable
     * @throws Exception
     */
    public static Serializable getRecordId(Record record) throws Exception {
        Document doc = getIdfDocument(record);
        return getRecordId(doc);
    }

    /**
     * Returns the largest BBOX of an IDF document as stated in
     * gmd:EX_GeographicBoundingBox as a List of polygon values. The result
     * contains 10 values describing the bbox as a polygon (south, west, north, west, north, east,
     * south, east, south, west).
     * 
     * In case the BBOX is just one point, the polygon only consists off 2
     * values (0- longitude, 1-latitude)
     * 
     * @param document
     * @return
     */
    public static List<Double> getEnclosingBoundingBoxAsPolygon(Document document) {
        Double[] bbox = getEnclosingBoundingBox(document);

        List<Double> result = new ArrayList<Double>();
        if (bbox[0].equals(bbox[1]) && bbox[2].equals(bbox[3])) {
            result.add(bbox[2]);
            result.add(bbox[0]);
        } else {
            result.add(bbox[2]);
            result.add(bbox[0]);
            result.add(bbox[3]);
            result.add(bbox[0]);
            result.add(bbox[3]);
            result.add(bbox[1]);
            result.add(bbox[2]);
            result.add(bbox[1]);
            result.add(bbox[2]);
            result.add(bbox[0]);
        }

        return result;

    }

    /**
     * Detects the largest BBOX of an IDF document as stated in
     * gmd:EX_GeographicBoundingBox.
     * 
     * @param document
     * @return An Array of Doubles where the values are: 0-west, 1-east,
     *         2-south, 3-north
     */
    public static Double[] getEnclosingBoundingBox(Document document) {
        XPathUtils xpath = new XPathUtils(new IDFNamespaceContext());
        String[] west = xpath.getStringArray(document, "//gmd:identificationInfo//gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox/gmd:westBoundLongitude/gco:Decimal");
        String[] east = xpath.getStringArray(document, "//gmd:identificationInfo//gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox/gmd:eastBoundLongitude/gco:Decimal");
        String[] south = xpath.getStringArray(document, "//gmd:identificationInfo//gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox/gmd:southBoundLatitude/gco:Decimal");
        String[] north = xpath.getStringArray(document, "//gmd:identificationInfo//gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox/gmd:northBoundLatitude/gco:Decimal");

        Double outerWest = null;
        Double outerEast = null;
        Double outerSouth = null;
        Double outerNorth = null;
        for (int i = 0; i < west.length; i++) {
            Double value = Double.valueOf(west[i]);
            if (outerWest == null) {
                outerWest = value;
            } else if (outerWest > value) {
                outerWest = value;
            }
            value = Double.valueOf(east[i]);
            if (outerEast == null) {
                outerEast = value;
            } else if (outerEast < value) {
                outerEast = value;
            }
            value = Double.valueOf(north[i]);
            if (outerNorth == null) {
                outerNorth = value;
            } else if (outerNorth < value) {
                outerNorth = value;
            }
            value = Double.valueOf(south[i]);
            if (outerSouth == null) {
                outerSouth = value;
            } else if (outerSouth > value) {
                outerSouth = value;
            }
        }

        return new Double[] { outerWest, outerEast, outerSouth, outerNorth };

    }
}
