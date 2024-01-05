/*
 * **************************************************-
 * ingrid-interface-search
 * ==================================================
 * Copyright (C) 2014 - 2024 wemove digital solutions GmbH
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
package de.ingrid.iface.opensearch.model.dcatapde.general;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class SpatialElement {

    @JacksonXmlProperty(localName = "Location", namespace = "http://purl.org/dc/terms/")
    private LocationElement location = new LocationElement();

    public LocationElement getLocation() {
        return location;
    }

    public void setLocation(LocationElement location) {
        this.location = location;
    }
}
