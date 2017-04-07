/*
 * **************************************************-
 * ingrid-interface-search
 * ==================================================
 * Copyright (C) 2014 - 2017 wemove digital solutions GmbH
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
/*
 * Copyright (c) 2006 wemove digital solutions. All rights reserved.
 */
package de.ingrid.iface.opensearch;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.weta.components.communication.server.TooManyRunningThreads;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.eclipse.jetty.http.HttpException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.ingrid.iface.opensearch.util.OpensearchUtil;
import de.ingrid.iface.opensearch.util.RequestWrapper;
import de.ingrid.iface.util.IBusHelper;
import de.ingrid.iface.util.IBusQueryResultIterator;
import de.ingrid.iface.util.IPlugHelper;
import de.ingrid.iface.util.SearchInterfaceConfig;
import de.ingrid.iface.util.SearchInterfaceServlet;
import de.ingrid.iface.util.URLUtil;
import de.ingrid.utils.IngridHit;
import de.ingrid.utils.IngridHitDetail;
import de.ingrid.utils.PlugDescription;
import de.ingrid.utils.dsc.Column;
import de.ingrid.utils.dsc.Record;
import de.ingrid.utils.idf.IdfTool;
import de.ingrid.utils.iplug.IPlugVersionInspector;
import de.ingrid.utils.query.IngridQuery;

/**
 * Servlet handles OpenSearch queries.
 * 
 * @author joachim@wemove.com
 */
@Service
public class OpensearchServlet extends HttpServlet implements SearchInterfaceServlet {

    private static final long serialVersionUID = 597250457306006899L;

    private final static Log log = LogFactory.getLog(OpensearchServlet.class);

    @Autowired
    private IBusHelper iBusHelper;

    private static Integer MAX_IBUS_RESULT_SET_SIZE = 100;

