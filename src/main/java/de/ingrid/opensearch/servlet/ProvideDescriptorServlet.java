/*
 * Copyright (c) 2006 wemove digital solutions. All rights reserved.
 */
package de.ingrid.opensearch.servlet;

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

import de.ingrid.opensearch.util.OpensearchConfig;

/**
 * Servlet handles OpenSearch queries.
 * 
 * @author joachim@wemove.com
 */
public class ProvideDescriptorServlet extends HttpServlet {

    private static final long serialVersionUID = 490753753823098934L;

    private final static Log log = LogFactory.getLog(ProvideDescriptorServlet.class);

    /**
     * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
     *      javax.servlet.http.HttpServletResponse)
     */
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {

        String descriptorFile = OpensearchConfig.getInstance()
            .getString(OpensearchConfig.DESCRIPTOR_FILE, "conf/descriptor.xml");
        log.debug("returning descriptor: " + descriptorFile);
        response.setContentType("text/xml");
        PrintWriter out = response.getWriter();
        String thisLine;
        BufferedReader in = new BufferedReader(new FileReader(descriptorFile));
        while ((thisLine = in.readLine()) != null) {
            out.println(thisLine);
        }
    }

    /**
     * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest,
     *      javax.servlet.http.HttpServletResponse)
     */
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }
}
