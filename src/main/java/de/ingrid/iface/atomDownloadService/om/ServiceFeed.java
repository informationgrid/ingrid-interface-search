/*
 * **************************************************-
 * ingrid-interface-search
 * ==================================================
 * Copyright (C) 2014 - 2020 wemove digital solutions GmbH
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

public class ServiceFeed {

    private String title;
    private String subTitle;
    private String uuid;
    private Link metadataAccessUrl;
    private Link selfReferencingLink;
    private Link openSearchDefinitionLink;
    private String identifier;
    private String copyright;
    private String updated;
    private Author author;
    private List<ServiceFeedEntry> entries;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubTitle() {
        return subTitle;
    }

    public void setSubTitle(String subTitle) {
        this.subTitle = subTitle;
    }

    public Link getMetadataAccessUrl() {
        return metadataAccessUrl;
    }

    public void setMetadataAccessUrl(Link metadataAccessUrl) {
        this.metadataAccessUrl = metadataAccessUrl;
    }

    public Link getSelfReferencingLink() {
        return selfReferencingLink;
    }

    public void setSelfReferencingLink(Link selfReferencingLink) {
        this.selfReferencingLink = selfReferencingLink;
    }

    public Link getOpenSearchDefinitionLink() {
        return openSearchDefinitionLink;
    }

    public void setOpenSearchDefinitionLink(Link openSearchDefinitionLink) {
        this.openSearchDefinitionLink = openSearchDefinitionLink;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getCopyright() {
        return copyright;
    }

    public void setCopyright(String copyright) {
        this.copyright = copyright;
    }

    public String getUpdated() {
        return updated;
    }

    public void setUpdated(String updated) {
        this.updated = updated;
    }

    public Author getAuthor() {
        return author;
    }

    public void setAuthor(Author author) {
        this.author = author;
    }

    public List<ServiceFeedEntry> getEntries() {
        return entries;
    }

    public void setEntries(List<ServiceFeedEntry> entries) {
        this.entries = entries;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
    
    

}
