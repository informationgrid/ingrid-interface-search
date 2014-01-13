package de.ingrid.iface.atomDownloadService.om;

import java.util.List;

public class DatasetFeedEntry {

    private String title;
    private String content;
    private List<Link> links;
    private String id;
    private String updated;
    private List<Category> crs;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<Link> getLinks() {
        return links;
    }

    public void setLinks(List<Link> links) {
        this.links = links;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUpdated() {
        return updated;
    }

    public void setUpdated(String updated) {
        this.updated = updated;
    }

    public List<Category> getCrs() {
        return crs;
    }

    public void setCrs(List<Category> crs) {
        this.crs = crs;
    }

}
