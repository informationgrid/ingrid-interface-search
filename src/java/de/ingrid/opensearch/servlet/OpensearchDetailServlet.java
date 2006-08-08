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

import com.thoughtworks.xstream.XStream;

import de.ingrid.ibus.client.BusClient;
import de.ingrid.opensearch.util.RequestWrapper;
import de.ingrid.utils.IBus;
import de.ingrid.utils.IngridHitDetail;
import de.ingrid.utils.IngridHits;
import de.ingrid.utils.query.IngridQuery;

/**
 * TODO Describe your created type (class, etc.) here.
 * 
 * @author joachim@wemove.com
 */
public class OpensearchDetailServlet extends HttpServlet {

    /**
     * TODO: Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 597250457306006899L;

    private BusClient client;

    private IBus bus;

    private XStream xstream;

    /**
     * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
     *      javax.servlet.http.HttpServletResponse)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        RequestWrapper r = new RequestWrapper(request);

        IngridQuery query = r.getQuery();
        int page = r.getRequestedPage();
        int hitsPerPage = r.getHitsPerPage();

        // switch ranking ON
        query.put(IngridQuery.RANKED, IngridQuery.SCORE_RANKED);

        client = BusClient.instance();
        bus = client.getBus();
        IngridHits hits = null;
        IngridHitDetail[] details = null;
        try {
            hits = bus.search(query, hitsPerPage, page, hitsPerPage, 60000);
            details = bus.getDetails(hits.getHits(), query, null);
            for (int i = 0; i < hits.getHits().length; i++) {
                hits.getHits()[i].put("detail", details[i]);
            }
        } catch (Exception e) {
            throw new ServletException(e);
        }

        // transform IngridHit to XML

        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/rss+xml");

        PrintWriter pout = response.getWriter();

        pout.write(xstream.toXML(hits));
        pout.close();
        request.getInputStream().close();

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

}
