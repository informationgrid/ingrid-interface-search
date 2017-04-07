/*-
 * **************************************************-
 * ingrid-interface-search
 * ==================================================
 * Copyright (C) 2014 - 2017 wemove digital solutions GmbH
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

import java.net.MalformedURLException;
import java.net.URL;

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

}
