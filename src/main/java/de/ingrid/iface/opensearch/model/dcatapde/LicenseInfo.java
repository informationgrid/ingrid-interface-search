package de.ingrid.iface.opensearch.model.dcatapde;

public class LicenseInfo {
    private String id;
    private String uri;
    private String[] textUris;
    private String name;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String[] getTextUris() {
        return textUris;
    }

    public void setTextUris(String[] textUris) {
        this.textUris = textUris;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