    /**
     * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
     *      javax.servlet.http.HttpServletResponse)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        long overallStartTime = 0;
        if (log.isDebugEnabled()) {
            overallStartTime = System.currentTimeMillis();
        }

        RequestWrapper requestWrapper = new RequestWrapper(request);

        if (log.isDebugEnabled()) {
            log.debug("incoming query: " + requestWrapper.getQueryString());
        }

        IngridQuery query = createIngridQueryFromRequest(requestWrapper);
        String[] requestedMetadata = getRequestedMetadata(requestWrapper, query);

        // set paging parameter for query
        int page = requestWrapper.getRequestedPage();
        int hitsPerPage = requestWrapper.getHitsPerPage();
        if (page <= 0)
            page = 1;
        int pageSize = (hitsPerPage > MAX_IBUS_RESULT_SET_SIZE) ? MAX_IBUS_RESULT_SET_SIZE : hitsPerPage;

        // set timeout
        int maxSearchTimeout = SearchInterfaceConfig.getInstance().getInt(SearchInterfaceConfig.IBUS_SEARCH_MAX_TIMEOUT, 60000);
        int searchTimeout = requestWrapper.getSearchTimeout();
        if (searchTimeout == 0 || searchTimeout > maxSearchTimeout) {
            searchTimeout = maxSearchTimeout;
        }

        PrintWriter pout = null;
        IBusQueryResultIterator hitIterator = null;
        int hitCounter = 0;
        boolean outputStreamWritten = false;
        try {

            hitIterator = new IBusQueryResultIterator(query, requestedMetadata, iBusHelper.getIBus(), pageSize, (page - 1), hitsPerPage * page);
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/rss+xml");
            Document doc = DocumentHelper.createDocument();
            while ((hitIterator.hasNext() && hitCounter < requestWrapper.getHitsPerPage()) || hitCounter == 0) {
        	if (hitCounter == 0) {
        	    if (log.isDebugEnabled()) {
        	        log.debug( "No result written yet, add opensearch response header..." );
        	    }
                    if (pout == null) {
                	pout = response.getWriter();
                    }
                    outputStreamWritten = true;
                    pout.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
                    pout.write("<rss xmlns:opensearch=\"http://a9.com/-/spec/opensearch/1.1/\" xmlns:relevance=\"http://a9.com/-/opensearch/extensions/relevance/1.0/\" xmlns:ingrid=\"http://www.portalu.de/opensearch/extension/1.0\" version=\"2.0\">");
                    pout.write("<channel>");
                    pout.write("<title>" + StringEscapeUtils.escapeXml(getChannelTitle(requestWrapper)) + "</title>");
                    String url = null;
                    String queryString = requestWrapper.getRequest().getQueryString();
                    if (queryString == null)
                        queryString = "";
                    queryString.replace("+", "%2B");
                    String proxyurl = URLUtil.updateProtocol( SearchInterfaceConfig.getInstance().getString(SearchInterfaceConfig.OPENSEARCH_PROXY_URL, null), request.getScheme());
                    if (proxyurl != null && proxyurl.trim().length() > 0) {
                        url = proxyurl.concat("/query").concat("?").concat(queryString);
                    } else {
                        url = requestWrapper.getRequest().getRequestURL().toString().concat("?").concat(queryString);
                    }
                    pout.write("<link>" + StringEscapeUtils.escapeXml(url) + "</link>");
                    pout.write("<description>Search results</description>");
                    pout.write("<opensearch:totalResults>" + hitIterator.getTotalResults() + "</opensearch:totalResults>");
                    pout.write("<opensearch:startIndex>" + String.valueOf(requestWrapper.getRequestedPage()) + "</opensearch:startIndex>");
                    pout.write("<opensearch:itemsPerPage>" + String.valueOf(requestWrapper.getHitsPerPage()) + "</opensearch:itemsPerPage>");
                    pout.write("<opensearch:Query role=\"request\" searchTerms=\"" + StringEscapeUtils.escapeXml(requestWrapper.getQueryString()) + "\"/>");
                }
                if (hitIterator.hasNext()) {
                    if (log.isDebugEnabled()) {
                        log.debug( "Add item..." );
                    }
                    IngridHit hit = hitIterator.next();
                    Element item = doc.addElement("item");
                    item.addNamespace("relevance", "http://a9.com/-/opensearch/extensions/relevance/1.0/");
                    if (requestWrapper.withIngridData() || requestWrapper.getMetadataDetail()) {
                        item.addNamespace("ingrid", "http://www.portalu.de/opensearch/extension/1.0");
                    }
                    if (requestWrapper.withGeoRSS()) {
                        item.addNamespace("georss", "http://www.georss.org/georss");
                    }

                    IngridHitDetail detail = (IngridHitDetail) hit.getHitDetail();

                    addItemTitle(item, hit, requestWrapper, true);
                    addItemLink(item, hit, requestWrapper, true);
                    item.addElement("description").addText(OpensearchUtil.removeInvalidChars(OpensearchUtil.deNullify(detail.getSummary())));
                    item.addElement("relevance:score").addText(String.valueOf(hit.getScore()));
                    addIngridData(item, hit, requestWrapper, true);
                    addGeoRssData(item, hit, requestWrapper);
                    pout.write(doc.getRootElement().asXML());
                    doc.clearContent();
                }
                hitCounter++;
            }
            doc = null;

            if (log.isDebugEnabled()) {
                log.debug("Time for complete search: " + (System.currentTimeMillis() - overallStartTime) + " ms");
            }

        } catch (TooManyRunningThreads e) {
            log.error("Too many threads!", e);
            throw (HttpException) new HttpException(503, "Too many threads!").initCause(e);
        } catch (Throwable t) {
            if (!outputStreamWritten) {
                log.error("Error serving request:" + requestWrapper.getRequest().getQueryString() + " Outputstream not written, send 500 Error", t);
                throw (HttpException) new HttpException(500, "Internal error!").initCause(t);
            } else {
                log.error("Error serving request:" + requestWrapper.getRequest().getQueryString(), t);
            }
        } finally {
            hitIterator.cleanup();
            hitIterator = null;
            if (pout != null) {
                if (outputStreamWritten) {
                    pout.write("</channel></rss>");
                }
                pout.close();
            }
        }

    }

    /**
     * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest,
     *      javax.servlet.http.HttpServletResponse)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }

    /**
     * @see javax.servlet.GenericServlet#init(javax.servlet.ServletConfig)
     */
    public void init(ServletConfig arg0) throws ServletException {
        super.init(arg0);
    }

