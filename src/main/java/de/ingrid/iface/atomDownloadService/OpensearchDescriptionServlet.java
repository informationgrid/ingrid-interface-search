package de.ingrid.iface.atomDownloadService;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jetty.server.Request;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.ingrid.iface.atomDownloadService.om.OpensearchDescription;
import de.ingrid.iface.atomDownloadService.requests.OpensearchDescriptionRequest;
import de.ingrid.iface.util.SearchInterfaceServlet;

@Service
public class OpensearchDescriptionServlet extends HttpServlet implements SearchInterfaceServlet {

    private static final long serialVersionUID = 134123478157L;

    private OpensearchDescriptionProducer opensearchDescriptionProducer;

    private OpensearchDescriptionBuilder opensearchDescriptionBuilder;

    private final static Log log = LogFactory.getLog(OpensearchDescriptionServlet.class);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (log.isDebugEnabled()) {
            log.debug("Incoming request: " + req.getPathInfo());
        }
        try {
            // extract method from path
            OpensearchDescriptionRequest opensearchDescriptionRequest = new OpensearchDescriptionRequest(req);
            // handle method, create response
            OpensearchDescription opensearchDescription = opensearchDescriptionProducer.produce(opensearchDescriptionRequest);
            String body = opensearchDescriptionBuilder.build(opensearchDescription);
            resp.setContentType("application/opensearchdescription+xml");
            resp.getWriter().print(body);
            ((Request) req).setHandled(true);

        } catch (Exception e) {
            log.error("Error executing get opensearch description.", e);
        }
    }

    @Override
    public String getName() {
        return "OpensearchDescriptionService";
    }

    @Override
    public String getPathSpec() {
        return "/dls/opensearch-description/*";
    }

    @Autowired
    public void setOpensearchDescriptionProducer(OpensearchDescriptionProducer opensearchDescriptionProducer) {
        this.opensearchDescriptionProducer = opensearchDescriptionProducer;
    }

    @Autowired
    public void setOpensearchDescriptionBuilder(OpensearchDescriptionBuilder opensearchDescriptionBuilder) {
        this.opensearchDescriptionBuilder = opensearchDescriptionBuilder;
    }

}
