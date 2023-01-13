/*
 * **************************************************-
 * ingrid-interface-search
 * ==================================================
 * Copyright (C) 2014 - 2023 wemove digital solutions GmbH
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
package de.ingrid.iface.opensearch.model.dcatapde;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import de.ingrid.iface.opensearch.model.dcatapde.general.DatatypeTextElement;
import de.ingrid.iface.opensearch.model.dcatapde.general.LangTextElement;
import de.ingrid.iface.opensearch.model.dcatapde.general.ResourceElement;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Distribution {

    // 1..n
    @JacksonXmlProperty(namespace = "http://www.w3.org/ns/dcat#")
    private ResourceElement accessURL = new ResourceElement();

    // 0..1
    @JacksonXmlProperty(namespace = "http://purl.org/dc/terms/")
    private ResourceElement license;

    // 0..1
    @JacksonXmlProperty(namespace = "http://purl.org/dc/terms/")
    private ResourceElement format;

    // 0..1
    @JacksonXmlProperty(namespace = "http://purl.org/dc/terms/")
    private String description;

    // 0..1
    @JacksonXmlProperty(namespace = "http://www.w3.org/ns/dcat#")
    private DatatypeTextElement byteSize;

    // 0..1
    @JacksonXmlProperty(namespace = "http://spdx.org/rdf/terms")
    private ResourceElement checksum;

    // 0..n
    @JacksonXmlProperty(namespace = "http://xmlns.com/foaf/0.1/")
    private ResourceElement page;

    // 0..n
    @JacksonXmlProperty(namespace = "http://www.w3.org/ns/dcat#")
    private ResourceElement downloadURL;

    // 0..n
    @JacksonXmlProperty(namespace = "http://purl.org/dc/terms/")
    private ResourceElement language;

    // 0..n
    @JacksonXmlProperty(namespace = "http://purl.org/dc/terms/")
    private ResourceElement conformsTo;

    // 0..1
    @JacksonXmlProperty(namespace = "http://www.w3.org/ns/dcat#")
    private ResourceElement mediaType;

    // 0..1
    @JacksonXmlProperty(namespace = "http://purl.org/dc/terms/")
    private DatatypeTextElement issued;

    // 0..1
    @JacksonXmlProperty(namespace = "http://purl.org/dc/terms/")
    private ResourceElement rights;

    // 0..1
    @JacksonXmlProperty(namespace = "http://www.w3.org/ns/adms#")
    private ResourceElement status;

    // 0..1
    @JacksonXmlProperty(namespace = "http://purl.org/dc/terms/")
    private LangTextElement title;

    // 0..1
    @JacksonXmlProperty(namespace = "http://purl.org/dc/terms/")
    private DatatypeTextElement modified;

    // 0..1
    @JacksonXmlProperty(namespace = "http://dcat-ap.de/def/dcatde/")
    private ResourceElement plannedAvailability;

    // 0..1
    @JacksonXmlProperty(namespace = "http://dcat-ap.de/def/dcatde/")
    private String licenseAttributionByText;

    @JacksonXmlProperty(isAttribute = true, namespace = "http://www.w3.org/1999/02/22-rdf-syntax-ns#")
    private String about;


    public ResourceElement getAccessURL() {
        return accessURL;
    }

    public void setAccessURL(ResourceElement accessURL) {
        this.accessURL = accessURL;
    }

    public ResourceElement getLicense() {
        return license;
    }

    public void setLicense(ResourceElement license) {
        this.license = license;
    }

    public ResourceElement getFormat() {
        return format;
    }

    public void setFormat(ResourceElement format) {
        this.format = format;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public DatatypeTextElement getByteSize() {
        return byteSize;
    }

    public void setByteSize(DatatypeTextElement byteSize) {
        this.byteSize = byteSize;
    }

    public ResourceElement getChecksum() {
        return checksum;
    }

    public void setChecksum(ResourceElement checksum) {
        this.checksum = checksum;
    }

    public ResourceElement getPage() {
        return page;
    }

    public void setPage(ResourceElement page) {
        this.page = page;
    }

    public ResourceElement getDownloadURL() {
        return downloadURL;
    }

    public void setDownloadURL(ResourceElement downloadURL) {
        this.downloadURL = downloadURL;
    }

    public ResourceElement getLanguage() {
        return language;
    }

    public void setLanguage(ResourceElement language) {
        this.language = language;
    }

    public ResourceElement getConformsTo() {
        return conformsTo;
    }

    public void setConformsTo(ResourceElement conformsTo) {
        this.conformsTo = conformsTo;
    }

    public ResourceElement getMediaType() {
        return mediaType;
    }

    public void setMediaType(ResourceElement mediaType) {
        this.mediaType = mediaType;
    }

    public DatatypeTextElement getIssued() {
        return issued;
    }

    public void setIssued(DatatypeTextElement issued) {
        this.issued = issued;
    }

    public ResourceElement getRights() {
        return rights;
    }

    public void setRights(ResourceElement rights) {
        this.rights = rights;
    }

    public ResourceElement getStatus() {
        return status;
    }

    public void setStatus(ResourceElement status) {
        this.status = status;
    }

    public LangTextElement getTitle() {
        return title;
    }

    public void setTitle(String title) {
        if (title != null) {
            this.title = new LangTextElement(title);
        }
    }

    public DatatypeTextElement getModified() {
        return modified;
    }

    public void setModified(DatatypeTextElement modified) {
        this.modified = modified;
    }

    public ResourceElement getPlannedAvailability() {
        return plannedAvailability;
    }

    public void setPlannedAvailability(ResourceElement plannedAvailability) {
        this.plannedAvailability = plannedAvailability;
    }

    public String getLicenseAttributionByText() {
        return licenseAttributionByText;
    }

    public void setLicenseAttributionByText(String licenseAttributionByText) {
        this.licenseAttributionByText = licenseAttributionByText;
    }

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }
}
