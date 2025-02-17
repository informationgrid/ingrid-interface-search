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
package de.ingrid.iface.atomDownloadService;

import de.ingrid.iface.atomDownloadService.om.ServiceFeed;
import de.ingrid.iface.atomDownloadService.requests.ServiceFeedRequest;
import de.ingrid.iface.util.SearchInterfaceServlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class GetServiceFeedServlet extends HttpServlet implements SearchInterfaceServlet {

    private static final long serialVersionUID = 13414157L;

    private ServiceFeedAtomBuilder serviceFeedAtomBuilder;

    private ServiceFeedProducer serviceFeedProducer;

    private final static Log log = LogFactory.getLog(GetServiceFeedServlet.class);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Long startTimer = 0L;
        if (log.isDebugEnabled()) {
            log.debug("#### Build service feed.");
            log.debug("Incoming request: " + req.getPathInfo());
            startTimer = System.currentTimeMillis();
        }
        try {
            // extract method from path
            ServiceFeedRequest serviceFeedRequest = new ServiceFeedRequest(req);
            // handle method, create response
            ServiceFeed serviceFeed = serviceFeedProducer.produce(serviceFeedRequest);
            if (serviceFeed == null) {
                throw new ServletException("Service Feed not found.");
            }
            String body = serviceFeedAtomBuilder.build(serviceFeed, req.getHeader("user-agent"));
            resp.setCharacterEncoding("UTF-8");
            resp.setContentType("application/atom+xml");
            resp.getWriter().print(body);
//            ((Request) req).setHandled(true);
            if (log.isDebugEnabled()) {
                log.debug("Finished request within " + (System.currentTimeMillis() - startTimer) + " ms.");
            }

        } catch (ServletException e) {
            throw (e);
        } catch (Exception e) {
            log.error("Error executing get service feed.", e);
        }
    }

    @Override
    public String getName() {
        return "AtomDownloadServiceFeed";
    }

    @Override
    public String getPathSpec() {
        return "/dls/service/*";
    }

    @Autowired
    public void setServiceFeedAtomBuilder(ServiceFeedAtomBuilder serviceFeedAtomBuilder) {
        this.serviceFeedAtomBuilder = serviceFeedAtomBuilder;
    }

    @Autowired
    public void setServiceFeedProducer(ServiceFeedProducer serviceFeedProducer) {
        this.serviceFeedProducer = serviceFeedProducer;
    }

}
