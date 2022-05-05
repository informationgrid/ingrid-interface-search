/*
 * **************************************************-
 * ingrid-interface-search
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

import de.ingrid.iface.opensearch.model.dcatapde.Catalog;
import de.ingrid.iface.opensearch.model.dcatapde.Dataset;
import de.ingrid.iface.opensearch.model.dcatapde.DcatApDe;
import de.ingrid.iface.opensearch.model.dcatapde.Distribution;
import de.ingrid.iface.opensearch.model.dcatapde.catalog.Agent;
import de.ingrid.iface.opensearch.model.dcatapde.catalog.AgentWrapper;
import de.ingrid.iface.opensearch.model.dcatapde.catalog.VCardOrganization;
import de.ingrid.iface.opensearch.model.dcatapde.catalog.VCardOrganizationWrapper;
import de.ingrid.iface.opensearch.model.dcatapde.general.*;
import de.ingrid.iface.util.IBusHelper;
import de.ingrid.iface.util.SearchInterfaceConfig;
import de.ingrid.utils.IngridHit;
import de.ingrid.utils.IngridHitDetail;
import de.ingrid.utils.PlugDescription;
import de.ingrid.utils.dsc.Record;
import de.ingrid.utils.idf.IdfTool;
import de.ingrid.utils.iplug.IPlugVersionInspector;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dom4j.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class MapperService {

    private final Logger log = LogManager.getLogger(MapperService.class);

    public static final String DISTRIBUTION_RESOURCE_POSTFIX = "#distribution";
    public static final String PUBLISHER_RESOURCE_POSTFIX = "#publisher";

    private final Pattern URL_PATTERN = Pattern.compile("\"url\":\\s*\"([^\"]+)\"");
    private final Pattern QUELLE_PATTERN = Pattern.compile("\"quelle\":\\s*\"([^\"]+)\"");

    @Autowired
    private FormatMapper formatMapper;

    @Autowired
    private IBusHelper iBusHelper;

    private Dataset mapDataset(Element idfDataNode) {
        Dataset dataset = new Dataset();


        Node idfMdMetadataNode = idfDataNode.selectSingleNode("./idf:body/idf:idfMdMetadata");

        Node abstractNode = idfMdMetadataNode.selectSingleNode("./gmd:identificationInfo[1]/gmd:MD_DataIdentification/gmd:abstract/gco:CharacterString");
        if (abstractNode != null) {
            dataset.setDescription(new LangTextElement(abstractNode.getText().trim()));
        }

        Node titleNode = idfMdMetadataNode.selectSingleNode("./gmd:identificationInfo[1]/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:title/gco:CharacterString");
        if(titleNode != null) {
            dataset.setTitle(new LangTextElement(titleNode.getText().trim()));
        }

        // Publisher / ContactPoint
        List<Node> responsiblePartyNodes = idfMdMetadataNode.selectNodes("./gmd:identificationInfo[1]/*/gmd:pointOfContact/idf:idfResponsibleParty");
        for(Node responsiblePartyNode: responsiblePartyNodes){
            Node contactRoleNode = responsiblePartyNode.selectSingleNode("./gmd:role/gmd:CI_RoleCode/@codeListValue");
            if(contactRoleNode != null && contactRoleNode.getText().trim().equals("pointOfContact")) {
                dataset.setContactPoint(mapVCard(responsiblePartyNode));
            }
        }


        for(Node responsiblePartyNode: responsiblePartyNodes){
            Node contactRoleNode = responsiblePartyNode.selectSingleNode("./gmd:role/gmd:CI_RoleCode/@codeListValue");
            if(contactRoleNode != null && contactRoleNode.getText().trim().equals("publisher")) {
                dataset.setPublisher(mapAgent(responsiblePartyNode));
            }
        }

