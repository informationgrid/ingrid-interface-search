package de.ingrid.iface.atomDownloadService.requests;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class BaseRequest {

    private final static Log log = LogFactory.getLog( BaseRequest.class );

    String protocol = null;

    protected void extractProtocol(HttpServletRequest req) {
        if (log.isDebugEnabled()) {
            log.debug( "extract protocl from request: " + req.getScheme() );
        }
        this.protocol = req.getScheme();
    }

    public String getProtocol() {
        return protocol;
    }

}
