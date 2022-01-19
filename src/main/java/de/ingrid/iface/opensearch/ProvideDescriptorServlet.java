/*
 * **************************************************-
 * ingrid-interface-search
 * ==================================================
 * Copyright (C) 2014 - 2022 wemove digital solutions GmbH
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

        String descriptorFile = SearchInterfaceConfig.getInstance().getString(SearchInterfaceConfig.DESCRIPTOR_FILE, "conf/descriptor.xml");
        if (log.isDebugEnabled()) {
            log.debug("returning descriptor: " + descriptorFile);
        }
        response.setContentType("application/opensearchdescription+xml");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        String thisLine;
        BufferedReader in = null;
        try {
            in = new BufferedReader(new FileReader(descriptorFile));
            while ((thisLine = in.readLine()) != null) {
                out.println(thisLine);
            }
        } catch (Exception e) {
            log.error("Error delivering opensearch descriptor", e);
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
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
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
