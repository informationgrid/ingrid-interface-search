package de.ingrid.iface.atomDownloadService.om;

import java.util.List;

public class ServiceFeedList {

    private Link selfReferencingLink;

    private List<ServiceFeed> entries;

    public Link getSelfReferencingLink() {
        return selfReferencingLink;
    }

    public void setSelfReferencingLink(Link selfReferencingLink) {
        this.selfReferencingLink = selfReferencingLink;
    }

    public List<ServiceFeed> getEntries() {
        return entries;
    }

    public void setEntries(List<ServiceFeed> entries) {
        this.entries = entries;
    }

}
