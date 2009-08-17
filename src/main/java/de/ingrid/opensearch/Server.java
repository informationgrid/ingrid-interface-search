/*
 * Copyright (c) 2006 wemove digital solutions. All rights reserved.
 */
package de.ingrid.opensearch;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mortbay.http.HttpContext;
import org.mortbay.http.HttpServer;
import org.mortbay.http.SocketListener;
import org.mortbay.jetty.servlet.ServletHandler;

import de.ingrid.opensearch.util.OpensearchConfig;

/**
 * TODO Describe your created type (class, etc.) here.
 *
 * @author joachim@wemove.com
 */
public class Server {

    private final static Log log = LogFactory.getLog(Server.class);

    /**
     * @param args
     * @throws Exception 
     */
    public static void main(String[] args) throws Exception {
//      Create the server
        log.info("starting opensearch server...");
        
        HttpServer server=new HttpServer();
          
        // Create a port listener
        SocketListener listener=new SocketListener();
        listener.setPort(OpensearchConfig.getInstance().getInt(OpensearchConfig.SERVER_PORT, 80));
        server.addListener(listener);

        // Create a context 
        HttpContext context = new HttpContext();
        context.setContextPath("/");
        server.addContext(context);
          
        // Create a servlet container
        ServletHandler servlets = new ServletHandler();
        context.addHandler(servlets);
        
        // Map a servlet onto the container
        servlets.setAutoInitializeServlets(true);
        servlets.addServlet("OpenSearch","/query","de.ingrid.opensearch.servlet.OpensearchServlet");
        servlets.addServlet("OpenSearchDetail","/detail","de.ingrid.opensearch.servlet.OpensearchDetailServlet");
        servlets.addServlet("ProvideDescription","/descriptor","de.ingrid.opensearch.servlet.ProvideDescriptorServlet");
        servlets.initialize(context);

        // Start the http server
        server.start();
        log.info(server.getClass().getPackage().getImplementationVersion()); 
        log.info("... started waiting for requests.");
    }
}
