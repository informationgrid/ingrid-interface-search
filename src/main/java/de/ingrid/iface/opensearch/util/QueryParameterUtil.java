/*-
 * **************************************************-
 * InGrid Interface Search
 * ==================================================
 * Copyright (C) 2014 - 2026 wemove digital solutions GmbH
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class QueryParameterUtil {

    public static String mergeQueryParameters(String oldQuery, String newQuery) {
        if (oldQuery == null || oldQuery.isEmpty()) {
            return newQuery;
        }
        if (newQuery == null || newQuery.isEmpty()) {
            return oldQuery;
        }

        Map<String, List<String>> oldParameters = new LinkedHashMap<>();
        Map<String, List<String>> newParameters = new LinkedHashMap<>();

        // Parse alte Parameter
        parseQueryString(oldQuery, oldParameters);

        // Parse neue Parameter
        parseQueryString(newQuery, newParameters);

        // Merge parameters, with new values replacing old ones
        Map<String, List<String>> mergedParameters = new LinkedHashMap<>(oldParameters);
        for (Map.Entry<String, List<String>> entry : newParameters.entrySet()) {
            mergedParameters.put(entry.getKey(), entry.getValue());
        }

        return buildQueryString(mergedParameters);
    }

    private static void parseQueryString(String queryString, Map<String, List<String>> parameters) {
        if (queryString == null || queryString.isEmpty()) {
            return;
        }

        String[] pairs = queryString.split("&");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=", 2);
            String key = urlDecode(keyValue[0]);
            String value = keyValue.length > 1 ? urlDecode(keyValue[1]) : "";

            parameters.computeIfAbsent(key, k -> new ArrayList<>()).add(value);
        }
    }

    private static String buildQueryString(Map<String, List<String>> parameters) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;

        for (Map.Entry<String, List<String>> entry : parameters.entrySet()) {
            String key = entry.getKey();
            for (String value : entry.getValue()) {
                if (!first) {
                    sb.append("&");
                }
                sb.append(urlEncode(key)).append("=").append(urlEncode(value));
                first = false;
            }
        }

        return sb.toString();
    }

    private static String urlEncode(String value) {
        try {
            return java.net.URLEncoder.encode(value, "UTF-8");
        } catch (java.io.UnsupportedEncodingException e) {
            return value;
        }
    }

    private static String urlDecode(String value) {
        try {
            return java.net.URLDecoder.decode(value, "UTF-8");
        } catch (java.io.UnsupportedEncodingException e) {
            return value;
        }
    }
}
