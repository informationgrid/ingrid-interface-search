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
package de.ingrid.iface.opensearch.util;

import java.util.ArrayList;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

import de.ingrid.utils.IngridHit;
import de.ingrid.utils.PlugDescription;
import de.ingrid.utils.query.IngridQuery;

public class OpensearchUtil {

    private static final String[][] XML_ESCAPE = { { "\"", "&", "<", ">" }, // "
                                                                            // -
                                                                            // strings
                                                                            // to
                                                                            // replace
            { "&quot;", "&amp;", "&lt;", "&gt;" } // & - replace with
    };
    
    public enum XML_TYPE {
        XML_1_0, XML_1_1  
    }
    
    private static final String XML_1_0_PATTERN = "[^"
            + "\u0009\r\n"
            + "\u0020-\uD7FF"
            + "\uE000-\uFFFD"
            + "\ud800\udc00-\udbff\udfff"
            + "]";

    private static final String XML_1_1_PATTERN = "[^"
            + "\u0001-\uD7FF"
            + "\uE000-\uFFFD"
            + "\ud800\udc00-\udbff\udfff"
            + "]+";


    public static boolean hasValue(String s) {
        return (s != null && s.length() > 0);
    }

    public static boolean hasPositiveDataType(IngridQuery q, String datatype) {
        String[] dtypes = q.getPositiveDataTypes();
        for (int i = 0; i < dtypes.length; i++) {
            if (dtypes[i].equalsIgnoreCase(datatype)) {
                return true;
            }
        }
        return false;
    }

    public static String deNullify(String s) {
        if (s == null) {
            return "";
        } else {
            return s;
        }
    }

    @SuppressWarnings("unchecked")
    public static String getDetailValue(IngridHit detail, String key) {
        Object obj = detail.get(key);
        if (obj == null) {
            return "";
        }

        StringBuffer values = new StringBuffer();
        if (obj instanceof String[]) {
            String[] valueArray = (String[]) obj;
            for (int i = 0; i < valueArray.length; i++) {
                if (i != 0) {
                    values.append(", ");
                }
                values.append(valueArray[i]);
            }
        } else if (obj instanceof ArrayList) {
            ArrayList valueList = (ArrayList) obj;
            for (int i = 0; i < valueList.size(); i++) {
                if (i != 0) {
                    values.append(", ");
                }
                values.append(valueList.get(i).toString());
            }
        } else {
            values.append(obj.toString());
        }
        return values.toString();
    }

    /**
     * Return true if the given iPlug has a specific data type.
     * 
     * @param iPlug
     *            The PlugDescription to work on.
     * @param dataType
     *            The data type to search for
     * @return True if the iPlug has the data type, false if not.
     */
    public static boolean hasDataType(PlugDescription iPlug, String dataType) {
        String[] dataTypes = iPlug.getDataTypes();
        for (int i = 0; i < dataTypes.length; i++) {
            if (dataTypes[i].equalsIgnoreCase(dataType)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Escapes XML special characters into the corresponding escape sequence.
     * The following characters will be replaced.
     * 
     * "\"", "&", "<", ">"
     * 
     * @param in
     *            The String to escape.
     * @return The escaped String.
     */
    public static String xmlEscape(String in) {
        StringEscapeUtils.escapeXml(in);
        return StringUtils.replaceEach(in, XML_ESCAPE[0], XML_ESCAPE[1]);
    }
    
    /**
     * Remove all illegal chars from a string according to the XML Literal specification.
     * 
     * @param in
     * @param xmlType Optional, defaults to XML 1.0.
     * @return
     */
    public static String removeInvalidChars(String in, XML_TYPE... xmlType) {
        if (xmlType.equals(XML_TYPE.XML_1_1)) {
            return in.replaceAll(XML_1_1_PATTERN, "");
        } else {
            return in.replaceAll(XML_1_0_PATTERN, "");
        }
    }

}
