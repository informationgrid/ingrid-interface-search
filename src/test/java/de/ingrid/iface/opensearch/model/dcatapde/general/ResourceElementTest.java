package de.ingrid.iface.opensearch.model.dcatapde.general;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import de.ingrid.iface.opensearch.service.dcatapde.XmlService;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ResourceElementTest {

    private XmlMapper getMapper() {
        return new XmlService().getMapper();
    }

    @Test
    public void testResourceDeserialization() throws Exception {
        String xml = "<dcat:theme xmlns:dcat=\"http://www.w3.org/ns/dcat#\" xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" rdf:resource=\"http://publications.europa.eu/resource/authority/data-theme/HEAL\"/>";
        XmlMapper mapper = getMapper();
        ResourceElement element = mapper.readValue(xml, ResourceElement.class);

        assertNotNull(element, "ResourceElement should not be null");
        assertEquals("http://publications.europa.eu/resource/authority/data-theme/HEAL", element.getResource());
    }

    @Test
    public void testResourceDeserializationInDataset() throws Exception {
        String xml = "<dcat:Dataset xmlns:dcat=\"http://www.w3.org/ns/dcat#\" xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\">" +
                     "  <dcat:theme rdf:resource=\"http://publications.europa.eu/resource/authority/data-theme/HEAL\"/>" +
                     "</dcat:Dataset>";
        XmlMapper mapper = getMapper();
        System.out.println("[DEBUG_LOG] XML: " + xml);
        // Dataset from de.ingrid.iface.opensearch.model.dcatapde.Dataset
        de.ingrid.iface.opensearch.model.dcatapde.Dataset dataset = mapper.readValue(xml, de.ingrid.iface.opensearch.model.dcatapde.Dataset.class);
        
        assertNotNull(dataset, "Dataset should not be null");
        assertNotNull(dataset.getThemes(), "Themes should not be null");
        assertEquals(1, dataset.getThemes().size(), "Should have 1 theme");
        ResourceElement theme = dataset.getThemes().get(0);
        System.out.println("[DEBUG_LOG] Theme object: " + theme);
        System.out.println("[DEBUG_LOG] Resource: '" + theme.getResource() + "'");
        assertEquals("http://publications.europa.eu/resource/authority/data-theme/HEAL", theme.getResource());
    }
}
