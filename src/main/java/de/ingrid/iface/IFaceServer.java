/*
 * **************************************************-
 * ingrid-interface-search
 * ==================================================
 * Copyright (C) 2014 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.1 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl5
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * **************************************************#
 */
/*
 * Copyright (c) 2006 wemove digital solutions. All rights reserved.
 */
package de.ingrid.iface;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
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
        log.info("starting search server...");

        Server server = new Server(SearchInterfaceConfig.getInstance().getInt(SearchInterfaceConfig.SERVER_PORT, 80));
        ServletHandler handler = new ServletHandler();

        ApplicationContext ctx = new AnnotationConfigApplicationContext("de.ingrid.iface");
        SearchInterfaceServletConfigurator searchInterfaceServletConfigurator = ctx.getBean(SearchInterfaceServletConfigurator.class);

        searchInterfaceServletConfigurator.addServlets(handler);

        ContextHandler context = new ContextHandler();
        context.setContextPath("/dls");
        ResourceHandler resourceHandler = new ResourceHandler();
        resourceHandler.setDirectoriesListed(false);
        resourceHandler.setWelcomeFiles(new String[] { "index.html" });
        resourceHandler.setResourceBase(SearchInterfaceConfig.getInstance().getString(SearchInterfaceConfig.ATOM_DOWNLOAD_SERVICE_CLIENT_PATH, "client"));
        context.setHandler(resourceHandler);
        log.info("Serving resources from '"+SearchInterfaceConfig.getInstance().getString(SearchInterfaceConfig.ATOM_DOWNLOAD_SERVICE_CLIENT_PATH, "client")+"' at '/dls'.");

        HandlerList handlers = new HandlerList();
        handlers.setHandlers(new Handler[] { context, handler });
        server.setHandler(handlers);

        server.start();
        server.join();
        log.info(server.getClass().getPackage().getImplementationVersion());
        log.info("Started Search IFaceServer on port " + SearchInterfaceConfig.getInstance().getInt(SearchInterfaceConfig.SERVER_PORT, 80) + " waiting for requests.");
    }

}
