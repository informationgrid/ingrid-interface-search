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
package de.ingrid.iface.opensearch.model.dcatapde;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import de.ingrid.iface.opensearch.model.dcatapde.general.DatatypeTextElement;
import de.ingrid.iface.util.SearchInterfaceConfig;
import de.ingrid.iface.util.URLUtil;
import jakarta.servlet.http.HttpServletRequest;

import java.util.ArrayList;
import java.util.List;

@JacksonXmlRootElement(localName = "RDF", namespace = "http://www.w3.org/1999/02/22-rdf-syntax-ns#")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DcatApDe {

    @JacksonXmlProperty(localName = "PagedCollection", namespace = "http://www.w3.org/ns/hydra/core#")
    private HydraCollection collection;

    //private String agent;
    //private String licenseDocument;
    //private String identifier;
    //private String periodOfTime;

    @JacksonXmlProperty(localName = "Catalog", namespace = "http://www.w3.org/ns/dcat#")
    private Catalog catalog = new Catalog();

    @JacksonXmlProperty(localName = "CatalogRecord", namespace = "http://www.w3.org/ns/dcat#")
    private CatalogRecord catalogRecord;

    @JacksonXmlProperty(localName = "Dataset", namespace = "http://www.w3.org/ns/dcat#")
    @JacksonXmlElementWrapper(useWrapping = false)
    private List<Dataset> dataset = new ArrayList<>();

    @JacksonXmlProperty(localName = "Distribution", namespace = "http://www.w3.org/ns/dcat#")
    @JacksonXmlElementWrapper(useWrapping = false)
    private List<Distribution> distribution = new ArrayList<>();

    @JacksonXmlProperty(localName = "Checksum")
    @JacksonXmlElementWrapper(useWrapping = false)
    private Checksum checksum;

    /*public String getAgent() {
        return agent;
    }

    public void setAgent(String agent) {
        this.agent = agent;
    }

    public String getLicenseDocument() {
        return licenseDocument;
    }

    public void setLicenseDocument(String licenseDocument) {
        this.licenseDocument = licenseDocument;
    }

    public String getPeriodOfTime() {
        return periodOfTime;
    }

    public void setPeriodOfTime(String periodOfTime) {
        this.periodOfTime = periodOfTime;
    }*/

    public CatalogRecord getCatalogRecord() {
        return catalogRecord;
    }

    public void setCatalogRecord(CatalogRecord catalogRecord) {
        this.catalogRecord = catalogRecord;
    }

    public List<Dataset> getDataset() {
        return dataset;
    }

    public void setDataset(List<Dataset> datasets) {
        this.dataset = datasets;
    }

    public List<Distribution> getDistribution() {
        return distribution;
    }

    public void setDistribution(List<Distribution> distribution) {
        this.distribution = distribution;
    }

    public Checksum getChecksum() {
        return checksum;
    }

    public void setChecksum(Checksum checksum) {
        this.checksum = checksum;
    }

    public Catalog getCatalog() {
        return catalog;
    }

    public void setCatalog(Catalog catalog) {
        this.catalog = catalog;
    }

    public HydraCollection getCollection() {
        return collection;
    }

    private void setCollection(HydraCollection collection) {
        this.collection = collection;
    }

    public void handlePaging(HttpServletRequest request, int page, int numPerPage, long totalCount) {
        HydraCollection hydraCollection = new HydraCollection();

        String baseURL = request.getRequestURL().toString();
        String proxyurl = URLUtil.updateProtocol(SearchInterfaceConfig.getInstance().getString(SearchInterfaceConfig.OPENSEARCH_PROXY_URL, null), request.getScheme());
        if (proxyurl != null && proxyurl.trim().length() > 0) {
            baseURL = proxyurl.concat("/query");
        } else {
            baseURL = request.getRequestURL().toString();
        }

        hydraCollection.setItemsPerPage(new DatatypeTextElement(String.valueOf(numPerPage)));
        hydraCollection.setTotalItems(new DatatypeTextElement(String.valueOf(totalCount)));


        request.getQueryString();
        String newQueryString = request.getQueryString() == null
                ? "p=" + page
                : request.getQueryString() + "&p=" + page;
        request.setAttribute("javax.servlet.forward.query_string", newQueryString);
        hydraCollection.setAbout(baseURL + "?" + request.getQueryString());

        String newQueryString1 = request.getQueryString() == null
                ? "p=1"
                : request.getQueryString() + "&p=1";
        request.setAttribute("javax.servlet.forward.query_string", newQueryString1);
        hydraCollection.setFirstPage(baseURL + "?" + request.getQueryString());

        int lastPage = (int) totalCount / numPerPage;
        if (totalCount > lastPage * numPerPage) lastPage++;
        String newQueryString2 = request.getQueryString() == null
                ? "p=" + lastPage
                : request.getQueryString() + "&p=" + lastPage;
        request.setAttribute("javax.servlet.forward.query_string", newQueryString2);
        hydraCollection.setLastPage(baseURL + "?" + request.getQueryString());

        if (totalCount > page * numPerPage) {
            String newQueryString3 = request.getQueryString() == null
                    ? "p=" + (page + 1)
                    : request.getQueryString() + "&p=" + (page + 1);
            request.setAttribute("javax.servlet.forward.query_string", newQueryString3);
            hydraCollection.setNextPage(baseURL + "?" + request.getQueryString());
        }
        if (page > 1 && page <= lastPage) {
            String newQueryString4 = request.getQueryString() == null
                    ? "p=" + (page - 1)
                    : request.getQueryString() + "&p=" + (page - 1);
            request.setAttribute("javax.servlet.forward.query_string", newQueryString4);
            hydraCollection.setPreviousPage(baseURL + "?" + request.getQueryString());
        }

        setCollection(hydraCollection);
    }
}
