package de.ingrid.iface.atomDownloadService.requests;

import javax.servlet.http.HttpServletRequest;

public class ServiceFeedListRequest {

    private String query;

    public ServiceFeedListRequest() {

    }

    public ServiceFeedListRequest(HttpServletRequest req) {

        query = req.getParameter("q");
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

}
