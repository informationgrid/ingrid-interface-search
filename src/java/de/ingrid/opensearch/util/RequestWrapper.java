/*
 * Copyright (c) 2006 wemove digital solutions. All rights reserved.
 */
package de.ingrid.opensearch.util;

import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import de.ingrid.utils.query.IngridQuery;
import de.ingrid.utils.queryparser.QueryStringParser;

/**
 * TODO Describe your created type (class, etc.) here.
 * 
 * @author joachim@wemove.com
 */
public class RequestWrapper extends HashMap {

    /**
     * TODO: Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 2153326583527416149L;

    public static final String PAGE = "page";

    public static final String METADATA_DETAIL_AS_XML = "xml";
    
    public static final String HITS_PER_PAGE = "hitsPerPage";

    public static final String QUERY = "query";
    public static final String QUERY_STR = "query_str";
    
    public static final String PLUG_ID = "iplug_id";
    public static final String DOC_ID = "doc_id";
    public static final String ALT_DOC_ID = "alt_doc_id";
    

    public RequestWrapper(HttpServletRequest request) throws ServletException {

        // get result page to display
        int page = 1;
        try {
            page = Integer.parseInt(request.getParameter("p"));
        } catch (NumberFormatException e) {
        }
        if (page <= 0) {
            page = 1;
        }
        this.put(RequestWrapper.PAGE, new Integer(page));

        // get number of hits for result page
        int maxRequestedHits = OpensearchConfig.getInstance().getInt(OpensearchConfig.MAX_REQUESTED_HITS, 10);
        int requestedHits = 10;
        try {
            requestedHits = Integer.parseInt(request.getParameter("h"));
        } catch (NumberFormatException e) {
        }
        if (requestedHits <= 0) {
            requestedHits = 1;
        } else if (requestedHits > maxRequestedHits) {
            requestedHits = maxRequestedHits;
        }
        this.put(RequestWrapper.HITS_PER_PAGE, new Integer(requestedHits));

        // get doc id
        int docId = 0;
        try {
            docId = Integer.parseInt(request.getParameter("docid"));
        } catch (NumberFormatException e) {
        }
        this.put(RequestWrapper.DOC_ID, new Integer(docId));

        // parameter for detail data display
        int metadataDetailAsXML = 0;
        try {
            metadataDetailAsXML = Integer.parseInt(request.getParameter(METADATA_DETAIL_AS_XML));
        } catch (NumberFormatException e) {
        }
        this.put(RequestWrapper.METADATA_DETAIL_AS_XML, new Integer(metadataDetailAsXML));
        
        // get alternative doc id
        String altDocId = null;
        altDocId = request.getParameter("altdocid");
        this.put(RequestWrapper.ALT_DOC_ID, altDocId);

        
        // get plug id
        String plugId = null;
        plugId = request.getParameter("plugid");
        this.put(RequestWrapper.PLUG_ID, plugId);
        
        
        // get query
        IngridQuery query = null;
        String queryString = request.getParameter("q");
        if (queryString == null) {
            queryString = "";
        }
        this.put(RequestWrapper.QUERY_STR, queryString);
        try {
            query = QueryStringParser.parse(queryString);
        } catch (Throwable t) {
            throw new ServletException(t);
        }
        this.put(RequestWrapper.QUERY, query);
    }

    public int getHitsPerPage() {
        return ((Integer) this.get(RequestWrapper.HITS_PER_PAGE)).intValue();
    }

    public int getRequestedPage() {
        return ((Integer) this.get(RequestWrapper.PAGE)).intValue();
    }

    public int getDocId() {
        return ((Integer) this.get(RequestWrapper.DOC_ID)).intValue();
    }
    
    public String getAltDocId() {
        return (String) this.get(RequestWrapper.ALT_DOC_ID);
    }

    public String getPlugId() {
        return (String) this.get(RequestWrapper.PLUG_ID);
    }

    public boolean getMetadataDetailAsXML() {
        return (((Integer) this.get(RequestWrapper.METADATA_DETAIL_AS_XML)).intValue() == 1);
    }
    
    public IngridQuery getQuery() {
        return (IngridQuery) this.get(RequestWrapper.QUERY);
    }

    public String getQueryString() {
        return (String) this.get(RequestWrapper.QUERY_STR);
    }

}
