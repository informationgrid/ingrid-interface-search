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
package de.ingrid.iface.atomDownloadService;

import org.apache.commons.lang.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.ingrid.iface.atomDownloadService.om.ServiceFeed;
import de.ingrid.iface.atomDownloadService.om.ServiceFeedList;
import de.ingrid.iface.util.StringUtils;
import de.ingrid.iface.util.UserAgentDetector;

@Service
public class ServiceFeedListAtomBuilder {

    private UserAgentDetector userAgentDetector = null;

    public String build(ServiceFeedList serviceFeedList, String agentString) {

        StringBuilder result = new StringBuilder();
        result.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        result.append("<feed xmlns=\"http://www.w3.org/2005/Atom\" xmlns:georss=\"http://www.georss.org/georss\" xmlns:inspire_dls=\"http://inspire.ec.europa.eu/schemas/inspire_dls/1.0\" xml:lang=\"de\">\n");
        result.append("<!-- feed title -->\n");
        result.append("<title>ATOM download service feeds</title>\n");
        result.append("<!-- self-referencing link to this feed -->\n");
        result.append("<link href=\"" + StringEscapeUtils.escapeXml(serviceFeedList.getSelfReferencingLink().getHref()) + "\" rel=\"" + serviceFeedList.getSelfReferencingLink().getRel() + "\" type=\""
                + serviceFeedList.getSelfReferencingLink().getType() + "\" hreflang=\"" + serviceFeedList.getSelfReferencingLink().getHrefLang() + "\" title=\""
                + StringEscapeUtils.escapeXml(serviceFeedList.getSelfReferencingLink().getTitle()) + "\"/>\n");
        result.append("<!-- identifier -->\n");
        result.append("<id>" + StringEscapeUtils.escapeXml(serviceFeedList.getSelfReferencingLink().getHref()) + "</id>\n");

        boolean isBrowserIE = userAgentDetector.isIE(agentString);

        for (ServiceFeed entry : serviceFeedList.getEntries()) {
            result.append("<entry>\n");
            result.append("<!-- title service feed -->\n");
            result.append("<title>" + StringEscapeUtils.escapeXml(entry.getTitle()) + "</title>\n");
            result.append("<!-- summary service feed -->\n");
            result.append("<summary>" + StringEscapeUtils.escapeXml(entry.getSubTitle()) + "</summary>\n");
            result.append("<!-- link to Service Feed -->\n");
            if (isBrowserIE) {
                result.append("<!-- do not add attribute \"type\" for IE browsers, since links are not displayed properly -->\n");
                result.append("<link rel=\"alternate\" href=\"" + StringEscapeUtils.escapeXml(entry.getSelfReferencingLink().getHref()) + "\" hreflang=\"" + entry.getSelfReferencingLink().getHrefLang() + "\""
                        + (entry.getSelfReferencingLink().getTitle() == null ? "" : " title=\"" + entry.getSelfReferencingLink().getTitle() + "\"") + "/>\n");
            } else {
                result.append("<link rel=\"alternate\" href=\"" + StringEscapeUtils.escapeXml(entry.getSelfReferencingLink().getHref()) + "\" type=\"" + entry.getSelfReferencingLink().getType() + "\" hreflang=\""
                        + entry.getSelfReferencingLink().getHrefLang() + "\"" + (entry.getSelfReferencingLink().getTitle() == null ? "" : " title=\"" + entry.getSelfReferencingLink().getTitle() + "\"") + "/>\n");
            }
            result.append("<!-- link to download service ISO 19139 metadata -->\n");
            result.append("<link href=\"" + StringEscapeUtils.escapeXml(entry.getMetadataAccessUrl().getHref()) + "\" rel=\"" + entry.getMetadataAccessUrl().getRel() + "\" type=\"" + entry.getMetadataAccessUrl().getType() + "\"/>\n");

            result.append("<!-- link to Open Search definition file for this service -->\n");
            result.append("<link href=\"" + StringEscapeUtils.escapeXml(entry.getOpenSearchDefinitionLink().getHref()) + "\" rel=\"" + entry.getOpenSearchDefinitionLink().getRel() + "\" type=\""
                    + entry.getOpenSearchDefinitionLink().getType() + "\" hreflang=\"" + entry.getOpenSearchDefinitionLink().getHrefLang() + "\""
                    + (entry.getOpenSearchDefinitionLink().getTitle() == null ? "" : " title=\"" + StringEscapeUtils.escapeXml(entry.getOpenSearchDefinitionLink().getTitle()) + "\"") + "/>\n");
            result.append("<!-- identifier -->\n");
            result.append("<id>" + StringEscapeUtils.escapeXml(entry.getSelfReferencingLink().getHref()) + "</id>\n");
            result.append("<!-- rights, access restrictions  -->\n");
            result.append("<rights>" + StringEscapeUtils.escapeXml(entry.getCopyright()) + "</rights>\n");
            result.append("<!-- date/time this feed was last updated -->\n");
            result.append("<updated>" + StringUtils.assureDateTime(entry.getUpdated()) + "</updated>\n");
            result.append("<!-- author contact information -->\n");
            result.append("<author>\n" + "<name>" + StringEscapeUtils.escapeXml(entry.getAuthor().getName()) + "</name>\n" + "<email>" + StringEscapeUtils.escapeXml(entry.getAuthor().getEmail()) + "</email>\n" + "</author>\n");

            result.append("</entry>\n");
        }
        result.append("</feed>\n");

        return result.toString();

    }

    @Autowired
    public void setUserAgentDetector(UserAgentDetector userAgentDetector) {
        this.userAgentDetector = userAgentDetector;
    }

}
