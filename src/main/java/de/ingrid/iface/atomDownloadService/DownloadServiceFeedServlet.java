package de.ingrid.iface.atomDownloadService;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mortbay.jetty.Request;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.ingrid.iface.atomDownloadService.om.ServiceFeed;
import de.ingrid.iface.atomDownloadService.requests.ServiceFeedRequest;
import de.ingrid.iface.util.SearchInterfaceServlet;

@Service
public class DownloadServiceFeedServlet extends HttpServlet implements SearchInterfaceServlet {

    private static final long serialVersionUID = 13414157L;

    @Autowired
    private ServiceFeedAtomBuilder serviceFeedAtomBuilder;

    @Autowired
    private ServiceFeedProducer serviceFeedProducer;

    private final static Log log = LogFactory.getLog(DownloadServiceFeedServlet.class);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (log.isDebugEnabled()) {
            log.debug("Incoming request: " + req.getPathInfo());
        }
        try {
            // extract method from path
            ServiceFeedRequest serviceFeedRequest = new ServiceFeedRequest(req);
            // handle method, create response
            ServiceFeed serviceFeed = serviceFeedProducer.produce(serviceFeedRequest);
            String body = serviceFeedAtomBuilder.build(serviceFeed);
            resp.setContentType("application/atom+xml");
            resp.getWriter().print(body);
            ((Request) req).setHandled(true);

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

}
