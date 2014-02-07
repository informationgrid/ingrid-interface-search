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

import de.ingrid.iface.atomDownloadService.om.ServiceFeed;
import de.ingrid.iface.atomDownloadService.requests.ServiceFeedRequest;
import de.ingrid.iface.util.SearchInterfaceServlet;

@Service
public class GetServiceFeedServlet extends HttpServlet implements SearchInterfaceServlet {

    private static final long serialVersionUID = 13414157L;

    private ServiceFeedAtomBuilder serviceFeedAtomBuilder;

    private ServiceFeedProducer serviceFeedProducer;

    private final static Log log = LogFactory.getLog(GetServiceFeedServlet.class);

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
            if (serviceFeed == null) {
                throw (HttpException) new HttpException(404, "Service Feed not found.");
            }
            String body = serviceFeedAtomBuilder.build(serviceFeed, req.getHeader("user-agent"));
            resp.setCharacterEncoding("UTF-8");
            resp.setContentType("application/atom+xml");
            resp.getWriter().print(body);
            ((Request) req).setHandled(true);

        } catch (HttpException e) {
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
