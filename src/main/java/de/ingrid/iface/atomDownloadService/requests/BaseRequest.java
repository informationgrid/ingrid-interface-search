package de.ingrid.iface.atomDownloadService.requests;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class BaseRequest {

    private final static Log log = LogFactory.getLog( BaseRequest.class );

    String protocol = null;

    protected void extractProtocol(HttpServletRequest req) {
        String proto = null;

        String xfp = req.getHeader( "X-Forwarded-Proto" );
        if (xfp != null && xfp.length() > 0) {
            proto = xfp;
        } else {
            proto = req.getScheme();
        }

        if (log.isDebugEnabled()) {
            log.debug( "extract protocol from request: " + proto + "; request: " + req.toString() + "; " + req.getServerName() + "; " + req.getServerPort() + "; "
                    + req.getScheme() + "; X-Forwarded-Proto:" + req.getHeader( "X-Forwarded-Proto" ) );
        }
        this.protocol = proto;
    }

    public String getProtocol() {
        return protocol;
    }

}
