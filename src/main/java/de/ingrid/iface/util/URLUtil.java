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
