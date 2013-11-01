package de.ingrid.iface.atomDownloadService;

import org.apache.commons.lang.StringEscapeUtils;
import org.springframework.stereotype.Service;

import de.ingrid.iface.atomDownloadService.om.DatasetFeed;
import de.ingrid.iface.atomDownloadService.om.DatasetFeedEntry;
import de.ingrid.iface.atomDownloadService.om.Link;

@Service
public class DatasetFeedAtomBuilder {

    public String build(DatasetFeed datasetFeed) {

        StringBuilder result = new StringBuilder();
        result.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        result.append("<!-- Example \" Dataset Feed\" -->\n");
        result.append("<feed xmlns=\"http://www.w3.org/2005/Atom\" xmlns:georss=\"http://www.georss.org/georss\" xml:lang=\"de\">\n");
        result.append("<author>\n" + "<name>" + StringEscapeUtils.escapeXml(datasetFeed.getAuthor().getName()) + "</name>\n" + "<email>" + StringEscapeUtils.escapeXml(datasetFeed.getAuthor().getEmail()) + "</email>\n" + "</author>\n");
        result.append("<!-- identifier -->\n");
        result.append("<id>" + StringEscapeUtils.escapeXml(datasetFeed.getSelfReferencingLink().getHref()) + "</id>\n");
        result.append("<!-- self-referencing link to this feed -->\n");
        result.append("<link href=\"" + StringEscapeUtils.escapeXml(datasetFeed.getSelfReferencingLink().getHref()) + "\" rel=\"" + datasetFeed.getSelfReferencingLink().getRel() + "\" type=\"" + datasetFeed.getSelfReferencingLink().getType() + "\" hreflang=\""
                + datasetFeed.getSelfReferencingLink().getHrefLang() + "\" title=\"" + StringEscapeUtils.escapeXml(datasetFeed.getSelfReferencingLink().getTitle()) + "\"/>\n");
        result.append("<!-- ‘upward’ link to the corresponding download service feed -->");
        result.append("<link href=\"" + StringEscapeUtils.escapeXml(datasetFeed.getDownloadServiceFeed().getHref()) + "\" rel=\"" + datasetFeed.getDownloadServiceFeed().getRel() + "\" type=\"" + datasetFeed.getDownloadServiceFeed().getType() + "\" hreflang=\""
                + datasetFeed.getDownloadServiceFeed() + "\" title=\"" + (datasetFeed.getDownloadServiceFeed().getTitle() == null ? "" : StringEscapeUtils.escapeXml(datasetFeed.getDownloadServiceFeed().getTitle())) + "\"/>\n");
        result.append("<!-- rights, access restrictions  -->\n");
        result.append("<rights>" + StringEscapeUtils.escapeXml(datasetFeed.getRights()) + "</rights>\n");
        result.append("<!-- feed subtitle -->\n");
        result.append("<subtitle>" + StringEscapeUtils.escapeXml(datasetFeed.getSubTitle()) + "</subtitle>\n");
        result.append("<!-- feed title -->\n");
        result.append("<title>" + StringEscapeUtils.escapeXml(datasetFeed.getTitle()) + "</title>\n");
        result.append("<!-- date/time this feed was last updated -->\n");
        result.append("<updated>" + datasetFeed.getUpdated() + "</updated>\n");

        for (DatasetFeedEntry entry : datasetFeed.getEntries()) {
            result.append("<entry>\n");
            result.append("<id>" + entry.getId() + "</id>\n");
            result.append("<!-- file download link -->\n");
            for (Link link : entry.getLinks()) {
                result.append("<link href=\"" + StringEscapeUtils.escapeXml(link.getHref()) + "\" rel=\"" + link.getRel() + "\" type=\"" + link.getType() + "\" length=\"" + link.getLength() + "\" title=\"" + StringEscapeUtils.escapeXml(link.getTitle()) + "\"/>\n");
            }
            result.append("<title>" + StringEscapeUtils.escapeXml(entry.getTitle()) + "</title>\n");
            result.append("</entry>\n");
        }
        result.append("</feed>\n");

        return result.toString();

    }

}