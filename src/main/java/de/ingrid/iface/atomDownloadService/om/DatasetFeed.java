package de.ingrid.iface.atomDownloadService.om;

import java.util.List;

public class DatasetFeed {

    private String title;
    private String subTitle;
    private String uuid;
    private List<Link> describedBy;
    private Link selfReferencingLink;
    private Link downloadServiceFeed;
    private String identifier;
    private String rights;
    private String updated;
    private Author author;
    private List<DatasetFeedEntry> entries;

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

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public List<Link> getDescribedBy() {
        return describedBy;
    }

    public void setDescribedBy(List<Link> describedBy) {
        this.describedBy = describedBy;
    }

    public Link getSelfReferencingLink() {
        return selfReferencingLink;
    }

    public void setSelfReferencingLink(Link selfReferencingLink) {
        this.selfReferencingLink = selfReferencingLink;
    }

    public Link getDownloadServiceFeed() {
        return downloadServiceFeed;
    }

    public void setDownloadServiceFeed(Link downloadServiceFeed) {
        this.downloadServiceFeed = downloadServiceFeed;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
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

    public Author getAuthor() {
        return author;
    }

    public void setAuthor(Author author) {
        this.author = author;
    }

    public List<DatasetFeedEntry> getEntries() {
        return entries;
    }

    public void setEntries(List<DatasetFeedEntry> entries) {
        this.entries = entries;
    }

}
