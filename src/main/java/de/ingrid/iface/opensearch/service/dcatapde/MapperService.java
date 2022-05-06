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
import de.ingrid.iface.opensearch.model.dcatapde.catalog.*;
import de.ingrid.iface.opensearch.model.dcatapde.general.*;
import de.ingrid.iface.util.IBusHelper;
import de.ingrid.iface.util.SearchInterfaceConfig;
import de.ingrid.utils.IngridHit;
import de.ingrid.utils.IngridHitDetail;
import de.ingrid.utils.PlugDescription;
import de.ingrid.utils.dsc.Record;
import de.ingrid.utils.idf.IdfTool;
import de.ingrid.utils.iplug.IPlugVersionInspector;
import de.ingrid.utils.xml.IDFNamespaceContext;
import de.ingrid.utils.xpath.XPathUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Node;
import org.dom4j.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.NodeList;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class MapperService {
    private static final XPathUtils XPATH = new XPathUtils(new IDFNamespaceContext());

    private final Logger log = LogManager.getLogger(MapperService.class);

    public static final String DISTRIBUTION_RESOURCE_POSTFIX = "#distribution";

    private final Pattern URL_PATTERN = Pattern.compile("\"url\":\\s*\"([^\"]+)\"");
    private final Pattern QUELLE_PATTERN = Pattern.compile("\"quelle\":\\s*\"([^\"]+)\"");

    @Autowired
    private FormatMapper formatMapper;

    @Autowired
    private IBusHelper iBusHelper;

    private Dataset mapDataset(Element idfDataNode) {
        Dataset dataset = new Dataset();


        Node idfMdMetadataNode = XPATH.getNode(idfDataNode,"./idf:body/idf:idfMdMetadata");

        Node abstractNode = XPATH.getNode(idfMdMetadataNode,"./gmd:identificationInfo[1]/gmd:MD_DataIdentification/gmd:abstract/gco:CharacterString");
        if (abstractNode != null) {
            dataset.setDescription(new LangTextElement(abstractNode.getTextContent().trim()));
        }

        Node titleNode = XPATH.getNode(idfMdMetadataNode,"./gmd:identificationInfo[1]/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:title/gco:CharacterString");
        if(titleNode != null) {
            dataset.setTitle(new LangTextElement(titleNode.getTextContent().trim()));
        }


        NodeList responsiblePartyNodes = XPATH.getNodeList(idfMdMetadataNode,"./gmd:identificationInfo[1]/*/gmd:pointOfContact/idf:idfResponsibleParty");
        if(responsiblePartyNodes != null) {
            for (int i = 0; i < responsiblePartyNodes.getLength(); i++) {
                Node responsiblePartyNode = responsiblePartyNodes.item(i);
                Node contactRoleNode = XPATH.getNode(responsiblePartyNode, "./gmd:role/gmd:CI_RoleCode/@codeListValue");
                if (contactRoleNode != null && contactRoleNode.getTextContent().trim().equals("pointOfContact")) {
                    dataset.setContactPoint(mapVCard(responsiblePartyNode));
                    break;
                }
            }

            for (int i = 0; i < responsiblePartyNodes.getLength(); i++) {
                Node responsiblePartyNode = responsiblePartyNodes.item(i);
                Node contactRoleNode = XPATH.getNode(responsiblePartyNode, "./gmd:role/gmd:CI_RoleCode/@codeListValue");
                if (contactRoleNode != null && contactRoleNode.getTextContent().trim().equals("publisher")) {
                    dataset.setPublisher(mapAgentWrapper(responsiblePartyNode));
                    break;
                }
            }

            List<OrganizationWrapper> originator = new ArrayList<>();
            for (int i = 0; i < responsiblePartyNodes.getLength(); i++) {
                Node responsiblePartyNode = responsiblePartyNodes.item(i);
                Node contactRoleNode = XPATH.getNode(responsiblePartyNode, "./gmd:role/gmd:CI_RoleCode/@codeListValue");
                if (contactRoleNode != null && contactRoleNode.getTextContent().trim().equals("originator")) {
                    originator.add(mapOrganizationWrapper(responsiblePartyNode));
                }
            }
            if (originator.size() > 0) {
                dataset.setOriginator(originator.toArray(new OrganizationWrapper[originator.size()]));
            }

            List<AgentWrapper> maintainer = new ArrayList<>();
            for (int i = 0; i < responsiblePartyNodes.getLength(); i++) {
                Node responsiblePartyNode = responsiblePartyNodes.item(i);
                Node contactRoleNode = XPATH.getNode(responsiblePartyNode, "./gmd:role/gmd:CI_RoleCode/@codeListValue");
                if (contactRoleNode != null && contactRoleNode.getTextContent().trim().equals("maintainer")) {
                    maintainer.add(mapAgentWrapper(responsiblePartyNode));
                }
            }
            if (maintainer.size() > 0) {
                dataset.setMaintainer(maintainer.toArray(new AgentWrapper[maintainer.size()]));
            }

            List<AgentWrapper> contributor = new ArrayList<>();
            for (int i = 0; i < responsiblePartyNodes.getLength(); i++) {
                Node responsiblePartyNode = responsiblePartyNodes.item(i);
                Node contactRoleNode = XPATH.getNode(responsiblePartyNode, "./gmd:role/gmd:CI_RoleCode/@codeListValue");
                if (contactRoleNode != null && contactRoleNode.getTextContent().trim().equals("originator")) {
                    contributor.add(mapAgentWrapper(responsiblePartyNode));
                }
            }
            if (contributor.size() > 0) {
                dataset.setContributor(contributor.toArray(new AgentWrapper[contributor.size()]));
            }

            List<OrganizationWrapper> creator = new ArrayList<>();
            for (int i = 0; i < responsiblePartyNodes.getLength(); i++) {
                Node responsiblePartyNode = responsiblePartyNodes.item(i);
                Node contactRoleNode = XPATH.getNode(responsiblePartyNode, "./gmd:role/gmd:CI_RoleCode/@codeListValue");
                if (contactRoleNode != null && contactRoleNode.getTextContent().trim().equals("creator")) {
                    creator.add(mapOrganizationWrapper(responsiblePartyNode));
                }
            }
            if (creator.size() > 0) {
                dataset.setOriginator(creator.toArray(new OrganizationWrapper[creator.size()]));
            }
        }

        // KEYWORDS
        List<String> keywords = Arrays.asList(XPATH.getStringArray(idfMdMetadataNode, "./gmd:identificationInfo[1]/*/gmd:descriptiveKeywords/*/gmd:keyword/gco:CharacterString"));
        if (keywords.size() > 0) {
            dataset.setKeyword(keywords);
        }

        List<String> categories = Arrays.asList(XPATH.getStringArray(idfMdMetadataNode, "./gmd:identificationInfo[1]/gmd:MD_DataIdentification/gmd:topicCategory/gmd:MD_TopicCategoryCode"));
        if (categories.size() > 0 || keywords.size() > 0) {
            Collection<Theme> themes = ThemeMapper.mapThemes(categories, keywords);
            if (themes.size() > 0) {
                dataset.setThemes(themes.stream().map(theme -> "http://publications.europa.eu/resource/authority/data-theme/" + theme.toString()).toArray(String[]::new));
            }
        }


        String modified = getDateOrDateTime(XPATH.getNode(idfMdMetadataNode,"./gmd:dateStamp"));
        dataset.setModified(modified);

        String issued = getDateOrDateTime(XPATH.getNode(idfMdMetadataNode,"./gmd:identificationInfo[1]/*/gmd:citation/gmd:CI_Citation/gmd:date/gmd:CI_Date/gmd:date"));
        dataset.setIssued(issued);

        // Distribution
        List<ResourceElement> distResources = new ArrayList<>();
        NodeList transferOptionNodes = XPATH.getNodeList(idfMdMetadataNode, "./gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions");
        if(transferOptionNodes != null) {
            for (int i = 0; i < transferOptionNodes.getLength(); i++) {
                Node transferOptionNode = transferOptionNodes.item(i);
                Node linkageNode = XPATH.getNode(transferOptionNode, "./gmd:MD_DigitalTransferOptions/gmd:onLine/idf:idfOnlineResource/gmd:linkage/gmd:URL");
                if (linkageNode == null) {
                    linkageNode = XPATH.getNode(transferOptionNode, "./gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:CI_OnlineResource/gmd:linkage/gmd:URL");
                }
                if (linkageNode == null) {
                    log.warn("Skip Distribution - No Linkage");
                    continue;
                }
                String accessURL = linkageNode.getTextContent().trim();
                distResources.add(new ResourceElement(accessURL + DISTRIBUTION_RESOURCE_POSTFIX));
            }
        }
        dataset.setDistribution(distResources);



        Node fileIdentifierNode = XPATH.getNode(idfMdMetadataNode,"./gmd:fileIdentifier/gco:CharacterString");
        if (fileIdentifierNode != null) {
            String fileIdentifier = fileIdentifierNode.getTextContent().trim();
            dataset.setIdentifier(fileIdentifierNode.getTextContent().trim());
            dataset.setAbout(SearchInterfaceConfig.getInstance().getString(SearchInterfaceConfig.METADATA_ACCESS_URL).replace("{uuid}", fileIdentifier));
        }

        Node languageNode = XPATH.getNode(idfMdMetadataNode,"./gmd:identificationInfo[1]/gmd:MD_DataIdentification/gmd:language/gmd:LanguageCode/@codeListValue");
        if(languageNode != null) {
            dataset.setLanguage(new LangTextElement(languageNode.getTextContent().trim()));
        }

            /*
        ResourceElement accrualPeriodicity = mapAccrualPeriodicity(hitDetail.getAccrual_periodicity());
        dataset.setAccrualPeriodicity(accrualPeriodicity);
*/

        // CONTRIBUTOR ID
        dataset.setContributorID(new ResourceElement(SearchInterfaceConfig.getInstance().getString(SearchInterfaceConfig.DCAT_CONTRIBUTOR_ID, "http://dcat-ap.de/def/contributors/InGrid")));


        // SPATIAL
        NodeList geographicBoundingBoxNodes = XPATH.getNodeList(idfMdMetadataNode, "./gmd:identificationInfo[1]/*/gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox");
        if(geographicBoundingBoxNodes != null) {
            for (int i = 0; i < geographicBoundingBoxNodes.getLength(); i++) {
                Node node = geographicBoundingBoxNodes.item(i);
                SpatialElement spatial = mapSpatial(node, null);
                dataset.setSpatial(spatial);
            }
        }




        // TEMPORAL
        NodeList temporalNodes = XPATH.getNodeList(idfMdMetadataNode, "./gmd:identificationInfo[1]/*/gmd:extent/gmd:EX_Extent/gmd:temporalElement/gmd:EX_TemporalExtent");
        if(temporalNodes != null) {
            for (int i = 0; i < temporalNodes.getLength(); i++) {
                Node temporalNode = temporalNodes.item(i);
                Node beginNode = XPATH.getNode(temporalNode, "./gmd:extent/gml:TimePeriod/gml:beginPosition");
                Node endNode = XPATH.getNode(temporalNode, "./gmd:extent/gml:TimePeriod/gml:endPosition");
                if ((beginNode != null && !beginNode.getTextContent().trim().isEmpty()) || (endNode != null && !endNode.getTextContent().trim().isEmpty())) {
                    PeriodOfTimeElement periodOfTimeElement = new PeriodOfTimeElement();

                    TemporalElement temporalElement = new TemporalElement();
                    temporalElement.setPeriodOfTime(periodOfTimeElement);

                    if (beginNode != null && !beginNode.getTextContent().trim().isEmpty()) {
                        DatatypeTextElement start = new DatatypeTextElement();
                        start.setDatatype("http://www.w3.org/2001/XMLSchema#dateTime");
                        start.setText(beginNode.getTextContent().trim());
                        periodOfTimeElement.setStartDate(start);
                    }

                    if (endNode != null && !endNode.getTextContent().trim().isEmpty()) {
                        DatatypeTextElement end = new DatatypeTextElement();
                        end.setDatatype("http://www.w3.org/2001/XMLSchema#dateTime");
                        end.setText(endNode.getTextContent().trim());
                        periodOfTimeElement.setEndDate(end);
                    }

                    if (dataset.getTemporal() == null) {
                        dataset.setTemporal(new ArrayList<>());
                    }
                    dataset.getTemporal().add(temporalElement);
                }
            }
        }


        NodeList constraintsNodes = XPATH.getNodeList(idfMdMetadataNode, "./gmd:identificationInfo[1]/gmd:MD_DataIdentification/gmd:resourceConstraints/gmd:MD_LegalConstraints");
        if(constraintsNodes != null) {
            for (int i = 0; i < constraintsNodes.getLength(); i++) {
                Node constraintsNode = constraintsNodes.item(i);
                Node useConstraintsNode = XPATH.getNode(constraintsNode, "./gmd:useConstraints/gmd:MD_RestrictionCode/@codeListValue");
                Node otherConstraintsNode = XPATH.getNode(constraintsNode, "./gmd:otherConstraints/gco:CharacterString");
                if (useConstraintsNode != null && otherConstraintsNode != null && useConstraintsNode.getTextContent().trim().equals("otherRestrictions")) {
                    dataset.setAccessRights(new LangTextElement(otherConstraintsNode.getTextContent().trim()));
                    break;
                }
            }
        }



        return dataset;
    }

    private OrganizationWrapper mapOrganizationWrapper(Node responsiblePartyNode) {
        OrganizationWrapper result = new OrganizationWrapper();
        result.setAgent(mapAgent(responsiblePartyNode));
        return result;
    }

    private AgentWrapper mapAgentWrapper(Node responsiblePartyNode) {
        AgentWrapper result = new AgentWrapper();
        result.setAgent(mapAgent(responsiblePartyNode));
        return result;
    }

    private Agent mapAgent(Node responsiblePartyNode) {
        Agent agent = new Agent();

        Node organisationNameNode = XPATH.getNode(responsiblePartyNode, "./gmd:organisationName/gco:CharacterString");
        Node individualNameNode = XPATH.getNode(responsiblePartyNode, "./gmd:organisationName/gco:CharacterString");
        if(organisationNameNode != null) {
            agent.setName(organisationNameNode.getTextContent().trim());
        } else if (individualNameNode != null) {
            agent.setName(individualNameNode.getTextContent().trim());
        }

        Node emailNode = XPATH.getNode(responsiblePartyNode, "./gmd:contactInfo/gmd:CI_Contact/gmd:address/gmd:CI_Address/gmd:electronicMailAddress/gco:CharacterString");
        if(emailNode != null){
            agent.setMbox(emailNode.getTextContent().trim());
        }

        Node urlNode = XPATH.getNode(responsiblePartyNode, "./gmd:contactInfo/gmd:CI_Contact/gmd:onlineResource/gmd:CI_OnlineResource/gmd:linkage/gmd:URL");
        if(urlNode != null){
            agent.setHomepage(urlNode.getTextContent().trim());
        }
        return agent;
    }

    private VCardOrganizationWrapper mapVCard(Node responsiblePartyNode) {
        VCardOrganization organization = new VCardOrganization();

        Node uuidNode = XPATH.getNode(responsiblePartyNode, "./@uuid");
        if(uuidNode != null){
            organization.setNodeID(uuidNode.getTextContent().trim());
        }

        Node organisationNameNode = XPATH.getNode(responsiblePartyNode, "./gmd:organisationName/gco:CharacterString");
        Node individualNameNode = XPATH.getNode(responsiblePartyNode, "./gmd:individualName/gco:CharacterString");
        if(organisationNameNode != null) {
            organization.setFn(organisationNameNode.getTextContent().trim());
        } else if (individualNameNode != null) {
            organization.setFn(individualNameNode.getTextContent().trim());
        }

        Node postalCodeNode = XPATH.getNode(responsiblePartyNode, "./gmd:contactInfo/gmd:CI_Contact/gmd:address/gmd:CI_Address/gmd:postalCode/gco:CharacterString");
        if(postalCodeNode != null){
            organization.setHasPostalCode(postalCodeNode.getTextContent().trim());
        }

        Node deliveryPointNode = XPATH.getNode(responsiblePartyNode, "./gmd:contactInfo/gmd:CI_Contact/gmd:address/gmd:CI_Address/gmd:deliveryPoint/gco:CharacterString");
        if(deliveryPointNode != null){
            organization.setHasStreetAddress(deliveryPointNode.getTextContent().trim());
        }

        Node cityNode = XPATH.getNode(responsiblePartyNode, "./gmd:contactInfo/gmd:CI_Contact/gmd:address/gmd:CI_Address/gmd:city/gco:CharacterString");
        if(cityNode != null){
            organization.setHasLocality(cityNode.getTextContent().trim());
        }

        Node countryNode = XPATH.getNode(responsiblePartyNode, "./gmd:contactInfo/gmd:CI_Contact/gmd:address/gmd:CI_Address/gmd:country/gco:CharacterString");
        if(countryNode != null){
            organization.setHasCountryName(countryNode.getTextContent().trim());
        }

        Node emailNode = XPATH.getNode(responsiblePartyNode, "./gmd:contactInfo/gmd:CI_Contact/gmd:address/gmd:CI_Address/gmd:electronicMailAddress/gco:CharacterString");
        if(emailNode != null){
            organization.setHasEmail(new ResourceElement(emailNode.getTextContent().trim()));
        }

        Node urlNode = XPATH.getNode(responsiblePartyNode, "./gmd:contactInfo/gmd:CI_Contact/gmd:onlineResource/gmd:CI_OnlineResource/gmd:linkage/gmd:URL");
        if(urlNode != null){
            organization.setHasURL(new ResourceElement(urlNode.getTextContent().trim()));
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

    private List<Distribution> mapDistribution(Element idfDataNode, List<String> currentDistributionUrls) {

        List<Distribution> dists = new ArrayList<>();

        Node idfMdMetadataNode = XPATH.getNode(idfDataNode, "./idf:body/idf:idfMdMetadata");

        String modified = getDateOrDateTime(XPATH.getNode(idfMdMetadataNode,"./gmd:identificationInfo[1]/*/gmd:citation/gmd:CI_Citation/gmd:date/gmd:CI_Date/gmd:date"));

        NodeList transferOptionNodes = XPATH.getNodeList(idfMdMetadataNode, "./gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions");

        NodeList formatNodes = XPATH.getNodeList(idfMdMetadataNode, "./gmd:distributionInfo/gmd:MD_Distribution/gmd:distributionFormat/gmd:MD_Format/gmd:name/gco:CharacterString|./gmd:distributionInfo/gmd:MD_Distribution/gmd:distributor/gmd:MD_Distributor/gmd:distributorFormat/gmd:MD_Format/gmd:name");

        if(transferOptionNodes != null) {
            for (int i = 0; i < transferOptionNodes.getLength(); i++) {
                Node transferOptionNode = transferOptionNodes.item(i);
                Node onlineResNode = XPATH.getNode(transferOptionNode, "./gmd:MD_DigitalTransferOptions/gmd:onLine/idf:idfOnlineResource");
                if (onlineResNode == null) {
                    onlineResNode = XPATH.getNode(transferOptionNode, "./gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:CI_OnlineResource");
                }

                if (onlineResNode == null) {
                    log.warn("Skip Distribution - No OnlineResource");
                    continue;
                }

                Node linkageNode = XPATH.getNode(onlineResNode, "./gmd:linkage/gmd:URL");

                if (linkageNode == null) {
                    log.warn("Skip Distribution - No Linkage");
                    continue;
                }

                Node functionNode = XPATH.getNode(onlineResNode, "./gmd:function/gmd:CI_OnLineFunctionCode/@codeListValue");
                if (functionNode == null || (!functionNode.getTextContent().equals("information") && !functionNode.getTextContent().equals("download"))) {
                    log.warn("Skip Distribution - Function neither information nor download");
                    continue;
                }

                String accessURL = linkageNode.getTextContent().trim();

                // skip distributions that are already added
                if (currentDistributionUrls.contains(accessURL)) {
                    continue;
                }

                currentDistributionUrls.add(accessURL);

                Distribution dist = new Distribution();
                dist.getAccessURL().setResource(accessURL);

                Node titleNode = XPATH.getNode(onlineResNode, "./gmd:name/gco:CharacterString");
                dist.setTitle(titleNode.getTextContent().trim());

                Node descriptionNode = XPATH.getNode(onlineResNode, "./gmd:description/gco:CharacterString");
                if (descriptionNode != null) {
                    dist.setDescription(descriptionNode.getTextContent().trim());
                }

                dist.setModified(new DatatypeTextElement(modified));
                dist.getModified().setDatatype("http://www.w3.org/2001/XMLSchema#dateTime");

                String format = null;
                if(formatNodes != null) {
                    for (int j = 0; j < formatNodes.getLength(); j++) {
                        Node formatNode = formatNodes.item(j);
                        format = formatMapper.map(formatNode.getTextContent().trim());
                        if (format != null) break;
                    }
                }

                if (format == null) {
                    Node applicationProfileNode = XPATH.getNode(onlineResNode, "./gmd:applicationProfile/gco:CharacterString");
                    if (applicationProfileNode != null) {
                        format = formatMapper.map(applicationProfileNode.getTextContent().trim());
                    }
                }

                if (format != null) {
                    dist.setFormat(new ResourceElement("http://publications.europa.eu/resource/authority/file-type/" + format));
                }
                dist.setAbout(accessURL + DISTRIBUTION_RESOURCE_POSTFIX);

                if (functionNode.getTextContent().equals("download")) {
                    dist.setDownloadURL(new ResourceElement(accessURL));
                } else if (functionNode.getTextContent().equals("information")) {
                    dist.setPage(new ResourceElement(accessURL));
                }

            /*
            if(distribution.getByteSize() != null){
                dist.setByteSize(new DatatypeTextElement(distribution.getByteSize().toString()));
                dist.getByteSize().setDatatype("http://www.w3.org/2001/XMLSchema#decimal");
            }
*/

                //License
                NodeList constraintsNodes = XPATH.getNodeList(idfMdMetadataNode, "./gmd:identificationInfo[1]/gmd:MD_DataIdentification/gmd:resourceConstraints/gmd:MD_LegalConstraints");

                String licenseURI = null;
                if(constraintsNodes != null) {
                    for (int constraintsNodeIndex = 0; constraintsNodeIndex < constraintsNodes.getLength(); constraintsNodeIndex++) {
                        Node constraintsNode = constraintsNodes.item(constraintsNodeIndex);
                        Node useConstraintsNode = XPATH.getNode(constraintsNode, "./gmd:useConstraints/gmd:MD_RestrictionCode/@codeListValue");
                        NodeList otherConstraintsNodes = XPATH.getNodeList(constraintsNode, "./gmd:otherConstraints/gco:CharacterString");
                        if (useConstraintsNode != null && otherConstraintsNodes != null && useConstraintsNode.getTextContent().trim().equals("otherRestrictions")) {
                            for (int otherConstraintsNodeIndex = 0; otherConstraintsNodeIndex < otherConstraintsNodes.getLength(); otherConstraintsNodeIndex++) {
                                Node otherConstraintsNode = otherConstraintsNodes.item(otherConstraintsNodeIndex);
                                Matcher urlMatcher = URL_PATTERN.matcher(otherConstraintsNode.getTextContent().trim());
                                if (urlMatcher.find()) {
                                    licenseURI = LicenseMapper.getURIFromLicenseURL(urlMatcher.group(1));
                                    dist.setLicense(new ResourceElement(licenseURI));
                                    Matcher quelleMatcher = QUELLE_PATTERN.matcher(otherConstraintsNode.getTextContent().trim());
                                    if (quelleMatcher.find()) {
                                        dist.setLicenseAttributionByText(quelleMatcher.group(1));
                                    }
                                    break;
                                }
                            }
                        }
                    }
                }
                if (licenseURI != null) {
                    dist.setLicense(new ResourceElement(licenseURI));
                } else {
                    dist.setLicense(new ResourceElement(SearchInterfaceConfig.getInstance().getString(SearchInterfaceConfig.DCAT_DEFAULT_LICENSE, "http://dcat-ap.de/def/licenses/other-open")));
                }

                dists.add(dist);
            }
        }


        return dists;

    }

    private String getDateOrDateTime(Node parent){
        Node node = XPATH.getNode(parent, "./gco:Date|./gco:DateTime");
        if(node != null)
            return node.getTextContent().trim();
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

        String north = XPATH.getNode(spatial, "./gmd:northBoundLatitude/gco:Decimal").getTextContent().trim();
        String east = XPATH.getNode(spatial, "./gmd:eastBoundLongitude/gco:Decimal").getTextContent().trim();
        String south = XPATH.getNode(spatial, "./gmd:southBoundLatitude/gco:Decimal").getTextContent().trim();
        String west = XPATH.getNode(spatial, "./gmd:westBoundLongitude/gco:Decimal").getTextContent().trim();

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

    private void mapCatalog(Catalog catalog) {
        catalog.setDescription(SearchInterfaceConfig.getInstance().getString(SearchInterfaceConfig.DCAT_CATALOG_DESCRIPTION));

        Agent agent = catalog.getPublisher().getAgent();
        agent.setName(SearchInterfaceConfig.getInstance().getString(SearchInterfaceConfig.DCAT_CATALOG_PUPLISHER_NAME));

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
                log.error(e.getMessage(), e);
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
