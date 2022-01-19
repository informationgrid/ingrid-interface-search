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
package de.ingrid.iface.util;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.ingrid.iface.atomDownloadService.om.Author;
import de.ingrid.iface.atomDownloadService.om.Link;
import de.ingrid.iface.atomDownloadService.om.ServiceFeed;
import de.ingrid.iface.atomDownloadService.requests.BaseRequest;
import de.ingrid.utils.xml.IDFNamespaceContext;
import de.ingrid.utils.xpath.XPathUtils;

@Service
public class ServiceFeedUtils {

    private static final XPathUtils XPATH = new XPathUtils(new IDFNamespaceContext());

    private SearchInterfaceConfig config;

    private String atomDownloadServiceFeedUrlPattern = null;

    private String atomDownloadOpensearchDefinitionUrlPattern = null;

    @PostConstruct
    public void init() {
        atomDownloadServiceFeedUrlPattern = org.apache.commons.lang.StringUtils.stripEnd(config.getString(SearchInterfaceConfig.ATOM_DOWNLOAD_SERVICE_URL), "/");
        atomDownloadServiceFeedUrlPattern += config.getString(SearchInterfaceConfig.ATOM_DOWNLOAD_SERVICE_FEED_EXTENSION);

        atomDownloadOpensearchDefinitionUrlPattern = org.apache.commons.lang.StringUtils.stripEnd(config.getString(SearchInterfaceConfig.ATOM_DOWNLOAD_SERVICE_URL), "/");
        atomDownloadOpensearchDefinitionUrlPattern += config.getString(SearchInterfaceConfig.ATOM_DOWNLOAD_OPENSEARCH_DEFINITION_EXTENSION);
    }

    public ServiceFeed createFromIdf(Document idfDoc, BaseRequest request) {
        ServiceFeed feed = new ServiceFeed();
        feed.setUuid(XPATH.getString(idfDoc, "//gmd:fileIdentifier/gco:CharacterString"));
        feed.setTitle(XPATH.getString(idfDoc, "//gmd:identificationInfo//gmd:citation/gmd:CI_Citation/gmd:title/gco:CharacterString"));
        feed.setSubTitle(XPATH.getString(idfDoc, "//gmd:identificationInfo//gmd:abstract/gco:CharacterString"));
        feed.setUpdated(XPATH.getString(idfDoc, "//gmd:dateStamp/gco:DateTime | //gmd:dateStamp/gco:Date[not(../gco:DateTime)]"));

        NodeList resourceConstraints = XPATH.getNodeList(idfDoc, "//gmd:identificationInfo/*/gmd:resourceConstraints[*/gmd:accessConstraints]");
        StringBuilder copyRight = new StringBuilder();
        for (int i = 0; i < resourceConstraints.getLength(); i++) {
            Node resourceConstraint = resourceConstraints.item(i);
            String restrictionCode = XPATH.getString(resourceConstraint, "*/gmd:accessConstraints/*/@codeListValue");
            if (copyRight.length() > 0) {
                copyRight.append("; ");
            }
            copyRight.append(restrictionCode);
            if (restrictionCode.equalsIgnoreCase("otherRestrictions")) {
                String otherRestrictions = XPATH.getString(resourceConstraint, "*/gmd:otherConstraints/gco:CharacterString");
                if (otherRestrictions != null && otherRestrictions.length() > 0) {
                    copyRight.append(": ").append(otherRestrictions);
                }
            }
        }
        feed.setCopyright(copyRight.toString());

        Link link = new Link();
        link.setHref(config.getString(SearchInterfaceConfig.METADATA_ACCESS_URL).replace("{uuid}", feed.getUuid()));
        link.setRel("describedby");
        link.setType("application/vnd.ogc.csw.GetRecordByIdResponse_xml");
        feed.setMetadataAccessUrl(link);

        link = new Link();
        String urlPattern = URLUtil.updateProtocol( atomDownloadServiceFeedUrlPattern, request.getProtocol() );
        link.setHref(urlPattern.replace("{servicefeed-uuid}", StringUtils.encodeForPath(feed.getUuid())));
        link.setHrefLang("de");
        link.setType("application/atom+xml");
        link.setRel("self");
        link.setTitle("Feed containing the dataset (in one or more downloadable formats)");
        feed.setSelfReferencingLink(link);
        feed.setIdentifier(link.getHref());

        link = new Link();
        urlPattern = URLUtil.updateProtocol( atomDownloadOpensearchDefinitionUrlPattern, request.getProtocol() );
        link.setHref(urlPattern.replace("{servicefeed-uuid}", StringUtils.encodeForPath(feed.getUuid())));
        link.setHrefLang("de");
        link.setType("application/opensearchdescription+xml");
        link.setTitle("Open Search Description");
        link.setRel("search");
        feed.setOpenSearchDefinitionLink(link);

        Author author = new Author();
        author.setName(XPATH.getString(idfDoc, "//gmd:identificationInfo//gmd:pointOfContact//gmd:organisationName/gco:CharacterString"));
        author.setEmail(XPATH.getString(idfDoc, "//gmd:identificationInfo//gmd:pointOfContact//gmd:contactInfo/gmd:CI_Contact/gmd:address/gmd:CI_Address/gmd:electronicMailAddress/gco:CharacterString"));
        feed.setAuthor(author);

        return feed;
    }

    @Autowired
    public void setConfig(SearchInterfaceConfig config) {
        this.config = config;
    }

}
