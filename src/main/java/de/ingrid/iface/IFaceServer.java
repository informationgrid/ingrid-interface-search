/*
 * **************************************************-
 * ingrid-interface-search
 * ==================================================
 * Copyright (C) 2014 - 2025 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.2 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl
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

import de.ingrid.iface.util.SearchInterfaceConfig;
import de.ingrid.iface.util.SearchInterfaceServletConfigurator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.servlet.ServletHandler;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.resource.ResourceFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;

import java.net.URL;

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
        int serverPort = SearchInterfaceConfig.getInstance().getInt(SearchInterfaceConfig.SERVER_PORT, 80);
        // Create the server
        log.info("starting search server ...");

        Server server = new Server(serverPort);
        ContextHandler contextHandler = new ServletContextHandler();
        ServletHandler servletHandler = new ServletHandler();

        AbstractApplicationContext ctx = new AnnotationConfigApplicationContext("de.ingrid.iface");
        SearchInterfaceServletConfigurator searchInterfaceServletConfigurator = ctx.getBean(SearchInterfaceServletConfigurator.class);

        // add a shutdown hook for the above context...
        ctx.registerShutdownHook();

        contextHandler.setHandler(servletHandler);

        searchInterfaceServletConfigurator.addServlets(servletHandler);
        ctx.close();

        ContextHandler atomContextHandler = new ContextHandler();
        atomContextHandler.setContextPath("/dls");



        ResourceHandler resourceHandler = new ResourceHandler();
        resourceHandler.setDirAllowed(false);
        resourceHandler.setWelcomeFiles("index.html");
        URL baseUrl = IFaceServer.class.getResource("/static");
        ResourceFactory resourceFactory = ResourceFactory.of(atomContextHandler);
        Resource baseResource = resourceFactory.newResource(baseUrl);

        log.info("BasePath:" + baseResource.toString());
        resourceHandler.setBaseResource(baseResource);
        atomContextHandler.setHandler(resourceHandler);
        log.info("==================================================");
        log.info("Server port: " + serverPort);
        log.info("Serving resources from '/static' at '/dls'.");
        log.info("Implementation Version: " + server.getClass().getPackage().getImplementationVersion());
        log.info("==================================================");

        ContextHandlerCollection contexts = new ContextHandlerCollection();
        contexts.addHandler(contextHandler);
        contexts.addHandler(atomContextHandler);
        server.setHandler(contexts);

        // fix correct redirect when behind proxy (see: https://github.com/jetty/jetty.project/issues/11947)
        HttpConnectionFactory httpConfig = server.getConnectors()[0].getConnectionFactory(HttpConnectionFactory.class);
        httpConfig.getHttpConfiguration().setRelativeRedirectAllowed(false);
        httpConfig.getHttpConfiguration().setSendServerVersion(false);
        httpConfig.getHttpConfiguration().addCustomizer(new ForwardedRequestCustomizer());

        server.start();
        server.join();
    }

}
