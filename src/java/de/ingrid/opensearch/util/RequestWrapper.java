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

    public static final String HITS_PER_PAGE = "hitsPerPage";

    public static final String GET_DETAILS = "getDetails";

    public static final String QUERY = "query";

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
        int requestedHits = 10;
        try {
            requestedHits = Integer.parseInt(request.getParameter("h"));
        } catch (NumberFormatException e) {
        }
        if (requestedHits <= 0) {
            requestedHits = 1;
        } else if (requestedHits > 10) {
            requestedHits = 10;
        }
        this.put(RequestWrapper.HITS_PER_PAGE, new Integer(requestedHits));

        // get details mode
        int getDetails = 0;
        try {
            getDetails = Integer.parseInt(request.getParameter("d"));
        } catch (NumberFormatException e) {
        }
        if (getDetails > 0) {
            this.put(RequestWrapper.GET_DETAILS, new Boolean(true));
        } else {
            this.put(RequestWrapper.GET_DETAILS, new Boolean(false));
        }

        // get query
        IngridQuery query = null;
        try {
            query = QueryStringParser.parse(request.getParameter("q"));
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

    public boolean getRequestedDetails() {
        return ((Boolean) this.get(RequestWrapper.GET_DETAILS)).booleanValue();
    }

    public IngridQuery getQuery() {
        return (IngridQuery) this.get(RequestWrapper.QUERY);
    }

}
