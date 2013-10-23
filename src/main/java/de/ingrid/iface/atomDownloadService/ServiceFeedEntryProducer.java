package de.ingrid.iface.atomDownloadService;

import java.util.List;

import org.w3c.dom.Document;

import de.ingrid.iface.atomDownloadService.om.ServiceFeed;
import de.ingrid.iface.atomDownloadService.om.ServiceFeedEntry;

public interface ServiceFeedEntryProducer {

    public List<ServiceFeedEntry> produce(Document idfDoc, ServiceFeed serviceFeed) throws Exception;

}