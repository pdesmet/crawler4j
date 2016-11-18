package edu.uci.ics.crawler4j.frontier;

import org.apache.commons.lang3.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sleepycat.je.Cursor;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;
import com.sleepycat.je.Transaction;

import edu.uci.ics.crawler4j.crawler.Configurable;
import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.model.CachedPage;

public class ProcessedPagesDB extends Configurable {
    private static final Logger logger = LoggerFactory.getLogger(ProcessedPagesDB.class);

    private final Database pagesDB;
    private static final String DATABASE_NAME = "processed_pages";
    private final Environment env;

    protected final Object mutex = new Object();

    public ProcessedPagesDB(Environment env, CrawlConfig config) {
        super(config);
        DatabaseConfig dbConfig = new DatabaseConfig();
        dbConfig.setAllowCreate(true);
        dbConfig.setTransactional(true);
        dbConfig.setDeferredWrite(false);
        pagesDB = env.openDatabase(null, DATABASE_NAME, dbConfig);
        this.env = env;
    }

    public CachedPage get(String canonicalUrl) {
        synchronized (mutex) {
            OperationStatus result = null;
            DatabaseEntry key = new DatabaseEntry(canonicalUrl.getBytes());
            DatabaseEntry value = new DatabaseEntry();
            Transaction txn = env.beginTransaction(null, null);

            try {
                result = pagesDB.get(txn, key, value, null);
                txn.commit();

            } catch (Exception e) {
                logger.error("Exception thrown while getting the processed page for" + canonicalUrl,
                             e);
                return null;
            }

            if ((result == OperationStatus.SUCCESS) && (value.getData().length > 0)) {
                return (CachedPage) SerializationUtils.deserialize(value.getData());
            }

            return null;
        }
    }

    public void set(CachedPage page) {
        synchronized (mutex) {
            try {
                if (pagesDB != null) {
                    Transaction txn = env.beginTransaction(null, null);
                    pagesDB.put(txn, new DatabaseEntry(page.getCanonicalUrl().getBytes()),
                                new DatabaseEntry(SerializationUtils.serialize(page)));
                    txn.commit();
                }
            } catch (Exception e) {
                logger.error(
                    "Exception thrown while setting the processed page: " + page.getCanonicalUrl(),
                    e);
            }
        }
    }

    public void delete(CachedPage page) {
        delete(page.getCanonicalUrl());
    }

    public void delete(String canonicalUrl) {
        synchronized (mutex) {
            try {
                if (pagesDB != null) {
                    Transaction txn = env.beginTransaction(null, null);
                    pagesDB.delete(txn, new DatabaseEntry(canonicalUrl.getBytes()));
                    txn.commit();
                }
            } catch (Exception e) {
                logger.error("Exception thrown while deleting the processed page: " + canonicalUrl,
                             e);
            }
        }
    }

    public void loadPagesToFrontierQueue(Frontier frontier) {
        Cursor cursor = null;
        try {
            cursor = pagesDB.openCursor(null, null);

            DatabaseEntry foundKey = new DatabaseEntry();
            DatabaseEntry foundData = new DatabaseEntry();

            while (cursor.getNext(foundKey, foundData, LockMode.DEFAULT) ==
                   OperationStatus.SUCCESS) {

                CachedPage page = SerializationUtils.deserialize(foundData.getData());
                frontier.schedule(page.getWebURL());
            }
        } catch (DatabaseException de) {
            System.err.println("Error accessing database." + de);
        } finally {
            // Cursors must be closed.
            cursor.close();
        }
    }
}
