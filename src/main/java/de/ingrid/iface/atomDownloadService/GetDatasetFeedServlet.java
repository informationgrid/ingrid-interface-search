/*
 * **************************************************-
 * ingrid-interface-search
 * ==================================================
 * Copyright (C) 2014 - 2015 wemove digital solutions GmbH
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
package de.ingrid.iface.atomDownloadService;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jetty.http.HttpException;
import org.eclipse.jetty.server.Request;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.ingrid.iface.atomDownloadService.om.DatasetFeed;
import de.ingrid.iface.atomDownloadService.requests.DatasetFeedRequest;
import de.ingrid.iface.util.SearchInterfaceServlet;

@Service
public class GetDatasetFeedServlet extends HttpServlet implements SearchInterfaceServlet {

    private static final long serialVersionUID = 13414157L;

    private DatasetFeedAtomBuilder datasetFeedAtomBuilder;

    private DatasetFeedProducer datasetFeedProducer;

    private final static Log log = LogFactory.getLog(GetDatasetFeedServlet.class);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (log.isDebugEnabled()) {
            log.debug("Incoming request: " + req.getPathInfo());
        }
        try {
            // extract method from path
            DatasetFeedRequest datasetFeedRequest = new DatasetFeedRequest(req);
            // handle method, create response
            DatasetFeed datasetFeed = datasetFeedProducer.produce(datasetFeedRequest);
            if (datasetFeed == null) {
                throw (HttpException) new HttpException(404, "Dataset Feed not found: " + req.getPathInfo());
            }
            String body = datasetFeedAtomBuilder.build(datasetFeed, req.getHeader("user-agent"));
            resp.setCharacterEncoding("UTF-8");
            resp.setContentType("application/atom+xml");
            resp.getWriter().print(body);
            ((Request) req).setHandled(true);
        } catch (Exception e) {
            log.error("Error executing get dataset feed: " + req.getPathInfo(), e);
        }
    }

    @Override
    public String getName() {
        return "AtomDownloadDatasetFeed";
    }

    @Override
    public String getPathSpec() {
        return "/dls/dataset/*";
    }

    @Autowired
    public void setDatasetFeedAtomBuilder(DatasetFeedAtomBuilder datasetFeedAtomBuilder) {
        this.datasetFeedAtomBuilder = datasetFeedAtomBuilder;
    }

    @Autowired
    public void setDatasetFeedProducer(DatasetFeedProducer datasetFeedProducer) {
        this.datasetFeedProducer = datasetFeedProducer;
    }

}