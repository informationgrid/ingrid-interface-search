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

import de.ingrid.iface.atomDownloadService.om.ServiceFeedList;
import de.ingrid.iface.atomDownloadService.requests.ServiceFeedListRequest;
import de.ingrid.iface.util.SearchInterfaceServlet;

@Service
public class GetServiceFeedListServlet extends HttpServlet implements SearchInterfaceServlet {

    private static final long serialVersionUID = 13414137L;

    private ServiceFeedListAtomBuilder serviceFeedListAtomBuilder;

    private ServiceFeedListProducer serviceFeedListProducer;

    private final static Log log = LogFactory.getLog(GetServiceFeedListServlet.class);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (log.isDebugEnabled()) {
            log.debug("Incoming request: " + req.getPathInfo());
        }
        try {
            // extract method from path
            ServiceFeedListRequest serviceFeedListRequest = new ServiceFeedListRequest(req);
            // handle method, create response
            ServiceFeedList serviceFeedList = serviceFeedListProducer.produce(serviceFeedListRequest);
            if (serviceFeedList == null || serviceFeedList.getEntries().isEmpty()) {
                throw (HttpException) new HttpException(404, "No service feeds found.");
            }
            String body = serviceFeedListAtomBuilder.build(serviceFeedList, req.getHeader("user-agent"));
            resp.setCharacterEncoding("UTF-8");
            resp.setContentType("application/atom+xml");
            resp.getWriter().print(body);
            ((Request) req).setHandled(true);

        } catch (HttpException e) {
            throw (e);
        } catch (Exception e) {
            log.error("Error executing get service feed list.", e);
        }
    }

    @Override
    public String getName() {
        return "AtomDownloadServiceFeedList";
    }

    @Override
    public String getPathSpec() {
        return "/dls/service-list/*";
    }

    @Autowired
    public void setServiceFeedAtomBuilder(ServiceFeedListAtomBuilder serviceFeedListAtomBuilder) {
        this.serviceFeedListAtomBuilder = serviceFeedListAtomBuilder;
    }

    @Autowired
    public void setServiceFeedProducer(ServiceFeedListProducer serviceFeedListProducer) {
        this.serviceFeedListProducer = serviceFeedListProducer;
    }

}
