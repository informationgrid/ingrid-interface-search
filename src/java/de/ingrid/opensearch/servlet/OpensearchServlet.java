/*
 * Copyright (c) 2006 wemove digital solutions. All rights reserved.
 */
package de.ingrid.opensearch.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.util.ArrayList;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import de.ingrid.ibus.client.BusClient;
import de.ingrid.opensearch.util.OpensearchConfig;
import de.ingrid.opensearch.util.RequestWrapper;
import de.ingrid.utils.IBus;
import de.ingrid.utils.IngridHit;
import de.ingrid.utils.IngridHitDetail;
import de.ingrid.utils.IngridHits;
import de.ingrid.utils.PlugDescription;
import de.ingrid.utils.query.IngridQuery;

/**
 * Servlet handles OpenSearch queries.
 * 
 * @author joachim@wemove.com
 */
public class OpensearchServlet extends HttpServlet {

    private static final long serialVersionUID = 597250457306006899L;

    private BusClient client;

    private IBus bus;

    private final static Log log = LogFactory.getLog(OpensearchServlet.class);

    /**
     * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
     *      javax.servlet.http.HttpServletResponse)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String[] requestedMetadata = null;

        RequestWrapper r = new RequestWrapper(request);

        if (log.isDebugEnabled()) {
            log.debug("incoming query: " + r.getQueryString());
        }

        IngridQuery query = r.getQuery();
        int page = r.getRequestedPage();
        int hitsPerPage = r.getHitsPerPage();

        if (!hasPositiveDataType(query, "address")) {
            requestedMetadata = new String[4];
            requestedMetadata[0] = "T011_obj_serv_op_connpoint.connect_point";
            requestedMetadata[1] = "T01_object.obj_class";
            requestedMetadata[2] = "partner";
            requestedMetadata[3] = "provider";
        } else {
            requestedMetadata = new String[9];
            requestedMetadata[0] = "T011_obj_serv_op_connpoint.connect_point";
            requestedMetadata[1] = "T02_address.typ";
            requestedMetadata[2] = "T02_address.firstname";
            requestedMetadata[3] = "T02_address.lastname";
            requestedMetadata[4] = "T02_address.title";
            requestedMetadata[5] = "T02_address.address";
            requestedMetadata[6] = "T02_address.adr_id";
            requestedMetadata[7] = "partner";
            requestedMetadata[8] = "provider";
        }

        // search
        client = BusClient.instance();
        bus = client.getBus();
        IngridHits hits = null;
        IngridHitDetail[] details = null;
        try {
            hits = bus.search(query, hitsPerPage, page, hitsPerPage, 60000);
            details = bus.getDetails(hits.getHits(), query, requestedMetadata);
            for (int i = 0; i < hits.getHits().length; i++) {
                hits.getHits()[i].put("detail", details[i]);
            }
        } catch (Exception e) {
            throw new ServletException(e);
        }

        // transform IngridHit to XML
        request.setCharacterEncoding("UTF-8");
        // response.setContentType("application/rss+xml");
        response.setContentType("text/xml");

        Document doc = DocumentHelper.createDocument();
        Element root = doc.addElement("rss");
        root.addNamespace("opensearch", "http://a9.com/-/spec/opensearch/1.1/");
        root.addNamespace("ingridsearch", "http://www.wemove.com/ingrid/opensearchextension/0.1/");
        root.addAttribute("version", "2.0");

        Element channel = root.addElement("channel");
        channel.addElement("title").addText("ingrid OpenSearch: " + r.getQueryString());

        String proxyurl = OpensearchConfig.getInstance().getString(OpensearchConfig.PROXY_URL, null);
        String url = null;
        if (proxyurl != null && proxyurl.trim().length() > 0) {
            url = proxyurl.concat("/query").concat("?").concat(request.getQueryString());
        } else {
            url = request.getRequestURL().toString().concat("?").concat(request.getQueryString());
        }

        String metadataDetailsUrl = OpensearchConfig.getInstance().getString(OpensearchConfig.METADATA_DETAILS_URL,
                null);

        channel.addElement("link").addText(url);
        channel.addElement("description").addText("Search results");
        channel.addElement("totalResults", "opensearch").addText(String.valueOf(hits.length()));
        channel.addElement("startIndex", "opensearch").addText(String.valueOf(r.getRequestedPage()));
        channel.addElement("itemsPerPage", "opensearch").addText(String.valueOf(r.getHitsPerPage()));
        channel.addElement("Query", "opensearch").addAttribute("role", "request").addAttribute("searchTerms",
                r.getQueryString());
        for (int i = 0; i < hits.getHits().length; i++) {
            IngridHit hit = hits.getHits()[i];
            IngridHitDetail detail = (IngridHitDetail) hit.get("detail");
            String iplugClass = detail.getIplugClassName();
            String plugId = hit.getPlugId();
            String docId = String.valueOf(hit.getDocumentId());
            String altDocId = r.getAltDocId();
            String udkClass = null;
            String udkAddrClass = null;
            String wmsURL = null;
            Element item = channel.addElement("item");
            String itemUrl = null;
            if (iplugClass != null
                    && (iplugClass.equals("de.ingrid.iplug.dsc.index.DSCSearcher")
                            || iplugClass.equals("de.ingrid.iplug.udk.UDKPlug")
                            || iplugClass.equals("de.ingrid.iplug.udk.CSWPlug") || iplugClass
                            .equals("de.ingrid.iplug.tamino.TaminoSearcher"))) {
                // handle the title
                PlugDescription plugDescription = bus.getIPlug(plugId);
                if (hasDataType(plugDescription, "dsc_ecs_address")) {
                    String title = getDetailValue(detail, "T02_address.title");
                    title = title.concat(" ").concat(getDetailValue(detail, "T02_address.firstname")).concat(" ");
                    title = title.concat(getDetailValue(detail, "T02_address.lastname"));
                    item.addElement("title").addText(title.trim());
                } else {
                    item.addElement("title").addText(detail.getTitle());
                }

                if (detail.get("url") != null) {
                    itemUrl = (String) detail.get("url");
                } else if (!r.getMetadataDetailAsXML() && metadataDetailsUrl != null && metadataDetailsUrl.length() > 0) {
                    itemUrl = metadataDetailsUrl.concat("?plugid=").concat(plugId).concat("&docid=").concat(docId);
                } else if (proxyurl != null && proxyurl.length() > 0) {
                    itemUrl = proxyurl.concat("/detail").concat("?plugid=").concat(plugId).concat("&docid=").concat(
                            docId);
                } else {
                    itemUrl = request.getRequestURL().substring(0, request.getRequestURL().lastIndexOf("/")).concat(
                            "/detail").concat("?plugid=").concat(plugId).concat("&docid=").concat(docId);
                }

                if (altDocId != null && altDocId.length() > 0) {
                    itemUrl.concat("&altdocid=").concat(altDocId);
                }

                udkClass = getDetailValue(detail, "T01_object.obj_class");
                udkAddrClass = getDetailValue(detail, "T02_address.typ");
                Object obj = detail.get("T011_obj_serv_op_connpoint.connect_point");
                if (obj instanceof String[]) {
                    wmsURL = ((String[]) obj)[0];
                } else {
                    wmsURL = getDetailValue(detail, "T011_obj_serv_op_connpoint.connect_point");
                }

            } else {
                // handle the title (default)
                item.addElement("title").addText(detail.getTitle());
                itemUrl = (String) detail.get("url");
            }
            if (itemUrl == null) {
                itemUrl = "";
            }
            item.addElement("link").addText(URLEncoder.encode(itemUrl, "UTF-8"));
            item.addElement("description").addText(detail.getSummary());
            item.addElement("plugid", "ingridsearch").addText(plugId);
            item.addElement("docid", "ingridsearch").addText(docId);
            if (altDocId != null && altDocId.length() > 0) {
                item.addElement("altdocid", "ingridsearch").addText(altDocId);
            }
            String provider = getDetailValue(detail, "provider");
            if (provider == null) {
                provider = detail.getOrganisation();
            }
            item.addElement("provider", "ingridsearch").addText(provider);

            String partner = getDetailValue(detail, "partner");
            item.addElement("partner", "ingridsearch").addText(partner);
            item.addElement("source", "ingridsearch").addText(detail.getDataSourceName());

            // handle udk class
            if (udkClass != null && udkClass.length() > 0) {
                item.addElement("udk-class", "ingridsearch").addText(udkClass);
            }
            // handle udk addr class
            if (udkAddrClass != null && udkAddrClass.length() > 0) {
                item.addElement("udk-addr-class", "ingridsearch").addText(udkAddrClass);
            }
            // handle wms url
            if (wmsURL != null && wmsURL.length() > 0) {
                item.addElement("wms-url", "ingridsearch").addText(URLEncoder.encode(wmsURL, "UTF-8"));
            }
        }

        PrintWriter pout = response.getWriter();

        pout.write(doc.asXML());
        pout.close();
        request.getInputStream().close();
        doc.clearContent();
    }

    /**
     * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest,
     *      javax.servlet.http.HttpServletResponse)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException,
            IOException {
        doGet(request, response);
    }

    /**
     * @see javax.servlet.GenericServlet#init(javax.servlet.ServletConfig)
     */
    public void init(ServletConfig arg0) throws ServletException {
        try {
            client = BusClient.instance();
            bus = client.getBus();
        } catch (IOException e) {
            throw new ServletException(e);
        }
        super.init(arg0);
    }

