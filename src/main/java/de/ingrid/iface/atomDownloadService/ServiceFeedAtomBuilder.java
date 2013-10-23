package de.ingrid.iface.atomDownloadService;

import org.springframework.stereotype.Service;

import de.ingrid.iface.atomDownloadService.om.Category;
import de.ingrid.iface.atomDownloadService.om.ServiceFeed;
import de.ingrid.iface.atomDownloadService.om.ServiceFeedEntry;

@Service
public class ServiceFeedAtomBuilder {

    public String build(ServiceFeed serviceFeed) {

        StringBuilder result = new StringBuilder();
        result.append("<!-- Example \"Download Service Feed\" -->\n");
        result.append("<feed xmlns=\"http://www.w3.org/2005/Atom\" xmlns:georss=\"http://www.georss.org/georss\" xmlns:inspire_dls=\"http://inspire.ec.europa.eu/schemas/inspire_dls/1.0\" xml:lang=\"de\">\n");
        result.append("<!-- feed title -->\n");
        result.append("<title>" + serviceFeed.getTitle() + "</title>\n");
        result.append("<!-- feed subtitle -->\n");
        result.append("<subtitle>" + serviceFeed.getSubTitle() + "</subtitle>\n");
        result.append("<!-- link to download service ISO 19139 metadata -->\n");
        result.append("<link href=\"" + serviceFeed.getMetadataAccessUrl().getHref() + "\" rel=\"" + serviceFeed.getMetadataAccessUrl().getRel() + "\" type=\"" + serviceFeed.getMetadataAccessUrl().getType() + "\"/>\n");
        result.append("<!-- self-referencing link to this feed -->\n");
        result.append("<link href=\"" + serviceFeed.getSelfReferencingLink().getHref() + "\" rel=\"" + serviceFeed.getSelfReferencingLink().getRel() + "\" type=\"" + serviceFeed.getSelfReferencingLink().getType() + "\" hreflang=\"" + serviceFeed.getSelfReferencingLink().getHrefLang() + "\" title=\"" + serviceFeed.getSelfReferencingLink().getTitle() + "\"/>\n");
        result.append("<!-- link to Open Search definition file for this service -->\n");
        result.append("<link href=\"" + serviceFeed.getOpenSearchDefinitionLink().getHref() + "\" rel=\"" + serviceFeed.getOpenSearchDefinitionLink().getRel() + "\" type=\"" + serviceFeed.getOpenSearchDefinitionLink().getType() + "\" hreflang=\"" + serviceFeed.getOpenSearchDefinitionLink().getHrefLang() + "\" title=\"" + serviceFeed.getOpenSearchDefinitionLink().getTitle() + "\"/>\n");
        result.append("<!-- identifier -->\n");
        result.append("<id>" + serviceFeed.getSelfReferencingLink().getHref() + "</id>\n");
        result.append("<!-- rights, access restrictions  -->\n");
        result.append("<rights>" + serviceFeed.getCopyright() + "</rights>\n");
        result.append("<!-- date/time this feed was last updated -->\n");
        result.append("<updated>" + serviceFeed.getUpdated() + "</updated>\n");
        result.append("<author>\n" + "<name>" + serviceFeed.getAuthor().getName() + "</name>\n" + "<email>" + serviceFeed.getAuthor().getEmail() + "</email>\n" + "</author>\n");

        for (ServiceFeedEntry entry : serviceFeed.getEntries()) {
            result.append("<entry>\n");
            result.append("<!-- feed dataset feed -->\n");
            result.append("<title>" + entry.getTitle() + "</title>\n");
            result.append("<!-- Spatial Dataset Unique Resourse Identifier for this dataset -->\n");
            if (entry.getSpatialDatasetIdentifierCode() != null && entry.getSpatialDatasetIdentifierNamespace() != null) {
                result.append("<inspire_dls:spatial_dataset_identifier_code>" + entry.getSpatialDatasetIdentifierCode() + "</inspire_dls:spatial_dataset_identifier_code>\n");
                result.append("<inspire_dls:spatial_dataset_identifier_namespace>" + entry.getSpatialDatasetIdentifierNamespace() + "</inspire_dls:spatial_dataset_identifier_namespace>\n");
            }
            result.append("<!-- link to dataset metadata record -->\n");
            result.append("<link href=\"" + entry.getDatasetMetadataRecord().getHref() + "\" rel=\"" + entry.getDatasetMetadataRecord().getRel() + "\" type=\"" + entry.getDatasetMetadataRecord().getType() + "\"/>\n");
            result.append("<!-- link to Dataset Feed -->\n");
            result.append("<link href=\"" + entry.getDatasetFeed().getHref() + "\" type=\"" + entry.getDatasetFeed().getType() + "\" hreflang=\"" + entry.getDatasetFeed().getHrefLang() + "\" title=\"" + entry.getDatasetFeed().getTitle() + "\"/>\n");
            result.append("<!-- identifier for \"Dataset Feed\" for pre-defined dataset -->\n");
            result.append("<id>" + entry.getDatasetIdentifier() + "</id>\n");
            result.append("<!-- rights, access info for pre-defined dataset -->\n");
            result.append("<rights>" + entry.getRights() + "</rights>\n");
            result.append("<!-- last date/time this entry was updated -->\n");
            result.append("<updated>" + entry.getUpdated() + "</updated>\n");
            result.append("<!-- summary -->\n");
            result.append("<summary>" + entry.getSummary() + "</summary>\n");
            if (entry.getPolygon() != null && entry.getPolygon().size() > 0) {
                result.append("<!-- optional GeoRSS-Simple polygon outlining the bounding box of the pre-defined dataset described by the entry. Must be lat lon -->\n");
                result.append("<georss:polygon>");
                for (Double val : entry.getPolygon()) {
                    result.append(val);
                    result.append(" ");
                }
                result.deleteCharAt(result.length() -1 );
                result.append("</georss:polygon>");
            }

            if (entry.getCrs() != null && entry.getCrs().size() > 0) {
                result.append("<!-- CRSs in which the pre-defined Dataset is available -->\n");
                for (Category cat : entry.getCrs()) {
                    result.append("<category term=\"" + cat.term + "\" label=\"" + cat.getLabel() + "\"/>");
                }
            }
            result.append("</entry>\n");
        }

        return result.toString();

    }

}