    private IngridQuery createIngridQueryFromRequest(RequestWrapper requestWrapper) {
        IngridQuery query = requestWrapper.getQuery();

        iBusHelper.injectCache(query);

        // if detail data are requested, set direct data property to get the
        // date during search
        // this improves performance, because we do not need getRecord requests
        // to retrieve the data
        if (requestWrapper.getMetadataDetail()) {
            query.put("cswDirectResponse", "full");
        }

        return query;
    }

    private String[] getRequestedMetadata(RequestWrapper requestWrapper, IngridQuery query) {

        String[] requestedMetadata = null;

        if (!OpensearchUtil.hasPositiveDataType(query, "address")) {
            requestedMetadata = new String[] { "t01_object.obj_id" };
            // check if GeoRSS data shall be checked too
            if (requestWrapper.withGeoRSS()) {
                String[] additional = new String[] { "x1", "x2", "y1", "y2" };
                requestedMetadata = (String[]) ArrayUtils.addAll(requestedMetadata, additional);
            }
            if (requestWrapper.withIngridData()) {
                String[] additional = new String[] { "t01_object.obj_class", "t011_obj_serv_op_connpoint.connect_point", "partner", "provider", "t1", "t2" };
                requestedMetadata = (String[]) ArrayUtils.addAll(requestedMetadata, additional);
            }
        } else {
            requestedMetadata = new String[] { "t02_address.firstname", "t02_address.lastname", "t02_address.title", "t02_address.address", "t02_address.adr_id" };
            if (requestWrapper.withIngridData()) {
                String[] additional = new String[] { "t02_address.typ", "t011_obj_serv_op_connpoint.connect_point", "partner", "provider", "t1", "t2" };
                requestedMetadata = (String[]) ArrayUtils.addAll(requestedMetadata, additional);
            }
        }

        return requestedMetadata;
    }

    /**
     * Get the title for the channel from the request url or, if not defined,
     * set a default title containing the search terms (query)
     * 
     * @param requestWrapper
     * @return
     */
    private String getChannelTitle(RequestWrapper requestWrapper) {
        String title = (String) requestWrapper.get(RequestWrapper.CHANNEL_TITLE);
        if (title.isEmpty()) {
            title = "ingrid OpenSearch: " + requestWrapper.getQueryString();
        }

        return title;
    }

    private void addGeoRssData(Element item, IngridHit hit, RequestWrapper requestWrapper) {
        if (requestWrapper.withGeoRSS()) {
            if (log.isDebugEnabled()) {
                log.debug( "Add Geo RSS data..." );
            }
            IngridHitDetail detail = (IngridHitDetail) hit.getHitDetail();
            if (detail.get("x1") != null && detail.get("x1") instanceof String[]) {

                try {
                    if (!((String[]) detail.get("x1"))[0].equals("")) {
                        int length = ((String[]) detail.get("x1")).length;
                        for (int j = 0; j < length; j++) {
                            Float x1 = Float.valueOf(((String[]) detail.get("x1"))[j]);
                            Float x2 = Float.valueOf(((String[]) detail.get("x2"))[j]);
                            Float y1 = Float.valueOf(((String[]) detail.get("y1"))[j]);
                            Float y2 = Float.valueOf(((String[]) detail.get("y2"))[j]);

                            if ((x1 + x2 + y1 + y2) != 0.0) {
                                item.addElement("georss:box").addText(
                                        OpensearchUtil.deNullify(x1.toString()) + " " + OpensearchUtil.deNullify(y1.toString()) + " " + OpensearchUtil.deNullify(x2.toString()) + " " + OpensearchUtil.deNullify(y2.toString()));
                                if (detail.containsKey("location")) {
                                    item.addElement("georss:featurename", (String) detail.get("location"));
                                }
                            }
                        }
                    }
                } catch (NumberFormatException e) {
                    // empty
                } catch (Exception e) {
                    log.debug("Exception when getting geo data: " + e.getMessage());
                }
            }
        }
    }

