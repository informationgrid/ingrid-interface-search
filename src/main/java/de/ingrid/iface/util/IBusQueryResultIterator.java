package de.ingrid.iface.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

import de.ingrid.utils.IBus;
import de.ingrid.utils.IngridHit;
import de.ingrid.utils.IngridHits;
import de.ingrid.utils.query.IngridQuery;

/**
 * Iterates over the result set of an query. The results are stored internally
 * in chunks of 10 results. Only 10 results are fetched at once. If more results
 * are available the ibus will be requeried dynamically.
 * 
 * 
 * @author joachim@wemove.com
 * 
 */
public class IBusQueryResultIterator implements Iterator<IngridHit> {

    private IngridQuery q;
    private String[] requestedFields;
    private IngridHits resultBuffer = null;;
    private int resultBufferCursor = 0;
    private long resultCursor = 0;
    private int resultPageCursor = 1;
    private IBus iBus;

    private static int PAGE_SIZE = 10;
    private static int TIMEOUT = 30000;

    public IBusQueryResultIterator(IngridQuery q, String[] requestedFields, IBus iBus) {
        this.q = q;
        this.requestedFields = requestedFields;
        this.iBus = iBus;
    }

    @Override
    public boolean hasNext() {
        // make sure we have hits
        if (resultBuffer == null) {
            resultBuffer = fetchHits(resultPageCursor);
        }
        if (resultCursor < resultBuffer.length() && resultBuffer.length() > 0) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public IngridHit next() {
        IngridHit result = null;
        // make sure we have hits
        if (resultBuffer == null) {
            resultBuffer = fetchHits(resultPageCursor);
        }
        // check for no more hits
        if (resultBuffer.getHits().length == 0 || resultCursor >= resultBuffer.length()) {
            throw new NoSuchElementException();
        }

        if (resultBufferCursor >= resultBuffer.getHits().length) {
            resultPageCursor++;
            resultBuffer = fetchHits(resultPageCursor);
            resultBufferCursor = 0;
        }
        // check for no more hits
        if (resultBuffer.getHits().length == 0) {
            throw new NoSuchElementException();
        }
        result = resultBuffer.getHits()[resultBufferCursor];
        resultBufferCursor++;
        resultCursor++;

        return result;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    private IngridHits fetchHits(int currentPage) {
        IngridHits result = null;
        try {
            result = iBus.searchAndDetail(q, PAGE_SIZE, currentPage, PAGE_SIZE, TIMEOUT, this.requestedFields);
        } catch (Exception e) {
        }
        return result;
    }

}
