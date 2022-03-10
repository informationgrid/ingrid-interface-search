/*-
 * **************************************************-
 * ingrid-interface-search
 * ==================================================
 * Copyright (C) 2014 - 2022 wemove digital solutions GmbH
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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.net.HttpURLConnection;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class URLUtil {

    private final static Log log = LogFactory.getLog( URLUtil.class );

    /**
     * Update a given URL with a protocol, effectively changing the protocol of
     * that URL. A scheme-relative URL ('//domain/path?...') is also accepted.
     * 
     * @param urlStr
     * @param protocol
     * @throws MalformedURLException
     */
    public static String updateProtocol(String urlStr, String protocol) {
        if (urlStr.startsWith( "//" )) {
            return (new StringBuilder()).append( protocol ).append( ":" ).append( urlStr ).toString();
        } else {
            URL url;
            try {
                url = new URL( urlStr );
                return urlStr.replace( url.getProtocol(), protocol );
            } catch (MalformedURLException e) {
                log.error( "Unable to parse URL string for protocol replacement.", e );
            }
            return urlStr;
        }
    }


    /**
     * Get the final redirected URL from a valid URL. Takes also HTTP -> HTTPS redirects into account.
     *
     * @param url
     * @return
     * @throws IOException
     */
    public static String getRedirectedUrl(String url) throws IOException {

        URL resourceUrl, base, next;
        Map<String, Integer> visited = new HashMap<>();
        HttpURLConnection conn;
        String location;
        int times;

        while (true)
        {
            times = visited.compute(url, (key, count) -> count == null ? 1 : count + 1);

            if (times > 3) {
                throw new IOException("Stuck in redirect loop");
            }

            resourceUrl = new URL(url);
            conn = (HttpURLConnection) resourceUrl.openConnection();

            conn.setConnectTimeout(15000);
            conn.setReadTimeout(15000);
            conn.setInstanceFollowRedirects(false);   // Make the logic below easier to detect redirections
            conn.setRequestProperty("User-Agent", "Mozilla/5.0...");

            switch (conn.getResponseCode())
            {
                case HttpURLConnection.HTTP_MOVED_PERM:
                case HttpURLConnection.HTTP_MOVED_TEMP:
                    location = conn.getHeaderField("Location");
                    location = URLDecoder.decode(location, "UTF-8");
                    base     = new URL(url);
                    next     = new URL(base, location);  // Deal with relative URLs
                    url      = next.toExternalForm();
                    continue;
            }

            break;
        }
        return url;
    }


}
