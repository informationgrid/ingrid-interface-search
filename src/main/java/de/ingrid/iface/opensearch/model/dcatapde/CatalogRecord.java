/*
 * **************************************************-
 * ingrid-interface-search
 * ==================================================
 * Copyright (C) 2014 - 2026 wemove digital solutions GmbH
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
package de.ingrid.iface.opensearch.model.dcatapde;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import de.ingrid.iface.opensearch.model.dcatapde.general.DateElement;
import de.ingrid.iface.opensearch.model.dcatapde.general.LangTextElement;
import de.ingrid.iface.opensearch.model.dcatapde.general.ResourceElement;

import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class CatalogRecord {

    // 1..1
    @JacksonXmlProperty(namespace = "http://xmlns.com/foaf/0.1/")
    private ResourceElement primaryTopic = new ResourceElement();

    // 1..1
    @JacksonXmlProperty(namespace = "http://purl.org/dc/terms/")
    private DateElement modified = new DateElement();

    // 0..1
    @JacksonXmlProperty(namespace = "http://purl.org/dc/terms/")
    private ResourceElement conformsTo;

    // 0..1
    @JacksonXmlProperty(namespace = "http://www.w3.org/ns/adms#")
    private ResourceElement status;

    // 0..1
    @JacksonXmlProperty(namespace = "http://purl.org/dc/terms/")
    private DateElement issued;

    // 0..n
    @JacksonXmlProperty(namespace = "http://purl.org/dc/terms/")
    private LangTextElement description;

    // 0..n
    @JacksonXmlProperty(namespace = "http://purl.org/dc/terms/")
    private List<ResourceElement> languages;

    // 0..1
    @JacksonXmlProperty(namespace = "http://purl.org/dc/terms/")
    private ResourceElement source;

    // 0..n
    @JacksonXmlProperty(namespace = "http://purl.org/dc/terms/")
    private LangTextElement title;

    public ResourceElement getPrimaryTopic() {
        return primaryTopic;
    }

    public void setPrimaryTopic(ResourceElement primaryTopic) {
        this.primaryTopic = primaryTopic;
    }

    public DateElement getModified() {
        return modified;
    }

    public void setModified(DateElement modified) {
        this.modified = modified;
    }

    public ResourceElement getConformsTo() {
        return conformsTo;
    }

    public void setConformsTo(ResourceElement conformsTo) {
        this.conformsTo = conformsTo;
    }

    public ResourceElement getStatus() {
        return status;
    }

    public void setStatus(ResourceElement status) {
        this.status = status;
    }

    public DateElement getIssued() {
        return issued;
    }

    public void setIssued(DateElement issued) {
        this.issued = issued;
    }

    public LangTextElement getDescription() {
        return description;
    }

    public void setDescription(LangTextElement description) {
        this.description = description;
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

    public ResourceElement getSource() {
        return source;
    }

    public void setSource(ResourceElement source) {
        this.source = source;
    }

    public LangTextElement getTitle() {
        return title;
    }

    public void setTitle(LangTextElement title) {
        this.title = title;
    }

}
