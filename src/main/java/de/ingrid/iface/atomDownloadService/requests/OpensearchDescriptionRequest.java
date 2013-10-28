package de.ingrid.iface.atomDownloadService.requests;

import javax.servlet.http.HttpServletRequest;

public class OpensearchDescriptionRequest {

    private String uuid;

    public OpensearchDescriptionRequest(HttpServletRequest req) throws Exception {

        uuid = req.getPathInfo().substring(1);
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

}