    private void addIngridData(Element item, IngridHit hit, RequestWrapper requestWrapper, boolean ibusConnected) {
        if (requestWrapper.withIngridData()) {
            if (log.isDebugEnabled()) {
                log.debug( "Add ingrid data..." );
            }
            IngridHitDetail detail = (IngridHitDetail) hit.getHitDetail();
            String plugId = hit.getPlugId();
            String docId = String.valueOf(hit.getDocumentId());
            String altDocId = (String) hit.get("alt_document_id");
            String udkClass = OpensearchUtil.getDetailValue(detail, "t01_object.obj_class");
            String udkAddrClass = OpensearchUtil.getDetailValue(detail, "t02_address.typ");
            String wmsUrl = null;
            Object obj = detail.get("t011_obj_serv_op_connpoint.connect_point");
            if (obj != null && obj instanceof String[] && ((String[]) obj).length > 0) {
                // get first entry from the array, ignore empty entries
                for (String value : ((String[]) obj)) {
                    if (value != null && value.length() > 0) {
                        wmsUrl = value;
                        break;
                    }
                }
            } else {
                wmsUrl = OpensearchUtil.getDetailValue(detail, "t011_obj_serv_op_connpoint.connect_point");
            }
            String docUuid = OpensearchUtil.getDetailValue(detail, "t01_object.obj_id");

            item.addElement("ingrid:plugid").addText(OpensearchUtil.deNullify(plugId));
            item.addElement("ingrid:docid").addText(OpensearchUtil.deNullify(docId));
            item.addElement("ingrid:docuuid").addText(OpensearchUtil.deNullify(docUuid));
            if (altDocId != null && altDocId.length() > 0) {
                item.addElement("ingrid:altdocid").addText(altDocId);
            }

            String providerId = OpensearchUtil.getDetailValue(detail, "provider");
            String providerName = iBusHelper.getProviderName(providerId, detail.getOrganisation());
            if (providerId == null || providerId.length() == 0) {
                providerId = detail.getOrganisation();
            }
            item.addElement("ingrid:provider").addText(OpensearchUtil.deNullify(providerId));
            item.addElement("ingrid:provider-name").addText(OpensearchUtil.removeInvalidChars(OpensearchUtil.deNullify(providerName)));

            String partnerId = OpensearchUtil.getDetailValue(detail, "partner");
            String partnerName = iBusHelper.getPartnerName(partnerId, detail.getOrganisation());
            if (partnerId == null || partnerId.length() == 0) {
                partnerId = detail.getOrganisation();
            }
            item.addElement("ingrid:partner").addText(OpensearchUtil.deNullify(partnerId));
            item.addElement("ingrid:partner-name").addText(OpensearchUtil.removeInvalidChars(OpensearchUtil.deNullify(partnerName)));
            item.addElement("ingrid:source").addText(OpensearchUtil.removeInvalidChars(OpensearchUtil.deNullify(detail.getDataSourceName())));

            // handle udk class
            if (udkClass != null && udkClass.length() > 0) {
                item.addElement("ingrid:udk-class").addText(udkClass);
            }
            // handle udk addr class
            if (udkAddrClass != null && udkAddrClass.length() > 0) {
                item.addElement("ingrid:udk-addr-class").addText(udkAddrClass);
            }
            // handle wms url
            if (wmsUrl != null && wmsUrl.length() > 0) {
                item.addElement("ingrid:wms-url").addText(wmsUrl);
            }
            // handle xml csw url
            String metadatatXmlUrl = SearchInterfaceConfig.getInstance().getString(SearchInterfaceConfig.METADATA_ACCESS_URL);
            if (docUuid != null && docUuid.length() > 0 && metadatatXmlUrl != null && metadatatXmlUrl.length() > 0) {
                metadatatXmlUrl = metadatatXmlUrl.replace("{uuid}", docUuid);
                item.addElement("ingrid:iso-xml-url").addText(metadatatXmlUrl);
            }

            // handle time reference
            if (detail.containsKey("t1") || detail.containsKey("t2")) {
                Element timeRef = item.addElement("ingrid:timeReference");
                if (detail.containsKey("t1") && !OpensearchUtil.getDetailValue(detail, "t1").equals("99999999"))
                    timeRef.addElement("ingrid:start").addText(OpensearchUtil.getDetailValue(detail, "t1"));
                if (detail.containsKey("t2") && !OpensearchUtil.getDetailValue(detail, "t2").equals("99999999"))
                    timeRef.addElement("ingrid:stop").addText(OpensearchUtil.getDetailValue(detail, "t2"));
            }

            if (ibusConnected && requestWrapper.getMetadataDetail() && !requestWrapper.getMetadataDetailAsXMLDoc()) {
                Element idfDataNode = null;
                try {
                    if (log.isDebugEnabled()) {
                        log.debug( "Add detail data from IDF..." );
                    }
                    PlugDescription plugDescription = iBusHelper.getPlugdescription(plugId);
                    if (IPlugVersionInspector.getIPlugVersion(plugDescription).equals(IPlugVersionInspector.VERSION_IDF_1_0_DSC_OBJECT)) {
                        // add IDF data
                        Record idfRecord = (Record) detail.get("idfRecord");
                        if (idfRecord == null) {
                            idfRecord = iBusHelper.getRecord(hit);
                        }
                        if (idfRecord != null) {
                            String idfData = IdfTool.getIdfDataFromRecord(idfRecord);
                            Document idfDoc = DocumentHelper.parseText(idfData);
                            idfDataNode = idfDoc.getRootElement();
                            Element details = item.addElement("ingrid:details");
                            details.add(idfDataNode);
                        }
                    } else {
                        if (log.isDebugEnabled()) {
                            log.debug( "Add detail data from record..." );
                        }
                        // generic record data
                        Record record = iBusHelper.getRecord(hit);
                        if (record != null && !record.isEmpty()) {

                            // search for column
                            Column[] columns = record.getColumns();

                            Element details = item.addElement("ingrid:details");

                            for (int i = 0; i < columns.length; i++) {

                                if (columns[i].toIndex()) {
                                    Element detailElement = details.addElement("ingrid:detail");

                                    String columnName = columns[i].getTargetName();
                                    detailElement.addElement("ingrid:detail-key").addText(columnName);
                                    detailElement.addElement("ingrid:detail-value").addText(OpensearchUtil.removeInvalidChars(record.getValueAsString(columns[i]).trim().replaceAll("\n", "<br />")));
                                }
                            }
                            addSubRecords(record, details);
                        }
                    }
                } catch (Exception e) {
                    log.error("Error retrieving detail data from hit {" + hit.getPlugId() + ", " + hit.getDocumentId() + "} serving request " + requestWrapper.getRequest().getQueryString() + ".", e);
                }
            }
        }
    }

