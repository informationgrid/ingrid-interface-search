/*-
 * **************************************************-
 * InGrid Interface Search
 * ==================================================
 * Copyright (C) 2014 - 2026 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.2 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * **************************************************#
 */
package de.ingrid.iface_test.opensearch.model.dcatapde;

import static org.junit.jupiter.api.Assertions.*;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import de.ingrid.iface.opensearch.model.dcatapde.Dataset;
import de.ingrid.iface.opensearch.model.dcatapde.catalog.Agent;
import de.ingrid.iface.opensearch.service.dcatapde.XmlService;
import org.junit.jupiter.api.Test;

public class PolymorphicMaintainerTest {

    @Test
    public void testPolymorphicMaintainer() throws Exception {
        XmlService xmlService = new XmlService();
        XmlMapper mapper = xmlService.getMapper();

        String xml = "<Dataset xmlns:dcatde=\"http://dcat-ap.de/def/dcatde/\" xmlns:foaf=\"http://xmlns.com/foaf/0.1/\">" +
                "  <dcatde:maintainer>" +
                "    <foaf:Agent>" +
                "      <foaf:name>Agent Name</foaf:name>" +
                "    </foaf:Agent>" +
                "  </dcatde:maintainer>" +
                "  <dcatde:maintainer>" +
                "    <foaf:Organization>" +
                "      <foaf:name>Org Name</foaf:name>" +
                "    </foaf:Organization>" +
                "  </dcatde:maintainer>" +
                "</Dataset>";

        Dataset dataset = mapper.readValue(xml, Dataset.class);

        assertNotNull(dataset.getMaintainer());
        assertEquals(2, dataset.getMaintainer().length);

        assertNotNull(dataset.getMaintainer()[0], "First maintainer should not be null");
        assertEquals("Agent Name", dataset.getMaintainer()[0].getAgent().getName());

        assertNotNull(dataset.getMaintainer()[1], "Second maintainer should not be null");
        assertEquals("Org Name", dataset.getMaintainer()[1].getAgent().getName());
    }
}
