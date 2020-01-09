/*
 * **************************************************-
 * ingrid-interface-search
 * ==================================================
 * Copyright (C) 2014 - 2020 wemove digital solutions GmbH
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
import java.util.List;

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
import de.ingrid.iface.atomDownloadService.om.DatasetFeedEntry;
import de.ingrid.iface.atomDownloadService.om.Link;
import de.ingrid.iface.atomDownloadService.requests.DatasetFeedRequest;
import de.ingrid.iface.util.SearchInterfaceServlet;

@Service
public class GetDatasetServlet extends HttpServlet implements SearchInterfaceServlet {

    private static final long serialVersionUID = 13411244157L;

    private DatasetAtomBuilder datasetAtomBuilder;

    private DatasetFeedProducer datasetFeedProducer;

    private final static Log log = LogFactory.getLog(GetDatasetServlet.class);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Long startTimer = 0L;
        if (log.isDebugEnabled()) {
            log.debug("#### Build dataset.");
            log.debug("Incoming request: " + req.getPathInfo());
            startTimer = System.currentTimeMillis();
        }
        try {
            // extract method from path
            DatasetFeedRequest datasetFeedRequest = new DatasetFeedRequest(req);
            // handle method, create response
            DatasetFeed datasetFeed = datasetFeedProducer.produce(datasetFeedRequest);
            if (datasetFeed == null) {
                throw (HttpException) new HttpException(404, "Dataset not found.");
            }
            // check if we only have one download link, if yes, redirect to the
            // download link
            List<DatasetFeedEntry> datasetFeedEntries = datasetFeed.getEntries();
            if (datasetFeedEntries != null && datasetFeedEntries.size() == 1) {
                DatasetFeedEntry entry = datasetFeedEntries.get(0);
                List<Link> links = entry.getLinks();
                if (links != null && links.size() == 1) {
                    Link link = links.get(0);
                    resp.sendRedirect(link.getHref());
                    ((Request) req).setHandled(true);
                    return;
                }
            }

            // if we have more than one download link, create a atom feed wit
            // all of them
            String body = datasetAtomBuilder.build(datasetFeed, req.getHeader("user-agent"));
            resp.setCharacterEncoding("UTF-8");
            resp.setContentType("application/atom+xml");
            resp.getWriter().print(body);
            ((Request) req).setHandled(true);
            if (log.isDebugEnabled()) {
                log.debug("Finished request within " + (System.currentTimeMillis() - startTimer) + " ms.");
            }
        } catch (Exception e) {
            log.error("Error executing get dataset feed.", e);
        }
    }

    @Override
    public String getName() {
        return "AtomDownloadDataset";
    }

    @Override
    public String getPathSpec() {
        return "/dls/get-dataset/*";
    }

    @Autowired
    public void setDatasetAtomBuilder(DatasetAtomBuilder datasetAtomBuilder) {
        this.datasetAtomBuilder = datasetAtomBuilder;
    }

    @Autowired
    public void setDatasetFeedProducer(DatasetFeedProducer datasetFeedProducer) {
        this.datasetFeedProducer = datasetFeedProducer;
    }

}
