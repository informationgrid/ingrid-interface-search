/*-
 * **************************************************-
 * ingrid-interface-search
 * ==================================================
 * Copyright (C) 2014 - 2021 wemove digital solutions GmbH
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
