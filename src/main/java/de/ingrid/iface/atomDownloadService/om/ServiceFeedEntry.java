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
package de.ingrid.iface.atomDownloadService.om;

import java.util.List;

public class ServiceFeedEntry {

    public enum EntryType {
        IGC, CSW
    };

    private String uuid;
    private String title;
    private String spatialDatasetIdentifierCode;
    private String spatialDatasetIdentifierNamespace;
    private Link datasetMetadataRecord;
    private Link datasetFeed;
    private String datasetIdentifier;
    private String rights;
    private String updated;
    private String summary;
    private List<Double> polygon;
    private List<Category> crs;
    private Author author;
    private EntryType type;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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

    public Link getDatasetMetadataRecord() {
        return datasetMetadataRecord;
    }

    public void setDatasetMetadataRecord(Link datasetMetadataRecord) {
        this.datasetMetadataRecord = datasetMetadataRecord;
    }

    public Link getDatasetFeed() {
        return datasetFeed;
    }

    public void setDatasetFeed(Link datasetFeed) {
        this.datasetFeed = datasetFeed;
    }

    public String getDatasetIdentifier() {
        return datasetIdentifier;
    }

    public void setDatasetIdentifier(String datasetIdentifier) {
        this.datasetIdentifier = datasetIdentifier;
    }

    public String getRights() {
        return rights;
    }

    public void setRights(String rights) {
        this.rights = rights;
    }

    public String getUpdated() {
        return updated;
    }

    public void setUpdated(String updated) {
        this.updated = updated;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public List<Double> getPolygon() {
        return polygon;
    }

    public void setPolygon(List<Double> polygon) {
        this.polygon = polygon;
    }

    public List<Category> getCrs() {
        return crs;
    }

    public void setCrs(List<Category> crs) {
        this.crs = crs;
    }

    public Author getAuthor() {
        return author;
    }

    public void setAuthor(Author author) {
        this.author = author;
    }

    public EntryType getType() {
        return type;
    }

    public void setType(EntryType type) {
        this.type = type;
    }

}