/*
        dataset.setPublisher(new AgentWrapper());
        Agent agent = dataset.getPublisher().getAgent();
        ESAgent[] publisher = hit.getPublisher();
        ESAgent[] displayContact = hit.getExtras().getDisplay_contact();

        // only take first one if available (see specification)
        if (publisher != null && publisher.length > 0) {
            agent.setName(publisher[0].getOrganization());
            //agent.setType(publisher[0].getOrganization());
            agent.setAbout(this.appConfig.getPortalDetailUrl() + hit.getId() + PUBLISHER_RESOURCE_POSTFIX);
        } else if (displayContact != null && displayContact.length > 0){
            agent.setName(displayContact[0].getOrganization() != null ? displayContact[0].getOrganization() : displayContact[0].getName());
            agent.setAbout(this.appConfig.getPortalDetailUrl() + hit.getId() + PUBLISHER_RESOURCE_POSTFIX);
        }

        // skip documents where no publisher could be found
        if (agent.getName() == null || agent.getName().trim().isEmpty()) {
            throw new SkipException("No publisher set in dataset: " + hit.getTitle() + " (" + hit.getId() + ")");
        }
*/


        /*
        // ContactPoint
        dataset.setContactPoint(mapContactPoint(hit.getContact_point()));

        // ORIGINATOR
        dataset.setOriginator(mapOrganizations(hit.getOriginator()));

        // CREATOR
        dataset.setCreator(mapOrganizations(hit.getCreator()));

*/


        // KEYWORDS
        List<Node> keywordNodes = idfMdMetadataNode.selectNodes("./gmd:identificationInfo[1]/*/gmd:descriptiveKeywords/*/gmd:keyword/gco:CharacterString");
        List<String> keywords = keywordNodes.stream().map(keywordNode -> keywordNode.getText().trim()).filter(keyword -> !keyword.isEmpty()).collect(Collectors.toList());
        if (keywords.size() > 0) {
            dataset.setKeyword(keywords);
        }

        List<Node> categoryNodes = idfMdMetadataNode.selectNodes("./gmd:identificationInfo[1]/gmd:MD_DataIdentification/gmd:topicCategory/gmd:MD_TopicCategoryCode");
        List<String> categories = categoryNodes.stream().map(categoryNode -> categoryNode.getText().trim()).filter(category -> !category.isEmpty()).collect(Collectors.toList());
        if (categories.size() > 0 || keywords.size() > 0) {
            Collection<Theme> themes = ThemeMapper.mapThemes(categories, keywords);
            if (themes.size() > 0) {
                dataset.setThemes(themes.stream().map(theme -> "http://publications.europa.eu/resource/authority/data-theme/" + theme.toString()).toArray(String[]::new));
            }
        }


        String modified = getDateOrDateTime(idfMdMetadataNode.selectSingleNode("./gmd:dateStamp"));
        dataset.setModified(modified);

        String issued = getDateOrDateTime(idfMdMetadataNode.selectSingleNode("./gmd:identificationInfo[1]/*/gmd:citation/gmd:CI_Citation/gmd:date/gmd:CI_Date/gmd:date"));
        dataset.setIssued(issued);

        // Distribution
        List<ResourceElement> distResources = new ArrayList<>();
        List<Node> transferOptionNodes = idfMdMetadataNode.selectNodes("./gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions");
        for (Node transferOptionNode : transferOptionNodes) {
            Node linkageNode = transferOptionNode.selectSingleNode("./gmd:MD_DigitalTransferOptions/gmd:onLine/idf:idfOnlineResource/gmd:linkage/gmd:URL");
            if(linkageNode == null) {
                linkageNode = transferOptionNode.selectSingleNode("./gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:CI_OnlineResource/gmd:linkage/gmd:URL");
            }
            if(linkageNode == null){
                log.warn("Skip Distribution - No Linkage");
                continue;
            }
            String accessURL = linkageNode.getText().trim();
            distResources.add(new ResourceElement(accessURL + DISTRIBUTION_RESOURCE_POSTFIX));
        }
        dataset.setDistribution(distResources);



        Node fileIdentifierNode = idfMdMetadataNode.selectSingleNode("./gmd:fileIdentifier/gco:CharacterString");
        if (fileIdentifierNode != null) {
            String fileIdentifier = fileIdentifierNode.getText().trim();
            dataset.setIdentifier(fileIdentifierNode.getText().trim());
            dataset.setAbout(SearchInterfaceConfig.getInstance().getString(SearchInterfaceConfig.METADATA_ACCESS_URL).replace("{uuid}", fileIdentifier));
        }

        Node languageNode = idfMdMetadataNode.selectSingleNode("./gmd:identificationInfo[1]/gmd:MD_DataIdentification/gmd:language/gmd:LanguageCode/@codeListValue");
        if(languageNode != null) {
            dataset.setLanguage(new LangTextElement(languageNode.getText().trim()));
        }

            /*
        ResourceElement accrualPeriodicity = mapAccrualPeriodicity(hitDetail.getAccrual_periodicity());
        dataset.setAccrualPeriodicity(accrualPeriodicity);
*/

        // CONTRIBUTOR ID
        dataset.setContributorID(new ResourceElement("http://dcat-ap.de/def/contributors/NUMIS"));

        // SPATIAL
        List<Node> geographicBoundingBoxNodes = idfMdMetadataNode.selectNodes("./gmd:identificationInfo[1]/*/gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox");
        if (geographicBoundingBoxNodes.size() > 0) {
            for (Node node : geographicBoundingBoxNodes) {
                SpatialElement spatial = mapSpatial(node, null);
                dataset.setSpatial(spatial);
            }
        }



        // TEMPORAL
        List<Node> temporalNodes = idfMdMetadataNode.selectNodes("./gmd:identificationInfo[1]/*/gmd:extent/gmd:EX_Extent/gmd:temporalElement/gmd:EX_TemporalExtent");
        for(Node temporalNode: temporalNodes){
            Node beginNode = temporalNode.selectSingleNode("./gmd:extent/gml:TimePeriod/gml:beginPosition");
            Node endNode = temporalNode.selectSingleNode("./gmd:extent/gml:TimePeriod/gml:endPosition");
            if((beginNode != null && !beginNode.getText().trim().isEmpty()) || (endNode != null && !endNode.getText().trim().isEmpty())){
                PeriodOfTimeElement periodOfTimeElement = new PeriodOfTimeElement();

                TemporalElement temporalElement = new TemporalElement();
                temporalElement.setPeriodOfTime(periodOfTimeElement);

                if(beginNode != null && !beginNode.getText().trim().isEmpty()){
                    DatatypeTextElement start = new DatatypeTextElement();
                    start.setDatatype("http://www.w3.org/2001/XMLSchema#dateTime");
                    start.setText(beginNode.getText().trim());
                    periodOfTimeElement.setStartDate(start);
                }

                if(endNode != null && !endNode.getText().trim().isEmpty()){
                    DatatypeTextElement end = new DatatypeTextElement();
                    end.setDatatype("http://www.w3.org/2001/XMLSchema#dateTime");
                    end.setText(endNode.getText().trim());
                    periodOfTimeElement.setEndDate(end);
                }

                if(dataset.getTemporal() == null){
                    dataset.setTemporal(new ArrayList<>());
                }
                dataset.getTemporal().add(temporalElement);
            }
        }


        List<Node> constraintsNodes = idfMdMetadataNode.selectNodes("./gmd:identificationInfo[1]/gmd:MD_DataIdentification/gmd:resourceConstraints/gmd:MD_LegalConstraints");
        for(Node constraintsNode: constraintsNodes){
            Node useConstraintsNode = constraintsNode.selectSingleNode("./gmd:useConstraints/gmd:MD_RestrictionCode/@codeListValue");
            Node otherConstraintsNode = constraintsNode.selectSingleNode("./gmd:otherConstraints/gco:CharacterString");
            if(useConstraintsNode != null && otherConstraintsNode != null && useConstraintsNode.getText().trim().equals("otherRestrictions")){
                dataset.setAccessRights(new LangTextElement(otherConstraintsNode.getText().trim()));
                break;
            }
        }



        return dataset;
    }

    private AgentWrapper mapAgent(Node responsiblePartyNode) {
        Agent agent = new Agent();

        Node organisationNameNode = responsiblePartyNode.selectSingleNode("./gmd:organisationName/gco:CharacterString");
        Node individualNameNode = responsiblePartyNode.selectSingleNode("./gmd:organisationName/gco:CharacterString");
        if(organisationNameNode != null) {
            agent.setName(organisationNameNode.getText().trim());
        } else if (individualNameNode != null) {
            agent.setName(individualNameNode.getText().trim());
        }

        Node emailNode = responsiblePartyNode.selectSingleNode("./gmd:contactInfo/gmd:CI_Contact/gmd:address/gmd:CI_Address/gmd:electronicMailAddress/gco:CharacterString");
        if(emailNode != null){
            agent.setMbox(emailNode.getText().trim());
        }

        Node urlNode = responsiblePartyNode.selectSingleNode("./gmd:contactInfo/gmd:CI_Contact/gmd:onlineResource/gmd:CI_OnlineResource/gmd:linkage/gmd:URL");
        if(urlNode != null){
            agent.setHomepage(urlNode.getText().trim());
        }

        AgentWrapper result = new AgentWrapper();
        result.setAgent(agent);
        return result;
    }

    private VCardOrganizationWrapper mapVCard(Node responsiblePartyNode) {
        VCardOrganization organization = new VCardOrganization();

        Node uuidNode = responsiblePartyNode.selectSingleNode("./@uuid");
        if(uuidNode != null){
            organization.setNodeID(uuidNode.getText().trim());
        }

        Node organisationNameNode = responsiblePartyNode.selectSingleNode("./gmd:organisationName/gco:CharacterString");
        Node individualNameNode = responsiblePartyNode.selectSingleNode("./gmd:individualName/gco:CharacterString");
        if(organisationNameNode != null) {
            organization.setFn(organisationNameNode.getText().trim());
        } else if (individualNameNode != null) {
            organization.setFn(individualNameNode.getText().trim());
        }

        Node postalCodeNode = responsiblePartyNode.selectSingleNode("./gmd:contactInfo/gmd:CI_Contact/gmd:address/gmd:CI_Address/gmd:postalCode/gco:CharacterString");
        if(postalCodeNode != null){
            organization.setHasPostalCode(postalCodeNode.getText().trim());
        }

        Node deliveryPointNode = responsiblePartyNode.selectSingleNode("./gmd:contactInfo/gmd:CI_Contact/gmd:address/gmd:CI_Address/gmd:deliveryPoint/gco:CharacterString");
        if(deliveryPointNode != null){
            organization.setHasStreetAddress(deliveryPointNode.getText().trim());
        }

        Node cityNode = responsiblePartyNode.selectSingleNode("./gmd:contactInfo/gmd:CI_Contact/gmd:address/gmd:CI_Address/gmd:city/gco:CharacterString");
        if(cityNode != null){
            organization.setHasLocality(cityNode.getText().trim());
        }

        Node countryNode = responsiblePartyNode.selectSingleNode("./gmd:contactInfo/gmd:CI_Contact/gmd:address/gmd:CI_Address/gmd:country/gco:CharacterString");
        if(countryNode != null){
            organization.setHasCountryName(countryNode.getText().trim());
        }

        Node emailNode = responsiblePartyNode.selectSingleNode("./gmd:contactInfo/gmd:CI_Contact/gmd:address/gmd:CI_Address/gmd:electronicMailAddress/gco:CharacterString");
        if(emailNode != null){
            organization.setHasEmail(new ResourceElement(emailNode.getText().trim()));
        }

        Node urlNode = responsiblePartyNode.selectSingleNode("./gmd:contactInfo/gmd:CI_Contact/gmd:onlineResource/gmd:CI_OnlineResource/gmd:linkage/gmd:URL");
        if(urlNode != null){
            organization.setHasURL(new ResourceElement(urlNode.getText().trim()));
        }

        VCardOrganizationWrapper result = new VCardOrganizationWrapper();
        result.setOrganization(organization);

        return result;
    }

    /*
        private VCardOrganizationWrapper mapContactPoint(ESContactPoint contactPoint) {
            if(contactPoint == null){
                return null;
            }

            VCardOrganization organization = new VCardOrganization();

            if(contactPoint.getOrganizationName() != null){
                organization.setFn(contactPoint.getOrganizationName());
            } else if(contactPoint.getFn() != null){
                organization.setFn(contactPoint.getFn());
            }

            if(contactPoint.getStreetAddress() != null) organization.setHasStreetAddress(contactPoint.getStreetAddress());
            if(contactPoint.getPostalCode() != null) organization.setHasPostalCode(contactPoint.getPostalCode());
            if(contactPoint.getRegion() != null) organization.setHasLocality(contactPoint.getRegion());
            if(contactPoint.getCountryName() != null) organization.setHasCountryName(contactPoint.getCountryName());

            if(contactPoint.getHasEmail() != null) {
                organization.setHasEmail(new ResourceElement(contactPoint.getHasEmail()));
            }

            if(contactPoint.getHasURL() != null) {
                organization.setHasURL(new ResourceElement(contactPoint.getHasURL()));
            }

            VCardOrganizationWrapper wrapper = new VCardOrganizationWrapper();
            wrapper.setOrganization(organization);

            return wrapper;
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

        private ResourceElement mapAccrualPeriodicity(String accrualPeriodicity) {
            if(accrualPeriodicity == null || accrualPeriodicity.trim().isEmpty()){
                return null;
            }
            String lowerCase = accrualPeriodicity.toLowerCase();
            if(lowerCase.startsWith("http://publications.europa.eu/resource/authority/frequency/")){
                return new ResourceElement(accrualPeriodicity);
            }

            if(PERIODICITIES.contains(accrualPeriodicity))
                return new ResourceElement("http://publications.europa.eu/resource/authority/frequency/" + accrualPeriodicity);


            if(PERIODICITIES_CONT.contains(accrualPeriodicity))
                return new ResourceElement("http://publications.europa.eu/resource/authority/frequency/CONT");

            log.warn("AccrualPeriodicity not accepted: "+accrualPeriodicity);

            return null;
        }
        */
