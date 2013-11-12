package de.ingrid.iface_test.atomDownloadService;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import junit.framework.TestCase;

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
import de.ingrid.iface.util.StringUtils;
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

        String atomDownloadServiceFeedUrlPattern = org.apache.commons.lang.StringUtils.stripEnd(config.getString(SearchInterfaceConfig.ATOM_DOWNLOAD_SERVICE_URL), "/");
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
            ServiceFeedProducer serviceFeedProducer = new ServiceFeedProducer();
            serviceFeedProducer.setConfig(config);
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
            serviceFeedProducer.init();

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
            String body = serviceFeedAtomBuilder.build(serviceFeed);
            Document atomServiceFeed = stringToDocument(body);

            TestCase.assertEquals(config.getString(SearchInterfaceConfig.METADATA_ACCESS_URL).replace("{uuid}", uuid), atomXPath.getString(atomServiceFeed, "/atom:feed/atom:link[@rel='describedby']/@href"));
            TestCase.assertEquals(atomDownloadServiceFeedUrlPattern.replace("{servicefeed-uuid}", StringUtils.encodeForPath(uuid)), atomXPath.getString(atomServiceFeed, "/atom:feed/atom:link[@rel='this document']/@href"));
            TestCase.assertEquals(XPATH.getString(idfDoc, "//gmd:identificationInfo//gmd:citation/gmd:CI_Citation/gmd:title/gco:CharacterString"), atomXPath.getString(atomServiceFeed, "/atom:feed/atom:title"));

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
                        TestCase.assertFalse(atomXPath.nodeExists(atomServiceFeed, "/atom:feed/atom:entry/atom:id[contains(.,'" + coupledUuid + "')]"));
                    } else {
                        System.out.println("Found coupled resource with data download: " + coupledHit.getHitDetail().getTitle());
                        System.out.println(XMLUtils.toString(idfDoc));
                        TestCase.assertTrue(atomXPath.nodeExists(atomServiceFeed, "/atom:feed/atom:entry/atom:id[contains(.,'" + coupledUuid + "')]"));
                        TestCase.assertTrue(atomXPath.nodeExists(atomServiceFeed,
                                "/atom:feed/atom:entry/atom:title[contains(.,'" + XPATH.getString(idfCoupledResourceDoc, "//gmd:identificationInfo//gmd:citation/gmd:CI_Citation/gmd:title/gco:CharacterString") + "')]"));
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
