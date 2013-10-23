package de.ingrid.iface.util;

import java.util.List;

import javax.servlet.http.HttpServlet;

import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SearchInterfaceServletConfigurator {

    @Autowired
    List<SearchInterfaceServlet> searchInterfaceServlet;

    public void addServlets(Context rootContext) {

        for (SearchInterfaceServlet servlet : searchInterfaceServlet) {
            rootContext.addServlet(new ServletHolder((HttpServlet) servlet), servlet.getPathSpec());
        }

    }

}
