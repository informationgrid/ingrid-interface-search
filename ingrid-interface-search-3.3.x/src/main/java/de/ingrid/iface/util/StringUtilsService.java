package de.ingrid.iface.util;

import org.springframework.stereotype.Service;
import org.w3c.dom.Document;

@Service
public class StringUtilsService {

    public Document urlToDocument(String urlString, Integer connectTimeout, Integer readTimeout) throws Exception {
        return StringUtils.urlToDocument(urlString, connectTimeout, readTimeout);
    }

}
