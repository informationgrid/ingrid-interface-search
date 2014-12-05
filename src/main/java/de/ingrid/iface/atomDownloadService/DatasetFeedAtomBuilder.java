/*
 * **************************************************-
 * ingrid-interface-search
 * ==================================================
 * Copyright (C) 2014 wemove digital solutions GmbH
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

import de.ingrid.iface.atomDownloadService.om.Category;
import de.ingrid.iface.atomDownloadService.om.DatasetFeed;
import de.ingrid.iface.atomDownloadService.om.DatasetFeedEntry;
import de.ingrid.iface.atomDownloadService.om.Link;
import de.ingrid.iface.util.StringUtils;
import de.ingrid.iface.util.UserAgentDetector;

@Service
public class DatasetFeedAtomBuilder {

    private UserAgentDetector userAgentDetector = null;

    public String build(DatasetFeed datasetFeed, String agentString) {

        StringBuilder result = new StringBuilder();
        result.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        result.append("<feed xmlns=\"http://www.w3.org/2005/Atom\" xmlns:georss=\"http://www.georss.org/georss\" xml:lang=\"de\">\n");
        result.append("<!-- feed title -->\n");
        result.append("<title>" + StringEscapeUtils.escapeXml(datasetFeed.getTitle()) + "</title>\n");
        result.append("<!-- feed subtitle -->\n");
        result.append("<subtitle>" + StringEscapeUtils.escapeXml(datasetFeed.getSubTitle()) + "</subtitle>\n");
        result.append("<!-- link to ISO 19139 metadata -->\n");
        for (Link link : datasetFeed.getDescribedBy()) {
            result.append("<link href=\"" + StringEscapeUtils.escapeXml(link.getHref()) + "\" rel=\"" + link.getRel() + "\" type=\"" + link.getType() + "\"  title=\"" + StringEscapeUtils.escapeXml(link.getTitle()) + "\"/>\n");
        }
        result.append("<!-- self-referencing link to this feed -->\n");
        result.append("<link href=\"" + StringEscapeUtils.escapeXml(datasetFeed.getSelfReferencingLink().getHref()) + "\" rel=\"" + datasetFeed.getSelfReferencingLink().getRel() + "\" type=\""
                + datasetFeed.getSelfReferencingLink().getType() + "\" hreflang=\"" + datasetFeed.getSelfReferencingLink().getHrefLang() + "\""
                + (datasetFeed.getSelfReferencingLink().getTitle() == null ? "" : " title=\"" + StringEscapeUtils.escapeXml(datasetFeed.getSelfReferencingLink().getTitle() + "\"")) + "/>\n");
        result.append("<!-- upward link to the corresponding download service feed -->\n");
        result.append("<link href=\"" + StringEscapeUtils.escapeXml(datasetFeed.getDownloadServiceFeed().getHref()) + "\" rel=\"" + datasetFeed.getDownloadServiceFeed().getRel() + "\" type=\""
                + datasetFeed.getDownloadServiceFeed().getType() + "\" hreflang=\"" + datasetFeed.getDownloadServiceFeed().getHrefLang() + "\""
                + (datasetFeed.getDownloadServiceFeed().getTitle() == null ? "" : " title=\"" + StringEscapeUtils.escapeXml(datasetFeed.getDownloadServiceFeed().getTitle()) + "\"") + "/>\n");
        if (datasetFeed.getDetailLink() != null) {
            result.append("<!-- detail-link to portal -->\n");
            result.append("<link href=\"" + StringEscapeUtils.escapeXml(datasetFeed.getDetailLink().getHref()) + "\" rel=\"" + datasetFeed.getDetailLink().getRel() + "\" type=\"html\""
                    + (datasetFeed.getDetailLink().getTitle() == null ? "" : " title=\"" + StringEscapeUtils.escapeXml(datasetFeed.getDetailLink().getTitle() + "\"")) + "/>\n");
        }
        result.append("<!-- identifier -->\n");
        result.append("<id>" + StringEscapeUtils.escapeXml(datasetFeed.getSelfReferencingLink().getHref()) + "</id>\n");
        result.append("<!-- rights, access restrictions  -->\n");
        result.append("<rights>" + StringEscapeUtils.escapeXml(datasetFeed.getRights()) + "</rights>\n");
        result.append("<!-- date/time this feed was last updated -->\n");
        result.append("<updated>" + StringUtils.assureDateTime(datasetFeed.getUpdated()) + "</updated>\n");
        result.append("<!-- author contact information -->\n");
        result.append("<author>\n" + "<name>" + StringEscapeUtils.escapeXml(datasetFeed.getAuthor().getName()) + "</name>\n" + "<email>" + StringEscapeUtils.escapeXml(datasetFeed.getAuthor().getEmail()) + "</email>\n" + "</author>\n");

        boolean isBrowserIE = userAgentDetector.isIE(agentString);

        for (DatasetFeedEntry entry : datasetFeed.getEntries()) {
            result.append("<entry>\n");
            result.append("<title>" + (entry.getTitle() == null ? "" : StringEscapeUtils.escapeXml(entry.getTitle())) + "</title>\n");
            result.append("<!-- file download link -->\n");
            for (Link link : entry.getLinks()) {
                if (isBrowserIE) {
                    result.append("<!-- do not add attribute \"type\" for IE browsers, since links are not displayed properly -->\n");
                    result.append("<link href=\"" + StringEscapeUtils.escapeXml(link.getHref()) + "\" rel=\"" + link.getRel() + "\"" + (link.getLength() == null ? "" : " length=\"" + link.getLength() + "\"") + " title=\""
                            + StringEscapeUtils.escapeXml(link.getTitle()) + "\"/>\n");
                } else {
                    result.append("<link href=\"" + StringEscapeUtils.escapeXml(link.getHref()) + "\" rel=\"" + link.getRel() + "\" type=\"" + link.getType() + "\"" + (link.getLength() == null ? "" : " length=\"" + link.getLength() + "\"")
                            + " title=\"" + StringEscapeUtils.escapeXml(link.getTitle()) + "\"/>\n");
                }

            }
            result.append("<id>" + entry.getId() + "</id>\n");
            result.append("<!-- date/time this feed was last updated -->\n");
            result.append("<updated>" + StringUtils.assureDateTime(datasetFeed.getUpdated()) + "</updated>\n");
            if (entry.getCrs() != null && entry.getCrs().size() > 0) {
                result.append("<!-- CRSs in which the pre-defined Dataset is available -->\n");
                for (Category cat : entry.getCrs()) {
                    result.append("<category term=\"" + StringEscapeUtils.escapeXml(cat.term) + "\" label=\"" + StringEscapeUtils.escapeXml(cat.getLabel()) + "\"/>\n");
                }
            }
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
