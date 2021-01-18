/*
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
package de.ingrid.iface.atomDownloadService;

import org.apache.commons.lang.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.ingrid.iface.atomDownloadService.om.Category;
import de.ingrid.iface.atomDownloadService.om.ServiceFeed;
import de.ingrid.iface.atomDownloadService.om.ServiceFeedEntry;
import de.ingrid.iface.util.StringUtils;
import de.ingrid.iface.util.UserAgentDetector;

@Service
public class ServiceFeedAtomBuilder {

    private UserAgentDetector userAgentDetector = null;

    public String build(ServiceFeed serviceFeed, String agentString) {

        StringBuilder result = new StringBuilder();
        result.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        result.append("<feed xmlns=\"http://www.w3.org/2005/Atom\" xmlns:georss=\"http://www.georss.org/georss\" xmlns:inspire_dls=\"http://inspire.ec.europa.eu/schemas/inspire_dls/1.0\" xml:lang=\"de\">\n");
        result.append("<!-- feed title -->\n");
        result.append("<title>" + StringEscapeUtils.escapeXml(serviceFeed.getTitle()) + "</title>\n");
        result.append("<!-- feed subtitle -->\n");
        result.append("<subtitle>" + StringEscapeUtils.escapeXml(serviceFeed.getSubTitle()) + "</subtitle>\n");
        result.append("<!-- link to download service ISO 19139 metadata -->\n");
        result.append("<link href=\"" + StringEscapeUtils.escapeXml(serviceFeed.getMetadataAccessUrl().getHref()) + "\" rel=\"" + serviceFeed.getMetadataAccessUrl().getRel() + "\" type=\"" + serviceFeed.getMetadataAccessUrl().getType()
                + "\"/>\n");
        result.append("<!-- self-referencing link to this feed -->\n");
        result.append("<link href=\"" + StringEscapeUtils.escapeXml(serviceFeed.getSelfReferencingLink().getHref()) + "\" rel=\"" + serviceFeed.getSelfReferencingLink().getRel() + "\" type=\""
                + serviceFeed.getSelfReferencingLink().getType() + "\" hreflang=\"" + serviceFeed.getSelfReferencingLink().getHrefLang() + "\" title=\"" + StringEscapeUtils.escapeXml(serviceFeed.getSelfReferencingLink().getTitle())
                + "\"/>\n");
        result.append("<!-- link to Open Search definition file for this service -->\n");
        result.append("<link href=\"" + StringEscapeUtils.escapeXml(serviceFeed.getOpenSearchDefinitionLink().getHref()) + "\" rel=\"" + serviceFeed.getOpenSearchDefinitionLink().getRel() + "\" type=\""
                + serviceFeed.getOpenSearchDefinitionLink().getType() + "\" hreflang=\"" + serviceFeed.getOpenSearchDefinitionLink().getHrefLang() + "\"" + (serviceFeed.getOpenSearchDefinitionLink().getTitle() == null ? "" : " title=\""
                + StringEscapeUtils.escapeXml(serviceFeed.getOpenSearchDefinitionLink().getTitle()) + "\"") + "/>\n");
        result.append("<!-- identifier -->\n");
        result.append("<id>" + StringEscapeUtils.escapeXml(serviceFeed.getSelfReferencingLink().getHref()) + "</id>\n");
        result.append("<!-- rights, access restrictions  -->\n");
        result.append("<rights>" + StringEscapeUtils.escapeXml(serviceFeed.getCopyright()) + "</rights>\n");
        result.append("<!-- date/time this feed was last updated -->\n");
        result.append("<updated>" + StringUtils.assureDateTime(serviceFeed.getUpdated()) + "</updated>\n");
        result.append("<!-- author contact information -->\n");
        result.append("<author>\n" + "<name>" + StringEscapeUtils.escapeXml(serviceFeed.getAuthor().getName()) + "</name>\n" + "<email>" + StringEscapeUtils.escapeXml(serviceFeed.getAuthor().getEmail()) + "</email>\n" + "</author>\n");

        boolean isBrowserIE = userAgentDetector.isIE(agentString);

        for (ServiceFeedEntry entry : serviceFeed.getEntries()) {
            result.append("<entry>\n");
            result.append("<!-- title dataset feed -->\n");
            result.append("<title>" + StringEscapeUtils.escapeXml(entry.getTitle()) + "</title>\n");
            result.append("<!-- link to Dataset Feed -->\n");
            if (isBrowserIE) {
                result.append("<!-- do not add attribute \"type\" for IE browsers, since links are not displayed properly -->\n");
                result.append("<link rel=\"" + entry.getDatasetFeed().getRel() + "\" href=\"" + StringEscapeUtils.escapeXml(entry.getDatasetFeed().getHref()) + "\" hreflang=\""
                        + entry.getDatasetFeed().getHrefLang() + "\"" + (entry.getDatasetFeed().getTitle() == null ? "" : " title=\"" + entry.getDatasetFeed().getTitle() + "\"") + "/>\n");
            } else {
                result.append("<link rel=\"" + entry.getDatasetFeed().getRel() + "\" href=\"" + StringEscapeUtils.escapeXml(entry.getDatasetFeed().getHref()) + "\" type=\"" + entry.getDatasetFeed().getType() + "\" hreflang=\""
                        + entry.getDatasetFeed().getHrefLang() + "\"" + (entry.getDatasetFeed().getTitle() == null ? "" : " title=\"" + entry.getDatasetFeed().getTitle() + "\"") + "/>\n");
            }
            result.append("<!-- link to dataset metadata record -->\n");
            result.append("<link href=\"" + StringEscapeUtils.escapeXml(entry.getDatasetMetadataRecord().getHref()) + "\" rel=\"" + entry.getDatasetMetadataRecord().getRel() + "\" type=\"" + entry.getDatasetMetadataRecord().getType()
                    + "\"/>\n");
            result.append("<!-- identifier for \"Dataset Feed\" for pre-defined dataset -->\n");
            result.append("<id>" + StringEscapeUtils.escapeXml(entry.getDatasetIdentifier()) + "</id>\n");
            result.append("<!-- rights, access info for pre-defined dataset -->\n");
            result.append("<rights>" + StringEscapeUtils.escapeXml(entry.getRights()) + "</rights>\n");
            result.append("<!-- last date/time this entry was updated -->\n");
            result.append("<updated>" + StringUtils.assureDateTime(entry.getUpdated()) + "</updated>\n");
            result.append("<!-- summary -->\n");
            result.append("<summary>" + StringEscapeUtils.escapeXml(entry.getSummary()) + "</summary>\n");
            if (entry.getPolygon() != null && entry.getPolygon().size() > 0) {
                result.append("<!-- optional GeoRSS-Simple polygon outlining the bounding box of the pre-defined dataset described by the entry. Must be lat lon -->\n");
                result.append("<georss:polygon>");
                for (Double val : entry.getPolygon()) {
                    result.append(val);
                    result.append(" ");
                }
                result.deleteCharAt(result.length() - 1);
                result.append("</georss:polygon>\n");
            }

            if (entry.getCrs() != null && entry.getCrs().size() > 0) {
                result.append("<!-- CRSs in which the pre-defined Dataset is available -->\n");
                for (Category cat : entry.getCrs()) {
                    result.append("<category term=\"" + StringEscapeUtils.escapeXml(cat.term) + "\" label=\"" + StringEscapeUtils.escapeXml(cat.getLabel()) + "\"/>\n");
                }
            }
            result.append("<!-- Spatial Dataset Unique Resourse Identifier for this dataset -->\n");
            if (entry.getSpatialDatasetIdentifierCode() != null) {
                result.append("<inspire_dls:spatial_dataset_identifier_code>" + StringEscapeUtils.escapeXml(entry.getSpatialDatasetIdentifierCode()) + "</inspire_dls:spatial_dataset_identifier_code>\n");
                if (entry.getSpatialDatasetIdentifierNamespace() != null) {
                    result.append("<inspire_dls:spatial_dataset_identifier_namespace>" + StringEscapeUtils.escapeXml(entry.getSpatialDatasetIdentifierNamespace()) + "</inspire_dls:spatial_dataset_identifier_namespace>\n");
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
