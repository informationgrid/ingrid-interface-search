/*
 * **************************************************-
 * ingrid-interface-search
 * ==================================================
 * Copyright (C) 2014 - 2025 wemove digital solutions GmbH
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
public class VCardOrganization {

    // 0..1
    @JacksonXmlProperty(isAttribute = true, namespace = "http://www.w3.org/1999/02/22-rdf-syntax-ns#")
    // Have not to start with a Number (#2922)
    private String nodeID;

    // 0..1
    @JacksonXmlProperty(namespace = "http://www.w3.org/2006/vcard/ns#")
    private String fn;

    // 0..1
    @JacksonXmlProperty(namespace = "http://www.w3.org/2006/vcard/ns#")
    private String hasPostalCode;

    // 0..1
    @JacksonXmlProperty(namespace = "http://www.w3.org/2006/vcard/ns#")
    private String hasStreetAddress;

    // 0..1
    @JacksonXmlProperty(namespace = "http://www.w3.org/2006/vcard/ns#")
    private String hasLocality;

    // 0..1
    @JacksonXmlProperty(namespace = "http://www.w3.org/2006/vcard/ns#")
    private String hasCountryName;

    // 0..1
    @JacksonXmlProperty(namespace = "http://www.w3.org/2006/vcard/ns#")
    private ResourceElement hasEmail;

    // 0..1
    @JacksonXmlProperty(namespace = "http://www.w3.org/2006/vcard/ns#")
    private ResourceElement hasURL;

    public String getNodeID() {
        return nodeID;
    }

    public void setNodeID(String nodeID) {
        this.nodeID = nodeID;
    }

    public String getFn() {
        return fn;
    }

    public void setFn(String fn) {
        this.fn = fn;
    }

    public String getHasPostalCode() {
        return hasPostalCode;
    }

    public void setHasPostalCode(String hasPostalCode) {
        this.hasPostalCode = hasPostalCode;
    }

    public String getHasStreetAddress() {
        return hasStreetAddress;
    }

    public void setHasStreetAddress(String hasStreetAddress) {
        this.hasStreetAddress = hasStreetAddress;
    }

    public String getHasLocality() {
        return hasLocality;
    }

    public void setHasLocality(String hasLocality) {
        this.hasLocality = hasLocality;
    }

    public String getHasCountryName() {
        return hasCountryName;
    }

    public void setHasCountryName(String hasCountryName) {
        this.hasCountryName = hasCountryName;
    }

    public ResourceElement getHasEmail() {
        return hasEmail;
    }

    public void setHasEmail(ResourceElement hasEmail) {
        this.hasEmail = hasEmail;
    }

    public ResourceElement getHasURL() {
        return hasURL;
    }

    public void setHasURL(ResourceElement hasURL) {
        this.hasURL = hasURL;
    }
}
