package de.ingrid.iface.opensearch.service.dcatapde;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FormatMapper {

    private final Logger log = LogManager.getLogger(FormatMapper.class);

    private final Map<String, String> formatMap;

    public FormatMapper() throws IOException {
        Map<String, List<String>> mapByType = getMapFromFile();

        formatMap = invertMap(mapByType);
    }

    private Map<String, String> invertMap(Map<String, List<String>> mapByType) {
        Map<String, String> map = new HashMap<>();
        mapByType.keySet().forEach( key -> {
            List<String> list = mapByType.get(key);
            list.forEach(l -> map.put(l.toLowerCase(), String.valueOf(key)));
        });
        return map;
    }

    private Map<String, List<String>> getMapFromFile() throws IOException {
        return new HashMap<>();
        /*
        ObjectMapper objectMapper = new ObjectMapper();
        InputStream fileStream = new ClassPathResource("mapping.json").getInputStream();
        TypeReference<HashMap<String,List<String>>> typeRef
                = new TypeReference<HashMap<String,List<String>>>() {};
        return objectMapper.readValue(fileStream, typeRef);

         */
    }

    public String map(String type, String url) {

        if (type == null) {
            log.warn("Format type is null!");
            return null;
        }

        String lowerCaseURL = url == null ? "" : url.toLowerCase();

        String mappedFormat = handleExcelFormats(type, lowerCaseURL);

        if (mappedFormat == null) {
            mappedFormat = formatMap.get(type.toLowerCase().trim());

            if (mappedFormat == null) {
                log.warn("FormatType not accepted: " + type + " and URL: " + url);
                return null;
            }
        }
        return mappedFormat;

    }

    private String handleExcelFormats(String type, String lowerCaseURL) {
        switch (type.trim()) {
            case "Dateidownload":
            case "FTP":
            case "SOS":
            case "WMTS":
            case "WCS":
            case "API":
                if (lowerCaseURL.contains("service=wfs")) {
                    return "WFS_SRVC";
                } else if (lowerCaseURL.contains("capabilities")) {
                    return "XML";
                } else if (lowerCaseURL.contains("geojson")) {
                    return "GeoJSON";
                } else if (lowerCaseURL.contains("f=kmz")) {
                    return "KMZ";
                } else if (lowerCaseURL.endsWith(".csv")) {
                    return "CSV";
                } else if (lowerCaseURL.endsWith(".pdf")) {
                    return "PDF";
                } else if (lowerCaseURL.endsWith(".xls")) {
                    return "XLS";
                } else if (lowerCaseURL.endsWith(".kml")) {
                    return "KML";
                } else if (lowerCaseURL.endsWith(".ovl")) {
                    return "GML";
                } else if (lowerCaseURL.endsWith(".gpx")) {
                    return "GML";
                } else if (lowerCaseURL.endsWith(".zip")) {
                    return "ZIP";
                }
            default:
                return null;
        }
    }
}
