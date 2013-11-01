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