/*
        private OrganizationWrapper[] mapOrganizations(ESAgent[] agents) {
            if (agents == null) {
                return null;
            }

            List<OrganizationWrapper> wrapper = new ArrayList<>();
            for (ESAgent creator : agents) {
                String name = creator.getName();
                if (name == null || name.isEmpty()) {
                    name = creator.getOrganization();
                }
                OrganizationWrapper aw = new OrganizationWrapper();
                aw.getAgent().setName(name);
                aw.getAgent().setHomepage(creator.getHomepage());
                aw.getAgent().setMbox(creator.getMbox());

                wrapper.add(aw);
            }
            return wrapper.toArray(new OrganizationWrapper[0]);
        }
    */
    private List<Distribution> mapDistribution(Element idfDataNode, List<String> currentDistributionUrls) {

        List<Distribution> dists = new ArrayList<>();

        Node idfMdMetadataNode = idfDataNode.selectSingleNode("./idf:body/idf:idfMdMetadata");

        String modified = getDateOrDateTime(idfMdMetadataNode.selectSingleNode("./gmd:identificationInfo[1]/*/gmd:citation/gmd:CI_Citation/gmd:date/gmd:CI_Date/gmd:date"));

        List<Node> transferOptionNodes = idfMdMetadataNode.selectNodes("./gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions");

        for (Node transferOptionNode : transferOptionNodes) {
            Node onlineResNode = transferOptionNode.selectSingleNode("./gmd:MD_DigitalTransferOptions/gmd:onLine/idf:idfOnlineResource");
            if(onlineResNode == null){
                onlineResNode = transferOptionNode.selectSingleNode("./gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:CI_OnlineResource");
            }

            if(onlineResNode == null){
                log.warn("Skip Distribution - No OnlineResource");
                continue;
            }

            Node linkageNode = onlineResNode.selectSingleNode("./gmd:linkage/gmd:URL");

            if(linkageNode == null){
                log.warn("Skip Distribution - No Linkage");
                continue;
            }

            Node functionNode = onlineResNode.selectSingleNode("./gmd:function/gmd:CI_OnLineFunctionCode/@codeListValue");
            if(functionNode == null || (!functionNode.getText().equals("information") && !functionNode.getText().equals("download"))){
                log.warn("Skip Distribution - Function neither information nor download");
                continue;
            }

            String accessURL = linkageNode.getText().trim();

            // skip distributions that are already added
            if (currentDistributionUrls.contains(accessURL)) {
                continue;
            }

            currentDistributionUrls.add(accessURL);

            Distribution dist = new Distribution();
            dist.getAccessURL().setResource(accessURL);

            Node titleNode = onlineResNode.selectSingleNode("./gmd:name/gco:CharacterString");
            dist.setTitle(titleNode.getText().trim());

            Node descriptionNode = onlineResNode.selectSingleNode("./gmd:description/gco:CharacterString");
            if(descriptionNode != null){
                dist.setDescription(descriptionNode.getText().trim());
            }

            dist.setModified(new DatatypeTextElement(modified));
            dist.getModified().setDatatype("http://www.w3.org/2001/XMLSchema#dateTime");

            String format = null;//mapFormat(accessURL, distribution.getFormat());

            if (format != null) {
                dist.setFormat(new ResourceElement("http://publications.europa.eu/resource/authority/file-type/" + format));
            }
            dist.setAbout(accessURL + DISTRIBUTION_RESOURCE_POSTFIX);

            if(functionNode.getText().equals("download")) {
                dist.setDownloadURL(new ResourceElement(accessURL));
            }

            /*
            if(distribution.getByteSize() != null){
                dist.setByteSize(new DatatypeTextElement(distribution.getByteSize().toString()));
                dist.getByteSize().setDatatype("http://www.w3.org/2001/XMLSchema#decimal");
            }
*/

            //License
            List<Node> constraintsNodes = idfMdMetadataNode.selectNodes("./gmd:identificationInfo[1]/gmd:MD_DataIdentification/gmd:resourceConstraints/gmd:MD_LegalConstraints");

            String licenseURI = null;
            for(Node constraintsNode: constraintsNodes){
                Node useConstraintsNode = constraintsNode.selectSingleNode("./gmd:useConstraints/gmd:MD_RestrictionCode/@codeListValue");
                List<Node> otherConstraintsNodes = constraintsNode.selectNodes("./gmd:otherConstraints/gco:CharacterString");
                if(useConstraintsNode != null && otherConstraintsNodes != null && useConstraintsNode.getText().trim().equals("otherRestrictions")){
                    for(Node otherConstraintsNode: otherConstraintsNodes) {
                        Matcher urlMatcher = URL_PATTERN.matcher(otherConstraintsNode.getText().trim());
                        if (urlMatcher.find()) {
                            licenseURI = LicenseMapper.getURIFromLicenseURL(urlMatcher.group(1));
                            dist.setLicense(new ResourceElement(licenseURI));
                            Matcher quelleMatcher = QUELLE_PATTERN.matcher(otherConstraintsNode.getText().trim());
                            if (quelleMatcher.find()) {
                                dist.setLicenseAttributionByText(quelleMatcher.group(1));
                            }
                            break;
                        }
                    }
                }
            }
            if(licenseURI != null) {
                dist.setLicense(new ResourceElement(licenseURI));
            } else {
                dist.setLicense(new ResourceElement("http://dcat-ap.de/def/licenses/other-open"));
            }

            dists.add(dist);
        }



        return dists;

    }

    private String getDateOrDateTime(Node parent){
        Node node = parent.selectSingleNode("./gco:Date|./gco:DateTime");
        if(node != null)
            return node.getText().trim();
        return null;
    }


    private SpatialElement mapSpatial(Node spatial, String spatialText) {
        if (spatial == null) {
            return null;
        }

        List<DatatypeTextElement> geometries = new ArrayList<>();

        DatatypeTextElement geoJSON = new DatatypeTextElement();
        geoJSON.setDatatype("https://www.iana.org/assignments/media-types/application/vnd.geo+json");
        geoJSON.setText(mapGeoJson(spatial));
        geometries.add(geoJSON);

        LocationElement location = new LocationElement();
        location.setGeometry(geometries);

        if (spatialText != null) {
            location.setPrefLabel(spatialText);
        }

        SpatialElement result = new SpatialElement();
        result.setLocation(location);

        return result;
    }

    private String mapGeoJson(Node spatial) {
        String type = "Polygon";

        String north = spatial.selectSingleNode("./gmd:northBoundLatitude/gco:Decimal").getText().trim();
        String east = spatial.selectSingleNode("./gmd:eastBoundLongitude/gco:Decimal").getText().trim();
        String south = spatial.selectSingleNode("./gmd:southBoundLatitude/gco:Decimal").getText().trim();
        String west = spatial.selectSingleNode("./gmd:westBoundLongitude/gco:Decimal").getText().trim();

        String coordinates = "[[[" + east + ", " + north + "], " +
                "[" + east + ", " + south + "], " +
                "[" + west + ", " + south + "], " +
                "[" + west + ", " + north + "], " +
                "[" + east + ", " + north + "]]]";
        return "{" +
                "\"type\": \"" + type + "\"" +
                ", \"coordinates\": " + coordinates +
                '}';
    }

    /**
     * Map data format to a DCAT-AP.de compatible format. Especially imported data from Excel file
     * has ambigous data formats.
     * See: http://publications.europa.eu/resource/authority/file-type
     *
     * @param url   is the URL of the given ling
     * @param types is the given type that shall define the target of the URL
     * @return the conform target type for DCAT-AP.de
     */
    private String mapFormat(String url, String[] types) {

        // only use the first format in case multiple formats are available
        String type = types == null || types.length == 0 ? null : types[0];

        return formatMapper.map(type, url);

    }

    private void mapCatalog(Catalog catalog) {
        catalog.setDescription(SearchInterfaceConfig.getInstance().getString(SearchInterfaceConfig.DCAT_CATALOG_DESCRIPTION));

        Agent agent = catalog.getPublisher().getAgent();
        agent.setName(SearchInterfaceConfig.getInstance().getString(SearchInterfaceConfig.DCAT_CATALOG_PUPLISHER_NAME));
        agent.setAbout(SearchInterfaceConfig.getInstance().getString(SearchInterfaceConfig.DCAT_CATALOG_PUPLISHER_NAME));

        catalog.setTitle(SearchInterfaceConfig.getInstance().getString(SearchInterfaceConfig.DCAT_CATALOG_TITLE));
        catalog.setHomepage(new ResourceElement(SearchInterfaceConfig.getInstance().getString(SearchInterfaceConfig.DCAT_CATALOG_PUPLISHER_URL)));
        catalog.setAbout(SearchInterfaceConfig.getInstance().getString(SearchInterfaceConfig.DCAT_CATALOG_PUPLISHER_URL));
    }

    public DcatApDe mapHitsToDcat(Iterable<IngridHit> hits) {
        DcatApDe dcatApDe = new DcatApDe();
        List<Dataset> datasets = new ArrayList<>();
        List<Distribution> distributions = new ArrayList<>();
        List<String> datasetIds = new ArrayList<>();
        List<String> distributionsIds = new ArrayList<>();

        for (IngridHit hit : hits) {
            try {

                IngridHitDetail hitDetail = hit.getHitDetail();
                String plugId = hit.getPlugId();
                Element idfDataNode = null;
                PlugDescription plugDescription = iBusHelper.getPlugdescription(plugId);
                if (IPlugVersionInspector.getIPlugVersion(plugDescription).equals(IPlugVersionInspector.VERSION_IDF_1_0_DSC_OBJECT)) {
                    // add IDF data
                    Record idfRecord = (Record) hitDetail.get("idfRecord");
                    if (idfRecord == null) {
                        idfRecord = iBusHelper.getRecord(hit);
                    }
                    if (idfRecord != null) {
                        String idfData = IdfTool.getIdfDataFromRecord(idfRecord);
                        Document idfDoc = DocumentHelper.parseText(idfData);
                        idfDataNode = idfDoc.getRootElement();
                    }
                }

                // mapDataset and mapDistribution can throw an exception to signal
                // that the document has to be skipped
                Dataset dataset = mapDataset(idfDataNode);
                List<Distribution> distributionList = mapDistribution(idfDataNode, distributionsIds);

                datasets.add(dataset);
                //datasetIds.add(this.appConfig.getPortalDetailUrl() + hit.getId());
                datasetIds.add(hit.getId().toString());
                distributions.addAll(distributionList);
            } catch (Exception e) {
                log.error(e);
                log.warn("Document has been skipped: " + e.getMessage());
            }
        }
        mapCatalog(dcatApDe.getCatalog());
        dcatApDe.getCatalog().setDataset(datasetIds.toArray(new String[0]));
        dcatApDe.setDataset(datasets);
        dcatApDe.setDistribution(distributions);
        return dcatApDe;
    }
}
