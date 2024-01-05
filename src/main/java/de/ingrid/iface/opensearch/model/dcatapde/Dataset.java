/*
 * **************************************************-
 * ingrid-interface-search
 * ==================================================
 * Copyright (C) 2014 - 2024 wemove digital solutions GmbH
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
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import de.ingrid.iface.opensearch.model.dcatapde.catalog.AgentWrapper;
import de.ingrid.iface.opensearch.model.dcatapde.catalog.OrganizationWrapper;
import de.ingrid.iface.opensearch.model.dcatapde.catalog.VCardOrganizationWrapper;
import de.ingrid.iface.opensearch.model.dcatapde.general.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Dataset {

    // 1..n
    @JacksonXmlProperty(namespace = "http://purl.org/dc/terms/")
    private LangTextElement description = new LangTextElement();

    // 1..n
    @JacksonXmlProperty(namespace = "http://purl.org/dc/terms/")
    private LangTextElement title = new LangTextElement();

    // 0..n
    @JacksonXmlProperty(namespace = "http://dcat-ap.de/def/dcatde/")
    private ResourceElement contributorID;

    // 0..n
    @JacksonXmlProperty(namespace = "http://www.w3.org/ns/dcat#")
    @JacksonXmlElementWrapper(useWrapping = false)
    private VCardOrganizationWrapper contactPoint;

    // 0..n
    @JacksonXmlProperty(namespace = "http://www.w3.org/ns/dcat#")
    @JacksonXmlElementWrapper(useWrapping = false)
    private List<ResourceElement> distribution;

    // 0..n
    @JacksonXmlProperty(namespace = "http://www.w3.org/ns/dcat#")
    @JacksonXmlElementWrapper(useWrapping = false)
    private List<LangTextElement> keyword;

    // 0..1
    @JacksonXmlProperty(namespace = "http://purl.org/dc/terms/")
    @JacksonXmlElementWrapper(useWrapping = false)
    private AgentWrapper publisher;

    // 0..n
    @JacksonXmlProperty(localName = "theme", namespace = "http://www.w3.org/ns/dcat#")
    @JacksonXmlElementWrapper(useWrapping = false)
    private List<ResourceElement> themes;

    // 0..1
    @JacksonXmlProperty(namespace = "http://dcat-ap.de/def/dcatde/")
    private ResourceElement qualityProcessURI;

    // 0..1
    @JacksonXmlProperty(namespace = "http://purl.org/dc/terms/")
    private LangTextElement accessRights;

    // 0..n
    @JacksonXmlProperty(namespace = "http://purl.org/dc/terms/")
    private LangTextElement conformsTo;

    // 0..n
    @JacksonXmlProperty(namespace = "http://dcat-ap.de/def/dcatde/")
    @JacksonXmlElementWrapper(useWrapping = false)
    private OrganizationWrapper[] originator;

    // 0..n
    @JacksonXmlProperty(namespace = "http://dcat-ap.de/def/dcatde/")
    @JacksonXmlElementWrapper(useWrapping = false)
    private AgentWrapper[] maintainer;

    // 0..n
    @JacksonXmlProperty(namespace = "http://purl.org/dc/terms/")
    @JacksonXmlElementWrapper(useWrapping = false)
    private AgentWrapper[] contributor;

    // 0..n
    @JacksonXmlProperty(namespace = "http://purl.org/dc/terms/")
    @JacksonXmlElementWrapper(useWrapping = false)
    private OrganizationWrapper[] creator;

    // 0..n
    @JacksonXmlProperty(namespace = "http://xmlns.com/foaf/0.1/")
    private LangTextElement page;

    // 0..1
    @JacksonXmlProperty(namespace = "http://purl.org/dc/terms/")
    private ResourceElement accrualPeriodicity;

    // 0..n
    @JacksonXmlProperty(namespace = "http://purl.org/dc/terms/")
    private LangTextElement hasVersion;

    // 0..n
    @JacksonXmlProperty(namespace = "http://purl.org/dc/terms/")
    private String identifier;

    // 0..n
    @JacksonXmlProperty(namespace = "http://purl.org/dc/terms/")
    private LangTextElement isVersionOf;

    // 0..n
    @JacksonXmlProperty(namespace = "http://www.w3.org/ns/dcat#")
    private LangTextElement landingPage;

    // 0..n
    @JacksonXmlProperty(namespace = "http://purl.org/dc/terms/")
    private LangTextElement language;

    // 0..n
    // TODO: localName must not be same as other field, although namespace is different
    @JacksonXmlProperty(localName = "adms:identifier", namespace = "http://www.w3.org/ns/adms#")
    private LangTextElement admsIdentifier;

    // 0..n
    @JacksonXmlProperty(namespace = "http://purl.org/dc/terms/")
    private LangTextElement provenance;

    // 0..n
    @JacksonXmlProperty(namespace = "http://purl.org/dc/terms/")
    private LangTextElement relation;

    // 0..1
    @JacksonXmlProperty(namespace = "http://purl.org/dc/terms/")
    private DateElement issued;

    // 0..n
    @JacksonXmlProperty(namespace = "http://www.w3.org/ns/adms#")
    private LangTextElement sample;

    // 0..n
    @JacksonXmlProperty(namespace = "http://purl.org/dc/terms/")
    private LangTextElement source;

    // 0..n
    @JacksonXmlProperty(namespace = "http://purl.org/dc/terms/")
    private SpatialElement spatial;

    // 0..n
    @JacksonXmlProperty(namespace = "http://purl.org/dc/terms/")
    @JacksonXmlElementWrapper(useWrapping = false)
    private List<TemporalElement> temporal;

    // 0..1
    @JacksonXmlProperty(namespace = "http://purl.org/dc/terms/")
    private LangTextElement type;

    // 0..1
    @JacksonXmlProperty(namespace = "http://purl.org/dc/terms/")
    private DateElement modified;

    // 0..1
    @JacksonXmlProperty(namespace = "http://www.w3.org/2002/07/owl#")
    private LangTextElement versionInfo;

    // 0..n
    @JacksonXmlProperty(namespace = "http://www.w3.org/ns/adms#")
    private LangTextElement versionNote;

    // 0..n
    @JacksonXmlProperty(namespace = "http://dcat-ap.de/def/dcatde/")
    private ResourceElement politicalGeocodingLevelURI;

    // 0..n
    @JacksonXmlProperty(namespace = "http://dcat-ap.de/def/dcatde/")
    private ResourceElement politicalGeocodingURI;

    // 0..n
    @JacksonXmlProperty(namespace = "http://dcat-ap.de/def/dcatde/")
    private ResourceElement geocodingDescription;

    // 0..n
    @JacksonXmlProperty(namespace = "http://dcat-ap.de/def/dcatde/")
    private ResourceElement legalBasis;

    @JacksonXmlProperty(isAttribute = true, namespace = "http://www.w3.org/1999/02/22-rdf-syntax-ns#")
    private String about;


    public LangTextElement getDescription() {
        return description;
    }

    public void setDescription(LangTextElement description) {
        this.description = description;
    }

    public LangTextElement getTitle() {
        return title;
    }

    public void setTitle(LangTextElement title) {
        this.title = title;
    }

    public ResourceElement getContributorID() {
        return contributorID;
    }

    public void setContributorID(ResourceElement contributorID) {
        this.contributorID = contributorID;
    }

    public VCardOrganizationWrapper getContactPoint() {
        return contactPoint;
    }

    public void setContactPoint(VCardOrganizationWrapper contactPoint) {
        this.contactPoint = contactPoint;
    }

    public List<ResourceElement> getDistribution() {
        return distribution;
    }

    public void setDistribution(List<ResourceElement> distribution) {
        this.distribution = distribution;
    }

    public List<LangTextElement> getKeyword() {
        return keyword;
    }

    public void setKeyword(List<String> keyword) {

        this.keyword = keyword.stream()
                .map(LangTextElement::new)
                .collect(Collectors.toList());

    }

    public AgentWrapper getPublisher() {
        return publisher;
    }

    public void setPublisher(AgentWrapper publisher) {
        this.publisher = publisher;
    }

    public List<ResourceElement> getThemes() {
        return themes;
    }

    public void setThemes(String... themes) {
        List<ResourceElement> resourceElements = new ArrayList<>();
        for (String resource : themes) {
            ResourceElement resourceElement = new ResourceElement();
            resourceElement.setResource(resource);
            resourceElements.add(resourceElement);
        }
        this.themes = resourceElements;
    }

    public ResourceElement getQualityProcessURI() {
        return qualityProcessURI;
    }

    public void setQualityProcessURI(ResourceElement qualityProcessURI) {
        this.qualityProcessURI = qualityProcessURI;
    }

    public LangTextElement getAccessRights() {
        return accessRights;
    }

    public void setAccessRights(LangTextElement accessRights) {
        this.accessRights = accessRights;
    }

    public LangTextElement getConformsTo() {
        return conformsTo;
    }

    public void setConformsTo(LangTextElement conformsTo) {
        this.conformsTo = conformsTo;
    }

    public OrganizationWrapper[] getOriginator() {
        return originator;
    }

    public void setOriginator(OrganizationWrapper[] agents) {
        this.originator = agents;
    }

    public AgentWrapper[] getMaintainer() {
        return maintainer;
    }

    public void setMaintainer(AgentWrapper[] maintainer) {
        this.maintainer = maintainer;
    }

    public AgentWrapper[] getContributor() {
        return contributor;
    }

    public void setContributor(AgentWrapper[] contributor) {
        this.contributor = contributor;
    }

    public OrganizationWrapper[] getCreator() {
        return creator;
    }

    public void setCreator(OrganizationWrapper[] creator) {
        this.creator = creator;
    }

    public LangTextElement getPage() {
        return page;
    }

    public void setPage(LangTextElement page) {
        this.page = page;
    }

    public ResourceElement getAccrualPeriodicity() {
        return accrualPeriodicity;
    }

    public void setAccrualPeriodicity(ResourceElement accrualPeriodicity) {
        this.accrualPeriodicity = accrualPeriodicity;
    }

    public LangTextElement getHasVersion() {
        return hasVersion;
    }

    public void setHasVersion(LangTextElement hasVersion) {
        this.hasVersion = hasVersion;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public LangTextElement getIsVersionOf() {
        return isVersionOf;
    }

    public void setIsVersionOf(LangTextElement isVersionOf) {
        this.isVersionOf = isVersionOf;
    }

    public LangTextElement getLandingPage() {
        return landingPage;
    }

    public void setLandingPage(LangTextElement landingPage) {
        this.landingPage = landingPage;
    }

    public LangTextElement getLanguage() {
        return language;
    }

    public void setLanguage(LangTextElement language) {
        this.language = language;
    }

    public LangTextElement getAdmsIdentifier() {
        return admsIdentifier;
    }

    public void setAdmsIdentifier(LangTextElement admsIdentifier) {
        this.admsIdentifier = admsIdentifier;
    }

    public LangTextElement getProvenance() {
        return provenance;
    }

    public void setProvenance(LangTextElement provenance) {
        this.provenance = provenance;
    }

    public LangTextElement getRelation() {
        return relation;
    }

    public void setRelation(LangTextElement relation) {
        this.relation = relation;
    }

    public DateElement getIssued() {
        return issued;
    }

    public void setIssued(String issued) {
        this.issued = new DateElement(issued);
    }

    public LangTextElement getSample() {
        return sample;
    }

    public void setSample(LangTextElement sample) {
        this.sample = sample;
    }

    public LangTextElement getSource() {
        return source;
    }

    public void setSource(LangTextElement source) {
        this.source = source;
    }

    public SpatialElement getSpatial() {
        return spatial;
    }

    public void setSpatial(SpatialElement spatial) {
        this.spatial = spatial;
    }

    public List<TemporalElement> getTemporal() {
        return temporal;
    }

    public void setTemporal(List<TemporalElement> temporal) {
        this.temporal = temporal;
    }

    public LangTextElement getType() {
        return type;
    }

    public void setType(LangTextElement type) {
        this.type = type;
    }

    public DateElement getModified() {
        return modified;
    }

    public void setModified(String modified) {

        this.modified = new DateElement(modified);
    }

    public LangTextElement getVersionInfo() {
        return versionInfo;
    }

    public void setVersionInfo(LangTextElement versionInfo) {
        this.versionInfo = versionInfo;
    }

    public LangTextElement getVersionNote() {
        return versionNote;
    }

    public void setVersionNote(LangTextElement versionNote) {
        this.versionNote = versionNote;
    }

    public ResourceElement getPoliticalGeocodingLevelURI() {
        return politicalGeocodingLevelURI;
    }

    public void setPoliticalGeocodingLevelURI(ResourceElement politicalGeocodingLevelURI) {
        this.politicalGeocodingLevelURI = politicalGeocodingLevelURI;
    }

    public ResourceElement getPoliticalGeocodingURI() {
        return politicalGeocodingURI;
    }

    public void setPoliticalGeocodingURI(ResourceElement politicalGeocodingURI) {
        this.politicalGeocodingURI = politicalGeocodingURI;
    }

    public ResourceElement getGeocodingDescription() {
        return geocodingDescription;
    }

    public void setGeocodingDescription(ResourceElement geocodingDescription) {
        this.geocodingDescription = geocodingDescription;
    }

    public ResourceElement getLegalBasis() {
        return legalBasis;
    }

    public void setLegalBasis(ResourceElement legalBasis) {
        this.legalBasis = legalBasis;
    }

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }
}
