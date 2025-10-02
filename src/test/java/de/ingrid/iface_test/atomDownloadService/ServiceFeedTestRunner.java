/*
 * **************************************************-
 * ingrid-interface-search
 * ==================================================
 * Copyright (C) 2014 - 2025 wemove digital solutions GmbH
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
package de.ingrid.iface_test.atomDownloadService;

import static org.junit.jupiter.api.Assertions.*;

import java.io.StringReader;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.springframework.util.xml.SimpleNamespaceContext;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import de.ingrid.iface.atomDownloadService.CSWGetRecordByIdServiceFeedEntryProducer;
import de.ingrid.iface.atomDownloadService.IGCCoupledResourcesServiceFeedEntryProducer;
import de.ingrid.iface.atomDownloadService.ServiceFeedAtomBuilder;
import de.ingrid.iface.atomDownloadService.ServiceFeedEntryProducer;
import de.ingrid.iface.atomDownloadService.ServiceFeedProducer;
import de.ingrid.iface.atomDownloadService.om.ServiceFeed;
import de.ingrid.iface.atomDownloadService.requests.ServiceFeedRequest;
import de.ingrid.iface.atomDownloadService.util.IngridQueryProducer;
import de.ingrid.iface.util.IBusHelper;
import de.ingrid.iface.util.IBusQueryResultIterator;
import de.ingrid.iface.util.IdfUtils;
import de.ingrid.iface.util.SearchInterfaceConfig;
import de.ingrid.iface.util.ServiceFeedUtils;
import de.ingrid.iface.util.StringUtils;
import de.ingrid.iface.util.UserAgentDetector;
import de.ingrid.utils.IBus;
import de.ingrid.utils.IngridHit;
import de.ingrid.utils.queryparser.ParseException;
import de.ingrid.utils.queryparser.QueryStringParser;
import de.ingrid.utils.xml.IDFNamespaceContext;
import de.ingrid.utils.xml.XMLUtils;
import de.ingrid.utils.xpath.XPathUtils;

public class ServiceFeedTestRunner {

    private static final String[] REQUESTED_FIELDS = new String[] {};
    private static final XPathUtils XPATH = new XPathUtils(new IDFNamespaceContext());

    private static SearchInterfaceConfig config = SearchInterfaceConfig.getInstance();

    /**
     * @param args
     * @throws Exception
     * @throws ParseException
     */
    public static void main(String[] args) throws ParseException, Exception {

        SimpleNamespaceContext atomNamespaceContext = new SimpleNamespaceContext();
        atomNamespaceContext.bindNamespaceUri("atom", "http://www.w3.org/2005/Atom");
        atomNamespaceContext.bindNamespaceUri("georss", "http://www.georss.org/georss");
        atomNamespaceContext.bindNamespaceUri("inspire_dls", "http://inspire.ec.europa.eu/schemas/inspire_dls/1.0");
        XPathUtils atomXPath = new XPathUtils(atomNamespaceContext);

        String atomDownloadServiceFeedUrlPattern = org.apache.commons.lang3.StringUtils.stripEnd(config.getString(SearchInterfaceConfig.ATOM_DOWNLOAD_SERVICE_URL), "/");
        atomDownloadServiceFeedUrlPattern += config.getString(SearchInterfaceConfig.ATOM_DOWNLOAD_SERVICE_FEED_EXTENSION);

        IBusHelper iBusHelper = new IBusHelper();
        IBus iBus = iBusHelper.getIBus();

        Document idfDoc = null;

        // get all geodataservice objects
        IBusQueryResultIterator serviceIterator = new IBusQueryResultIterator(QueryStringParser.parse("ranking:score datatype:metadata metaclass:geoservice"), REQUESTED_FIELDS, iBus);
        while (serviceIterator.hasNext()) {

            IngridHit hit = serviceIterator.next();
            System.out.println("Working on geodata service: " + hit.getHitDetail().getTitle());
            idfDoc = IdfUtils.getIdfDocument(iBus.getRecord(hit));
            System.out.println(XMLUtils.toString(idfDoc));

            String uuid = XPATH.getString(idfDoc, "//gmd:fileIdentifier/gco:CharacterString");

            // get the service Feed

            ServiceFeedUtils serviceFeedUtils = new ServiceFeedUtils();
            serviceFeedUtils.setConfig(SearchInterfaceConfig.getInstance());
            serviceFeedUtils.init();

            ServiceFeedProducer serviceFeedProducer = new ServiceFeedProducer();
            serviceFeedProducer.setServiceFeedUtils(serviceFeedUtils);
            serviceFeedProducer.setiBusHelper(iBusHelper);

            IngridQueryProducer ingridQueryProducer = new IngridQueryProducer();
            ingridQueryProducer.setiBusHelper(iBusHelper);

            serviceFeedProducer.setIngridQueryProducer(ingridQueryProducer);

            List<ServiceFeedEntryProducer> serviceFeedEntryProducer = new ArrayList<ServiceFeedEntryProducer>();

            IGCCoupledResourcesServiceFeedEntryProducer igcCoupledResourcesServiceFeedEntryProducer = new IGCCoupledResourcesServiceFeedEntryProducer();
            igcCoupledResourcesServiceFeedEntryProducer.setConfig(config);
            igcCoupledResourcesServiceFeedEntryProducer.setiBusHelper(iBusHelper);
            igcCoupledResourcesServiceFeedEntryProducer.setIngridQueryProducer(ingridQueryProducer);
            igcCoupledResourcesServiceFeedEntryProducer.init();
            serviceFeedEntryProducer.add(igcCoupledResourcesServiceFeedEntryProducer);

            CSWGetRecordByIdServiceFeedEntryProducer cswGetRecordByIdServiceFeedEntryProducer = new CSWGetRecordByIdServiceFeedEntryProducer();
            cswGetRecordByIdServiceFeedEntryProducer.setConfig(config);
            cswGetRecordByIdServiceFeedEntryProducer.init();
            serviceFeedEntryProducer.add(cswGetRecordByIdServiceFeedEntryProducer);

            serviceFeedProducer.setServiceFeedEntryProducer(serviceFeedEntryProducer);

            ServiceFeedRequest serviceFeedRequest = new ServiceFeedRequest();
            serviceFeedRequest.setUuid(uuid);
            if (uuid.equals("F858C987-9C50-4021-9958-D5C42121E8D3")) {
                System.out.println(XMLUtils.toString(idfDoc));
            }
            ServiceFeed serviceFeed = serviceFeedProducer.produce(serviceFeedRequest);
            if (serviceFeed == null) {
                System.out.println("Cannot produce Service Feed for uuid: " + uuid);
                continue;
            }
            ServiceFeedAtomBuilder serviceFeedAtomBuilder = new ServiceFeedAtomBuilder();
            serviceFeedAtomBuilder.setUserAgentDetector(new UserAgentDetector());
            String body = serviceFeedAtomBuilder.build(serviceFeed, "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.0; WOW64; SLCC1; .NET CLR 2.0.50727; .NET CLR 3.0.04506; Media Center PC 5.0; .NET CLR 1.1.4322)");
            Document atomServiceFeed = stringToDocument(body);

            assertEquals(config.getString(SearchInterfaceConfig.METADATA_ACCESS_URL).replace("{uuid}", uuid), atomXPath.getString(atomServiceFeed, "/atom:feed/atom:link[@rel='describedby']/@href"));
            assertEquals(atomDownloadServiceFeedUrlPattern.replace("{servicefeed-uuid}", StringUtils.encodeForPath(uuid)), atomXPath.getString(atomServiceFeed, "/atom:feed/atom:link[@rel='self']/@href"));
            assertEquals(XPATH.getString(idfDoc, "//gmd:identificationInfo//gmd:citation/gmd:CI_Citation/gmd:title/gco:CharacterString"), atomXPath.getString(atomServiceFeed, "/atom:feed/atom:title"));

            // get the data manually
            String[] coupledUuids = XPATH.getStringArray(idfDoc, "//srv:operatesOn/@uuidref");
            for (String coupledUuid : coupledUuids) {
                IBusQueryResultIterator coupledResourceIterator = new IBusQueryResultIterator(QueryStringParser.parse("ranking:score (t01_object.obj_id:" + coupledUuid + " OR t01_object.org_obj_id:" + coupledUuid + ")"), REQUESTED_FIELDS,
                        iBus);
                if (coupledResourceIterator.hasNext()) {
                    IngridHit coupledHit = coupledResourceIterator.next();
                    Document idfCoupledResourceDoc = IdfUtils.getIdfDocument(iBus.getRecord(coupledHit));
                    // check for data sets without data download links
                    if (!XPATH.nodeExists(idfCoupledResourceDoc, "//gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine[.//gmd:function/gmd:CI_OnLineFunctionCode/@codeListValue='Download of data']")) {
                        System.out.println("No Download Data Links found in coupled resource: " + coupledHit.getHitDetail().getTitle());
                        assertFalse(atomXPath.nodeExists(atomServiceFeed, "/atom:feed/atom:entry/atom:id[contains(.,'" + coupledUuid + "')]"));
                    } else {
                        System.out.println("Found coupled resource with data download: " + coupledHit.getHitDetail().getTitle());
                        System.out.println(XMLUtils.toString(idfDoc));
                        assertTrue(atomXPath.nodeExists(atomServiceFeed, "/atom:feed/atom:entry/atom:id[contains(.,'" + coupledUuid + "')]"));
                        assertTrue(atomXPath.nodeExists(atomServiceFeed,
                                "/atom:feed/atom:entry/atom:title[contains(.,'" + XPATH.getString(idfCoupledResourceDoc, "//gmd:identificationInfo//gmd:citation/gmd:CI_Citation/gmd:title/gco:CharacterString") + "')]"));
                    }
                }
            }
            // check service feed against W3C validator
            String feedUrl = atomXPath.getString(atomServiceFeed, "/atom:feed/atom:link[@rel='self']/@href");
            String validatorUrl = "http://validator.w3.org/feed/check.cgi?url=" + URLEncoder.encode(feedUrl, "UTF-8");
            Scanner s = new Scanner(new URL(validatorUrl).openStream(), "UTF-8").useDelimiter("\\A");
            String out = s.next();
            s.close();
            if (!out.toLowerCase().contains("this is a valid atom 1.0 feed.")) {
                System.out.println("Invalid ATOM Feed:" + feedUrl);
                assertEquals(true, out.toLowerCase().contains("this is a valid atom 1.0 feed."));
            } else {
                String[] datasetFeedUrls = atomXPath.getStringArray(atomServiceFeed, "/atom:feed/atom:entry/atom:link[@rel='alternate']/@href");
                for (String datasetFeedUrl : datasetFeedUrls) {
                    validatorUrl = "http://validator.w3.org/feed/check.cgi?url=" + URLEncoder.encode(datasetFeedUrl, "UTF-8");
                    s = new Scanner(new URL(validatorUrl).openStream(), "UTF-8").useDelimiter("\\A");
                    out = s.next();
                    s.close();
                    if (!out.toLowerCase().contains("this is a valid atom 1.0 feed.")) {
                        System.out.println("Invalid ATOM Feed:" + feedUrl);
                        assertEquals(true, out.toLowerCase().contains("this is a valid atom 1.0 feed."));
                    }
                }
            }

        }
        System.out.println("Test finished.");
    }

    public static Document stringToDocument(String string) throws Exception {
        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        domFactory.setNamespaceAware(true);
        DocumentBuilder builder = domFactory.newDocumentBuilder();
        Document doc = builder.parse(new InputSource(new StringReader(string)));
        return doc;
    }

}
