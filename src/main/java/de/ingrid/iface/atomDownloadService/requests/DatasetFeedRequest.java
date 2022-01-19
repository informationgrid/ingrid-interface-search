/*
 * **************************************************-
 * ingrid-interface-search
 * ==================================================
 * Copyright (C) 2014 - 2022 wemove digital solutions GmbH
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

import org.springframework.util.StringUtils;

import de.ingrid.iface.atomDownloadService.om.ServiceFeedEntry.EntryType;

public class DatasetFeedRequest extends BaseRequest {

    private String datasetFeedUuid;
    private String serviceFeedUuid;
    private String spatialDatasetIdentifierCode;
    private String spatialDatasetIdentifierNamespace;
    private String language;
    private String crs;
    private boolean detail;
    
    private EntryType type;
    private String metadataUrl;

    public DatasetFeedRequest() {
        super();
    }

    public DatasetFeedRequest(HttpServletRequest req) throws Exception {

        
        this.extractProtocol( req );
        
        String p = req.getParameter("spatial_dataset_identifier_code");
        if (p != null) {
            spatialDatasetIdentifierCode = p;
        }

        p = req.getParameter("spatial_dataset_identifier_namespace");
        if (p != null) {
            spatialDatasetIdentifierNamespace = p;
        }

        p = req.getParameter("language");
        if (p != null) {
            language = p;
        }

        p = req.getParameter("crs");
        if (p != null) {
            crs = p;
        }
        
        p = req.getParameter("detail");
        if (p != null) {
            detail = "true".equals( p );
        }

        serviceFeedUuid = StringUtils.trimLeadingCharacter(req.getPathInfo(), '/');
        datasetFeedUuid = req.getParameter("datasetUuid");
        
        if (datasetFeedUuid != null && datasetFeedUuid.toLowerCase().contains("getrecordbyid")) {
            this.setType(EntryType.CSW);
            this.setMetadataUrl(datasetFeedUuid);
        }
        
    }

    public String toString() {
        return "DatasetFeedRequest: {servicefeedUuid:" + serviceFeedUuid + ", datasetFeedUuid:" + datasetFeedUuid + ", spatialDatasetIdentifierCode: " + spatialDatasetIdentifierCode + ", spatialDatasetIdentifierNamespace: "
                + spatialDatasetIdentifierNamespace + ", crs:" + crs + ", language:" + language;
    }

    public String getDatasetFeedUuid() {
        return datasetFeedUuid;
    }

    public void setDatasetFeedUuid(String uuid) {
        this.datasetFeedUuid = uuid;
    }

    public String getServiceFeedUuid() {
        return serviceFeedUuid;
    }

    public void setServiceFeedUuid(String serviceFeedUuid) {
        this.serviceFeedUuid = serviceFeedUuid;
    }

    public String getSpatialDatasetIdentifierCode() {
        return spatialDatasetIdentifierCode;
    }

    public void setSpatialDatasetIdentifierCode(String spatialDatasetIdentifierCode) {
        this.spatialDatasetIdentifierCode = spatialDatasetIdentifierCode;
    }

    public String getSpatialDatasetIdentifierNamespace() {
        return spatialDatasetIdentifierNamespace;
    }

    public void setSpatialDatasetIdentifierNamespace(String spatialDatasetIdentifierNamespace) {
        this.spatialDatasetIdentifierNamespace = spatialDatasetIdentifierNamespace;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getCrs() {
        return crs;
    }

    public void setCrs(String crs) {
        this.crs = crs;
    }

    public EntryType getType() {
        return type;
    }

    public void setType(EntryType type) {
        this.type = type;
    }

    public String getMetadataUrl() {
        return metadataUrl;
    }

    public void setMetadataUrl(String metadataUrl) {
        this.metadataUrl = metadataUrl;
    }

    public boolean isDetail() {
        return detail;
    }

    public void setDetail( boolean detail ) {
        this.detail = detail;
    }

    

}
