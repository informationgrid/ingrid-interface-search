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
public class RequestWrapper extends HashMap<String, Object> {

    /**
     * TODO: Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID 			= 2153326583527416149L;

    public static final String PAGE 					= "page";

    public static final String METADATA_DETAIL_AS_XML 	= "xml";
    
    public static final String HITS_PER_PAGE 			= "hitsPerPage";

    public static final String QUERY 					= "query";
    public static final String QUERY_STR 				= "query_str";
    
    public static final String PLUG_ID 					= "iplug_id";
    public static final String DOC_ID 					= "doc_id";
    public static final String DOC_UUID 				= "doc_uuid";
    public static final String ALT_DOC_ID 				= "alt_doc_id";
    
    public static final String GEORSS 					= "georss";
    
    public static final String INGRID_DATA 				= "ingrid";
    
    public static final String SEARCH_TIMEOUT 			= "timeout";

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

        // get doc uuid
        String docUuid = null;
        docUuid = request.getParameter("docuuid");
        this.put(RequestWrapper.DOC_UUID, docUuid);
        
        
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

        mapIntParamToBoolean(request, "xml",    RequestWrapper.METADATA_DETAIL_AS_XML);
        mapIntParamToBoolean(request, "georss", RequestWrapper.GEORSS);
        mapIntParamToBoolean(request, "ingrid", RequestWrapper.INGRID_DATA);
        
        // get search timeout
        int searchTimeout = 0;
        try {
        	searchTimeout = Integer.parseInt(request.getParameter("t"));
        } catch (NumberFormatException e) {
        }
        if (searchTimeout <= 0) {
        	searchTimeout = 0;
        }
        this.put(RequestWrapper.SEARCH_TIMEOUT, new Integer(searchTimeout));
        
        
    }

    /**
     * Map 'param' from the request, which is expected to be an integer, to a boolean
     * value under the parameter name 'mappedParam'. If the param doesn't exist, no entry
     * will be made in the HashMap.
     * 
     * @param request, is the HTTPRequest which contains the parameter
     * @param param, is the parameter to look for
     * @param mappedParam, is the mapped name of the converted parameter, containing a boolean value
     */
    private void mapIntParamToBoolean(HttpServletRequest request, String param, String mappedParam) {
    	int paramValue = 0;
        try {
        	paramValue = Integer.parseInt(request.getParameter(param));
        } catch (NumberFormatException e) {}
        if (paramValue == 1) {
        	this.put(mappedParam, new Boolean(true));
        } else {
        	this.put(mappedParam, new Boolean(false));
        }
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

    public String getDocUuid() {
        return (String) this.get(RequestWrapper.DOC_UUID);
    }
    
    public String getAltDocId() {
        return (String) this.get(RequestWrapper.ALT_DOC_ID);
    }

    public String getPlugId() {
        return (String) this.get(RequestWrapper.PLUG_ID);
    }

    public boolean getMetadataDetailAsXML() {
        return (Boolean) this.get(RequestWrapper.METADATA_DETAIL_AS_XML);
    }
    
    public IngridQuery getQuery() {
        return (IngridQuery) this.get(RequestWrapper.QUERY);
    }

    public String getQueryString() {
        return (String) this.get(RequestWrapper.QUERY_STR);
    }
    
    public boolean withGeoRSS() {
    	return (Boolean) this.get(GEORSS);
    }
    
    public boolean withIngridData() {
    	return (Boolean) this.get(INGRID_DATA);
    }
    
    public int getSearchTimeout() {
        return ((Integer) this.get(RequestWrapper.SEARCH_TIMEOUT)).intValue();
    }
    

}
