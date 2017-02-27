package de.ingrid.iface.util;

import static org.junit.Assert.*;

import java.net.MalformedURLException;

import org.junit.Test;

public class URLUtilTest {

    @Test
    public void testUpdateProtocol() throws MalformedURLException {
        
        assertEquals( "https://domain/path/", URLUtil.updateProtocol( "http://domain/path/", "https" ));
        assertEquals( "https://domain/path/", URLUtil.updateProtocol( "//domain/path/", "https" ));
        assertEquals( "http://domain/path/", URLUtil.updateProtocol( "https://domain/path/", "http" ));
        assertEquals( "https://192.168.0.228:8181/dls/dataset/{servicefeed-uuid}/?spatial_dataset_identifier_code={inspire_dls:spatial_dataset_identifier_code?}&amp;spatial_dataset_identifier_namespace={inspire_dls:spatial_dataset_identifier_namespace?}&amp;language={language?}", URLUtil.updateProtocol( "http://192.168.0.228:8181/dls/dataset/{servicefeed-uuid}/?spatial_dataset_identifier_code={inspire_dls:spatial_dataset_identifier_code?}&amp;spatial_dataset_identifier_namespace={inspire_dls:spatial_dataset_identifier_namespace?}&amp;language={language?}", "https" ));
    }

}
