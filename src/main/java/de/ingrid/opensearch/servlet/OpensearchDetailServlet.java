/*
 * Copyright (c) 2006 wemove digital solutions. All rights reserved.
 */
package de.ingrid.opensearch.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.weta.components.communication.server.TooManyRunningThreads;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.mortbay.http.HttpException;

import de.ingrid.opensearch.util.IBusHelper;
import de.ingrid.opensearch.util.IPlugHelper;
import de.ingrid.opensearch.util.IPlugVersionInspector;
import de.ingrid.opensearch.util.OpensearchConfig;
import de.ingrid.opensearch.util.RequestWrapper;
import de.ingrid.utils.IBus;
import de.ingrid.utils.IngridHit;
import de.ingrid.utils.IngridHits;
import de.ingrid.utils.PlugDescription;
import de.ingrid.utils.dsc.Column;
import de.ingrid.utils.dsc.Record;
import de.ingrid.utils.query.IngridQuery;
import de.ingrid.utils.queryparser.ParseException;
import de.ingrid.utils.queryparser.QueryStringParser;

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

    private IBus bus;

    private final static Log log = LogFactory.getLog(OpensearchDetailServlet.class);
    
    /**
     * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
     *      javax.servlet.http.HttpServletResponse)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    	// FIXME AW: doGet is always called twice!
    	// -> this happens only in firefox, cause seems to be the rss-element
    	// -> renaming it helps
    	
        RequestWrapper r = new RequestWrapper(request);
        
        long overallStartTime = 0;
        if (log.isDebugEnabled()) {
            overallStartTime = System.currentTimeMillis();
        }
        
        if (log.isDebugEnabled()) {
            log.debug("incoming query for detail: plugid=" + r.getPlugId() + ", docid=" + r.getDocId() + ", alt_document_id=" + r.getAltDocId() + ".");
        }
        
        IngridHit hit = null;
        PlugDescription plugDescription = null;
        plugDescription = bus.getIPlug(r.getPlugId().trim());
        
        String docUuid = r.getDocUuid();
        if (docUuid != null && docUuid.length() > 0) {
            String qStr = null;
            String iPlugVersion = IPlugVersionInspector.getIPlugVersion(plugDescription);
        	if (iPlugVersion.equals(IPlugVersionInspector.VERSION_IDC_1_0_3_DSC_OBJECT)) {
        		qStr = IPlugHelper.HIT_KEY_OBJ_ID + ":" + docUuid.trim() + " iplugs:\"" + r.getPlugId().trim() + "\" ranking:score";
            } else if (iPlugVersion.equals(IPlugVersionInspector.VERSION_IDC_1_0_2_DSC_OBJECT)) {
        		qStr = IPlugHelper.HIT_KEY_OBJ_ID + ":" + docUuid.trim() + " iplugs:\"" + r.getPlugId().trim() + "\" ranking:score";
            } else if (iPlugVersion.equals(IPlugVersionInspector.VERSION_UDK_5_0_DSC_OBJECT)) {
        		qStr = IPlugHelper.HIT_KEY_OBJ_ID + ":" + docUuid.trim() + " iplugs:\"" + r.getPlugId().trim() + "\" ranking:score";
            } else if (iPlugVersion.equals(IPlugVersionInspector.VERSION_UDK_5_0_DSC_ADDRESS)) {
        		qStr = IPlugHelper.HIT_KEY_ADDRESS_ADDRID + ":" + docUuid.trim() + " iplugs:\"" + r.getPlugId().trim() + "\" ranking:score";
            } else if (iPlugVersion.equals(IPlugVersionInspector.VERSION_IDC_1_0_2_DSC_ADDRESS)) {
        		qStr = IPlugHelper.HIT_KEY_ADDRESS_ADDRID + ":" + docUuid.trim() + " iplugs:\"" + r.getPlugId().trim() + "\" ranking:score";
            } else {
        		qStr = docUuid.trim() + " iplugs:\"" + r.getPlugId().trim() + "\" ranking:score";
            }
        	IngridHits hits;
			try {
				IngridQuery q = QueryStringParser.parse(qStr);
				IBusHelper.injectCache(q);
				hits = bus.search(q, 1, 1, 0, 3000);
			} catch (ParseException e) {
				log.error("Error parsing query.", e);
				throw (HttpException) new HttpException(500).initCause(e);
			} catch (Exception e) {
				log.error("Error query the iBus.", e);
				throw (HttpException) new HttpException(500).initCause(e);
			}
        	if (hits.length() < 1) {
        		log.error("No record found for document uuid:" + docUuid + " using iplug: " + r.getPlugId().trim());
        		throw (HttpException) new HttpException(404, "No record found for document uuid:" + docUuid + " using iplug: " + r.getPlugId().trim());
        	} else {
        		hit = hits.getHits()[0];
        	}
        } else {
            hit = new IngridHit();
            hit.setDocumentId(r.getDocId());
            hit.setPlugId(r.getPlugId());
        }
        if (r.getAltDocId() != null) {
            hit.put("alt_document_id", r.getAltDocId());
        }
        

        // transform IngridHit to XML
        request.setCharacterEncoding("UTF-8");
        // response.setContentType("application/rss+xml");
        response.setContentType("text/xml");

               
        String proxyurl = OpensearchConfig.getInstance().getString(OpensearchConfig.PROXY_URL, null);
        String url = null;
        String queryString = request.getQueryString();
        if (queryString == null) queryString = "";
        queryString.replace("+", "%2B");
        if (proxyurl != null && proxyurl.trim().length() > 0) {
            url = proxyurl.concat("/detail").concat("?").concat(queryString);
        } else {
            url = request.getRequestURL().toString().concat("?").concat(queryString);
        }
        
        Document doc = DocumentHelper.createDocument();
        Element root = doc.addElement("rss");
        root.addNamespace("opensearch", "http://a9.com/-/spec/opensearch/1.1/");
        root.addNamespace("ingridsearch", "http://www.wemove.com/ingrid/opensearchextension/0.1/");
        root.addAttribute("version", "2.0");

        Element channel = root.addElement("channel");
        channel.addElement("title").addText("portalu.de OpenSearch: show detail");
        channel.addElement("link").addText(StringEscapeUtils.escapeXml(url));
        channel.addElement("description").addText("Search results");
        channel.addElement("totalResults", "opensearch").addText("1");
        channel.addElement("startIndex", "opensearch").addText("1");
        channel.addElement("itemsPerPage", "opensearch").addText("1");
        channel.addElement("Query", "opensearch").addAttribute("role", "request").addAttribute("searchTerms",
                "show details");
        Element item = channel.addElement("item");
        item.addElement("link").addText(StringEscapeUtils.escapeXml(url));
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
        } catch (TooManyRunningThreads e) {
            throw (HttpException) new HttpException(503, "Too many threads!").initCause(e);
        } catch (Throwable t) {
            throw (HttpException) new HttpException(500).initCause(t);
        }

        PrintWriter pout = response.getWriter();
        pout.write(doc.asXML());
        pout.close();
        request.getInputStream().close();
        doc.clearContent();
        
        if (log.isDebugEnabled()) {
            log.debug("Time for complete detail: " + (System.currentTimeMillis() - overallStartTime) + " ms");
        }
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
           	bus = IBusHelper.getIBus();
        } catch (Exception e) {
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