    /**
     * Private method handling all Detail Fetching
     * 
     * @param detail
     * @param key
     * @return
     */
    private String getDetailValue(IngridHit detail, String key) {
        Object obj = detail.get(key);
        if (obj == null) {
            return "";
        }

        StringBuffer values = new StringBuffer();
        if (obj instanceof String[]) {
            String[] valueArray = (String[]) obj;
            for (int i = 0; i < valueArray.length; i++) {
                if (i != 0) {
                    values.append(", ");
                }
                values.append(valueArray[i]);
            }
        } else if (obj instanceof ArrayList) {
            ArrayList valueList = (ArrayList) obj;
            for (int i = 0; i < valueList.size(); i++) {
                if (i != 0) {
                    values.append(", ");
                }
                values.append(valueList.get(i).toString());
            }
        } else {
            values.append(obj.toString());
        }
        return values.toString();
    }

    private boolean hasPositiveDataType(IngridQuery q, String datatype) {
        String[] dtypes = q.getPositiveDataTypes();
        for (int i = 0; i < dtypes.length; i++) {
            if (dtypes[i].equalsIgnoreCase(datatype)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Return true if the given iPlug has a specific data type.
     * 
     * @param iPlug
     *            The PlugDescription to work on.
     * @param dataType
     *            The data type to search for
     * @return True if the iPlug has the data type, false if not.
     */
    private boolean hasDataType(PlugDescription iPlug, String dataType) {
        String[] dataTypes = iPlug.getDataTypes();
        for (int i = 0; i < dataTypes.length; i++) {
            if (dataTypes[i].equalsIgnoreCase(dataType)) {
                return true;
            }
        }
        return false;
    }

}