    private void addSubRecords(Record record, Element e) {
        Column[] columns;
        Record[] subRecords = record.getSubRecords();

        for (int i = 0; i < subRecords.length; i++) {
            Element subElement = e.addElement("ingrid:detail-subrecord");
            columns = subRecords[i].getColumns();
            for (int j = 0; j < columns.length; j++) {
                if (columns[j].toIndex()) {
                    Element detail = subElement.addElement("ingrid:detail");
                    String columnName = columns[j].getTargetName();
                    detail.addElement("ingrid:detail-key").addText(columnName);
                    detail.addElement("ingrid:detail-value").addText(OpensearchUtil.removeInvalidChars(subRecords[i].getValueAsString(columns[j]).trim().replaceAll("\n", "<br />")));
                }
            }

            addSubRecords(subRecords[i], subElement);
        }
    }

    private void addItemLink(Element item, IngridHit hit, RequestWrapper requestWrapper, boolean ibusConnected) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug( "Add link..." );
        }

        IngridHitDetail detail = (IngridHitDetail) hit.getHitDetail();
        String itemLink = null;

        // check for property "url"
        String url = null;
        if (detail.get("url") != null) {
            if (detail.get("url") instanceof String && ((String) detail.get("url")).length() > 0) {
                url = (String) detail.get("url");
            } else if (detail.get("url") instanceof String[] && ((String[]) detail.get("url")).length > 0) {
                url = ((String[]) detail.get("url"))[0];
            }
        }

        if (url == null) {
            String plugId = hit.getPlugId();
            String docId = String.valueOf(hit.getDocumentId());
            String altDocId = (String) hit.get("alt_document_id");

            String metadataDetailsUrl = SearchInterfaceConfig.getInstance().getString(SearchInterfaceConfig.METADATA_DETAILS_URL, null);
            String docUuid = OpensearchUtil.getDetailValue(detail, "t01_object.obj_id");
            String proxyurl = URLUtil.updateProtocol( SearchInterfaceConfig.getInstance().getString(SearchInterfaceConfig.OPENSEARCH_PROXY_URL, null), requestWrapper.getRequest().getScheme());

            String qStr;
            try {
                qStr = URLEncoder.encode(getDetailQueryString(plugId, docUuid, ibusConnected), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                log.error("Error encoding query.", e);
                throw new RuntimeException(e);
            }

            if (!requestWrapper.getMetadataDetailAsXMLDoc() && metadataDetailsUrl != null && metadataDetailsUrl.length() > 0) {
                itemLink = metadataDetailsUrl.concat("?plugid=").concat(plugId).concat("&docid=").concat(docId).concat("&docuuid=").concat(docUuid);
            } else if (proxyurl != null && proxyurl.length() > 0) {
                itemLink = proxyurl.concat("/query").concat("?q=").concat(qStr).concat("&docid=").concat(docId).concat("&docuuid=").concat(docUuid).concat("&detail=1&ingrid=1");
            } else {
                itemLink = requestWrapper.getRequest().getRequestURL().substring(0, requestWrapper.getRequest().getRequestURL().lastIndexOf("/")).concat("/query").concat("?q=").concat(qStr).concat("&docid=").concat(docId)
                        .concat("&docuuid=").concat(docUuid).concat("&detail=1&ingrid=1");
            }

            if (altDocId != null && altDocId.length() > 0) {
                itemLink = itemLink.concat("&altdocid=").concat(altDocId);
            }

        } else {
            itemLink = url;
        }
        if (itemLink == null) {
            itemLink = "";
        }
        item.addElement("link").addText(itemLink);
    }

    private String getDetailQueryString(String plugId, String docUuid, boolean ibusConnected) throws Exception {

        String qStr = null;
        PlugDescription plugDescription = ibusConnected ? iBusHelper.getPlugdescription(plugId) : null;
        String iPlugVersion = ibusConnected ? IPlugVersionInspector.getIPlugVersion(plugDescription) : "";
        if (iPlugVersion.equals(IPlugVersionInspector.VERSION_UDK_5_0_DSC_ADDRESS)) {
            qStr = IPlugHelper.HIT_KEY_ADDRESS_ADDRID + ":" + docUuid.trim() + " iplugs:\"" + plugId + "\" ranking:score";
        } else if (iPlugVersion.equals(IPlugVersionInspector.VERSION_IDC_1_0_2_DSC_ADDRESS)) {
            qStr = IPlugHelper.HIT_KEY_ADDRESS_ADDRID + ":" + docUuid.trim() + " iplugs:\"" + plugId + "\" ranking:score";
        } else if (iPlugVersion.equals(IPlugVersionInspector.VERSION_UNKNOWN)) {
            qStr = docUuid.trim() + " iplugs:\"" + plugId + "\" ranking:score";
        } else {
            qStr = IPlugHelper.HIT_KEY_OBJ_ID + ":" + docUuid.trim() + " iplugs:\"" + plugId + "\" ranking:score";
        }

        return qStr;

    }

    private void addItemTitle(Element item, IngridHit hit, RequestWrapper requestWrapper, boolean ibusConnected) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug( "Add title..." );
        }
        String plugId = hit.getPlugId();
        String title = null;
        IngridHitDetail detail = (IngridHitDetail) hit.getHitDetail();
        PlugDescription plugDescription = ibusConnected ? iBusHelper.getPlugdescription(plugId) : null;
        if ((plugDescription != null) && (OpensearchUtil.hasDataType(plugDescription, "dsc_ecs_address"))) {
            title = OpensearchUtil.getDetailValue(detail, "t02_address.title");
            title = title.concat(" ").concat(OpensearchUtil.getDetailValue(detail, "t02_address.firstname")).concat(" ");
            title = title.concat(OpensearchUtil.getDetailValue(detail, "t02_address.lastname"));
            title = title.trim(); // remove whitespace
        } 
        if (!OpensearchUtil.hasValue( title )) {
            title = OpensearchUtil.removeInvalidChars(detail.getTitle());
        } 
        item.addElement("title").addText(OpensearchUtil.removeInvalidChars(detail.getTitle()));
    }
    

    @Override
    public String getName() {
        return "OpenSearch";
    }

    @Override
    public String getPathSpec() {
        return "/opensearch/query";
    }

}
