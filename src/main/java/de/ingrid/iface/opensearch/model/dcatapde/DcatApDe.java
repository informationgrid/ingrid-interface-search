/*
 * **************************************************-
 * ingrid-interface-search
 * ==================================================
 * Copyright (C) 2014 - 2022 wemove digital solutions GmbH
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
package de.ingrid.iface.opensearch.model.dcatapde;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import de.ingrid.iface.opensearch.model.dcatapde.general.DatatypeTextElement;
import de.ingrid.iface.util.SearchInterfaceConfig;
import org.eclipse.jetty.server.Request;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
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

    public void handlePaging(Request request, int page, int numPerPage, long totalCount) {
        HydraCollection hydraCollection = new HydraCollection();
        String baseURL = request.getRequestURL().toString();
        baseURL = SearchInterfaceConfig.getInstance().getString(SearchInterfaceConfig.OPENSEARCH_PROXY_URL, baseURL);

        hydraCollection.setItemsPerPage(new DatatypeTextElement(String.valueOf(numPerPage)));
        hydraCollection.setTotalItems(new DatatypeTextElement(String.valueOf(totalCount)));


        request.getQueryString();
        request.mergeQueryString("p="+page);
        hydraCollection.setAbout(baseURL + "?" + request.getQueryString());

        request.mergeQueryString("p=1");
        hydraCollection.setFirstPage(baseURL + "?" + request.getQueryString());

        int lastPage = (int)totalCount/numPerPage;
        if (totalCount > lastPage*numPerPage)lastPage++;
        request.mergeQueryString("p="+lastPage);
        hydraCollection.setLastPage(baseURL + "?" + request.getQueryString());

        if (totalCount > page*numPerPage) {
            request.mergeQueryString("p="+(page+1));
            hydraCollection.setNextPage(baseURL + "?" + request.getQueryString());
        }
        if (page > 1 && page <= lastPage) {
            request.mergeQueryString("p="+(page-1));
            hydraCollection.setPreviousPage(baseURL + "?" + request.getQueryString());
        }

        setCollection(hydraCollection);
    }
}
