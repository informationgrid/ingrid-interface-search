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
        ObjectMapper objectMapper = new ObjectMapper();
        InputStream fileStream = new ClassPathResource("dcatapde/file-types.json").getInputStream();
        TypeReference<HashMap<String,List<String>>> typeRef
                = new TypeReference<HashMap<String,List<String>>>() {};
        return objectMapper.readValue(fileStream, typeRef);
    }

    public String map(String type) {

        if (type == null) {
            return null;
        }

        String mappedFormat = formatMap.get(type.toLowerCase().trim());

        return mappedFormat;

    }
}
