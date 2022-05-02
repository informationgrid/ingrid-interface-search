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

import java.util.*;
import java.util.stream.Collectors;

@Service
public class MapperService {

    private final Logger log = LogManager.getLogger(MapperService.class);

    public static final String DISTRIBUTION_RESOURCE_POSTFIX = "/distribution";
    public static final String PUBLISHER_RESOURCE_POSTFIX = "/#publisher";

    @Autowired
    private FormatMapper formatMapper;

    @Autowired
    private IBusHelper iBusHelper;

    private Dataset mapDataset(Element idfDataNode) {
        Dataset dataset = new Dataset();


        Node idfMdMetadataNode = idfDataNode.selectSingleNode("//idf:idfMdMetadata");

        Node abstractNode = idfMdMetadataNode.selectSingleNode("//gmd:identificationInfo[1]/gmd:MD_DataIdentification/gmd:abstract/gco:CharacterString");
        if (abstractNode != null) {
            dataset.setDescription(new LangTextElement(abstractNode.getText().trim()));
        }

        Node titleNode = idfMdMetadataNode.selectSingleNode("//gmd:identificationInfo[1]/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:title/gco:CharacterString");
        if(titleNode != null) {
            dataset.setTitle(new LangTextElement(titleNode.getText().trim()));
        }
/*
        // Publisher / ContactPoint

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


        // KEYWORDS
        List<Node> keywordNodes = idfMdMetadataNode.selectNodes("//gmd:identificationInfo[1]/*/gmd:descriptiveKeywords/*/gmd:keyword/gco:CharacterString");
        List<String> keywords = keywordNodes.stream().map(keywordNode -> keywordNode.getText().trim()).filter(keyword -> !keyword.isEmpty()).collect(Collectors.toList());
        if (keywords.size() > 0) {
            dataset.setKeyword(keywords);
        }

        List<Node> categoryNodes = idfMdMetadataNode.selectNodes("//gmd:identificationInfo[1]/gmd:MD_DataIdentification/gmd:topicCategory/gmd:MD_TopicCategoryCode");
        List<String> categories = categoryNodes.stream().map(categoryNode -> categoryNode.getText().trim()).filter(category -> !category.isEmpty()).collect(Collectors.toList());
        if (categories.size() > 0 || keywords.size() > 0) {
            Collection<Theme> themes = ThemeMapper.mapThemes(categories, keywords);
            if (themes.size() > 0) {
                dataset.setThemes(themes.stream().map(theme -> "http://publications.europa.eu/resource/authority/data-theme/" + theme.toString()).toArray(String[]::new));
            }
        }


        String modified = idfMdMetadataNode.selectSingleNode("//gmd:dateStamp/gco:Date").getText().trim();
        dataset.setModified(modified);

        String issued = idfMdMetadataNode.selectSingleNode("//gmd:identificationInfo[1]/*/gmd:citation/gmd:CI_Citation/gmd:date/gmd:CI_Date/gmd:date/gco:DateTime").getText().trim();
        dataset.setIssued(issued);

        // Distribution
        List<ResourceElement> distResources = new ArrayList<>();
        List<Node> transferOptionNodes = idfMdMetadataNode.selectNodes("//gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions");
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



        Node fileIdentifierNode = idfMdMetadataNode.selectSingleNode("//gmd:fileIdentifier/gco:CharacterString");
        if (fileIdentifierNode != null) {
            dataset.setIdentifier(fileIdentifierNode.getText().trim());
        }


        // TODO!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        //dataset.setAbout(this.appConfig.getPortalDetailUrl() + hit.getId());
        dataset.setAbout(fileIdentifierNode.getText().trim());

        Node languageNode = idfMdMetadataNode.selectSingleNode("//gmd:identificationInfo[1]/gmd:MD_DataIdentification/gmd:language/gmd:LanguageCode/@codeListValue");
        if(languageNode != null) {
            dataset.setLanguage(new LangTextElement(languageNode.getText().trim()));
        }

            /*
        ResourceElement accrualPeriodicity = mapAccrualPeriodicity(hitDetail.getAccrual_periodicity());
        dataset.setAccrualPeriodicity(accrualPeriodicity);
*/

        /*
        // ContactPoint
        dataset.setContactPoint(mapContactPoint(hit.getContact_point()));

        // ORIGINATOR
        dataset.setOriginator(mapOrganizations(hit.getOriginator()));

        // CREATOR
        dataset.setCreator(mapOrganizations(hit.getCreator()));

*/
        // CONTRIBUTOR ID
        dataset.setContributorID(new ResourceElement("http://dcat-ap.de/def/contributors/NUMIS"));

        // SPATIAL
        List<Node> geographicBoundingBoxNodes = idfMdMetadataNode.selectNodes("//gmd:identificationInfo[1]/*/gmd:extent/gmd:EX_Extent/gmd:geographicElement/gmd:EX_GeographicBoundingBox");
        if (geographicBoundingBoxNodes.size() > 0) {
            for (Node node : geographicBoundingBoxNodes) {
                SpatialElement spatial = mapSpatial(node, null);
                dataset.setSpatial(spatial);
            }
        }

        /*

        // TEMPORAL
        if (hit.getExtras() != null && hit.getExtras().getTemporal() != null) {
            List<TemporalElement> temporal = mapTemporal(hit.getExtras().getTemporal());
            dataset.setTemporal(temporal);
        }


 */

        List<Node> constraintsNodes = idfMdMetadataNode.selectNodes("//gmd:identificationInfo[1]/gmd:MD_DataIdentification/gmd:resourceConstraints/gmd:MD_LegalConstraints");
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

        private SpatialElement mapSpatial(ESGeoShape spatial, String spatialText) {
            if(spatial == null){
                return null;
            }

            List<DatatypeTextElement> geometries = new ArrayList<>();

            DatatypeTextElement geoJSON = new DatatypeTextElement();
            geoJSON.setDatatype("https://www.iana.org/assignments/media-types/application/vnd.geo+json");
            geoJSON.setText(mapGeoJson(spatial));
            geometries.add(geoJSON);

            DatatypeTextElement wkt = new DatatypeTextElement();
            wkt.setDatatype("http://www.opengis.net/ont/geosparql#wktLiteral");
            wkt.setText(mapWKT(spatial));
            geometries.add(wkt);

            LocationElement location = new LocationElement();
            location.setGeometry(geometries);

            if(spatialText != null) {
                location.setPrefLabel(spatialText);
            }

            SpatialElement result = new SpatialElement();
            result.setLocation(location);

            return result;
        }

        private String mapGeoJson(ESGeoShape spatial) {
            String type = mapGeoJsonType(spatial.getType());
            if(type.toLowerCase().equals("geometrycollection")){
                String geometries = "["+spatial.getGeometries().stream().map(this::mapGeoJson).collect(Collectors.joining(","))+"]";
                return "{" +
                        "\"type\": \"" + type + "\"" +
                        ", \"geometries\": " + geometries +
                        '}';
            }
            String coordinates = spatial.getCoordinates().toString();
            if(spatial.getType().toLowerCase().equals("envelope")){
                List<List> coords = spatial.getCoordinates();
                coordinates = "[[["+coords.get(0).get(0).toString()+", "+coords.get(0).get(1).toString()+"], " +
                        "["+coords.get(1).get(0).toString()+", "+coords.get(0).get(1).toString()+"], " +
                        "["+coords.get(1).get(0).toString()+", "+coords.get(1).get(1).toString()+"], " +
                        "["+coords.get(0).get(0).toString()+", "+coords.get(1).get(1).toString()+"], " +
                        "["+coords.get(0).get(0).toString()+", "+coords.get(0).get(1).toString()+"]]]";
            }
                return "{" +
                        "\"type\": \"" + type + "\"" +
                        ", \"coordinates\": " + coordinates +
                        '}';
        }

        private String mapGeoJsonType(String type) {
            switch(type.toLowerCase())
            {
                case "point":
                    return "Point";
                case "linestring":
                    return "LineString";
                case "polygon":
                case "envelope":
                    return "Polygon";
                case "multipoint":
                    return "MultiPoint";
                case "multilinestring":
                    return "MultiLineString";
                case "multipolygon":
                    return "MultiPolygon";
                case "geometrycollection":
                    return "GeometryCollection";
                default: return "UNKNOWN";
            }
        }

        private String mapWKT(ESGeoShape spatial) {
            String type = mapWKTType(spatial.getType());
            if(type.toLowerCase().equals("geometrycollection")){
                String geometries = "("+spatial.getGeometries().stream().map(this::mapWKT).collect(Collectors.joining(","))+")";
                return type + geometries;
            }
            String coordinates = mapWKTCoordinates(spatial.getCoordinates());
            if(spatial.getType().toLowerCase().equals("envelope")){
                List<List> coords = spatial.getCoordinates();
                coordinates = "(("+coords.get(0).get(0).toString()+" "+coords.get(0).get(1).toString()+", " +
                        coords.get(1).get(0).toString()+" "+coords.get(0).get(1).toString()+", " +
                        coords.get(1).get(0).toString()+" "+coords.get(1).get(1).toString()+", " +
                        coords.get(0).get(0).toString()+" "+coords.get(1).get(1).toString()+", " +
                        coords.get(0).get(0).toString()+" "+coords.get(0).get(1).toString()+"))";
            }
            return type + " " + coordinates;
        }

        private String mapWKTType(String type) {
            switch(type.toLowerCase())
            {
                case "point":
                    return "POINT";
                case "linestring":
                    return "LINESTRING";
                case "polygon":
                case "envelope":
                    return "POLYGON";
                case "multipoint":
                    return "MULTIPOINT";
                case "multilinestring":
                    return "MULTILINESTRING";
                case "multipolygon":
                    return "MULTIPOLYGON";
                case "geometrycollection":
                    return "GEOMETRYCOLLECTION";
                default: return "UNKNOWN";
            }
        }


        private String mapWKTCoordinates(List coordinates) {
            StringBuilder sb = new StringBuilder();

            sb.append("(");
            for(Object coords: coordinates){
                if(coords instanceof List){
                    if(((List) coords).get(0) instanceof List){
                        sb.append(mapWKTCoordinates((List) coords));
                    } else {
                        for(Object coord : (List)coords){
                            sb.append(coord);
                            sb.append(" ");
                        }
                        sb.deleteCharAt(sb.length()-1);
                    }
                    sb.append(",");
                }
                else{
                    sb.append(coords);
                }
                sb.append(" ");
            }
            sb.deleteCharAt(sb.length()-1);
            if(sb.charAt(sb.length()-1) == ',') {
                sb.deleteCharAt(sb.length() - 1);
            }

            sb.append(")");

            return sb.toString();
        }

        private List<TemporalElement> mapTemporal(List<ESDateRange> temporal) {
            List<TemporalElement> result = new ArrayList<>();
            for (ESDateRange range: temporal) {
                if(range.getGte() != null || range.getLte() != null) {
                    TemporalElement temporalElement = new TemporalElement();
                    PeriodOfTimeElement periodOfTime = new PeriodOfTimeElement();
                    temporalElement.setPeriodOfTime(periodOfTime);
                    if (range.getGte() != null) {
                        DatatypeTextElement start = new DatatypeTextElement();
                        start.setDatatype("http://www.w3.org/2001/XMLSchema#dateTime");
                        start.setText(range.getGte().toInstant().atZone(ZoneId.of("Europe/Berlin")).toLocalDateTime().toString());
                        periodOfTime.setStartDate(start);
                    }
                    if (range.getLte() != null) {
                        DatatypeTextElement end = new DatatypeTextElement();
                        end.setDatatype("http://www.w3.org/2001/XMLSchema#dateTime");
                        end.setText(range.getLte().toInstant().atZone(ZoneId.of("Europe/Berlin")).toLocalDateTime().toString());
                        periodOfTime.setEndDate(end);
                    }
                    result.add(temporalElement);
                }
            }
            if(result.size() > 0) {
                return result;
            }
            return null;
        }

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

        Node idfMdMetadataNode = idfDataNode.selectSingleNode("//idf:idfMdMetadata");

        String modified = idfMdMetadataNode.selectSingleNode("//gmd:identificationInfo[1]/*/gmd:citation/gmd:CI_Citation/gmd:date/gmd:CI_Date/gmd:date/gco:DateTime").getText().trim();

        List<Node> transferOptionNodes = idfMdMetadataNode.selectNodes("//gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions");

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

            dist.setModified(new DatatypeTextElement(modified));
            dist.getModified().setDatatype("http://www.w3.org/2001/XMLSchema#dateTime");

            String format = null;//mapFormat(accessURL, distribution.getFormat());

            if (format != null) {
                dist.setFormat(new ResourceElement("http://publications.europa.eu/resource/authority/file-type/" + format));
            }
            dist.setAbout(accessURL + DISTRIBUTION_RESOURCE_POSTFIX);
            dist.setDownloadURL(new ResourceElement(accessURL));

            /*
            if(distribution.getByteSize() != null){
                dist.setByteSize(new DatatypeTextElement(distribution.getByteSize().toString()));
                dist.getByteSize().setDatatype("http://www.w3.org/2001/XMLSchema#decimal");
            }
            if(distribution.getIssued() != null){
                dist.setIssued(new DatatypeTextElement(distribution.getIssued().toInstant().toString()));
                dist.getIssued().setDatatype("http://www.w3.org/2001/XMLSchema#dateTime");
            }
            if(distribution.getModified() != null){
                dist.setModified(new DatatypeTextElement(distribution.getModified().toInstant().toString()));
                dist.getModified().setDatatype("http://www.w3.org/2001/XMLSchema#dateTime");
            }
            if(distribution.getDescription() != null && !distribution.getDescription().trim().isEmpty()) {
                dist.setDescription(distribution.getDescription().trim());
            }

            if (license != null) {
                String uriFromLicenseURL = LicenseMapper.getURIFromLicenseURL(license.getUrl());
                if (uriFromLicenseURL == null) {
                    log.warn("Using free license for dataset: " + hit.getTitle() + " (" + hit.getId() + ")");
                    // TODO: make default license configurable
                    dist.setLicense(new ResourceElement("http://dcat-ap.de/def/licenses/other-open"));
                } else {
                    dist.setLicense(new ResourceElement(uriFromLicenseURL));
                }
            }
            */

            dists.add(dist);
        }



        return dists;

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

        String north = spatial.selectSingleNode("//gmd:northBoundLatitude/gco:Decimal").getText().trim();
        String east = spatial.selectSingleNode("//gmd:eastBoundLongitude/gco:Decimal").getText().trim();
        String south = spatial.selectSingleNode("//gmd:southBoundLatitude/gco:Decimal").getText().trim();
        String west = spatial.selectSingleNode("//gmd:westBoundLongitude/gco:Decimal").getText().trim();

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

/*
    public DcatApDe mapHitDocToDcat(IngridHit hit) {

        DcatApDe dcatApDe = new DcatApDe();
        mapCatalog(dcatApDe.getCatalog());
        Dataset dataset = mapDataset(hit);
        List<Dataset> datasets = new ArrayList<>();
        datasets.add(dataset);
        dcatApDe.getCatalog().setDataset(this.appConfig.getPortalDetailUrl() + hit.getId());
        dcatApDe.setDataset(datasets);
        dcatApDe.setDistribution(mapDistribution(hit, new ArrayList<>()));

        return dcatApDe;
    }
    */

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
