package de.ingrid.iface.atomDownloadService.om;

public class Query {

    private String role;

    private String spatialDatasetIdentifierNamespace;

    private String spatialDatasetIdentifierCode;

    private String crs;

    private String language;

    private String title;

    private Integer count;

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getSpatialDatasetIdentifierNamespace() {
        return spatialDatasetIdentifierNamespace;
    }

    public void setSpatialDatasetIdentifierNamespace(String spatialDatasetIdentifierNamespace) {
        this.spatialDatasetIdentifierNamespace = spatialDatasetIdentifierNamespace;
    }

    public String getSpatialDatasetIdentifierCode() {
        return spatialDatasetIdentifierCode;
    }

    public void setSpatialDatasetIdentifierCode(String spatialDatasetIdentifierCode) {
        this.spatialDatasetIdentifierCode = spatialDatasetIdentifierCode;
    }

    public String getCrs() {
        return crs;
    }

    public void setCrs(String crs) {
        this.crs = crs;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

}
