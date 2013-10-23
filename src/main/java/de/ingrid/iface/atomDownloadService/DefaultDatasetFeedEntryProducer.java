package de.ingrid.iface.atomDownloadService;

import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.apache.tika.config.TikaConfig;
import org.apache.tika.detect.Detector;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import de.ingrid.iface.atomDownloadService.om.DatasetFeedEntry;
import de.ingrid.iface.atomDownloadService.om.Link;
import de.ingrid.utils.xml.IDFNamespaceContext;
import de.ingrid.utils.xpath.XPathUtils;

@Service
public class DefaultDatasetFeedEntryProducer implements DatasetFeedEntryProducer {

    private static final XPathUtils XPATH = new XPathUtils(new IDFNamespaceContext());
    
    private TikaConfig tikaConfig = TikaConfig.getDefaultConfig();
    private Detector detector = tikaConfig.getDetector();


    public List<DatasetFeedEntry> produce(Document doc) throws Exception {
        List<DatasetFeedEntry> results = new ArrayList<DatasetFeedEntry>();

        NodeList linkages = XPATH.getNodeList(doc, "//gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine[.//gmd:function/gmd:CI_OnLineFunctionCode/@codeListValue='Download of data']");
        for (int i = 0; i < linkages.getLength(); i++) {
            DatasetFeedEntry entry = new DatasetFeedEntry();

            String linkage = XPATH.getString(linkages.item(i), ".//gmd:linkage/gmd:URL");
            List<Link> links = new ArrayList<Link>();
            Link link = new Link();
            link.setHref(linkage);
            link.setRel("alternate");
            try {

                TikaInputStream stream = TikaInputStream.get(new URL(link.getHref()));

                Metadata metadata = new Metadata();
                metadata.add(Metadata.RESOURCE_NAME_KEY, link.getHref());
                MediaType mediaType = detector.detect(stream, metadata);
                link.setType(mediaType.toString());
            } catch (UnknownHostException e) {
                continue;
            } catch (Exception e) {
                link.setType("application/octet-stream");
            }
            String name = XPATH.getString(linkages.item(i), ".//gmd:name//gco:CharacterString");
            if (name == null) {
                name = link.getHref();
            }
            link.setTitle(name);
            if (entry.getTitle() == null) {
                entry.setTitle(name);
            }
            links.add(link);
            entry.setLinks(links);
            entry.setId(link.getHref());

            results.add(entry);
        }

        return results;
    }

}