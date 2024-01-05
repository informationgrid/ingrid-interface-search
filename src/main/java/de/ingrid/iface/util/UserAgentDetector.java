/*
 * **************************************************-
 * ingrid-interface-search
 * ==================================================
 * Copyright (C) 2014 - 2024 wemove digital solutions GmbH
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
package de.ingrid.iface.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import eu.bitwalker.useragentutils.Browser;

@Service
public class UserAgentDetector {

    private final static Log log = LogFactory.getLog(UserAgentDetector.class);
    
    public boolean isIE(String agentString) {
        if (log.isDebugEnabled()) {
            if (agentString == null) {
                log.debug("Agent string: '" + agentString + "' isIE: false");
            } else {
                log.debug("Agent string: '" + agentString + "' isIE:" + Browser.parseUserAgentString(agentString).getGroup().equals(Browser.IE));
            }
        }
        if (agentString == null) {
            return false;
        }
        return Browser.parseUserAgentString(agentString).getGroup().equals(Browser.IE);
    }

}
