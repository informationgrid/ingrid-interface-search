package de.ingrid.iface.atomDownloadService;

import java.util.List;

import org.w3c.dom.Document;

import de.ingrid.iface.atomDownloadService.om.DatasetFeedEntry;

public interface DatasetFeedEntryProducer {

    public List<DatasetFeedEntry> produce(Document doc) throws Exception;

}