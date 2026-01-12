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
package de.ingrid.iface.opensearch.model.dcatapde;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import de.ingrid.iface.opensearch.model.dcatapde.general.DatatypeTextElement;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class HydraCollection {

    @JacksonXmlProperty(isAttribute = true, namespace = "http://www.w3.org/1999/02/22-rdf-syntax-ns#")
    private String about;

    @JacksonXmlProperty(namespace = "http://www.w3.org/ns/hydra/core#")
    private DatatypeTextElement itemsPerPage;

    @JacksonXmlProperty(namespace = "http://www.w3.org/ns/hydra/core#")
    private DatatypeTextElement totalItems;

    @JacksonXmlProperty(namespace = "http://www.w3.org/ns/hydra/core#")
    private String firstPage;

    @JacksonXmlProperty(namespace = "http://www.w3.org/ns/hydra/core#")
    private String lastPage;

    @JacksonXmlProperty(namespace = "http://www.w3.org/ns/hydra/core#")
    private String previousPage;

    @JacksonXmlProperty(namespace = "http://www.w3.org/ns/hydra/core#")
    private String nextPage;


    public DatatypeTextElement getItemsPerPage() {
        return itemsPerPage;
    }

    public void setItemsPerPage(DatatypeTextElement itemsPerPage) {
        this.itemsPerPage = itemsPerPage;
    }

    public String getNextPage() {
        return nextPage;
    }

    public void setNextPage(String nextPage) {
        this.nextPage = nextPage;
    }

    public String getFirstPage() {
        return firstPage;
    }

    public void setFirstPage(String firstPage) {
        this.firstPage = firstPage;
    }

    public String getLastPage() {
        return lastPage;
    }

    public void setLastPage(String lastPage) {
        this.lastPage = lastPage;
    }

    public String getPreviousPage() {
        return previousPage;
    }

    public void setPreviousPage(String previousPage) {
        this.previousPage = previousPage;
    }

    public DatatypeTextElement getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(DatatypeTextElement totalItems) {
        this.totalItems = totalItems;
    }

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }
}
