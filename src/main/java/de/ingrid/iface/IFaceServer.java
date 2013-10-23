/*
 * Copyright (c) 2006 wemove digital solutions. All rights reserved.
 */
package de.ingrid.iface;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHandler;
import org.mortbay.jetty.servlet.ServletHolder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.sun.net.httpserver.HttpContext;

import de.ingrid.iface.atomDownloadService.DownloadServiceFeedServlet;
import de.ingrid.iface.util.SearchInterfaceConfig;
import de.ingrid.iface.util.SearchInterfaceServletConfigurator;

/**
 * TODO Describe your created type (class, etc.) here.
 * 
 * @author joachim@wemove.com
 */
public class IFaceServer {

    private final static Log log = LogFactory.getLog(IFaceServer.class);

    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        // Create the server
        log.info("starting opensearch server...");

        Server server = new Server(SearchInterfaceConfig.getInstance().getInt(SearchInterfaceConfig.SERVER_PORT, 80));

        Context rootContext = new Context(server,"/",Context.SESSIONS);
        ApplicationContext ctx = new AnnotationConfigApplicationContext("de.ingrid.iface");
        SearchInterfaceServletConfigurator searchInterfaceServletConfigurator = ctx.getBean(SearchInterfaceServletConfigurator.class);

        searchInterfaceServletConfigurator.addServlets(rootContext);

        // Start the http server
        server.start();
        log.info(server.getClass().getPackage().getImplementationVersion());
        log.info("Started Opensearch IFaceServer on port " + SearchInterfaceConfig.getInstance().getInt(SearchInterfaceConfig.SERVER_PORT, 80) + " waiting for requests.");
    }

}
