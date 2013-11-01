/*
 * Copyright (c) 2012 wemove digital solutions. All rights reserved.
 */
package de.ingrid.iface.util;

import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.util.UUID;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

public class StringUtils {

    public static String join(Object[] parts, String separator) {
        StringBuilder str = new StringBuilder();
        for (Object part : parts) {
            str.append(part).append(separator);
        }
        if (str.length() > 0)
            return str.substring(0, str.length() - separator.length());

        return str.toString();
    }

    public static String join(Object[] parts, String separator, String prefix) {
        StringBuilder str = new StringBuilder();
        for (Object part : parts) {
            str.append(prefix).append(part).append(separator);
        }
        if (str.length() > 0)
            return str.substring(0, str.length() - separator.length());

        return str.toString();
    }

    public static String nodeToString(Node node) {
        try {
            Source source = new DOMSource(node);
            StringWriter stringWriter = new StringWriter();
            Result result = new StreamResult(stringWriter);
            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer();
            // just set this to get literally equal results
            transformer.setOutputProperty(OutputKeys.STANDALONE, "no");
            transformer.transform(source, result);
            return stringWriter.getBuffer().toString();
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    public static Document stringToDocument(String string) throws Exception {
        return StringUtils.inputSourceToDocument(new InputSource(new StringReader(string)));
    }

    public static Document urlToDocument(String urlString) throws Exception {
        URL url = new URL(urlString);
        InputStream stream = url.openStream();
        Document doc = StringUtils.inputSourceToDocument(new InputSource(stream));
        return doc;
    }

    public static Document inputSourceToDocument(InputSource source) throws Exception {
        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        domFactory.setNamespaceAware(true);
        DocumentBuilder builder = domFactory.newDocumentBuilder();
        Document doc = builder.parse(source);
        return doc;
    }

    public static String generateUuid() {
        UUID uuid = UUID.randomUUID();
        StringBuffer idcUuid = new StringBuffer(uuid.toString().toUpperCase());
        while (idcUuid.length() < 36) {
            idcUuid.append("0");
        }
        return idcUuid.toString();
    }

    /**
     * Prepare a string to be a valid path segment.
     * 
     * @param path
     * @return
     */
    public static String encodeForPath(String path) {
        String result = null;
        try {
            result = new URI("http", "domain.com", "/" + path, null).toURL().getPath();
            // remove trailing slash
            result = result.substring(1);
        } catch (Exception e) {
            try {
                result = URLEncoder.encode(path, "UTF-8");
            } catch (UnsupportedEncodingException e1) {
            }
            result.replaceAll("%2F", "/");
        }
        return result;
    }
}