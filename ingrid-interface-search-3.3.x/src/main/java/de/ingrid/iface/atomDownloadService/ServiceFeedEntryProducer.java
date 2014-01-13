package de.ingrid.iface.atomDownloadService;

import java.util.List;

import org.w3c.dom.Document;

import de.ingrid.iface.atomDownloadService.om.ServiceFeed;
import de.ingrid.iface.atomDownloadService.om.ServiceFeedEntry;
import de.ingrid.iface.atomDownloadService.requests.ServiceFeedRequest;

public interface ServiceFeedEntryProducer {

    public List<ServiceFeedEntry> produce(Document idfDoc, ServiceFeed serviceFeed, ServiceFeedRequest serviceFeedRequest) throws Exception;

}