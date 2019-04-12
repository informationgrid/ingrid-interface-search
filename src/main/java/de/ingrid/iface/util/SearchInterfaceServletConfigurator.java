/*
 * **************************************************-
 * ingrid-interface-search
 * ==================================================
 * Copyright (C) 2014 - 2019 wemove digital solutions GmbH
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
