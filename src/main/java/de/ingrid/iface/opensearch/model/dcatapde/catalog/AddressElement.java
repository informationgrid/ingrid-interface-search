/*
 * **************************************************-
 * ingrid-interface-search
 * ==================================================
 * Copyright (C) 2014 - 2023 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.1 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl5
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * **************************************************#
 */
package de.ingrid.iface.opensearch.model.dcatapde.catalog;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import de.ingrid.iface.opensearch.model.dcatapde.general.ResourceElement;

public class AddressElement {

    @JacksonXmlProperty(namespace = "http://www.w3.org/ns/locn#")
    private String fullAddress;

    @JacksonXmlProperty(namespace = "http://www.w3.org/ns/locn#")
    private String poBox;

    @JacksonXmlProperty(namespace = "http://www.w3.org/ns/locn#")
    private String locatorDesignator;

    @JacksonXmlProperty(namespace = "http://www.w3.org/ns/locn#")
    private String locatorName;

    @JacksonXmlProperty(namespace = "http://www.w3.org/ns/locn#")
    private String addressArea;

    @JacksonXmlProperty(namespace = "http://www.w3.org/ns/locn#")
    private String postName;

    @JacksonXmlProperty(namespace = "http://www.w3.org/ns/locn#")
    private ResourceElement adminUnitL2;

    @JacksonXmlProperty(namespace = "http://www.w3.org/ns/locn#")
    private ResourceElement adminUnitL1;

    public String getFullAddress() {
        return fullAddress;
    }

    public void setFullAddress(String fullAddress) {
        this.fullAddress = fullAddress;
    }

    public String getPoBox() {
        return poBox;
    }

    public void setPoBox(String poBox) {
        this.poBox = poBox;
    }

    public String getLocatorDesignator() {
        return locatorDesignator;
    }

    public void setLocatorDesignator(String locatorDesignator) {
        this.locatorDesignator = locatorDesignator;
    }

    public String getLocatorName() {
        return locatorName;
    }

    public void setLocatorName(String locatorName) {
        this.locatorName = locatorName;
    }

    public String getAddressArea() {
        return addressArea;
    }

    public void setAddressArea(String addressArea) {
        this.addressArea = addressArea;
    }

    public String getPostName() {
        return postName;
    }

    public void setPostName(String postName) {
        this.postName = postName;
    }

    public ResourceElement getAdminUnitL2() {
        return adminUnitL2;
    }

    public void setAdminUnitL2(ResourceElement adminUnitL2) {
        this.adminUnitL2 = adminUnitL2;
    }

    public ResourceElement getAdminUnitL1() {
        return adminUnitL1;
    }

    public void setAdminUnitL1(ResourceElement adminUnitL1) {
        this.adminUnitL1 = adminUnitL1;
    }

    public String getPostCode() {
        return postCode;
    }

    public void setPostCode(String postCode) {
        this.postCode = postCode;
    }

    public String getAddressID() {
        return addressID;
    }

    public void setAddressID(String addressID) {
        this.addressID = addressID;
    }

    @JacksonXmlProperty(namespace = "http://www.w3.org/ns/locn#")
    private String postCode;

    @JacksonXmlProperty(namespace = "http://www.w3.org/ns/locn#")
    private String addressID;
}
