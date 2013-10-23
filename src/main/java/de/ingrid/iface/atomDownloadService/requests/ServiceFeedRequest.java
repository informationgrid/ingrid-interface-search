package de.ingrid.iface.atomDownloadService.requests;

import javax.servlet.http.HttpServletRequest;

public class ServiceFeedRequest {

    private String uuid;

    private String query;

    public ServiceFeedRequest(HttpServletRequest req) {

        uuid = req.getPathInfo().substring(1);
        query = req.getParameter("q");
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

}
