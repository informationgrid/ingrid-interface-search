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
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import de.ingrid.iface.opensearch.model.dcatapde.catalog.AgentWrapper;
import de.ingrid.iface.opensearch.model.dcatapde.general.DateElement;
import de.ingrid.iface.opensearch.model.dcatapde.general.LangTextElement;
import de.ingrid.iface.opensearch.model.dcatapde.general.ResourceElement;
import de.ingrid.iface.opensearch.model.dcatapde.general.SpatialElement;

import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Catalog {

    @JacksonXmlProperty(isAttribute = true, namespace = "http://www.w3.org/1999/02/22-rdf-syntax-ns#")
    private String about;

    // 1..n
    @JacksonXmlProperty(namespace = "http://www.w3.org/ns/dcat#")
    @JacksonXmlElementWrapper(useWrapping = false)
    private List<ResourceElement> dataset = new ArrayList<>();

    // 1..n (for each languages)
    @JacksonXmlProperty(namespace = "http://purl.org/dc/terms/")
    private LangTextElement description = new LangTextElement();

    // 1
    @JacksonXmlProperty(namespace = "http://purl.org/dc/terms/")
    private AgentWrapper publisher = new AgentWrapper();

    // 1..n (for each languages)
    @JacksonXmlProperty(namespace = "http://purl.org/dc/terms/")
    private LangTextElement title = new LangTextElement();

    // 0..1
    @JacksonXmlProperty(namespace = "http://purl.org/dc/terms/")
    private ResourceElement license;

    // 0..1
    @JacksonXmlProperty(namespace = "http://xmlns.com/foaf/0.1/")
    private ResourceElement homepage;

    // 0..n
    @JacksonXmlProperty(namespace = "http://purl.org/dc/terms/")
    @JacksonXmlElementWrapper(useWrapping = false)
    private List<ResourceElement> languages;

    // 0..1
    @JacksonXmlProperty(namespace = "http://purl.org/dc/terms/")
    private DateElement issued;

    // 0..n
    @JacksonXmlProperty(namespace = "http://www.w3.org/ns/dcat#")
    private ResourceElement themeTaxonomy;

    // 0..1
    @JacksonXmlProperty(namespace = "http://purl.org/dc/terms/")
    private DateElement modified;

    // 0..n
    @JacksonXmlProperty(namespace = "http://purl.org/dc/terms/")
    private ResourceElement hasPart;

    // 0..1
    @JacksonXmlProperty(namespace = "http://purl.org/dc/terms/")
    private ResourceElement isPartOf;

    // 0..n
    @JacksonXmlProperty(namespace = "http://www.w3.org/ns/dcat#")
    private ResourceElement record;

    // 0..1
    @JacksonXmlProperty(namespace = "http://purl.org/dc/terms/")
    private ResourceElement rights;

    // 0..n
    @JacksonXmlProperty(namespace = "http://purl.org/dc/terms/")
    private SpatialElement spatial;


    public List<ResourceElement> getDataset() {
        return dataset;
    }

    public void setDataset(String... datasetUrls) {
        ArrayList<ResourceElement> resources = new ArrayList<>();
        for (String url : datasetUrls) {
            resources.add(new ResourceElement(url));
        }

        this.dataset = resources;
    }

    public ResourceElement getHomepage() {
        return homepage;
    }

    public void setHomepage(ResourceElement homepage) {
        this.homepage = homepage;
    }

    public AgentWrapper getPublisher() {
        return publisher;
    }

    public void setPublisher(AgentWrapper publisher) {
        this.publisher = publisher;
    }

    public List<ResourceElement> getLanguages() {
        return languages;
    }

    public void setLanguages(String... languages) {
        List<ResourceElement> resList = new ArrayList<>();
        for (String resource : languages) {
            ResourceElement resourceElement = new ResourceElement();
            resourceElement.setResource(resource);
            resList.add(resourceElement);
        }
        this.languages = resList;
    }

    public ResourceElement getLicense() {
        return license;
    }

    public void setLicense(ResourceElement license) {
        this.license = license;
    }

    public DateElement getIssued() {
        return issued;
    }

    public void setIssued(DateElement issued) {
        this.issued = issued;
    }

    public ResourceElement getThemeTaxonomy() {
        return themeTaxonomy;
    }

    public void setThemeTaxonomy(ResourceElement themeTaxonomy) {
        this.themeTaxonomy = themeTaxonomy;
    }

    public ResourceElement getHasPart() {
        return hasPart;
    }

    public void setHasPart(ResourceElement hasPart) {
        this.hasPart = hasPart;
    }

    public ResourceElement getIsPartOf() {
        return isPartOf;
    }

    public void setIsPartOf(ResourceElement isPartOf) {
        this.isPartOf = isPartOf;
    }

    public DateElement getModified() {
        return modified;
    }

    public void setModified(DateElement modified) {
        this.modified = modified;
    }

    public ResourceElement getRecord() {
        return record;
    }

    public void setRecord(ResourceElement record) {
        this.record = record;
    }

    public ResourceElement getRights() {
        return rights;
    }

    public void setRights(ResourceElement rights) {
        this.rights = rights;
    }

    public SpatialElement getSpatial() {
        return spatial;
    }

    public void setSpatial(SpatialElement spatial) {
        this.spatial = spatial;
    }

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }

    public LangTextElement getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title.setText(title);
    }

    public LangTextElement getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description.setText(description);
    }
}
