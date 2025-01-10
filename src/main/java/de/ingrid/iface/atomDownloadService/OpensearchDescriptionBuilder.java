/*
 * **************************************************-
 * ingrid-interface-search
 * ==================================================
 * Copyright (C) 2014 - 2025 wemove digital solutions GmbH
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
package de.ingrid.iface.atomDownloadService;

import org.apache.commons.lang.StringEscapeUtils;
import org.springframework.stereotype.Service;

import de.ingrid.iface.atomDownloadService.om.OpensearchDescription;
import de.ingrid.iface.atomDownloadService.om.OpensearchDescriptionUrl;
import de.ingrid.iface.atomDownloadService.om.Query;

@Service
public class OpensearchDescriptionBuilder {

    public String build(OpensearchDescription opensearchDescription) {
        StringBuilder result = new StringBuilder();
        result.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        result.append("<OpenSearchDescription xmlns=\"http://a9.com/-/spec/opensearch/1.1/\"\n" + "xmlns:inspire_dls=\"http://inspire.ec.europa.eu/schemas/inspire_dls/1.0\"\n" + "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
                + "xsi:schemaLocation=\"http://a9.com/-/spec/opensearch/1.1/ OpenSearch.xsd\">\n");
        result.append("<ShortName>" + StringEscapeUtils.escapeXml(opensearchDescription.getShortName()) + "</ShortName>\n");
        result.append("<Description>" +  StringEscapeUtils.escapeXml(opensearchDescription.getDescription()) + "</Description>\n");
        result.append("<!--URL of this document-->\n");
        OpensearchDescriptionUrl tpl = opensearchDescription.getSelfReferencingUrlTemplate();
        result.append("<Url type=\"" + tpl.getType() + "\"" + " rel=\"" + tpl.getRel() + "\"" + " template=\"" + tpl.getTemplate() + "\"" + "/>\n");
        result.append("<!--Generic URL template for browser integration-->\n");
        tpl = opensearchDescription.getResultsUrlTemplate();
        result.append("<Url type=\"" + tpl.getType() + "\"" + " rel=\"" + tpl.getRel() + "\"" + " template=\"" + tpl.getTemplate() + "\"" + "/>\n");
        result.append("<!--Describe Spatial Data Set Operation request URL template to be used in order to retrieve the description of Spatial Object Types in a Spatial Dataset-->\n");
        tpl = opensearchDescription.getDescribeSpatialDatasetOperationUrlTemplate();
        result.append("<Url type=\"" + tpl.getType() + "\"" + " rel=\"" + tpl.getRel() + "\"" + " template=\"" + tpl.getTemplate() + "\"" + "/>\n");
        result.append("<!--Get Spatial Data Set Operation request URL template to be used in order to retrieve a Spatial Dataset-->\n");
        tpl = opensearchDescription.getGetSpatialDatasetOperationUrlTemplate();
        result.append("<Url type=\"" + tpl.getType() + "\"" + " rel=\"" + tpl.getRel() + "\"" + " template=\"" + tpl.getTemplate() + "\"" + "/>\n");
        result.append("<Contact>" +  StringEscapeUtils.escapeXml(opensearchDescription.getContact()) + "</Contact>\n");
        result.append("<LongName>" +  StringEscapeUtils.escapeXml(opensearchDescription.getLongName()) + "</LongName>\n");

        for (Query q : opensearchDescription.getExamples()) {
            result.append("<Query role=\"" + q.getRole() + "\"" + " inspire_dls:spatial_dataset_identifier_namespace=\"" + q.getSpatialDatasetIdentifierNamespace() + "\"" + " inspire_dls:spatial_dataset_identifier_code=\""
                    + q.getSpatialDatasetIdentifierCode() + "\"");
            if (q.getCrs() != null) {
                result.append(" inspire_dls:crs=\"" + StringEscapeUtils.escapeXml(q.getCrs()) + "\"");
            }
            result.append(" language=\"" + q.getLanguage() + "\"");
            result.append(" title=\"" +  StringEscapeUtils.escapeXml(q.getTitle()) + "\"");
            result.append(" count=\"" + q.getCount() + "\"");
            result.append("/>\n");
        }

        for (String lang : opensearchDescription.getLanguages()) {
            result.append("<Language>" + lang + "</Language>\n");
        }
        result.append("</OpenSearchDescription>\n");

        return result.toString();
    }

}
