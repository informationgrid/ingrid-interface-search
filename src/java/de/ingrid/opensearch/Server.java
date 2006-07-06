/*
 * Copyright (c) 2006 wemove digital solutions. All rights reserved.
 */
package de.ingrid.opensearch;

import org.mortbay.http.HttpContext;
import org.mortbay.http.HttpServer;
import org.mortbay.http.SocketListener;
import org.mortbay.http.handler.ResourceHandler;
import org.mortbay.jetty.servlet.ServletHandler;

/**
 * TODO Describe your created type (class, etc.) here.
 *
 * @author joachim@wemove.com
 */
public class Server {

    /**
     * @param args
     * @throws Exception 
     */
    public static void main(String[] args) throws Exception {
//      Create the server
        HttpServer server=new HttpServer();
          
        // Create a port listener
        SocketListener listener=new SocketListener();
        listener.setPort(8181);
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
        servlets.initialize(context);

        // Start the http server
        server.start();
    }
}
