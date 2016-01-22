/*
 * **************************************************-
 * ingrid-interface-search
 * ==================================================
 * Copyright (C) 2014 - 2016 wemove digital solutions GmbH
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
package de.ingrid.iface.atomDownloadService.om;

import java.util.List;

public class OpensearchDescription {

    private String shortName;

    private String description;

    private OpensearchDescriptionUrl selfReferencingUrlTemplate;

    private OpensearchDescriptionUrl resultsUrlTemplate;

    private OpensearchDescriptionUrl describeSpatialDatasetOperationUrlTemplate;

    private OpensearchDescriptionUrl getSpatialDatasetOperationUrlTemplate;

    private String contact;

    private String tags;

    private String longName;

    private Image image;

    private List<Query> examples;

    private String developer;

    private List<String> languages;

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public OpensearchDescriptionUrl getSelfReferencingUrlTemplate() {
        return selfReferencingUrlTemplate;
    }

    public void setSelfReferencingUrlTemplate(OpensearchDescriptionUrl selfReferencingUrlTemplate) {
        this.selfReferencingUrlTemplate = selfReferencingUrlTemplate;
    }

    public OpensearchDescriptionUrl getResultsUrlTemplate() {
        return resultsUrlTemplate;
    }

    public void setResultsUrlTemplate(OpensearchDescriptionUrl resultsUrlTemplate) {
        this.resultsUrlTemplate = resultsUrlTemplate;
    }

    public OpensearchDescriptionUrl getDescribeSpatialDatasetOperationUrlTemplate() {
        return describeSpatialDatasetOperationUrlTemplate;
    }

    public void setDescribeSpatialDatasetOperationUrlTemplate(OpensearchDescriptionUrl describeSpatialDatasetOperationUrlTemplate) {
        this.describeSpatialDatasetOperationUrlTemplate = describeSpatialDatasetOperationUrlTemplate;
    }

    public OpensearchDescriptionUrl getGetSpatialDatasetOperationUrlTemplate() {
        return getSpatialDatasetOperationUrlTemplate;
    }

    public void setGetSpatialDatasetOperationUrlTemplate(OpensearchDescriptionUrl getSpatialDatasetOperationUrlTemplate) {
        this.getSpatialDatasetOperationUrlTemplate = getSpatialDatasetOperationUrlTemplate;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getLongName() {
        return longName;
    }

    public void setLongName(String longName) {
        this.longName = longName;
    }

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
    }

    public List<Query> getExamples() {
        return examples;
    }

    public void setExamples(List<Query> examples) {
        this.examples = examples;
    }

    public String getDeveloper() {
        return developer;
    }

    public void setDeveloper(String developer) {
        this.developer = developer;
    }

    public List<String> getLanguages() {
        return languages;
    }

    public void setLanguages(List<String> languages) {
        this.languages = languages;
    }

}
