package de.ingrid.opensearch.util;

import java.util.ArrayList;

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
        return StringUtils.replaceEach(in, XML_ESCAPE[0], XML_ESCAPE[1]);
    }

}
