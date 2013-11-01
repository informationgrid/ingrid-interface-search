package de.ingrid.iface.atomDownloadService;

import org.apache.commons.lang.StringEscapeUtils;
import org.springframework.stereotype.Service;

import de.ingrid.iface.atomDownloadService.om.DatasetFeed;
import de.ingrid.iface.atomDownloadService.om.DatasetFeedEntry;
import de.ingrid.iface.atomDownloadService.om.Link;

@Service
public class DatasetAtomBuilder {

    public String build(DatasetFeed datasetFeed) {

        StringBuilder result = new StringBuilder();
        result.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        result.append("<!-- Example \" Dataset Feed\" -->\n");
        result.append("<feed xmlns=\"http://www.w3.org/2005/Atom\" xml:lang=\"de\">\n");
        result.append("<!-- feed title -->\n");
        result.append("<title>" + StringEscapeUtils.escapeXml(datasetFeed.getTitle()) + "</title>\n");
        result.append("<!-- identifier -->\n");
        result.append("<id>" + StringEscapeUtils.escapeXml(datasetFeed.getUuid()) + "</id>\n");
        result.append("<!-- date/time this feed was last updated -->\n");
        result.append("<updated>" + datasetFeed.getUpdated() + "</updated>\n");

        for (DatasetFeedEntry entry : datasetFeed.getEntries()) {
            result.append("<entry>\n");
            result.append("<id>" + entry.getId() + "</id>\n");
            result.append("<title>" + StringEscapeUtils.escapeXml(entry.getTitle()) + "</title>\n");
            result.append("<!-- file download link -->\n");
            for (Link link : entry.getLinks()) {
                result.append("<link href=\"" + link.getHref() + "\" rel=\"" + link.getRel() + "\" type=\"" + link.getType() + "\" length=\"" + link.getLength() + "\" title=\"" + StringEscapeUtils.escapeXml(link.getTitle()) + "\"/>\n");
            }
            result.append("<updated>" + entry.getUpdated() + "</updated>\n");
            result.append("</entry>\n");
        }
        result.append("</feed>\n");

        return result.toString();

    }

}
