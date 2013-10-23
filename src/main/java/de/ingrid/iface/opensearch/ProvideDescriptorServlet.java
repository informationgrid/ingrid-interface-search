/*
 * Copyright (c) 2006 wemove digital solutions. All rights reserved.
 */
package de.ingrid.iface.opensearch;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import de.ingrid.iface.util.SearchInterfaceConfig;
import de.ingrid.iface.util.SearchInterfaceServlet;

/**
 * Servlet handles OpenSearch queries.
 * 
 * @author joachim@wemove.com
 */
@Service
public class ProvideDescriptorServlet extends HttpServlet implements SearchInterfaceServlet {

    private static final long serialVersionUID = 490753753823098934L;

    private final static Log log = LogFactory.getLog(ProvideDescriptorServlet.class);

    /**
     * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
     *      javax.servlet.http.HttpServletResponse)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String descriptorFile = SearchInterfaceConfig.getInstance().getString(SearchInterfaceConfig.DESCRIPTOR_FILE,
                "conf/descriptor.xml");
        log.debug("returning descriptor: " + descriptorFile);
        response.setContentType("text/xml");
        PrintWriter out = response.getWriter();
        String thisLine;
        BufferedReader in = null;
        try {
            in = new BufferedReader(new FileReader(descriptorFile));
            while ((thisLine = in.readLine()) != null) {
                out.println(thisLine);
            }
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }

    /**
     * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest,
     *      javax.servlet.http.HttpServletResponse)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException,
            IOException {
        doGet(request, response);
    }

    @Override
    public String getName() {
        return "ProvideDescription";
    }

    @Override
    public String getPathSpec() {
        return "/opensearch/descriptor";
    }
}
