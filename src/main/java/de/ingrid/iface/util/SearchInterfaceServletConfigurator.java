package de.ingrid.iface.util;

import java.util.List;

import javax.servlet.http.HttpServlet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SearchInterfaceServletConfigurator {

    private final static Log log = LogFactory.getLog(SearchInterfaceServletConfigurator.class);

    @Autowired
    List<SearchInterfaceServlet> searchInterfaceServlet;
    

    public void addServlets(ServletHandler handler) {

        for (SearchInterfaceServlet servlet : searchInterfaceServlet) {
            handler.addServletWithMapping(new ServletHolder((HttpServlet) servlet), servlet.getPathSpec());
            if (log.isInfoEnabled()) {
                log.info("Added servlet '" + servlet.getName() + "' at '" + servlet.getPathSpec() + "'.");
            }
        }

    }

}
