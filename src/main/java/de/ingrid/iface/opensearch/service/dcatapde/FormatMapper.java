/*-
 * **************************************************-
 * InGrid Interface Search
 * ==================================================
 * Copyright (C) 2014 - 2022 wemove digital solutions GmbH
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
