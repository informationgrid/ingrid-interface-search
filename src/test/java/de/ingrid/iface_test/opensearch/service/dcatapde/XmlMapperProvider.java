package de.ingrid.iface_test.opensearch.service.dcatapde;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.databind.DeserializationFeature;

/**
 * Test helper providing a shared, pre-configured XmlMapper instance for tests.
 */
public final class XmlMapperProvider {

    public static final XmlMapper INSTANCE = create();

    private static XmlMapper create() {
        XmlMapper mapper = new XmlMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper;
    }

    private XmlMapperProvider() {
        // no instances
    }
}

