/*
 * Copyright (c) 2006 wemove digital solutions. All rights reserved.
 */
package de.ingrid.opensearch.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;

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
import de.ingrid.utils.dsc.Column;
import de.ingrid.utils.dsc.Record;

/**
 * TODO Describe your created type (class, etc.) here.
 * 
 * @author joachim@wemove.com
 */
public class OpensearchDetailServlet extends HttpServlet {

    /**
     * TODO: Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 597250457306006799L;

    private BusClient client;

    private IBus bus;

    private final static Log log = LogFactory.getLog(OpensearchDetailServlet.class);
    
    /**
     * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
     *      javax.servlet.http.HttpServletResponse)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        RequestWrapper r = new RequestWrapper(request);

        if (log.isDebugEnabled()) {
            log.debug("incoming query for detail: plugid=" + r.getPlugId() + ", docid=" + r.getDocId() + ", alt_document_id=" + r.getAltDocId() + ".");
        }
        
        IngridHit hit = new IngridHit();
        hit.setDocumentId(r.getDocId());
        hit.setPlugId(r.getPlugId());
        if (r.getAltDocId() != null) {
            hit.put("alt_document_id", r.getAltDocId());
        }

        // transform IngridHit to XML
        request.setCharacterEncoding("UTF-8");
        // response.setContentType("application/rss+xml");
        response.setContentType("text/xml");

        
        String proxyurl = OpensearchConfig.getInstance().getString(OpensearchConfig.PROXY_URL, null);
        String url = null;
        if (proxyurl != null && proxyurl.trim().length() > 0) {
            url = proxyurl.concat("/query").concat("?").concat(request.getQueryString());
        } else {
            url = request.getRequestURL().toString().concat("?").concat(request.getQueryString());
        }
        
        Document doc = DocumentHelper.createDocument();
        Element root = doc.addElement("rss");
        root.addNamespace("opensearch", "http://a9.com/-/spec/opensearch/1.1/");
        root.addNamespace("ingridsearch", "http://www.wemove.com/ingrid/opensearchextension/0.1/");
        root.addAttribute("version", "2.0");

        Element channel = root.addElement("channel");
        channel.addElement("title").addText("portalu.de OpenSearch: show detail");
        channel.addElement("link").addText(URLEncoder.encode(url, "UTF-8"));
        channel.addElement("description").addText("Search results");
        channel.addElement("totalResults", "opensearch").addText("1");
        channel.addElement("startIndex", "opensearch").addText("1");
        channel.addElement("itemsPerPage", "opensearch").addText("1");
        channel.addElement("Query", "opensearch").addAttribute("role", "request").addAttribute("searchTerms",
                "show details");
        Element item = channel.addElement("item");
        item.addElement("link").addText(URLEncoder.encode(url, "UTF-8"));
        item.addElement("description").addText("detail data of the search result.");
        item.addElement("plugid", "ingridsearch").addText(r.getPlugId());
        item.addElement("docid", "ingridsearch").addText(String.valueOf(r.getDocId()));
        if (r.getAltDocId() != null && r.getAltDocId().length() > 0) {
            item.addElement("altdocid", "ingridsearch").addText(r.getAltDocId());
        }

        try {
            Record record = bus.getRecord(hit);

            // search for column
            Column[] columns = record.getColumns();

            Element details = item.addElement("details", "ingridsearch");

            for (int i = 0; i < columns.length; i++) {

                if (columns[i].toIndex()) {
                    Element detail = details.addElement("detail", "ingridsearch");

                    String columnName = columns[i].getTargetName();
                    detail.addElement("detail-key", "ingridsearch").addText(columnName);
                    detail.addElement("detail-value", "ingridsearch").addText(
                            record.getValueAsString(columns[i]).trim().replaceAll("\n", "<br />"));
                }
            }

            addSubRecords(record, details);
        } catch (Throwable t) {
            t.printStackTrace();
        }

        PrintWriter pout = response.getWriter();
        pout.write(doc.asXML());
        pout.close();
        request.getInputStream().close();
        doc.clearContent();

        super.doGet(request, response);
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

    private void addSubRecords(Record record, Element e) {
        Column[] columns;
        Record[] subRecords = record.getSubRecords();

        for (int i = 0; i < subRecords.length; i++) {
            Element subElement = e.addElement("detail-subrecord", "ingridsearch");
            columns = subRecords[i].getColumns();
            for (int j = 0; j < columns.length; j++) {
                if (columns[j].toIndex()) {
                    Element detail = subElement.addElement("detail", "ingridsearch");
                    String columnName = columns[j].getTargetName();
                    detail.addElement("detail-key", "ingridsearch").addText(columnName);
                    detail.addElement("detail-value", "ingridsearch").addText(
                            subRecords[i].getValueAsString(columns[j]).trim().replaceAll("\n", "<br />"));
                }
            }

            addSubRecords(subRecords[i], subElement);
        }
    }

}
