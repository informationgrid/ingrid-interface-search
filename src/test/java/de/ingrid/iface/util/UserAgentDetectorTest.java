/*
 * **************************************************-
 * ingrid-interface-search
 * ==================================================
 * Copyright (C) 2014 - 2020 wemove digital solutions GmbH
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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class UserAgentDetectorTest {

    String[] ie6clients = new String[] { "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0; T312461)", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; .NET CLR 1.1.4322; XMPP Tiscali Communicator v.10.0.2; .NET CLR 2.0.50727)",
            "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.2; SV1; .NET CLR 1.1.4322; .NET CLR 2.0.50727)" };

    String[] ie7clients = new String[] { "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; SV1; .NET CLR 2.0.50727)", "Mozilla/4.0 (compatible; MSIE 7.0b; Windows NT 6.0 ; .NET CLR 2.0.50215; SL Commerce Client v1.0; Tablet PC 2.0",
            "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; .NET CLR 1.1.4322; .NET CLR 2.0.50727)", "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.0; SLCC1; .NET CLR 2.0.50727; .NET CLR 3.0.04506)" // Windows
                                                                                                                                                                                                             // Mail
                                                                                                                                                                                                             // on
                                                                                                                                                                                                             // Vista
    };

    String[] ie8clients = new String[] { "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.0; WOW64; SLCC1; .NET CLR 2.0.50727; .NET CLR 3.0.04506; Media Center PC 5.0; .NET CLR 1.1.4322)",
            "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.0; Win64; x64; .NET CLR 2.0.50727; SLCC1; Media Center PC 5.0; .NET CLR 3.0.04506)" };

    String[] ie9clients = new String[] {
            "Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; WOW64; Trident/5.0; SLCC2; .NET CLR 2.0.50727; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0; Zune 4.0; InfoPath.3; MS-RTC LM 8; .NET4.0C; .NET4.0E)",
            "Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; Win64; x64; Trident/5.0" };

    String[] ie10clients = new String[] { "Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.1; Trident/6.0)", "Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.2; Trident/6.0)" };

    String[] ie11clients = new String[] { "Mozilla/5.0 (Windows NT 6.3; WOW64; Trident/7.0; rv:11.0) like Gecko", "Mozilla/5.0 (Windows NT 6.3; WOW64; Trident/7.0; ASU2JS; rv:11.0) like Gecko" // 64bit
                                                                                                                                                                                                 // Win8
    };

    String[] ie55clients = new String[] { "Mozilla/4.0 (compatible; MSIE 5.5; Windows NT 5.0; .NET CLR 1.1.4322)", "Mozilla/4.0 (compatible; MSIE 5.5; Windows NT 5.0)", "Mozilla/4.0 (compatible; MSIE 5.5; Windows 95)" };

    String[] ieTooOld = new String[] { "Mozilla/4.0 (compatible; MSIE 4.01; Windows 95)", "Mozilla/4.0 (compatible; MSIE 4.0; Windows 95; .NET CLR 1.1.4322; .NET CLR 2.0.50727)", "Mozilla/2.0 (compatible; MSIE 3.03; Windows 3.1)" };

    String[] outlook2007 = new String[] { "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.0; SLCC1; .NET CLR 2.0.50727; .NET CLR 3.0.04506; .NET CLR 1.1.4322; MSOffice 12)" };

    String[] outlook2010 = new String[] {
            "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.0; Trident/4.0; GTB6.4; Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1) ; SLCC1; .NET CLR 2.0.50727; Media Center PC 5.0; .NET CLR 1.1.4322; .NET CLR 3.5.30729; .NET CLR 3.0.30729; OfficeLiveConnector.1.3; OfficeLivePatch.0.0; MSOffice 14)",
            "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.1; Trident/4.0; .NET CLR 2.0.50727; .NET CLR 3.0.30729; .NET CLR 3.5.30729; Media Center PC 6.0; SLCC2; ms-office; MSOffice 14)" };

    String[] outookExpress = new String[] { "Outlook-Express/7.0 (MSIE 6.0; Windows NT 5.1; SV1; SIMBAR={xxx}; .NET CLR 2.0.50727; .NET CLR 1.1.4322; TmstmpExt)",
            "Outlook-Express/7.0 (MSIE 7.0; Windows NT 5.1; InfoPath.2; .NET CLR 1.1.4322; .NET CLR 2.0.50727; TmstmpExt)" };

    String[] ieMobile6 = new String[] { "HTC_TyTN Mozilla/4.0 (compatible; MSIE 6.0; Windows CE; IEMobile 6.12)", "Mozilla/4.0 (compatible; MSIE 6.0; Windows CE; IEMobile 6.12) Vodafone/1.0/HTC_s710/1.22.172.3",
            "Mozilla/4.0 (compatible; MSIE 6.0; Windows CE; IEMobile 6.8) PPC; 240x320; HTC_TyTN/1.0 Profile/MIDP-2.0 Configuration/CLDC-1.1" };

    String[] ieMobile7 = new String[] { "HTC_TouchDual Mozilla/4.0 (compatible; MSIE 6.0; Windows CE; IEMobile 7.6)",
            "PPC; 240x320; HTC_P3450/1.0 Profile/MIDP-2.0 Configuration/CLDC-1.1 Mozilla/4.0 (compatible; MSIE 6.0; Windows CE; IEMobile 7.6)",
            "Mozilla/4.0 (compatible; MSIE 6.0; Windows CE; IEMobile 7.6) PPC; MDA Vario/3.0 Profile/MIDP-2.0 Configuration/CLDC-1.1", "Palm750/v0005 Mozilla/4.0 (compatible; MSIE 6.0; Windows CE; IEMobile 7.6) UP.Link/6.3.0.0.0" };

    String[] ieMobile9 = new String[] { "Mozilla/5.0 (compatible; MSIE 9.0; Windows Phone OS 7.5; Trident/5.0; IEMobile/9.0)" };

    String[] ieMobile10 = new String[] { "Mozilla/5.0 (compatible; MSIE 10.0; Windows Phone 8.0; Trident/6.0; IEMobile/10.0; ARM; Touch; NOKIA; Lumia 920)" };

    String[] ie11Rss = new String[] {
            "Windows-RSS-Platform/2.0 (IE 11.0; Windows NT 6.1)"
    };
    
    
    @Test
    public void testUserAgentDetector() {

        UserAgentDetector userAgentDetector = new UserAgentDetector();

        assertTrue(userAgentDetector.isIE(ie6clients[0]));
        assertTrue(userAgentDetector.isIE(ie7clients[0]));
        assertTrue(userAgentDetector.isIE(ie8clients[0]));
        assertTrue(userAgentDetector.isIE(ie9clients[0]));
        assertTrue(userAgentDetector.isIE(ie10clients[0]));
        assertTrue(userAgentDetector.isIE(ie11clients[0]));
        assertTrue(userAgentDetector.isIE(ieTooOld[0]));
        assertTrue(userAgentDetector.isIE(ieMobile10[0]));
        assertTrue(userAgentDetector.isIE(ie11Rss[0]));
        
        assertFalse(userAgentDetector.isIE(null));
        assertFalse(userAgentDetector.isIE(""));
    }
}
