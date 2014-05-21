package de.ingrid.iface.atomDownloadService.requests;

import javax.servlet.http.HttpServletRequest;

import org.springframework.util.StringUtils;

import de.ingrid.iface.atomDownloadService.om.ServiceFeedEntry.EntryType;

public class DatasetFeedRequest {

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

        String path = StringUtils.trimLeadingCharacter(req.getPathInfo(), '/');
        int idx = path.indexOf('/');
        if (idx > 0) {
            serviceFeedUuid = path.substring(0, idx);
            datasetFeedUuid = path.substring(idx + 1);
        } else {
            serviceFeedUuid = path;
        }
        
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
