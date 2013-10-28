/*
 * Copyright (c) 2006 wemove digital solutions. All rights reserved.
 */
package de.ingrid.iface;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

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
        ServletHandler handler = new ServletHandler();
        server.setHandler(handler);

        ApplicationContext ctx = new AnnotationConfigApplicationContext("de.ingrid.iface");
        SearchInterfaceServletConfigurator searchInterfaceServletConfigurator = ctx.getBean(SearchInterfaceServletConfigurator.class);

        searchInterfaceServletConfigurator.addServlets(handler);
        server.start();
        log.info(server.getClass().getPackage().getImplementationVersion());
        log.info("Started Opensearch IFaceServer on port " + SearchInterfaceConfig.getInstance().getInt(SearchInterfaceConfig.SERVER_PORT, 80) + " waiting for requests.");
    }

}
