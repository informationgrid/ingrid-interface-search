/*
 * **************************************************-
 * ingrid-interface-search
 * ==================================================
 * Copyright (C) 2014 - 2026 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.2 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * **************************************************#
 */
package de.ingrid.iface.opensearch.model.dcatapde.catalog;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import de.ingrid.iface.opensearch.model.dcatapde.general.ResourceElement;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Agent {

    // 0..n
    @JacksonXmlProperty(namespace = "http://xmlns.com/foaf/0.1/")
    private String name;

    // 0..n
    @JacksonXmlProperty(namespace = "http://xmlns.com/foaf/0.1/")
    private ResourceElement mbox;

    // 0..n
    @JacksonXmlProperty(namespace = "http://xmlns.com/foaf/0.1/")
    private String homepage;

    // 0..1
    @JacksonXmlProperty(namespace = "http://purl.org/dc/terms/")
    private String type;

    @JacksonXmlProperty(isAttribute = true, namespace = "http://www.w3.org/1999/02/22-rdf-syntax-ns#")
    private String about;

    @JacksonXmlProperty(namespace = "http://www.w3.org/ns/locn#")
    private AddressWrapper address;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }

    public String getHomepage() {
        return homepage;
    }

    public void setHomepage(String homepage) {
        this.homepage = homepage;
    }

    public ResourceElement getMbox() {
        return mbox;
    }

    public void setMbox(ResourceElement mbox) {
        this.mbox = mbox;
    }

    public AddressWrapper getAddress() {
        return address;
    }

    public void setAddress(AddressWrapper address) {
        this.address = address;
    }
}
