package de.ingrid.iface.util;

import java.util.List;

import javax.servlet.http.HttpServlet;

import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SearchInterfaceServletConfigurator {

    @Autowired
    List<SearchInterfaceServlet> searchInterfaceServlet;

    public void addServlets(ServletHandler handler) {

        for (SearchInterfaceServlet servlet : searchInterfaceServlet) {
            handler.addServletWithMapping(new ServletHolder((HttpServlet) servlet), servlet.getPathSpec());
        }

    }

}
