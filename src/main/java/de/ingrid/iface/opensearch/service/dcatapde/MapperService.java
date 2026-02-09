/*
 * **************************************************-
 * ingrid-interface-search
 * ==================================================
 * Copyright (C) 2014 - 2026 wemove digital solutions GmbH
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class MapperService {
    private static final XPathUtils XPATH = new XPathUtils(new IDFNamespaceContext());

    private final Logger log = LogManager.getLogger(MapperService.class);

    public static final String DISTRIBUTION_RESOURCE_POSTFIX = "#distribution";

    private static final List<String> ALLOWED_DISTRIBUTION_FUNCTIONCODES = new ArrayList<String>() {{
        add("download");
        add("information");
    }};

    private final Pattern URL_PATTERN = Pattern.compile("\"url\":\\s*\"([^\"]+)\"");
    private final Pattern QUELLE_PATTERN = Pattern.compile("\"quelle\":\\s*\"([^\"]+)\"");

    private final String RDF_NS = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";

    @Autowired
    private FormatMapper formatMapper;

    @Autowired
    private PeriodicityMapper periodicityMapper;

    @Autowired
    private IBusHelper iBusHelper;

    private Dataset mapDataset(Element idfDataNode) {
        Dataset dataset = new Dataset();


        Node idfMdMetadataNode = XPATH.getNode(idfDataNode, "./body/idfMdMetadata");

        String datasetURI = "";
        Node fileIdentifierNode = XPATH.getNode(idfMdMetadataNode, "./fileIdentifier/CharacterString");
        if (fileIdentifierNode != null) {
            String fileIdentifier = fileIdentifierNode.getTextContent().trim();
            dataset.setIdentifier(fileIdentifierNode.getTextContent().trim());
            datasetURI = SearchInterfaceConfig.getInstance().getString(SearchInterfaceConfig.METADATA_ACCESS_URL).replace("{uuid}", fileIdentifier);
            dataset.setAbout(datasetURI);
        }

        Node abstractNode = XPATH.getNode(idfMdMetadataNode, "./identificationInfo[1]/MD_DataIdentification/abstract/CharacterString|./identificationInfo[1]/SV_ServiceIdentification/abstract/CharacterString");
        if (abstractNode != null) {
            dataset.setDescription(new LangTextElement(abstractNode.getTextContent().trim()));
        }

        Node titleNode = XPATH.getNode(idfMdMetadataNode, "./identificationInfo[1]/MD_DataIdentification/citation/CI_Citation/title/CharacterString|./identificationInfo[1]/SV_ServiceIdentification/citation/CI_Citation/title/CharacterString");
        if (titleNode != null) {
            dataset.setTitle(new LangTextElement(titleNode.getTextContent().trim()));
        }


        NodeList responsiblePartyNodes = XPATH.getNodeList(idfMdMetadataNode, "./identificationInfo[1]/*/pointOfContact/idfResponsibleParty");
        if (responsiblePartyNodes != null) {
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
            if (responsiblePartyForPublisherNode == null && responsiblePartyNodes.getLength() > 0) {
                responsiblePartyForPublisherNode = responsiblePartyNodes.item(0);
            }
            if (responsiblePartyForPublisherNode != null && responsiblePartyNodes.getLength() > 0) {
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
                // previously creators were incorrectly set as originators
                dataset.setCreator(creator.toArray(new OrganizationWrapper[creator.size()]));
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
            } else {
                log.warn("No Themes!");
            }
        } else {
            log.warn("No Keywords or Categories!");
        }

        // HVD
        List<String> hvdCategories = Arrays.asList(XPATH.getStringArray(idfMdMetadataNode, "./identificationInfo[1]/*/descriptiveKeywords[./MD_Keywords/thesaurusName/CI_Citation/title/Anchor/@href='http://data.europa.eu/bna/asd487ae75']/*/keyword/Anchor/@href"));
        boolean isHVD = hvdCategories.size() > 0;
        if (isHVD) {
            dataset.setApplicableLegislation(new ResourceElement("http://data.europa.eu/eli/reg_impl/2023/138/oj"));
            dataset.setHvdCategory(hvdCategories.stream().map(hvdCategory -> new ResourceElement(hvdCategory)).collect(Collectors.toList()));
        }


        String modified = getDateOrDateTime(XPATH.getNode(idfMdMetadataNode, "./dateStamp"));
        dataset.setModified(modified);
        if (modified.contains("T")) {
            dataset.getModified().setDatatype("http://www.w3.org/2001/XMLSchema#dateTime");
        } else {
            dataset.getModified().setDatatype("http://www.w3.org/2001/XMLSchema#date");
        }

        String issued = getDateOrDateTime(XPATH.getNode(idfMdMetadataNode, "./identificationInfo[1]/*/citation/CI_Citation/date/CI_Date/date"));
        dataset.setIssued(issued);
        if (issued.contains("T")) {
            dataset.getIssued().setDatatype("http://www.w3.org/2001/XMLSchema#dateTime");
        } else {
            dataset.getIssued().setDatatype("http://www.w3.org/2001/XMLSchema#date");
        }

        // Distribution
        List<ResourceElement> distResources = new ArrayList<>();
        NodeList transferOptionNodes = XPATH.getNodeList(idfMdMetadataNode, "./distributionInfo/MD_Distribution/transferOptions");
        if (transferOptionNodes != null) {
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
                if (functionNode != null && (!ALLOWED_DISTRIBUTION_FUNCTIONCODES.contains(functionNode.getTextContent()))) {
                    log.warn("Skip Distribution - Function neither information nor download");
                    continue;
                }
                distResources.add(new ResourceElement(datasetURI + DISTRIBUTION_RESOURCE_POSTFIX + "-" + (distResources.size() + 1)));
            }
        }
        Node serviceIdentificationNode = XPATH.getNode(idfMdMetadataNode, "./identificationInfo[1]/SV_ServiceIdentification");
        if (serviceIdentificationNode != null) {
            NodeList containsOperationsNodes = XPATH.getNodeList(serviceIdentificationNode, "./containsOperations");
            for (int i = 0; i < containsOperationsNodes.getLength(); i++) {
                Node containsOperationsNode = containsOperationsNodes.item(i);
                Node linkageNode = XPATH.getNode(containsOperationsNode, "./SV_OperationMetadata/connectPoint/CI_OnlineResource/linkage/URL");
                if (linkageNode != null) {
                    distResources.add(new ResourceElement(datasetURI + DISTRIBUTION_RESOURCE_POSTFIX + "-" + (distResources.size() + 1)));
                }
            }
        }
        dataset.setDistribution(distResources);

        /*
        Node languageNode = XPATH.getNode(idfMdMetadataNode,"./identificationInfo[1]/MD_DataIdentification/language/LanguageCode/@codeListValue");
        if(languageNode != null) {
            dataset.setLanguage(new LangTextElement(languageNode.getTextContent().trim()));
        }
        */

        Node maintenanceFrequencyNode = XPATH.getNode(idfMdMetadataNode, "./identificationInfo[1]/*/resourceMaintenance/MD_MaintenanceInformation/maintenanceAndUpdateFrequency/MD_MaintenanceFrequencyCode/@codeListValue");
        if (maintenanceFrequencyNode != null) {
            String periodicity = periodicityMapper.map(maintenanceFrequencyNode.getTextContent().trim());
            if (periodicity != null) {
                dataset.setAccrualPeriodicity(new ResourceElement(periodicity));
            } else {
                log.warn("Unknown Periodicity: " + maintenanceFrequencyNode.getTextContent().trim());
            }
        }

        // CONTRIBUTOR ID
        dataset.setContributorID(new ResourceElement(SearchInterfaceConfig.getInstance().getString(SearchInterfaceConfig.DCAT_CONTRIBUTOR_ID, "http://dcat-ap.de/def/contributors/InGrid")));


        // SPATIAL
        NodeList geographicBoundingBoxNodes = XPATH.getNodeList(idfMdMetadataNode, "./identificationInfo[1]/*/extent/EX_Extent/geographicElement/EX_GeographicBoundingBox");
        if (geographicBoundingBoxNodes != null) {
            for (int i = 0; i < geographicBoundingBoxNodes.getLength(); i++) {
                Node node = geographicBoundingBoxNodes.item(i);
                SpatialElement spatial = mapSpatial(node, null);
                dataset.setSpatial(spatial);
            }
        }
        NodeList geographicIdentifierNodes = XPATH.getNodeList(idfMdMetadataNode, "./identificationInfo[1]/*/extent/EX_Extent/geographicElement/EX_GeographicDescription/geographicIdentifier/MD_Identifier/code/CharacterString");
        if (geographicIdentifierNodes != null) {
            List<String> geocodingDescriptions = new ArrayList<>();
            for (int i = 0; i < geographicIdentifierNodes.getLength(); i++) {
                Node node = geographicIdentifierNodes.item(i);
                geocodingDescriptions.add(node.getTextContent());
            }
            dataset.setGeocodingDescription(geocodingDescriptions);
        }


        // TEMPORAL
        NodeList temporalNodes = XPATH.getNodeList(idfMdMetadataNode, "./identificationInfo[1]/*/extent/EX_Extent/temporalElement/EX_TemporalExtent");
        if (temporalNodes != null) {
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
                        if (beginNode.getTextContent().contains("T"))
                            start.setDatatype("http://www.w3.org/2001/XMLSchema#dateTime");
                        else
                            start.setDatatype("http://www.w3.org/2001/XMLSchema#date");
                        start.setText(beginNode.getTextContent().trim());
                        periodOfTimeElement.setStartDate(start);
                    }

                    if (endNode != null && !endNode.getTextContent().trim().isEmpty()) {
                        DatatypeTextElement end = new DatatypeTextElement();
                        if (endNode.getTextContent().contains("T"))
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
        if (constraintsNodes != null) {
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
            if (accessRights != null) {
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
        if (organisationNameNode != null) {
            agent.setName(organisationNameNode.getTextContent().trim());
        } else if (individualNameNode != null) {
            agent.setName(individualNameNode.getTextContent().trim());
        }

        Node emailNode = XPATH.getNode(responsiblePartyNode, "./contactInfo/CI_Contact/address/CI_Address/electronicMailAddress/CharacterString");
        if (emailNode != null) {
            agent.setMbox(emailNode.getTextContent().trim());
        }

        Node urlNode = XPATH.getNode(responsiblePartyNode, "./contactInfo/CI_Contact/onlineResource/CI_OnlineResource/linkage/URL");
        if (urlNode != null) {
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
        if (organisationNameNode != null) {
            organization.setFn(organisationNameNode.getTextContent().trim());
        } else if (individualNameNode != null) {
            organization.setFn(individualNameNode.getTextContent().trim());
        }

        Node postalCodeNode = XPATH.getNode(responsiblePartyNode, "./contactInfo/CI_Contact/address/CI_Address/postalCode/CharacterString");
        if (postalCodeNode != null) {
            organization.setHasPostalCode(postalCodeNode.getTextContent().trim());
        }

        Node deliveryPointNode = XPATH.getNode(responsiblePartyNode, "./contactInfo/CI_Contact/address/CI_Address/deliveryPoint/CharacterString");
        if (deliveryPointNode != null) {
            organization.setHasStreetAddress(deliveryPointNode.getTextContent().trim());
        }

        Node cityNode = XPATH.getNode(responsiblePartyNode, "./contactInfo/CI_Contact/address/CI_Address/city/CharacterString");
        if (cityNode != null) {
            organization.setHasLocality(cityNode.getTextContent().trim());
        }

        Node countryNode = XPATH.getNode(responsiblePartyNode, "./contactInfo/CI_Contact/address/CI_Address/country/CharacterString");
        if (countryNode != null) {
            organization.setHasCountryName(countryNode.getTextContent().trim());
        }

        Node emailNode = XPATH.getNode(responsiblePartyNode, "./contactInfo/CI_Contact/address/CI_Address/electronicMailAddress/CharacterString");
        if (emailNode != null) {
            String email = emailNode.getTextContent().trim();
            if (!email.toLowerCase().startsWith("mailto:")) {
                email = "mailto:" + email;
            }
            organization.setHasEmail(new ResourceElement(email));
        }

        Node urlNode = XPATH.getNode(responsiblePartyNode, "./contactInfo/CI_Contact/onlineResource/CI_OnlineResource/linkage/URL");
        if (urlNode != null) {
            organization.setHasURL(new ResourceElement(urlNode.getTextContent().trim()));
        }

        VCardOrganizationWrapper result = new VCardOrganizationWrapper();
        result.setOrganization(organization);

        return result;
    }

    private List<Distribution> mapDistribution(Element idfDataNode, List<String> currentDistributionUrls) {

        List<Distribution> dists = new ArrayList<>();

        Node idfMdMetadataNode = XPATH.getNode(idfDataNode, "./body/idfMdMetadata");

        String datasetURI = "";
        Node fileIdentifierNode = XPATH.getNode(idfMdMetadataNode, "./fileIdentifier/CharacterString");
        if (fileIdentifierNode != null) {
            String fileIdentifier = fileIdentifierNode.getTextContent().trim();
            datasetURI = SearchInterfaceConfig.getInstance().getString(SearchInterfaceConfig.METADATA_ACCESS_URL).replace("{uuid}", fileIdentifier);
        }

        String modified = getDateOrDateTime(XPATH.getNode(idfMdMetadataNode, "./identificationInfo[1]/*/citation/CI_Citation/date/CI_Date/date"));

        NodeList transferOptionNodes = XPATH.getNodeList(idfMdMetadataNode, "./distributionInfo/MD_Distribution/transferOptions");

        NodeList formatNodes = XPATH.getNodeList(idfMdMetadataNode, "./distributionInfo/MD_Distribution/distributionFormat/MD_Format/name/CharacterString|./distributionInfo/MD_Distribution/distributor/MD_Distributor/distributorFormat/MD_Format/name");

        // HVD
        List<String> hvdCategories = Arrays.asList(XPATH.getStringArray(idfMdMetadataNode, "./identificationInfo[1]/*/descriptiveKeywords[./MD_Keywords/thesaurusName/CI_Citation/title/Anchor/@href='http://data.europa.eu/bna/asd487ae75']/*/keyword/Anchor/@href"));
        boolean isHVD = hvdCategories.size() > 0;

        if (transferOptionNodes != null) {
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
                if (functionNode != null && (!ALLOWED_DISTRIBUTION_FUNCTIONCODES.contains(functionNode.getTextContent()))) {
                    log.warn("Skip Distribution - Function neither information nor download");
                    continue;
                }

                String accessURL = linkageNode.getTextContent().trim();

                Distribution dist = new Distribution();
                dist.getAccessURL().setResource(accessURL);

                Node titleNode = XPATH.getNode(onlineResNode, "./name/CharacterString");
                Node descriptionNode = XPATH.getNode(onlineResNode, "./description/CharacterString");
                Node datasetTitleNode = XPATH.getNode(idfMdMetadataNode, "./identificationInfo[1]/MD_DataIdentification/citation/CI_Citation/title/CharacterString|./identificationInfo[1]/SV_ServiceIdentification/citation/CI_Citation/title/CharacterString");

                if (titleNode != null) {
                    dist.setTitle(titleNode.getTextContent().trim());
                } else if (descriptionNode != null) {
                    dist.setTitle(descriptionNode.getTextContent().trim());
                } else if (datasetTitleNode != null) {
                    dist.setTitle(datasetTitleNode.getTextContent().trim());
                }

                if (descriptionNode != null) {
                    dist.setDescription(descriptionNode.getTextContent().trim());
                }

                dist.setModified(new DatatypeTextElement(modified));
                if (modified.contains("T")) {
                    dist.getModified().setDatatype("http://www.w3.org/2001/XMLSchema#dateTime");
                } else {
                    dist.getModified().setDatatype("http://www.w3.org/2001/XMLSchema#date");
                }

                String format = null;
                if (formatNodes != null) {
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
                dist.setAbout(datasetURI + DISTRIBUTION_RESOURCE_POSTFIX + "-" + (dists.size() + 1));

                if (isHVD) {
                    dist.setApplicableLegislation(new ResourceElement("http://data.europa.eu/eli/reg_impl/2023/138/oj"));
                }

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
        if (serviceIdentificationNode != null) {
            NodeList containsOperationsNodes = XPATH.getNodeList(serviceIdentificationNode, "./containsOperations");
            for (int i = 0; i < containsOperationsNodes.getLength(); i++) {
                Node containsOperationsNode = containsOperationsNodes.item(i);
                Node linkageNode = XPATH.getNode(containsOperationsNode, "./SV_OperationMetadata/connectPoint/CI_OnlineResource/linkage/URL");
                if (linkageNode != null) {
                    String accessURL = linkageNode.getTextContent().trim();

                    Distribution dist = new Distribution();
                    dist.getAccessURL().setResource(accessURL);

                    dist.setAbout(datasetURI + DISTRIBUTION_RESOURCE_POSTFIX + "-" + (dists.size() + 1));

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
                    if (modified.contains("T")) {
                        dist.getModified().setDatatype("http://www.w3.org/2001/XMLSchema#dateTime");
                    } else {
                        dist.getModified().setDatatype("http://www.w3.org/2001/XMLSchema#date");
                    }

                    Node abstractNode = XPATH.getNode(serviceIdentificationNode, "./abstract/CharacterString");
                    if (abstractNode != null) {
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
        if (constraintsNodes != null) {
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

    private String getDateOrDateTime(Node parent) {
        Node node = XPATH.getNode(parent, "./Date|./DateTime");
        if (node != null)
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


    // FIXME
    private Dataset mapDatasetFromRdfElement(Element datasetElement) {
        if (datasetElement == null) return null;
        Dataset dataset = new Dataset();

        // about (attrib oder rdf:about)
        String aboutAttr = datasetElement.getAttribute("about");
        if (aboutAttr == null || aboutAttr.isEmpty()) {
            aboutAttr = datasetElement.getAttributeNS(RDF_NS, "about");
        }
        if (aboutAttr != null && !aboutAttr.isEmpty()) {
            dataset.setAbout(aboutAttr);
        }

        // title (dcterms:title oder title)
        NodeList titleNodes = datasetElement.getElementsByTagName("dcterms:title");
        if (titleNodes.getLength() == 0) titleNodes = datasetElement.getElementsByTagName("title");
        if (titleNodes.getLength() > 0 && titleNodes.item(0).getTextContent() != null) {
            dataset.setTitle(new LangTextElement(titleNodes.item(0).getTextContent().trim()));
        }

        // description (dcterms:description oder description)
        NodeList descNodes = datasetElement.getElementsByTagName("dcterms:description");
        if (descNodes.getLength() == 0) descNodes = datasetElement.getElementsByTagName("description");
        if (descNodes.getLength() > 0 && descNodes.item(0).getTextContent() != null) {
            dataset.setDescription(new LangTextElement(descNodes.item(0).getTextContent().trim()));
        }

        // identifier (dcterms:identifier oder identifier)
        NodeList idNodes = datasetElement.getElementsByTagName("dcterms:identifier");
        if (idNodes.getLength() == 0) idNodes = datasetElement.getElementsByTagName("identifier");
        if (idNodes.getLength() > 0 && idNodes.item(0).getTextContent() != null) {
            dataset.setIdentifier(idNodes.item(0).getTextContent().trim());
        }

        // contributorID (rdf:resource)
        NodeList contribIdNodes = datasetElement.getElementsByTagName("dcatde:contributorID");
        if (contribIdNodes.getLength() == 0) contribIdNodes = datasetElement.getElementsByTagName("contributorID");
        if (contribIdNodes.getLength() > 0) {
            Node n = contribIdNodes.item(0);
            if (n instanceof Element) {
                Element el = (Element) n;
                String r = el.getAttribute("rdf:resource");
                if (r == null || r.isEmpty()) r = el.getAttributeNS(RDF_NS, "resource");
                if (r != null && !r.isEmpty()) {
                    dataset.setContributorID(new ResourceElement(r));
                }
            }
        }

        // contactPoint (vcard:Organization)
        NodeList contactNodes = datasetElement.getElementsByTagName("dcat:contactPoint");
        if (contactNodes.getLength() == 0) contactNodes = datasetElement.getElementsByTagName("contactPoint");
        if (contactNodes.getLength() > 0) {
            Node cpNode = contactNodes.item(0);
            if (cpNode instanceof Element) {
                Element cpEl = (Element) cpNode;
                // find vcard:Organization child
                NodeList orgNodes = cpEl.getElementsByTagName("vcard:Organization");
                if (orgNodes.getLength() == 0) orgNodes = cpEl.getElementsByTagName("Organization");
                if (orgNodes.getLength() > 0 && orgNodes.item(0) instanceof Element) {
                    Element orgEl = (Element) orgNodes.item(0);
                    VCardOrganization org = new VCardOrganization();
                    NodeList fnNodes = orgEl.getElementsByTagName("vcard:fn");
                    if (fnNodes.getLength() == 0) fnNodes = orgEl.getElementsByTagName("fn");
                    if (fnNodes.getLength() > 0 && fnNodes.item(0).getTextContent() != null) {
                        org.setFn(fnNodes.item(0).getTextContent().trim());
                    }
                    // email as rdf:resource or element text
                    NodeList emailNodes = orgEl.getElementsByTagName("vcard:hasEmail");
                    if (emailNodes.getLength() == 0) emailNodes = orgEl.getElementsByTagName("hasEmail");
                    if (emailNodes.getLength() > 0) {
                        Node em = emailNodes.item(0);
                        if (em instanceof Element) {
                            Element emEl = (Element) em;
                            String mail = emEl.getAttribute("rdf:resource");
                            if (mail == null || mail.isEmpty()) mail = emEl.getAttributeNS(RDF_NS, "resource");
                            if ((mail == null || mail.isEmpty()) && em.getTextContent() != null) {
                                mail = em.getTextContent().trim();
                            }
                            if (mail != null && !mail.isEmpty()) {
                                if (!mail.toLowerCase().startsWith("mailto:")) mail = "mailto:" + mail;
                                org.setHasEmail(new ResourceElement(mail));
                            }
                        } else if (em.getTextContent() != null && !em.getTextContent().trim().isEmpty()) {
                            String mail = em.getTextContent().trim();
                            if (!mail.toLowerCase().startsWith("mailto:")) mail = "mailto:" + mail;
                            org.setHasEmail(new ResourceElement(mail));
                        }
                    }
                    VCardOrganizationWrapper wrapper = new VCardOrganizationWrapper();
                    wrapper.setOrganization(org);
                    dataset.setContactPoint(wrapper);
                }
            }
        }

        // FIXME: the following are missing: attributes, dcat:downloadURL,
        // distribution (simple rdf:resource references)
        NodeList distNodes = datasetElement.getElementsByTagName("dcat:distribution");
        if (distNodes.getLength() == 0) distNodes = datasetElement.getElementsByTagName("distribution");
        List<ResourceElement> distResources = new ArrayList<>();
        for (int i = 0; i < distNodes.getLength(); i++) {
            Node dn = distNodes.item(i);
            if (!(dn instanceof Element)) continue;
            Element de = (Element) dn;
            String uri = de.getAttribute("rdf:resource");
            if (uri == null || uri.isEmpty()) uri = de.getAttributeNS(RDF_NS, "resource");
            if (uri == null || uri.isEmpty()) uri = de.getAttribute("about");
            if (uri == null || uri.isEmpty()) uri = de.getAttributeNS(RDF_NS, "about");
            if ((uri == null || uri.isEmpty()) && de.getTextContent() != null) {
                uri = de.getTextContent().trim();
            }
            if (uri != null && !uri.isEmpty()) {
                distResources.add(new ResourceElement(uri));
            }
        }
        if (!distResources.isEmpty()) {
            dataset.setDistribution(distResources);
        }

        // keywords
        NodeList keywordNodes = datasetElement.getElementsByTagName("dcat:keyword");
        if (keywordNodes.getLength() == 0) keywordNodes = datasetElement.getElementsByTagName("keyword");
        List<String> keywords = new ArrayList<>();
        for (int i = 0; i < keywordNodes.getLength(); i++) {
            Node kn = keywordNodes.item(i);
            if (kn != null && kn.getTextContent() != null) {
                String t = kn.getTextContent().trim();
                if (!t.isEmpty()) keywords.add(t);
            }
        }
        if (!keywords.isEmpty()) {
            dataset.setKeyword(keywords);
        }

        // FIXME publisher no output
        // publisher (foaf:Agent -> foaf:mbox)
        NodeList publisherNodes = datasetElement.getElementsByTagName("dcterms:publisher");
        if (publisherNodes.getLength() == 0) publisherNodes = datasetElement.getElementsByTagName("publisher");
        if (publisherNodes.getLength() > 0) {
            Node p = publisherNodes.item(0);
            if (p instanceof Element) {
                Element pel = (Element) p;
                NodeList agentNodes = pel.getElementsByTagName("foaf:Agent");
                if (agentNodes.getLength() == 0) agentNodes = pel.getElementsByTagName("Agent");
                if (agentNodes.getLength() > 0 && agentNodes.item(0) instanceof Element) {
                    Element agEl = (Element) agentNodes.item(0);
                    NodeList mboxNodes = agEl.getElementsByTagName("foaf:mbox");
                    if (mboxNodes.getLength() == 0) mboxNodes = agEl.getElementsByTagName("mbox");
                    if (mboxNodes.getLength() > 0 && mboxNodes.item(0).getTextContent() != null) {
                        Agent agent = new Agent();
                        String mail = mboxNodes.item(0).getTextContent().trim();
                        if (!mail.toLowerCase().startsWith("mailto:")) mail = "mailto:" + mail;
                        agent.setMbox(mail);
                        AgentWrapper aw = new AgentWrapper();
                        aw.setAgent(agent);
                        dataset.setPublisher(aw);
                    }
                }
            }
        }

        // accessRights
        NodeList accessNodes = datasetElement.getElementsByTagName("dcterms:accessRights");
        if (accessNodes.getLength() == 0) accessNodes = datasetElement.getElementsByTagName("accessRights");
        if (accessNodes.getLength() > 0 && accessNodes.item(0).getTextContent() != null) {
            dataset.setAccessRights(new LangTextElement(accessNodes.item(0).getTextContent().trim()));
        }

        // accrualPeriodicity (rdf:resource)
        NodeList accrualNodes = datasetElement.getElementsByTagName("dcterms:accrualPeriodicity");
        if (accrualNodes.getLength() == 0) accrualNodes = datasetElement.getElementsByTagName("accrualPeriodicity");
        if (accrualNodes.getLength() > 0 && accrualNodes.item(0) instanceof Element) {
            Element el = (Element) accrualNodes.item(0);
            String r = el.getAttribute("rdf:resource");
            if (r == null || r.isEmpty()) r = el.getAttributeNS(RDF_NS, "resource");
            if (r != null && !r.isEmpty()) {
                dataset.setAccrualPeriodicity(new ResourceElement(r));
            }
        }

        // issued (datatype aware)
        NodeList issuedNodes = datasetElement.getElementsByTagName("dcterms:issued");
        if (issuedNodes.getLength() == 0) issuedNodes = datasetElement.getElementsByTagName("issued");
        if (issuedNodes.getLength() > 0 && issuedNodes.item(0).getTextContent() != null) {
            String issued = issuedNodes.item(0).getTextContent().trim();
            dataset.setIssued(issued);
            if (issued.contains("T")) {
                if (dataset.getIssued() != null) dataset.getIssued().setDatatype("http://www.w3.org/2001/XMLSchema#dateTime");
            } else {
                if (dataset.getIssued() != null) dataset.getIssued().setDatatype("http://www.w3.org/2001/XMLSchema#date");
            }
        }

        // spatial (locn:geometry with GeoJSON)
        NodeList spatialNodes = datasetElement.getElementsByTagName("dcterms:spatial");
        if (spatialNodes.getLength() == 0) spatialNodes = datasetElement.getElementsByTagName("spatial");
        if (spatialNodes.getLength() > 0) {
            Node sp = spatialNodes.item(0);
            if (sp instanceof Element) {
                Element spEl = (Element) sp;
                NodeList locnNodes = spEl.getElementsByTagName("dcterms:Location");
                if (locnNodes.getLength() == 0) locnNodes = spEl.getElementsByTagName("Location");
                if (locnNodes.getLength() > 0 && locnNodes.item(0) instanceof Element) {
                    Element locnEl = (Element) locnNodes.item(0);
                    NodeList geomNodes = locnEl.getElementsByTagName("locn:geometry");
                    if (geomNodes.getLength() == 0) geomNodes = locnEl.getElementsByTagName("geometry");
                    if (geomNodes.getLength() > 0 && geomNodes.item(0) != null) {
                        Node g = geomNodes.item(0);
                        String geoText = g.getTextContent().trim();
                        String dtype = null;
                        if (g instanceof Element) {
                            Element gEl = (Element) g;
                            if (gEl.hasAttribute("rdf:datatype")) dtype = gEl.getAttribute("rdf:datatype");
                            if ((dtype == null || dtype.isEmpty()) && gEl.hasAttribute("datatype"))
                                dtype = gEl.getAttribute("datatype");
                        }
                        DatatypeTextElement dt = new DatatypeTextElement();
                        if (dtype != null && !dtype.isEmpty()) dt.setDatatype(dtype);
                        dt.setText(geoText);
                        List<DatatypeTextElement> geoms = new ArrayList<>();
                        geoms.add(dt);
                        LocationElement location = new LocationElement();
                        location.setGeometry(geoms);
                        SpatialElement se = new SpatialElement();
                        se.setLocation(location);
                        dataset.setSpatial(se);
                    }
                }
            }
        }

        // temporal (PeriodOfTime -> start/end)
        NodeList temporalNodes = datasetElement.getElementsByTagName("dcterms:temporal");
        if (temporalNodes.getLength() == 0) temporalNodes = datasetElement.getElementsByTagName("temporal");
        if (temporalNodes.getLength() > 0) {
            List<TemporalElement> temporalList = new ArrayList<>();
            for (int i = 0; i < temporalNodes.getLength(); i++) {
                Node tn = temporalNodes.item(i);
                if (!(tn instanceof Element)) continue;
                Element tnEl = (Element) tn;
                NodeList periodNodes = tnEl.getElementsByTagName("dcterms:PeriodOfTime");
                if (periodNodes.getLength() == 0) periodNodes = tnEl.getElementsByTagName("PeriodOfTime");
                for (int p = 0; p < periodNodes.getLength(); p++) {
                    Node pn = periodNodes.item(p);
                    if (!(pn instanceof Element)) continue;
                    Element pnEl = (Element) pn;
                    PeriodOfTimeElement pot = new PeriodOfTimeElement();
                    NodeList startNodes = pnEl.getElementsByTagName("dcat:startDate");
                    if (startNodes.getLength() == 0) startNodes = pnEl.getElementsByTagName("startDate");
                    if (startNodes.getLength() > 0 && startNodes.item(0).getTextContent() != null) {
                        DatatypeTextElement start = new DatatypeTextElement();
                        start.setText(startNodes.item(0).getTextContent().trim());
                        start.setDatatype("http://www.w3.org/2001/XMLSchema#dateTime");
                        pot.setStartDate(start);
                    }
                    NodeList endNodes = pnEl.getElementsByTagName("dcat:endDate");
                    if (endNodes.getLength() == 0) endNodes = pnEl.getElementsByTagName("endDate");
                    if (endNodes.getLength() > 0 && endNodes.item(0).getTextContent() != null) {
                        DatatypeTextElement end = new DatatypeTextElement();
                        end.setText(endNodes.item(0).getTextContent().trim());
                        end.setDatatype("http://www.w3.org/2001/XMLSchema#dateTime");
                        pot.setEndDate(end);
                    }
                    TemporalElement te = new TemporalElement();
                    te.setPeriodOfTime(pot);
                    temporalList.add(te);
                }
            }
            if (!temporalList.isEmpty()) dataset.setTemporal(temporalList);
        }

        // modified
        NodeList modifiedNodes = datasetElement.getElementsByTagName("dcterms:modified");
        if (modifiedNodes.getLength() == 0) modifiedNodes = datasetElement.getElementsByTagName("modified");
        if (modifiedNodes.getLength() > 0 && modifiedNodes.item(0).getTextContent() != null) {
            String modified = modifiedNodes.item(0).getTextContent().trim();
            dataset.setModified(modified);
            if (modified.contains("T")) {
                if (dataset.getModified() != null) dataset.getModified().setDatatype("http://www.w3.org/2001/XMLSchema#dateTime");
            } else {
                if (dataset.getModified() != null) dataset.getModified().setDatatype("http://www.w3.org/2001/XMLSchema#date");
            }
        }

        // geocodingDescription (multiple)
        NodeList geocodingNodes = datasetElement.getElementsByTagName("dcatde:geocodingDescription");
        if (geocodingNodes.getLength() == 0) geocodingNodes = datasetElement.getElementsByTagName("geocodingDescription");
        List<String> geocodings = new ArrayList<>();
        for (int i = 0; i < geocodingNodes.getLength(); i++) {
            Node gn = geocodingNodes.item(i);
            if (gn != null && gn.getTextContent() != null) {
                String t = gn.getTextContent().trim();
                if (!t.isEmpty()) geocodings.add(t);
            }
        }
        if (!geocodings.isEmpty()) dataset.setGeocodingDescription(geocodings);

        // themes (rdf:resource)
        NodeList themeNodes = datasetElement.getElementsByTagName("dcat:theme");
        if (themeNodes.getLength() == 0) themeNodes = datasetElement.getElementsByTagName("theme");
        List<String> themes = new ArrayList<>();
        for (int i = 0; i < themeNodes.getLength(); i++) {
            Node tn = themeNodes.item(i);
            if (!(tn instanceof Element)) continue;
            Element tEl = (Element) tn;
            String r = tEl.getAttribute("rdf:resource");
            if (r == null || r.isEmpty()) r = tEl.getAttributeNS(RDF_NS, "resource");
            if (r != null && !r.isEmpty()) themes.add(r);
        }
        if (!themes.isEmpty()) {
            dataset.setThemes(themes.toArray(new String[0]));
        }

        return dataset;
    }

    // FIXME
    private List<Distribution> mapDistributionFromRdfElement(Element distributionElement) {
        List<Distribution> result = new ArrayList<>();
        if (distributionElement == null) return result;

        // Helper to read attribute with fallback to rdf: namespace
        java.util.function.Function<Element, String> readAboutOrResource = (el) -> {
            String v = el.getAttribute("about");
            if (v == null || v.isEmpty()) v = el.getAttributeNS(RDF_NS, "about");
            if (v == null || v.isEmpty()) v = el.getAttribute("rdf:resource");
            if (v == null || v.isEmpty()) v = el.getAttributeNS(RDF_NS, "resource");
            return (v != null && !v.isEmpty()) ? v : null;
        };

        // Many RDF documents reference a distribution as a resource only (rdf:resource / rdf:about)
        String ref = readAboutOrResource.apply(distributionElement);
        // If the element only references a resource, return a single Distribution with about set
        if (ref != null) {
            Distribution dist = new Distribution();
            dist.setAbout(ref);
            result.add(dist);
            return result;
        }

        // Otherwise parse content
        Distribution dist = new Distribution();

        // about (attribute)
        String aboutAttr = distributionElement.getAttribute("about");
        if (aboutAttr == null || aboutAttr.isEmpty()) {
            aboutAttr = distributionElement.getAttributeNS(RDF_NS, "about");
        }
        if (aboutAttr != null && !aboutAttr.isEmpty()) {
            dist.setAbout(aboutAttr);
        }

        // title
        NodeList titleNodes = distributionElement.getElementsByTagName("dcterms:title");
        if (titleNodes.getLength() == 0) titleNodes = distributionElement.getElementsByTagName("title");
        if (titleNodes.getLength() > 0 && titleNodes.item(0) != null && titleNodes.item(0).getTextContent() != null) {
            dist.setTitle(titleNodes.item(0).getTextContent().trim());
        }

        // description
        NodeList descNodes = distributionElement.getElementsByTagName("dcterms:description");
        if (descNodes.getLength() == 0) descNodes = distributionElement.getElementsByTagName("description");
        if (descNodes.getLength() > 0 && descNodes.item(0) != null && descNodes.item(0).getTextContent() != null) {
            dist.setDescription(descNodes.item(0).getTextContent().trim());
        }

        // modified
        NodeList modifiedNodes = distributionElement.getElementsByTagName("dcterms:modified");
        if (modifiedNodes.getLength() == 0) modifiedNodes = distributionElement.getElementsByTagName("modified");
        if (modifiedNodes.getLength() > 0 && modifiedNodes.item(0) != null && modifiedNodes.item(0).getTextContent() != null) {
            String modified = modifiedNodes.item(0).getTextContent().trim();
            dist.setModified(new DatatypeTextElement(modified));
            if (modified.contains("T")) {
                dist.getModified().setDatatype("http://www.w3.org/2001/XMLSchema#dateTime");
            } else {
                dist.getModified().setDatatype("http://www.w3.org/2001/XMLSchema#date");
            }
        }

        // accessURL / downloadURL / page - try attributes first, then child elements
        String accessURL = null;
        // check common attributes on the distribution element (rdf:resource etc.)
        accessURL = distributionElement.getAttribute("rdf:resource");
        if (accessURL == null || accessURL.isEmpty())
            accessURL = distributionElement.getAttributeNS(RDF_NS, "resource");
        if (accessURL == null || accessURL.isEmpty()) accessURL = distributionElement.getAttribute("about");
        if (accessURL == null || accessURL.isEmpty()) accessURL = distributionElement.getAttributeNS(RDF_NS, "about");

        if (accessURL == null || accessURL.isEmpty()) {
            NodeList accessNodes = distributionElement.getElementsByTagName("dcat:accessURL");
            if (accessNodes.getLength() == 0) accessNodes = distributionElement.getElementsByTagName("accessURL");
            if (accessNodes.getLength() > 0 && accessNodes.item(0) != null && accessNodes.item(0).getTextContent() != null) {
                accessURL = accessNodes.item(0).getTextContent().trim();
            }
        }
        if (accessURL != null && !accessURL.isEmpty()) {
            dist.getAccessURL().setResource(accessURL);
        }

        // downloadURL
        NodeList downloadNodes = distributionElement.getElementsByTagName("dcat:downloadURL");
        if (downloadNodes.getLength() == 0) downloadNodes = distributionElement.getElementsByTagName("downloadURL");
        if (downloadNodes.getLength() > 0 && downloadNodes.item(0) != null && downloadNodes.item(0).getTextContent() != null) {
            String download = downloadNodes.item(0).getTextContent().trim();
            if (!download.isEmpty()) dist.setDownloadURL(new ResourceElement(download));
        }

        // page
        NodeList pageNodes = distributionElement.getElementsByTagName("dcat:page");
        if (pageNodes.getLength() == 0) pageNodes = distributionElement.getElementsByTagName("page");
        if (pageNodes.getLength() > 0 && pageNodes.item(0) != null && pageNodes.item(0).getTextContent() != null) {
            String page = pageNodes.item(0).getTextContent().trim();
            if (!page.isEmpty()) dist.setPage(new ResourceElement(page));
        }

        // format - try literal text then map with formatMapper
        NodeList formatNodes = distributionElement.getElementsByTagName("dcterms:format");
        if (formatNodes.getLength() == 0) formatNodes = distributionElement.getElementsByTagName("format");
        if (formatNodes.getLength() > 0 && formatNodes.item(0) != null && formatNodes.item(0).getTextContent() != null) {
            String fmt = formatNodes.item(0).getTextContent().trim();
            String mapped = formatMapper.map(fmt);
            if (mapped != null) {
                dist.setFormat(new ResourceElement("http://publications.europa.eu/resource/authority/file-type/" + mapped));
            }
        }

        // license - try dcterms:license element or license literal/url
        String licenseUri = null;
        NodeList licenseNodes = distributionElement.getElementsByTagName("dcterms:license");
        if (licenseNodes.getLength() == 0) licenseNodes = distributionElement.getElementsByTagName("license");
        if (licenseNodes.getLength() > 0 && licenseNodes.item(0) != null) {
            Node lic = licenseNodes.item(0);
            if (lic.getNodeType() == Node.ELEMENT_NODE) {
                Element licEl = (Element) lic;
                String l = licEl.getAttribute("rdf:resource");
                if (l == null || l.isEmpty()) l = licEl.getAttributeNS(RDF_NS, "resource");
                if (l != null && !l.isEmpty()) licenseUri = l;
                else if (lic.getTextContent() != null && !lic.getTextContent().trim().isEmpty())
                    licenseUri = lic.getTextContent().trim();
            } else if (lic.getTextContent() != null && !lic.getTextContent().trim().isEmpty()) {
                licenseUri = lic.getTextContent().trim();
            }
        }
        if (licenseUri != null && !licenseUri.isEmpty()) {
            dist.setLicense(new ResourceElement(licenseUri));
        } else {
            // fallback to default
            dist.setLicense(new ResourceElement(SearchInterfaceConfig.getInstance().getString(SearchInterfaceConfig.DCAT_DEFAULT_LICENSE, "http://dcat-ap.de/def/licenses/other-open")));
        }

        result.add(dist);
        return result;
    }


    public DcatApDe mapHitsToDcat(IBusQueryResultIterator hitIterator, int hitsPerPage) {
        DcatApDe dcatApDe = new DcatApDe();
        List<Dataset> datasets = new ArrayList<>();
        List<Distribution> distributions = new ArrayList<>();
        List<String> datasetIds = new ArrayList<>();
        List<String> distributionsIds = new ArrayList<>();

        for (int counter = 0; (counter < hitsPerPage) && hitIterator.hasNext(); counter++) {
            IngridHit hit = hitIterator.next();
            try {
                IngridHitDetail hitDetail = hit.getHitDetail();
                String plugId = hit.getPlugId();
                Element idfDataNode = null;
                String rdfContent = null;
                PlugDescription plugDescription = iBusHelper.getPlugdescription(plugId);

                // if the detail already has the rdf-field from the index #8314
                String[] rdfContentArray = (String[]) hitDetail.get("rdf");
                if (rdfContentArray != null && rdfContentArray.length > 0) {
                    rdfContent = rdfContentArray[0].replace("\n", "");
                    ;      // extract

                }

                if (rdfContent != null) {
                    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                    ByteArrayInputStream input = new ByteArrayInputStream(
                            rdfContent.getBytes("UTF-8"));
                    DocumentBuilder builder = factory.newDocumentBuilder();

                    Document rdfDoc = builder.parse(input);

                    // the catalog, paging and opening tags are handled elsewhere
                    // map only the dataset and distributions

                    NodeList datasetNodes = rdfDoc.getElementsByTagName("dcat:Dataset");

                    for (int i = 0; i < datasetNodes.getLength(); i++) {
                        Element datasetElement = (Element) datasetNodes.item(i);

                        Dataset rdfDataset = mapDatasetFromRdfElement(datasetElement);
                        datasets.add(rdfDataset);
                        datasetIds.add(rdfDataset.getAbout());
                    }

                    NodeList distributionNodes = rdfDoc.getElementsByTagName("dcat:Distribution");
                    for (int i = 0; i < distributionNodes.getLength(); i++) {
                        Element distributionElement = (Element) distributionNodes.item(i);

                        List<Distribution> distribution = mapDistributionFromRdfElement(distributionElement);
                        distributions.addAll(distribution);
                    }

                    // skip the normal IDF -> DCAT mapping for this hit
                    continue;
                }

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
