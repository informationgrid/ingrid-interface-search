package de.ingrid.iface.util;

import org.springframework.stereotype.Service;

import eu.bitwalker.useragentutils.Browser;

@Service
public class UserAgentDetector {

    public boolean isIE(String agentString) {
        if (agentString == null) {
            return false;
        }
        return Browser.parseUserAgentString(agentString).getGroup().equals(Browser.IE);
    }

}
