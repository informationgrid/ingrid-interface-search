/*
 * Copyright (c) 2006 wemove digital solutions. All rights reserved.
 */
package de.ingrid.opensearch.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;

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
import org.mortbay.http.HttpException;

import de.ingrid.opensearch.util.IBusHelper;
import de.ingrid.opensearch.util.OpensearchConfig;
import de.ingrid.opensearch.util.RequestWrapper;
import de.ingrid.utils.IBus;
import de.ingrid.utils.IngridHit;
import de.ingrid.utils.IngridHitDetail;
import de.ingrid.utils.IngridHits;
import de.ingrid.utils.PlugDescription;
import de.ingrid.utils.query.FieldQuery;
import de.ingrid.utils.query.IngridQuery;

/**
 * Servlet handles OpenSearch queries.
 * 
 * @author joachim@wemove.com
 */
public class OpensearchServlet extends HttpServlet {

    private static final long serialVersionUID 	= 597250457306006899L;
    
    private IBus bus;

    private final static Log log = LogFactory.getLog(OpensearchServlet.class);

    /**
     * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
     *      javax.servlet.http.HttpServletResponse)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        long overallStartTime = 0;
        if (log.isDebugEnabled()) {
            overallStartTime = System.currentTimeMillis();
        }
        
        String[] requestedMetadata = null;

        RequestWrapper r = new RequestWrapper(request);

        if (log.isDebugEnabled()) {
            log.debug("incoming query: " + r.getQueryString());
        }

        IngridQuery query = r.getQuery();
        int page = r.getRequestedPage();
        int hitsPerPage = r.getHitsPerPage();
        if (page <= 0)
            page = 1;
        int startHit = (page-1) *  hitsPerPage;
        
        if (!hasPositiveDataType(query, "address")) {
            requestedMetadata = new String[] {
            		"t01_object.obj_id"
            		};
            // check if GeoRSS data shall be checked too
            if (r.withGeoRSS()) {
    	        String[] additional = new String[] {"x1","x2","y1","y2"}; 
    	        requestedMetadata = (String[])ArrayUtils.addAll(requestedMetadata, additional);
    	    }
            if (r.withIngridData()) {
            	String[] additional = new String[] {"T01_object.obj_class","T011_obj_serv_op_connpoint.connect_point","partner","provider","t1","t2"}; 
    	        requestedMetadata = (String[])ArrayUtils.addAll(requestedMetadata, additional);
            }
        } else {
        	requestedMetadata = new String[] {
            		"T02_address.firstname", 
            		"T02_address.lastname", 
            		"T02_address.title", 
            		"T02_address.address", 
            		"T02_address.adr_id"
            		};
        	if (r.withIngridData()) {
            	String[] additional = new String[] {"T02_address.typ","T011_obj_serv_op_connpoint.connect_point","partner","provider","t1","t2"}; 
    	        requestedMetadata = (String[])ArrayUtils.addAll(requestedMetadata, additional);
            }
        }

        IngridHits hits = null;
        long startTime = 0;
        try {
            if (log.isDebugEnabled()) {
                startTime = System.currentTimeMillis();
            }
            IBusHelper.injectCache(query);
            
            hits = bus.searchAndDetail(query, hitsPerPage, page, startHit, 60000, requestedMetadata);
            
            if (log.isDebugEnabled()) {
                log.debug("Time for searchAndDetail: " + (System.currentTimeMillis() - startTime) + " ms");
            }

        } catch (TooManyRunningThreads e) {
            throw (HttpException) new HttpException(503, "Too many threads!").initCause(e);
        } catch (Exception e) {
            log.error("Error serving request", e);
        	throw (HttpException) new HttpException(500, "Internal error!").initCause(e);
        }

		// transform IngridHit to XML
        request.setCharacterEncoding("UTF-8");
        // response.setContentType("application/rss+xml");
        response.setContentType("text/xml");
        
        Document doc = createXMLDocumentFromIngrid(request, r, hits, false);

        PrintWriter pout = response.getWriter();

        pout.write(doc.asXML());
        pout.close();
        request.getInputStream().close();
        doc.clearContent();

        if (log.isDebugEnabled()) {
            log.debug("Time for complete search: " + (System.currentTimeMillis() - overallStartTime) + " ms");
        }
    }

    // TODO: add proxy URL as a parameter, which can come from a plugDescription (server-opensearch)!
	public Document createXMLDocumentFromIngrid(HttpServletRequest request,
			RequestWrapper reqWrapper,
			IngridHits hits,
			boolean noIBusUsed)
			throws UnsupportedEncodingException {

		long startTime = -1;
		HashMap<String, PlugDescription> plugIds = new HashMap<String, PlugDescription>(10);

        Document doc = DocumentHelper.createDocument();
        Element root = doc.addElement("rss");
        root.addNamespace("opensearch", "http://a9.com/-/spec/opensearch/1.1/");
        root.addNamespace("relevance", "http://a9.com/-/opensearch/extensions/relevance/1.0/");
        if (reqWrapper.withIngridData()) {
        	root.addNamespace("ingrid", "http://www.portalu.de/opensearch/extension/1.0");
        }
        if (reqWrapper.withGeoRSS()) {
        	root.addNamespace("georss", "http://www.georss.org/georss");
        }
        root.addAttribute("version", "2.0");

        Element channel = root.addElement("channel");
        channel.addElement("title").addText("ingrid OpenSearch: " + reqWrapper.getQueryString());

        String proxyurl = OpensearchConfig.getInstance().getString(OpensearchConfig.PROXY_URL, null);
        String url = null;
        String queryString = request.getQueryString();
        if (queryString == null) queryString = "";
        queryString.replace("+", "%2B");
        if (proxyurl != null && proxyurl.trim().length() > 0) {
            url = proxyurl.concat("/query").concat("?").concat(queryString);
        } else {
            url = request.getRequestURL().toString().concat("?").concat(queryString);
        }

        String metadataDetailsUrl = OpensearchConfig.getInstance().getString(OpensearchConfig.METADATA_DETAILS_URL,
                null);

        channel.addElement("link").addText(url);
        channel.addElement("description").addText("Search results");
        channel.addElement("opensearch:totalResults").addText(String.valueOf(hits.length()));
        channel.addElement("opensearch:startIndex").addText(String.valueOf(reqWrapper.getRequestedPage()));
        channel.addElement("opensearch:itemsPerPage").addText(String.valueOf(reqWrapper.getHitsPerPage()));
        channel.addElement("opensearch:Query").addAttribute("role", "request").addAttribute("searchTerms",
                reqWrapper.getQueryString());
        if (reqWrapper.withIngridData()) {
        	// TODO: channel.addElement("ingrid:keyword").addText(r.getQueryString());
        }
        
        if (log.isDebugEnabled()) {
            startTime = System.currentTimeMillis();
        }
        for (int i = 0; i < hits.getHits().length; i++) {
            IngridHit hit = hits.getHits()[i];
            IngridHitDetail detail = (IngridHitDetail)hit.getHitDetail();
            String iplugClass = detail.getIplugClassName();
            String plugId = hit.getPlugId();
            String docId = String.valueOf(hit.getDocumentId());
            String altDocId = (String)hit.get("alt_document_id");
            String udkClass = null;
            String udkUuid = null;
            String udkAddrClass = null;
            String wmsURL = null;
            Element item = channel.addElement("item");
            String itemUrl = null;
            
            if (noIBusUsed || (iplugClass != null
                    && (iplugClass.equals("de.ingrid.iplug.dsc.index.DSCSearcher")
                            || iplugClass.equals("de.ingrid.iplug.udk.UDKPlug")
                            || iplugClass.equals("de.ingrid.iplug.csw.CSWPlug")
                            // TODO: || iplugClass.equals("de.ingrid.iplug.Opensearch") // ??? 
                            || iplugClass.equals("de.ingrid.iplug.tamino.TaminoSearcher")))) {
                // handle the title

                PlugDescription plugDescription = (PlugDescription) plugIds.get(plugId);
                if (!noIBusUsed && null == plugDescription) {
                    try {
                        plugDescription = bus.getIPlug(plugId);
                    } catch (Exception e) {
                        log.error("Doesn't get PlugDescription for " + plugId, e);
                    }
                    plugIds.put(plugId, plugDescription);
                }

                if ((plugDescription != null) && (hasDataType(plugDescription, "dsc_ecs_address"))) {
                    String title = getDetailValue(detail, "T02_address.title");
                    title = title.concat(" ").concat(getDetailValue(detail, "T02_address.firstname")).concat(" ");
                    title = title.concat(getDetailValue(detail, "T02_address.lastname"));
                    item.addElement("title").addText(title.trim());
                } else {
                    item.addElement("title").addText(detail.getTitle());
                }

                udkUuid = getDetailValue(detail, "t01_object.obj_id");

                if (detail.get("url") != null) {
                    itemUrl = (String) detail.get("url");
                } else if (!reqWrapper.getMetadataDetailAsXML() && metadataDetailsUrl != null && metadataDetailsUrl.length() > 0) {
                    itemUrl = metadataDetailsUrl.concat("?plugid=").concat(plugId).concat("&docid=").concat(docId).concat("&docuuid=").concat(udkUuid);
                } else if (proxyurl != null && proxyurl.length() > 0) {
                    itemUrl = proxyurl.concat("/detail").concat("?plugid=").concat(plugId).concat("&docid=").concat(
                            docId).concat("&docuuid=").concat(udkUuid);
                } else {
                    itemUrl = request.getRequestURL().substring(0, request.getRequestURL().lastIndexOf("/")).concat(
                            "/detail").concat("?plugid=").concat(plugId).concat("&docid=").concat(docId).concat("&docuuid=").concat(udkUuid);
                }

                if (altDocId != null && altDocId.length() > 0) {
                	itemUrl = itemUrl.concat("&altdocid=").concat(altDocId);
                }

                udkClass = getDetailValue(detail, "T01_object.obj_class");
                udkAddrClass = getDetailValue(detail, "T02_address.typ");
                Object obj = detail.get("T011_obj_serv_op_connpoint.connect_point");
                if (obj != null  && obj instanceof String[]) {
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
            item.addElement("link").addText(itemUrl);
            item.addElement("description").addText(deNullify(detail.getSummary()));
            item.addElement("relevance:score").addText(String.valueOf(detail.getScore()));
            
            if (reqWrapper.withIngridData()) {
	            item.addElement("ingrid:plugid").addText(deNullify(plugId));
	            item.addElement("ingrid:docid").addText(deNullify(docId));
	            item.addElement("ingrid:docuuid").addText(deNullify(udkUuid));
	            if (altDocId != null && altDocId.length() > 0) {
	                item.addElement("ingrid:altdocid").addText(altDocId);
	            }
	            String provider = getDetailValue(detail, "provider");
	            detail.get("searchTerms");
	            if (provider == null) {
	                provider = detail.getOrganisation();
	            }
	            item.addElement("ingrid:provider").addText(deNullify(provider));
	
	            String partner = getDetailValue(detail, "partner");
	            item.addElement("ingrid:partner").addText(deNullify(partner));
	            item.addElement("ingrid:source").addText(deNullify(detail.getDataSourceName()));
	
	            // handle udk class
	            if (udkClass != null && udkClass.length() > 0) {
	                item.addElement("ingrid:udk-class").addText(udkClass);
	            }
	            // handle udk addr class
	            if (udkAddrClass != null && udkAddrClass.length() > 0) {
	                item.addElement("ingrid:udk-addr-class").addText(udkAddrClass);
	            }
	            // handle wms url
	            if (wmsURL != null && wmsURL.length() > 0) {
	                item.addElement("ingrid:wms-url").addText(StringEscapeUtils.escapeXml(wmsURL));
	            }
	            
	            // handle time reference
	            if (detail.containsKey("t1") || detail.containsKey("t2")) {
		            Element timeRef = item.addElement("ingrid:timeReference");
		            if (detail.containsKey("t1") && !getDetailValue(detail, "t1").equals("99999999")) timeRef.addElement("ingrid:start").addText(getDetailValue(detail, "t1"));
		            if (detail.containsKey("t2") && !getDetailValue(detail, "t2").equals("99999999")) timeRef.addElement("ingrid:stop").addText(getDetailValue(detail, "t2"));
	            }
            }
            
            // handle geoRSS data
            if (reqWrapper.withGeoRSS()) {
	            if (detail.get("x1") != null &&
	            		detail.get("x1") instanceof String[]) 
	            {
	
					try {
						if (!((String[])detail.get("x1"))[0].equals("")) {
							int length = ((String[])detail.get("x1")).length;
							for (int j=0; j<length; j++) {
								Float x1 = Float.valueOf(((String[])detail.get("x1"))[j]);
								Float x2 = Float.valueOf(((String[])detail.get("x2"))[j]);
								Float y1 = Float.valueOf(((String[])detail.get("y1"))[j]);
								Float y2 = Float.valueOf(((String[])detail.get("y2"))[j]);
								
								if ((x1+x2+y1+y2) != 0.0) {
									item.addElement("georss:box").addText(
					            			  deNullify(x1.toString()) + " "
					            			+ deNullify(y1.toString()) + " "
					            			+ deNullify(x2.toString()) + " "
					            			+ deNullify(y2.toString())
					            	);
									if (detail.containsKey("location")) {
										item.addElement("georss:featurename", (String)detail.get("location"));
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
        if (log.isDebugEnabled()) {
            log.debug("Time for plugids: " + (System.currentTimeMillis() - startTime) + " ms");
        }
		return doc;
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
    
    private String deNullify(String s) {
    	if (s == null) {
    		return "";
    	} else {
    		return s;
    	}
    }
    
    private String getFieldValue(IngridQuery query, String field) {
    	FieldQuery[] fieldQueries = query.getFields();
    	for (FieldQuery fq : fieldQueries) {
    		if (fq.getFieldName().equals(field)) {
    			return fq.getFieldValue();
    		}
    	}
    	return "";
    }
}
