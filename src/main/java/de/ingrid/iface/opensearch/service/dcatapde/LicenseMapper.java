/*-
 * **************************************************-
 * InGrid Interface Search
 * ==================================================
 * Copyright (C) 2014 - 2024 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.2 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * **************************************************#
 */
package de.ingrid.iface.opensearch.service.dcatapde;

import de.ingrid.iface.opensearch.model.dcatapde.LicenseInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

@Service
public class LicenseMapper {

    private static Logger log = LogManager.getLogger(LicenseMapper.class);

    private static List<LicenseInfo> licenses = prepareLicenses(getProperties("dcatapde/licenses.properties"));

    private static Properties getProperties(String filename) {
        Properties licenseProps = new Properties();
        ClassPathResource propFile = new ClassPathResource(filename);
        try {
            licenseProps.load(propFile.getInputStream());
            return licenseProps;
        } catch (IOException e) {
            log.error("Error reading license.properties", e);
        }
        return null;
    }

    /**
     * Maps license text URL to DCAT-AP.de URI
     * See: https://www.dcat-ap.de/def/licenses/
     *
     * @param licenseUrl is the URL of license text
     * @return the license
     */
    public static String getURIFromLicenseURL(String licenseUrl) {

        if (licenseUrl == null || licenseUrl.trim().isEmpty()) {
            log.warn("License is empty");
            return null;
        }

        int protocolIndex = licenseUrl.indexOf("://");
        String licenseUrlWithoutProtocol = protocolIndex == -1 ? licenseUrl : licenseUrl.substring(protocolIndex + 3);
        Optional<LicenseInfo> licenseInfo = licenses.stream()
                .filter(
                        l -> l.getTextUris() != null &&
                                Arrays.stream(l.getTextUris())
                                        .anyMatch(textUri -> textUri.endsWith(licenseUrlWithoutProtocol.trim())))
                .findFirst();

        if (!licenseInfo.isPresent()) {
            log.warn("License could not be mapped: " + licenseUrl);
            return null;
        }

        return licenseInfo.get().getUri();
    }

    private static List<LicenseInfo> prepareLicenses(Properties props) {
        return props.keySet().stream()
                .map(key -> {
                    LicenseInfo licenseInfo = new LicenseInfo();
                    licenseInfo.setId((String) key);

                    String[] values = props.getProperty((String) key).split(";");

                    licenseInfo.setName(values[1]);
                    licenseInfo.setUri(values[0]);
                    if (values.length > 2) licenseInfo.setTextUris(values[2].split(","));
                    return licenseInfo;
                }).collect(Collectors.toList());
    }

}
