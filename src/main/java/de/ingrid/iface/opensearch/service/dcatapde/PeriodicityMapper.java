/*-
 * **************************************************-
 * InGrid Interface Search
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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@Service
public class PeriodicityMapper {

    private final Logger log = LogManager.getLogger(PeriodicityMapper.class);

    private final Map<String, String> periodicityMap;

    public PeriodicityMapper() throws IOException {
        Map<String, List<String>> mapByType = getMapFromFile();

        periodicityMap = invertMap(mapByType);
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
        InputStream fileStream = new ClassPathResource("dcatapde/periodicity.json").getInputStream();
        TypeReference<HashMap<String,List<String>>> typeRef
                = new TypeReference<HashMap<String,List<String>>>() {};
        return objectMapper.readValue(fileStream, typeRef);
    }

    public String map(String type) {

        if (type == null) {
            return null;
        }

        String mappedPeriodicity = periodicityMap.get(type.toLowerCase().trim());

        if(PERIODICITIES.contains(mappedPeriodicity))
            return "http://publications.europa.eu/resource/authority/frequency/" + mappedPeriodicity;


        if(PERIODICITIES_CONT.contains(mappedPeriodicity))
            return "http://publications.europa.eu/resource/authority/frequency/CONT";

        return null;

    }


    private static List<String> PERIODICITIES = new ArrayList<>();
    {
        String[] array = {"ANNUAL","ANNUAL_2","ANNUAL_3","BIENNIAL","BIMONTHLY","BIWEEKLY","CONT","DAILY","DAILY_2","IRREG","MONTHLY","MONTHLY_2","MONTHLY_3","NEVER","OP_DATPRO","QUARTERLY","TRIENNIAL","UNKNOWN","UPDATE_CONT","WEEKLY","WEEKLY_2","WEEKLY_3","QUINQUENNIAL","DECENNIAL","HOURLY","QUADRENNIAL","BIHOURLY","TRIHOURLY","BIDECENNIAL","TRIDECENNIAL","OTHER"};
        PERIODICITIES = Arrays.asList(array);
    }

    private static List<String> PERIODICITIES_CONT = new ArrayList<>();
    {
        String[] array = {"MINUTLY", "BIMINUTLY", "FIVEMINUTLY", "TENMINUTLY", "FIFTEENMINUTLY"};
        PERIODICITIES_CONT = Arrays.asList(array);
    }

}
