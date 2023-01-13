/*
 * **************************************************-
 * ingrid-interface-search
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

import de.ingrid.iface.opensearch.model.dcatapde.Catalog;
import de.ingrid.iface.opensearch.model.dcatapde.Dataset;
import de.ingrid.iface.opensearch.model.dcatapde.DcatApDe;
import de.ingrid.iface.opensearch.model.dcatapde.Distribution;
import de.ingrid.iface.opensearch.model.dcatapde.catalog.*;
import de.ingrid.iface.opensearch.model.dcatapde.general.*;
import de.ingrid.iface.util.IBusHelper;
import de.ingrid.iface.util.IBusQueryResultIterator;
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
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
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
    private PeriodicityMapper periodicityMapper;

    @Autowired
    private IBusHelper iBusHelper;

    private Dataset mapDataset(Element idfDataNode) {
        Dataset dataset = new Dataset();


        Node idfMdMetadataNode = XPATH.getNode(idfDataNode,"./body/idfMdMetadata");

        Node abstractNode = XPATH.getNode(idfMdMetadataNode,"./identificationInfo[1]/MD_DataIdentification/abstract/CharacterString|./identificationInfo[1]/SV_ServiceIdentification/abstract/CharacterString");
        if (abstractNode != null) {
            dataset.setDescription(new LangTextElement(abstractNode.getTextContent().trim()));
        }

        Node titleNode = XPATH.getNode(idfMdMetadataNode,"./identificationInfo[1]/MD_DataIdentification/citation/CI_Citation/title/CharacterString|./identificationInfo[1]/SV_ServiceIdentification/citation/CI_Citation/title/CharacterString");
        if(titleNode != null) {
            dataset.setTitle(new LangTextElement(titleNode.getTextContent().trim()));
        }


        NodeList responsiblePartyNodes = XPATH.getNodeList(idfMdMetadataNode,"./identificationInfo[1]/*/pointOfContact/idfResponsibleParty");
        if(responsiblePartyNodes != null) {
            for (int i = 0; i < responsiblePartyNodes.getLength(); i++) {
                Node responsiblePartyNode = responsiblePartyNodes.item(i);
                Node contactRoleNode = XPATH.getNode(responsiblePartyNode, "./role/CI_RoleCode/@codeListValue");
                if (contactRoleNode != null && contactRoleNode.getTextContent().trim().equals("pointOfContact")) {
                    dataset.setContactPoint(mapVCard(responsiblePartyNode));
                    break;
                }
            }

            Node responsiblePartyForPublisherNode = null;
            for (int i = 0; i < responsiblePartyNodes.getLength(); i++) {
                Node responsiblePartyNode = responsiblePartyNodes.item(i);
                Node contactRoleNode = XPATH.getNode(responsiblePartyNode, "./role/CI_RoleCode/@codeListValue");
                if (contactRoleNode != null && contactRoleNode.getTextContent().trim().equals("publisher")) {
                    responsiblePartyForPublisherNode = responsiblePartyNode;
                    break;
                } else if (contactRoleNode != null && contactRoleNode.getTextContent().trim().equals("publisher") && responsiblePartyForPublisherNode == null) {
                    responsiblePartyForPublisherNode = responsiblePartyNode;
                }
            }
            if(responsiblePartyForPublisherNode == null && responsiblePartyNodes.getLength() > 0){
                responsiblePartyForPublisherNode = responsiblePartyNodes.item(0);
            }
            if(responsiblePartyForPublisherNode != null && responsiblePartyNodes.getLength() > 0){
                dataset.setPublisher(mapAgentWrapper(responsiblePartyForPublisherNode));
            }

            List<OrganizationWrapper> originator = new ArrayList<>();

            for (int i = 0; i < responsiblePartyNodes.getLength(); i++) {
                Node responsiblePartyNode = responsiblePartyNodes.item(i);
                Node contactRoleNode = XPATH.getNode(responsiblePartyNode, "./role/CI_RoleCode/@codeListValue");
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
                Node contactRoleNode = XPATH.getNode(responsiblePartyNode, "./role/CI_RoleCode/@codeListValue");
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
                Node contactRoleNode = XPATH.getNode(responsiblePartyNode, "./role/CI_RoleCode/@codeListValue");
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
                Node contactRoleNode = XPATH.getNode(responsiblePartyNode, "./role/CI_RoleCode/@codeListValue");
                if (contactRoleNode != null && contactRoleNode.getTextContent().trim().equals("creator")) {
                    creator.add(mapOrganizationWrapper(responsiblePartyNode));
                }
            }
            if (creator.size() > 0) {
                dataset.setOriginator(creator.toArray(new OrganizationWrapper[creator.size()]));
            }
        }

        // KEYWORDS
        List<String> keywords = Arrays.asList(XPATH.getStringArray(idfMdMetadataNode, "./identificationInfo[1]/*/descriptiveKeywords/*/keyword/CharacterString"));
        if (keywords.size() > 0) {
            dataset.setKeyword(keywords);
        }

        List<String> categories = Arrays.asList(XPATH.getStringArray(idfMdMetadataNode, "./identificationInfo[1]/MD_DataIdentification/topicCategory/MD_TopicCategoryCode"));
        if (categories.size() > 0 || keywords.size() > 0) {
            Collection<Theme> themes = ThemeMapper.mapThemes(categories, keywords);
            if (themes.size() > 0) {
                dataset.setThemes(themes.stream().map(theme -> "http://publications.europa.eu/resource/authority/data-theme/" + theme.toString()).toArray(String[]::new));
            }
            else {
                log.warn("No Themes!");
            }
        }
        else {
            log.warn("No Keywords or Categories!");
        }


        String modified = getDateOrDateTime(XPATH.getNode(idfMdMetadataNode,"./dateStamp"));
        dataset.setModified(modified);

        String issued = getDateOrDateTime(XPATH.getNode(idfMdMetadataNode,"./identificationInfo[1]/*/citation/CI_Citation/date/CI_Date/date"));
        dataset.setIssued(issued);

        // Distribution
        List<ResourceElement> distResources = new ArrayList<>();
        NodeList transferOptionNodes = XPATH.getNodeList(idfMdMetadataNode, "./distributionInfo/MD_Distribution/transferOptions");
        if(transferOptionNodes != null) {
            for (int i = 0; i < transferOptionNodes.getLength(); i++) {
                Node transferOptionNode = transferOptionNodes.item(i);
                Node linkageNode = XPATH.getNode(transferOptionNode, "./MD_DigitalTransferOptions/onLine/idfOnlineResource/linkage/URL");
                if (linkageNode == null) {
                    linkageNode = XPATH.getNode(transferOptionNode, "./MD_DigitalTransferOptions/onLine/CI_OnlineResource/linkage/URL");
                }
                if (linkageNode == null) {
                    log.warn("Skip Distribution - No Linkage");
                    continue;
                }
                String accessURL = linkageNode.getTextContent().trim();
                distResources.add(new ResourceElement(accessURL + DISTRIBUTION_RESOURCE_POSTFIX));
            }
        }
        Node serviceIdentificationNode = XPATH.getNode(idfMdMetadataNode, "./identificationInfo[1]/SV_ServiceIdentification");
        if(serviceIdentificationNode != null) {
            NodeList containsOperationsNodes = XPATH.getNodeList(serviceIdentificationNode, "./containsOperations");
            for (int i = 0; i < containsOperationsNodes.getLength(); i++) {
                Node containsOperationsNode = containsOperationsNodes.item(i);
                Node linkageNode = XPATH.getNode(containsOperationsNode, "./SV_OperationMetadata/connectPoint/CI_OnlineResource/linkage/URL");
                if (linkageNode != null) {
                    String accessURL = linkageNode.getTextContent().trim();
                    distResources.add(new ResourceElement(accessURL + DISTRIBUTION_RESOURCE_POSTFIX));
                }
            }
        }
        dataset.setDistribution(distResources);



        Node fileIdentifierNode = XPATH.getNode(idfMdMetadataNode,"./fileIdentifier/CharacterString");
        if (fileIdentifierNode != null) {
            String fileIdentifier = fileIdentifierNode.getTextContent().trim();
            dataset.setIdentifier(fileIdentifierNode.getTextContent().trim());
            dataset.setAbout(SearchInterfaceConfig.getInstance().getString(SearchInterfaceConfig.METADATA_ACCESS_URL).replace("{uuid}", fileIdentifier));
        }

        /*
        Node languageNode = XPATH.getNode(idfMdMetadataNode,"./identificationInfo[1]/MD_DataIdentification/language/LanguageCode/@codeListValue");
        if(languageNode != null) {
            dataset.setLanguage(new LangTextElement(languageNode.getTextContent().trim()));
        }
        */

        Node maintenanceFrequencyNode = XPATH.getNode(idfMdMetadataNode,"./identificationInfo[1]/*/resourceMaintenance/MD_MaintenanceInformation/maintenanceAndUpdateFrequency/MD_MaintenanceFrequencyCode/@codeListValue");
        if(maintenanceFrequencyNode != null) {
            String periodicity = periodicityMapper.map(maintenanceFrequencyNode.getTextContent().trim());
            if(periodicity != null){
                dataset.setAccrualPeriodicity(new ResourceElement(periodicity));
            } else {
                log.warn("Unknown Periodicity: "+maintenanceFrequencyNode.getTextContent().trim());
            }
        }

        // CONTRIBUTOR ID
        dataset.setContributorID(new ResourceElement(SearchInterfaceConfig.getInstance().getString(SearchInterfaceConfig.DCAT_CONTRIBUTOR_ID, "http://dcat-ap.de/def/contributors/InGrid")));


        // SPATIAL
        NodeList geographicBoundingBoxNodes = XPATH.getNodeList(idfMdMetadataNode, "./identificationInfo[1]/*/extent/EX_Extent/geographicElement/EX_GeographicBoundingBox");
        if(geographicBoundingBoxNodes != null) {
            for (int i = 0; i < geographicBoundingBoxNodes.getLength(); i++) {
                Node node = geographicBoundingBoxNodes.item(i);
                SpatialElement spatial = mapSpatial(node, null);
                dataset.setSpatial(spatial);
            }
        }




        // TEMPORAL
        NodeList temporalNodes = XPATH.getNodeList(idfMdMetadataNode, "./identificationInfo[1]/*/extent/EX_Extent/temporalElement/EX_TemporalExtent");
        if(temporalNodes != null) {
            for (int i = 0; i < temporalNodes.getLength(); i++) {
                Node temporalNode = temporalNodes.item(i);
                Node beginNode = XPATH.getNode(temporalNode, "./extent/TimePeriod/beginPosition");
                Node endNode = XPATH.getNode(temporalNode, "./extent/TimePeriod/endPosition");
                if ((beginNode != null && !beginNode.getTextContent().trim().isEmpty()) || (endNode != null && !endNode.getTextContent().trim().isEmpty())) {
                    PeriodOfTimeElement periodOfTimeElement = new PeriodOfTimeElement();

                    TemporalElement temporalElement = new TemporalElement();
                    temporalElement.setPeriodOfTime(periodOfTimeElement);

                    if (beginNode != null && !beginNode.getTextContent().trim().isEmpty()) {
                        DatatypeTextElement start = new DatatypeTextElement();
                        if(beginNode.getTextContent().contains("T"))
                            start.setDatatype("http://www.w3.org/2001/XMLSchema#dateTime");
                        else
                            start.setDatatype("http://www.w3.org/2001/XMLSchema#date");
                        start.setText(beginNode.getTextContent().trim());
                        periodOfTimeElement.setStartDate(start);
                    }

                    if (endNode != null && !endNode.getTextContent().trim().isEmpty()) {
                        DatatypeTextElement end = new DatatypeTextElement();
                        if(endNode.getTextContent().contains("T"))
                            end.setDatatype("http://www.w3.org/2001/XMLSchema#dateTime");
                        else
                            end.setDatatype("http://www.w3.org/2001/XMLSchema#date");
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


        NodeList constraintsNodes = XPATH.getNodeList(idfMdMetadataNode, "./identificationInfo[1]/MD_DataIdentification/resourceConstraints/MD_LegalConstraints|./identificationInfo[1]/SV_ServiceIdentification/resourceConstraints/MD_LegalConstraints");
        String accessRights = null;
        if(constraintsNodes != null) {
            for (int i = 0; i < constraintsNodes.getLength(); i++) {
                Node constraintsNode = constraintsNodes.item(i);
                Node useConstraintsNode = XPATH.getNode(constraintsNode, "./useConstraints/MD_RestrictionCode/@codeListValue");
                NodeList otherConstraintsNodes = XPATH.getNodeList(constraintsNode, "./otherConstraints/CharacterString");
                for (int j = 0; j < otherConstraintsNodes.getLength(); j++) {
                    Node otherConstraintsNode = otherConstraintsNodes.item(j);
                    if (useConstraintsNode != null && otherConstraintsNode != null && useConstraintsNode.getTextContent().trim().equals("otherRestrictions")) {
                        Matcher urlMatcher = URL_PATTERN.matcher(otherConstraintsNode.getTextContent().trim());
                        if (!urlMatcher.find()) {
                            accessRights = otherConstraintsNode.getTextContent();
                            break;
                        }
                    }
                }
            }
            if(accessRights != null){
                dataset.setAccessRights(new LangTextElement(accessRights));
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

        Node organisationNameNode = XPATH.getNode(responsiblePartyNode, "./organisationName/CharacterString");
        Node individualNameNode = XPATH.getNode(responsiblePartyNode, "./organisationName/CharacterString");
        if(organisationNameNode != null) {
            agent.setName(organisationNameNode.getTextContent().trim());
        } else if (individualNameNode != null) {
            agent.setName(individualNameNode.getTextContent().trim());
        }

        Node emailNode = XPATH.getNode(responsiblePartyNode, "./contactInfo/CI_Contact/address/CI_Address/electronicMailAddress/CharacterString");
        if(emailNode != null){
            agent.setMbox(emailNode.getTextContent().trim());
        }

        Node urlNode = XPATH.getNode(responsiblePartyNode, "./contactInfo/CI_Contact/onlineResource/CI_OnlineResource/linkage/URL");
        if(urlNode != null){
            agent.setHomepage(urlNode.getTextContent().trim());
        }
        return agent;
    }

    private VCardOrganizationWrapper mapVCard(Node responsiblePartyNode) {
        VCardOrganization organization = new VCardOrganization();

        // NodeID darf nicht mit Zahl beginnen, UUID daher ungeeignet. Wenn keine Referenzierung erfolgt ist keine NodeID nötig.
        /*
        Node uuidNode = XPATH.getNode(responsiblePartyNode, "./@uuid");
        if(uuidNode != null){
            organization.setNodeID(uuidNode.getTextContent().trim());
        }
        */

        Node organisationNameNode = XPATH.getNode(responsiblePartyNode, "./organisationName/CharacterString");
        Node individualNameNode = XPATH.getNode(responsiblePartyNode, "./individualName/CharacterString");
        if(organisationNameNode != null) {
            organization.setFn(organisationNameNode.getTextContent().trim());
        } else if (individualNameNode != null) {
            organization.setFn(individualNameNode.getTextContent().trim());
        }

        Node postalCodeNode = XPATH.getNode(responsiblePartyNode, "./contactInfo/CI_Contact/address/CI_Address/postalCode/CharacterString");
        if(postalCodeNode != null){
            organization.setHasPostalCode(postalCodeNode.getTextContent().trim());
        }

        Node deliveryPointNode = XPATH.getNode(responsiblePartyNode, "./contactInfo/CI_Contact/address/CI_Address/deliveryPoint/CharacterString");
        if(deliveryPointNode != null){
            organization.setHasStreetAddress(deliveryPointNode.getTextContent().trim());
        }

        Node cityNode = XPATH.getNode(responsiblePartyNode, "./contactInfo/CI_Contact/address/CI_Address/city/CharacterString");
        if(cityNode != null){
            organization.setHasLocality(cityNode.getTextContent().trim());
        }

        Node countryNode = XPATH.getNode(responsiblePartyNode, "./contactInfo/CI_Contact/address/CI_Address/country/CharacterString");
        if(countryNode != null){
            organization.setHasCountryName(countryNode.getTextContent().trim());
        }

        Node emailNode = XPATH.getNode(responsiblePartyNode, "./contactInfo/CI_Contact/address/CI_Address/electronicMailAddress/CharacterString");
        if(emailNode != null){
            organization.setHasEmail(new ResourceElement(emailNode.getTextContent().trim()));
        }

        Node urlNode = XPATH.getNode(responsiblePartyNode, "./contactInfo/CI_Contact/onlineResource/CI_OnlineResource/linkage/URL");
        if(urlNode != null){
            organization.setHasURL(new ResourceElement(urlNode.getTextContent().trim()));
        }

        VCardOrganizationWrapper result = new VCardOrganizationWrapper();
        result.setOrganization(organization);

        return result;
    }

    private List<Distribution> mapDistribution(Element idfDataNode, List<String> currentDistributionUrls) {

        List<Distribution> dists = new ArrayList<>();

        Node idfMdMetadataNode = XPATH.getNode(idfDataNode, "./body/idfMdMetadata");

        String modified = getDateOrDateTime(XPATH.getNode(idfMdMetadataNode,"./identificationInfo[1]/*/citation/CI_Citation/date/CI_Date/date"));

        NodeList transferOptionNodes = XPATH.getNodeList(idfMdMetadataNode, "./distributionInfo/MD_Distribution/transferOptions");

        NodeList formatNodes = XPATH.getNodeList(idfMdMetadataNode, "./distributionInfo/MD_Distribution/distributionFormat/MD_Format/name/CharacterString|./distributionInfo/MD_Distribution/distributor/MD_Distributor/distributorFormat/MD_Format/name");

        if(transferOptionNodes != null) {
            for (int i = 0; i < transferOptionNodes.getLength(); i++) {
                Node transferOptionNode = transferOptionNodes.item(i);
                Node onlineResNode = XPATH.getNode(transferOptionNode, "./MD_DigitalTransferOptions/onLine/idfOnlineResource");
                if (onlineResNode == null) {
                    onlineResNode = XPATH.getNode(transferOptionNode, "./MD_DigitalTransferOptions/onLine/CI_OnlineResource");
                }

                if (onlineResNode == null) {
                    log.warn("Skip Distribution - No OnlineResource");
                    continue;
                }

                Node linkageNode = XPATH.getNode(onlineResNode, "./linkage/URL");

                if (linkageNode == null) {
                    log.warn("Skip Distribution - No Linkage");
                    continue;
                }

                Node functionNode = XPATH.getNode(onlineResNode, "./function/CI_OnLineFunctionCode/@codeListValue");
                if (functionNode != null && (!functionNode.getTextContent().equals("information") && !functionNode.getTextContent().equals("download"))) {
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

                Node titleNode = XPATH.getNode(onlineResNode, "./name/CharacterString");
                Node descriptionNode = XPATH.getNode(onlineResNode, "./description/CharacterString");
                Node datasetTitleNode = XPATH.getNode(idfMdMetadataNode,"./identificationInfo[1]/MD_DataIdentification/citation/CI_Citation/title/CharacterString|./identificationInfo[1]/SV_ServiceIdentification/citation/CI_Citation/title/CharacterString");

                if(titleNode != null) {
                    dist.setTitle(titleNode.getTextContent().trim());
                } else if (descriptionNode != null){
                    dist.setTitle(descriptionNode.getTextContent().trim());
                } else if (datasetTitleNode != null){
                    dist.setTitle(datasetTitleNode.getTextContent().trim());
                }

                if (descriptionNode != null) {
                    dist.setDescription(descriptionNode.getTextContent().trim());
                }

                dist.setModified(new DatatypeTextElement(modified));
                if(modified.contains("T")) {
                    dist.getModified().setDatatype("http://www.w3.org/2001/XMLSchema#dateTime");
                } else {
                    dist.getModified().setDatatype("http://www.w3.org/2001/XMLSchema#date");
                }

                String format = null;
                if(formatNodes != null) {
                    for (int j = 0; j < formatNodes.getLength(); j++) {
                        Node formatNode = formatNodes.item(j);
                        format = formatMapper.map(formatNode.getTextContent().trim());
                        if (format != null) break;
                    }
                }

                if (format == null) {
                    Node applicationProfileNode = XPATH.getNode(onlineResNode, "./applicationProfile/CharacterString");
                    if (applicationProfileNode != null) {
                        format = formatMapper.map(applicationProfileNode.getTextContent().trim());
                    }
                }

                if (format != null) {
                    dist.setFormat(new ResourceElement("http://publications.europa.eu/resource/authority/file-type/" + format));
                }
                dist.setAbout(accessURL + DISTRIBUTION_RESOURCE_POSTFIX);

                if (functionNode != null && functionNode.getTextContent().equals("download")) {
                    dist.setDownloadURL(new ResourceElement(accessURL));
                } else if (functionNode != null && functionNode.getTextContent().equals("information")) {
                    dist.setPage(new ResourceElement(accessURL));
                }

            /*
            if(distribution.getByteSize() != null){
                dist.setByteSize(new DatatypeTextElement(distribution.getByteSize().toString()));
                dist.getByteSize().setDatatype("http://www.w3.org/2001/XMLSchema#decimal");
            }
*/

                //License
                setLicense(idfMdMetadataNode, dist);

                dists.add(dist);
            }
        }


        Node serviceIdentificationNode = XPATH.getNode(idfMdMetadataNode, "./identificationInfo[1]/SV_ServiceIdentification");
        if(serviceIdentificationNode != null) {
            NodeList containsOperationsNodes = XPATH.getNodeList(serviceIdentificationNode, "./containsOperations");
            for (int i = 0; i < containsOperationsNodes.getLength(); i++) {
                Node containsOperationsNode = containsOperationsNodes.item(i);
                Node linkageNode = XPATH.getNode(containsOperationsNode, "./SV_OperationMetadata/connectPoint/CI_OnlineResource/linkage/URL");
                if (linkageNode != null) {
                    String accessURL = linkageNode.getTextContent().trim();

                    // skip distributions that are already added
                    if (currentDistributionUrls.contains(accessURL)) {
                        continue;
                    }

                    currentDistributionUrls.add(accessURL);

                    Distribution dist = new Distribution();
                    dist.getAccessURL().setResource(accessURL);

                    dist.setAbout(accessURL + DISTRIBUTION_RESOURCE_POSTFIX);

                    setLicense(idfMdMetadataNode, dist);

                    Node serviceTypeNode = XPATH.getNode(serviceIdentificationNode, "./serviceType/LocalName");
                    if (serviceTypeNode != null) {
                        String format = formatMapper.map(serviceTypeNode.getTextContent().trim());
                        if (format != null) {
                            dist.setFormat(new ResourceElement("http://publications.europa.eu/resource/authority/file-type/" + format));
                        }
                    }

                    Node titleNode = XPATH.getNode(idfMdMetadataNode, "./identificationInfo[1]/SV_ServiceIdentification/citation/CI_Citation/title/CharacterString");
                    if (titleNode != null) {
                        dist.setTitle(titleNode.getTextContent().trim());
                    }

                    dist.setModified(new DatatypeTextElement(modified));
                    if(modified.contains("T")) {
                        dist.getModified().setDatatype("http://www.w3.org/2001/XMLSchema#dateTime");
                    } else {
                        dist.getModified().setDatatype("http://www.w3.org/2001/XMLSchema#date");
                    }

                    Node abstractNode = XPATH.getNode(serviceIdentificationNode, "./abstract/CharacterString");
                    if(abstractNode != null){
                        dist.setDescription(abstractNode.getTextContent().trim());
                    }

                    dists.add(dist);
                }
            }
        }

        return dists;

    }

    private void setLicense(Node idfMdMetadataNode, Distribution dist) {
        String licenseURI = null;
        NodeList constraintsNodes = XPATH.getNodeList(idfMdMetadataNode, "./identificationInfo[1]/MD_DataIdentification/resourceConstraints/MD_LegalConstraints|./identificationInfo[1]/SV_ServiceIdentification/resourceConstraints/MD_LegalConstraints");
        if(constraintsNodes != null) {
            for (int constraintsNodeIndex = 0; constraintsNodeIndex < constraintsNodes.getLength(); constraintsNodeIndex++) {
                Node constraintsNode = constraintsNodes.item(constraintsNodeIndex);
                /*
                NodeList useConstraintsNodes = XPATH.getNodeList(constraintsNode, "./useConstraints/MD_RestrictionCode/@codeListValue");
                boolean hasOtherRestrictionsUseContraints = false;
                for(int useConstraintsNodeIndex = 0; useConstraintsNodeIndex < useConstraintsNodes.getLength(); useConstraintsNodeIndex++){
                    if(useConstraintsNodes.item(useConstraintsNodeIndex).getTextContent().trim().equals("otherRestrictions")) hasOtherRestrictionsUseContraints = true;
                }
                 */
                NodeList otherConstraintsNodes = XPATH.getNodeList(constraintsNode, "./otherConstraints/CharacterString");
                if (otherConstraintsNodes != null) {
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
            log.warn("No License found - Use Default License!");
            dist.setLicense(new ResourceElement(SearchInterfaceConfig.getInstance().getString(SearchInterfaceConfig.DCAT_DEFAULT_LICENSE, "http://dcat-ap.de/def/licenses/other-open")));
        }
    }

    private String getDateOrDateTime(Node parent){
        Node node = XPATH.getNode(parent, "./Date|./DateTime");
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

        String north = XPATH.getNode(spatial, "./northBoundLatitude/Decimal").getTextContent().trim();
        String east = XPATH.getNode(spatial, "./eastBoundLongitude/Decimal").getTextContent().trim();
        String south = XPATH.getNode(spatial, "./southBoundLatitude/Decimal").getTextContent().trim();
        String west = XPATH.getNode(spatial, "./westBoundLongitude/Decimal").getTextContent().trim();

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

    public DcatApDe mapHitsToDcat(IBusQueryResultIterator hitIterator, int hitsPerPage) {
        DcatApDe dcatApDe = new DcatApDe();
        List<Dataset> datasets = new ArrayList<>();
        List<Distribution> distributions = new ArrayList<>();
        List<String> datasetIds = new ArrayList<>();
        List<String> distributionsIds = new ArrayList<>();

        for(int counter = 0; (counter < hitsPerPage) && hitIterator.hasNext(); counter++){
            IngridHit hit = hitIterator.next();
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
                        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                        DocumentBuilder builder = factory.newDocumentBuilder();
                        StringBuilder xmlStringBuilder = new StringBuilder();
                        xmlStringBuilder.append(idfData);
                        ByteArrayInputStream input = new ByteArrayInputStream(
                                xmlStringBuilder.toString().getBytes("UTF-8"));
                        Document idfDoc = builder.parse(input);

                        idfDataNode = idfDoc.getDocumentElement();
                    }
                }

                // mapDataset and mapDistribution can throw an exception to signal
                // that the document has to be skipped
                Dataset dataset = mapDataset(idfDataNode);
                List<Distribution> distributionList = mapDistribution(idfDataNode, distributionsIds);

                datasets.add(dataset);
                datasetIds.add(dataset.getAbout());
                distributions.addAll(distributionList);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                log.warn("Document " + hit.getId() + " has been skipped: " + e.getMessage());
            }
        }
        mapCatalog(dcatApDe.getCatalog());
        dcatApDe.getCatalog().setDataset(datasetIds.toArray(new String[0]));
        dcatApDe.setDataset(datasets);
        dcatApDe.setDistribution(distributions);
        return dcatApDe;
    }
}
