/*
 * **************************************************-
 * ingrid-interface-search
 * ==================================================
 * Copyright (C) 2014 - 2019 wemove digital solutions GmbH
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
package de.ingrid.iface.atomDownloadService;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;

import de.ingrid.iface.atomDownloadService.om.Author;
import de.ingrid.iface.atomDownloadService.om.Category;
import de.ingrid.iface.atomDownloadService.om.OpensearchDescription;
import de.ingrid.iface.atomDownloadService.om.OpensearchDescriptionUrl;
import de.ingrid.iface.atomDownloadService.om.Query;
import de.ingrid.iface.atomDownloadService.om.ServiceFeed;
import de.ingrid.iface.atomDownloadService.om.ServiceFeedEntry;
import de.ingrid.iface.atomDownloadService.requests.OpensearchDescriptionRequest;
import de.ingrid.iface.atomDownloadService.requests.ServiceFeedRequest;
import de.ingrid.iface.atomDownloadService.util.IngridQueryProducer;
import de.ingrid.iface.util.IBusHelper;
import de.ingrid.iface.util.IBusQueryResultIterator;
import de.ingrid.iface.util.IdfUtils;
import de.ingrid.iface.util.SearchInterfaceConfig;
import de.ingrid.iface.util.StringUtils;
import de.ingrid.iface.util.URLUtil;
import de.ingrid.utils.IBus;
import de.ingrid.utils.IngridHit;
import de.ingrid.utils.xml.IDFNamespaceContext;
import de.ingrid.utils.xpath.XPathUtils;

@Service
public class OpensearchDescriptionProducer {

    private IngridQueryProducer ingridQueryProducer;

    private SearchInterfaceConfig config;

    private IBusHelper iBusHelper;

    private ServiceFeedProducer serviceFeedProducer;

    private static final String[] REQUESTED_FIELDS = new String[] {};

    private static final XPathUtils XPATH = new XPathUtils(new IDFNamespaceContext());

    private final static Log log = LogFactory.getLog(OpensearchDescriptionProducer.class);

    private String atomDownloadServiceOpensearchDescriptionUrlPattern = null;

    private String atomDownloadOpensearchDescribeSpatialDatasetUrlTemplate = null;

    private String atomDownloadOpensearchGetSpatialDatasetUrlTemplate = null;

    private String atomDownloadOpensearchGetResultsTemplate = null;

    @PostConstruct
    public void init() {
        atomDownloadServiceOpensearchDescriptionUrlPattern = org.apache.commons.lang.StringUtils.stripEnd(config.getString(SearchInterfaceConfig.ATOM_DOWNLOAD_SERVICE_URL), "/");
        atomDownloadServiceOpensearchDescriptionUrlPattern += config.getString(SearchInterfaceConfig.ATOM_DOWNLOAD_OPENSEARCH_DEFINITION_EXTENSION);

        atomDownloadOpensearchDescribeSpatialDatasetUrlTemplate = org.apache.commons.lang.StringUtils.stripEnd(config.getString(SearchInterfaceConfig.ATOM_DOWNLOAD_SERVICE_URL), "/");
        atomDownloadOpensearchDescribeSpatialDatasetUrlTemplate += config.getString(SearchInterfaceConfig.ATOM_DOWNLOAD_OPENSEARCH_DESCRIBE_SPATIAL_DATASET_TEMPLATE);

        atomDownloadOpensearchGetSpatialDatasetUrlTemplate = org.apache.commons.lang.StringUtils.stripEnd(config.getString(SearchInterfaceConfig.ATOM_DOWNLOAD_SERVICE_URL), "/");
        atomDownloadOpensearchGetSpatialDatasetUrlTemplate += config.getString(SearchInterfaceConfig.ATOM_DOWNLOAD_OPENSEARCH_GET_SPATIAL_DATASET_TEMPLATE);

        atomDownloadOpensearchGetResultsTemplate = org.apache.commons.lang.StringUtils.stripEnd(config.getString(SearchInterfaceConfig.ATOM_DOWNLOAD_SERVICE_URL), "/");
        atomDownloadOpensearchGetResultsTemplate += config.getString(SearchInterfaceConfig.ATOM_DOWNLOAD_OPENSEARCH_GET_RESULTS_TEMPLATE);
    }

    public OpensearchDescription produce(OpensearchDescriptionRequest opensearchDescriptionRequest) throws Exception {
        OpensearchDescription result = new OpensearchDescription();

        if (log.isDebugEnabled()) {
            log.debug("Build service feed from IGC resource for service: " + opensearchDescriptionRequest.getUuid());
        }

        IBus iBus = iBusHelper.getIBus();

        // create response header
        IBusQueryResultIterator serviceIterator = new IBusQueryResultIterator(ingridQueryProducer.createServiceFeedInGridQuery(opensearchDescriptionRequest.getUuid()), REQUESTED_FIELDS, iBus);
        if (serviceIterator.hasNext()) {
            IngridHit hit = serviceIterator.next();
            Long startTimer = 0L;
            if (log.isDebugEnabled()) {
                log.debug("Found valid service: " + hit.getHitDetail().getTitle() + "' from iPlug '" + hit.getPlugId() + "'");
                startTimer = System.currentTimeMillis();
            }
            Document idfDoc = IdfUtils.getIdfDocument(iBus.getRecord(hit));
            if (log.isDebugEnabled()) {
                log.debug("Fetched IDF record within " + (System.currentTimeMillis() - startTimer) + " ms.");
            }
            // make sure the shortname is no longer than 16 chars, see INGRID-2351
            result.setShortName("ATOM Download");
            result.setDescription(XPATH.getString(idfDoc, "//gmd:identificationInfo//gmd:abstract/gco:CharacterString"));

            OpensearchDescriptionUrl tpl = new OpensearchDescriptionUrl();
            String urlPattern = URLUtil.updateProtocol( atomDownloadServiceOpensearchDescriptionUrlPattern, opensearchDescriptionRequest.getProtocol() );
            tpl.setTemplate(urlPattern.replace("{servicefeed-uuid}", StringUtils.encodeForPath(opensearchDescriptionRequest.getUuid())));
            tpl.setType("application/opensearchdescription+xml");
            tpl.setRel("self");
            result.setSelfReferencingUrlTemplate(tpl);

            tpl = new OpensearchDescriptionUrl();
            urlPattern = URLUtil.updateProtocol( atomDownloadOpensearchGetResultsTemplate, opensearchDescriptionRequest.getProtocol() );
            tpl.setTemplate(urlPattern.replace("{servicefeed-uuid}", StringUtils.encodeForPath(opensearchDescriptionRequest.getUuid())));
            tpl.setType("text/html");
            tpl.setRel("results");
            result.setResultsUrlTemplate(tpl);

            tpl = new OpensearchDescriptionUrl();
            urlPattern = URLUtil.updateProtocol( atomDownloadOpensearchDescribeSpatialDatasetUrlTemplate, opensearchDescriptionRequest.getProtocol() );
            tpl.setTemplate(urlPattern.replace("{servicefeed-uuid}", StringUtils.encodeForPath(opensearchDescriptionRequest.getUuid())));
            tpl.setType("application/atom+xml");
            tpl.setRel("describedby");
            result.setDescribeSpatialDatasetOperationUrlTemplate(tpl);

            tpl = new OpensearchDescriptionUrl();
            urlPattern = URLUtil.updateProtocol( atomDownloadOpensearchGetSpatialDatasetUrlTemplate, opensearchDescriptionRequest.getProtocol() );
            tpl.setTemplate(urlPattern.replace("{servicefeed-uuid}", StringUtils.encodeForPath(opensearchDescriptionRequest.getUuid())));
            tpl.setType("application/atom+xml");
            tpl.setRel("results");
            result.setGetSpatialDatasetOperationUrlTemplate(tpl);

            Author author = new Author();
            author.setName(XPATH.getString(idfDoc, "//gmd:identificationInfo//gmd:pointOfContact//gmd:organisationName/gco:CharacterString"));
            author.setEmail(XPATH.getString(idfDoc, "//gmd:identificationInfo//gmd:pointOfContact//gmd:contactInfo/gmd:CI_Contact/gmd:address/gmd:CI_Address/gmd:electronicMailAddress/gco:CharacterString"));
            result.setContact(author.getEmail());

            result.setLongName(result.getShortName());

            ServiceFeedRequest serviceFeedRequest = new ServiceFeedRequest();
            serviceFeedRequest.setUuid(opensearchDescriptionRequest.getUuid());
            ServiceFeed serviceFeed = serviceFeedProducer.produce(serviceFeedRequest);
            result.setExamples(new ArrayList<Query>());
            for (ServiceFeedEntry entry : serviceFeed.getEntries()) {
                Query query = new Query();
                query.setRole("example");
                query.setSpatialDatasetIdentifierCode(entry.getSpatialDatasetIdentifierCode());
                query.setSpatialDatasetIdentifierNamespace(entry.getSpatialDatasetIdentifierNamespace());
                query.setCount(1);
                query.setLanguage("de");
                query.setTitle(entry.getTitle());
                List<Category> crsCat = entry.getCrs();
                if (crsCat != null && crsCat.size() > 0) {
                    query.setCrs(crsCat.get(0).getLabel());
                }
                result.getExamples().add(query);
            }
            List<String> sl = new ArrayList<String>();
            sl.add("de");
            result.setLanguages(sl);
        }

        return result;
    }

    @Autowired
    public void setIngridQueryProducer(IngridQueryProducer ingridQueryProducer) {
        this.ingridQueryProducer = ingridQueryProducer;
    }

    @Autowired
    public void setConfig(SearchInterfaceConfig config) {
        this.config = config;
    }

    @Autowired
    public void setiBusHelper(IBusHelper iBusHelper) {
        this.iBusHelper = iBusHelper;
    }

    @Autowired
    public void setServiceFeedProducer(ServiceFeedProducer serviceFeedProducer) {
        this.serviceFeedProducer = serviceFeedProducer;
    }

}
