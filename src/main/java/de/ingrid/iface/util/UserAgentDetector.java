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
            log.debug("Agent string: '" + agentString + "' isIE:" + Browser.parseUserAgentString(agentString).getGroup().equals(Browser.IE));
        }
        if (agentString == null) {
            return false;
        }
        return Browser.parseUserAgentString(agentString).getGroup().equals(Browser.IE);
    }

}
