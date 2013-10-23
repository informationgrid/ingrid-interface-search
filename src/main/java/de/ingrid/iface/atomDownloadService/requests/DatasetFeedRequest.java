package de.ingrid.iface.atomDownloadService.requests;

import javax.servlet.http.HttpServletRequest;

import org.springframework.util.StringUtils;

public class DatasetFeedRequest {

    private String uuid;
    private String serviceFeedUuid;

    public DatasetFeedRequest(HttpServletRequest req) throws Exception {

        String path = StringUtils.trimLeadingCharacter(req.getPathInfo(), '/');
        int idx = path.indexOf('/');
        if (idx <= 0) {
            throw new Exception("Could not handle data set request parameter: " + req.getPathInfo());
        }
        serviceFeedUuid = path.substring(0, idx - 1);
        uuid = path.substring(idx + 1);
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getServiceFeedUuid() {
        return serviceFeedUuid;
    }

    public void setServiceFeedUuid(String serviceFeedUuid) {
        this.serviceFeedUuid = serviceFeedUuid;
    }

}
