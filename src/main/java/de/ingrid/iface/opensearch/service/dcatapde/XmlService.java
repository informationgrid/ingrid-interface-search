/*
 * **************************************************-
 * ingrid-interface-search
 * ==================================================
 * Copyright (C) 2014 - 2023 wemove digital solutions GmbH
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
package de.ingrid.iface.opensearch.service.dcatapde;

import com.ctc.wstx.api.WstxInputProperties;
import com.ctc.wstx.stax.WstxOutputFactory;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import org.springframework.stereotype.Service;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.Writer;

@Service
public class XmlService {

    private final XmlMapper xmlMapper;

    public XmlService() {
        xmlMapper = new XmlMapper();

        // override default instance of WstxOutputFactory
        // see: https://stackoverflow.com/questions/22885431/how-to-serialize-pojo-to-xml-with-namespace-prefix
        xmlMapper.getFactory().setXMLOutputFactory(new WstxOutputFactory() {
            @Override
            public XMLStreamWriter createXMLStreamWriter(Writer w) throws XMLStreamException {
                mConfig.setProperty(WstxInputProperties.P_RETURN_NULL_FOR_DEFAULT_NAMESPACE, true);
                XMLStreamWriter result = super.createXMLStreamWriter(w);
                result.setPrefix("adms", "http://www.w3.org/ns/adms#");
                result.setPrefix("dcat", "http://www.w3.org/ns/dcat#");
                result.setPrefix("dcatde", "http://dcat-ap.de/def/dcatde/");
                result.setPrefix("dcterms", "http://purl.org/dc/terms/");
                result.setPrefix("foaf", "http://xmlns.com/foaf/0.1/");
                result.setPrefix("gml", "http://www.opengis.net/gml/3.2");
                result.setPrefix("locn", "http://www.w3.org/ns/locn#");
                result.setPrefix("odrs", "http://schema.theodi.org/odrs#");
                result.setPrefix("owl", "http://www.w3.org/2002/07/owl#");
                result.setPrefix("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
                result.setPrefix("rdfs", "http://www.w3.org/2000/01/rdf-schema");
                result.setPrefix("schema", "http://schema.org/");
                result.setPrefix("skos", "http://www.w3.org/2004/02/skos/core#");
                result.setPrefix("spdx", "http://spdx.org/rdf/terms");
                result.setPrefix("xsd", "http://www.w3.org/2001/XMLSchema");
                result.setPrefix("vcard", "http://www.w3.org/2006/vcard/ns#");
                result.setPrefix("hydra", "http://www.w3.org/ns/hydra/core#");
                return result;
            }
        });
        xmlMapper.enable(SerializationFeature.INDENT_OUTPUT);
        xmlMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        xmlMapper.configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true);
    }

    public XmlMapper getMapper() {
        return xmlMapper;
    }

    public String attachNamespaces(String xmlDcat) {
        return xmlDcat.replace("<RDF>", "<rdf:RDF\n" +
                "    xmlns:adms=\"http://www.w3.org/ns/adms#\"\n" +
                "    xmlns:dcat=\"http://www.w3.org/ns/dcat#\"\n" +
                "    xmlns:dcatde=\"http://dcat-ap.de/def/dcatde/\"\n" +
                "    xmlns:dcterms=\"http://purl.org/dc/terms/\"\n" +
                "    xmlns:foaf=\"http://xmlns.com/foaf/0.1/\"\n" +
                "    xmlns:gml=\"http://www.opengis.net/gml/3.2\"\n" +
                "    xmlns:locn=\"http://www.w3.org/ns/locn#\"\n" +
                "    xmlns:odrs=\"http://schema.theodi.org/odrs#\"\n" +
                "    xmlns:owl=\"http://www.w3.org/2002/07/owl#\"\n" +
                "    xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n" +
                "    xmlns:schema=\"http://schema.org/\"\n" +
                "    xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\"\n" +
                "    xmlns:vcard=\"http://www.w3.org/2006/vcard/ns#\"\n" +
                "    xmlns:hydra=\"http://www.w3.org/ns/hydra/core#\">")
                .replace("</RDF>", "</rdf:RDF>");

    }
}
