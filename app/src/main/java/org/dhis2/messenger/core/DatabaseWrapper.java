package org.dhis2.messenger.core;

import android.content.Context;
import android.util.Log;

import com.couchbase.lite.Attachment;
import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.Manager;
import com.couchbase.lite.Revision;
import com.couchbase.lite.UnsavedRevision;
import com.couchbase.lite.android.AndroidContext;
import com.couchbase.lite.replicator.Replication;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * This class was meant to demonstrate usage of the couchbase lite,
 * however since our project needs exactly that kind of wrapper,
 * to simply store/restore objects on start/exit,
 * I have copied & refactored it to suit our purposes.
 *
 * The plan is that RESTSessionStorage & XMPPSessionStorage will use that class on exit/start.
 *
 * Created by iraycd on 13/12/14.
 * Modified/Refactored by Vladislav on 10/12/15.
 * This class was part of the TestCouchDBLite project: https://github.com/iraycd/TestCouchLiteAndroid
 *
 * Github link: https://github.com/iraycd/TestCouchLiteAndroid/blob/master/app/src/main/java/com/iraycd/testcouchdblite/models/DatabaseWrapper.java
 */
public class DatabaseWrapper {

    private Database database;
    private Context context;
    private Manager manager;
    // keep a reference to a running replication to avoid GC
    private Replication replica;


    public DatabaseWrapper(String dbname, Context context) {

        this.context = context;
        /* Manages access to databases */
        try {
            manager = new Manager(new AndroidContext(context), Manager.DEFAULT_OPTIONS);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        // create a name for the database and make sure the name is legal
        // Only the following characters are valid:
        // abcdefghijklmnopqrstuvwxyz0123456789_$()+-/
        if (!Manager.isValidDatabaseName(dbname)) {
            Log.e("DatabaseWrapper", "Invalid database name.");
            return;
        }
        // get existing db with that name
        // or create a new one if it doesn't exist
        try {
            database = manager.getDatabase(dbname);
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
            return;
        }
    }

    /**
     * Release all resources and close all Databases.
     */
    public void close() {
        if (manager != null) {
            manager.close();
        }
    }

    /**
     * A replica can be active/stopped/off-line/idle
     *
     * @param rep
     * @return
     */
    public boolean isReplicaActive(Replication rep) {
        return rep != null && (rep.getStatus() ==
                Replication.ReplicationStatus.REPLICATION_ACTIVE);
    }

    /* Attachments *********************************/

    /**
     * Write an Attachment for a given Document
     *
     * @param docId
     * @param attachName
     * @param mimeType   e.g. "image/jpeg"
     * @param in
     */
    public void writeAttachment(String docId, String attachName, String mimeType, InputStream in) {

        try {
            Document doc = database.getDocument(docId);
            UnsavedRevision newRev = doc.getCurrentRevision().createRevision();
            newRev.setAttachment(attachName, mimeType, in);
            newRev.save();
        } catch (CouchbaseLiteException e) {
           e.printStackTrace();
        }
    }

    /**
     * Get a given Document's attachment if any
     *
     * @param docId
     * @param attachName
     * @return Attachment
     */
    public Attachment getAttachment(String docId, String attachName) {

        Document doc = database.getDocument(docId);
        Revision rev = doc.getCurrentRevision();
        return rev.getAttachment(attachName);
    }

    /**
     * Remove an Attachment from a Document
     *
     * @param docId
     * @param attachName
     */
    public void deleteAttachment(String docId, String attachName) {

        try {
            Document doc = database.getDocument(docId);
            UnsavedRevision newRev = doc.getCurrentRevision().createRevision();
            newRev.removeAttachment(attachName);
            // (You could also update newRev.properties while you're here)
            newRev.save();
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }
    }

     /* CRUD Operations *********************************/
    /**
     * C-rud
     *
     * @param docContent
     * @return docId
     */
    public String create(Map<String, Object> docContent) {

        if (database == null) {
            Log.e("DatabaseWrapper", "database is null : DatabaseWrapper.create(" + ")");
            return "";
        }
        // create an empty document
        Document doc = database.createDocument();
        // add content to document and write the document to the database
        try {
            doc.putProperties(docContent);
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
            return "";
        }
        return doc.getId();
    }

    /**
     * c-R-ud
     *
     * @param docId
     * @return Doc content
     */
    public Map<String, Object> retrieve(String docId) {

        if (database == null) {
            return new HashMap<String, Object>();//empty
        }
        // retrieve the document from the database
        Document doc = database.getDocument(docId);
        // display the retrieved document
        return doc.getProperties();
    }

    /**
     * cr-U-d
     *
     * @param key
     * @param value
     * @param docId
     * @return success or failure
     */
    public boolean update(final String key, final Object value, String docId) {

        if (database == null) {
            Log.e("DatabaseWrapper", "database is null : DatabaseWrapper.update");
            return false;
        }
        // update the document
        try {
            Document doc = database.getDocument(docId);

            // this alternative way is better for handling write conflicts
            doc.update(new Document.DocumentUpdater() {
                @Override
                public boolean update(UnsavedRevision newRevision) {
                    Map<String, Object> properties = newRevision.getUserProperties();
                    properties.put(key, value);
                    newRevision.setUserProperties(properties);
                    return true;
                }
            });

		/*	Map<String, Object> docContent = doc.getProperties();
			//Working on a copy
			Map<String, Object> updatedContent = new HashMap<String, Object>();
			updatedContent.putAll(docContent);
			updatedContent.put(key, value);
			doc.putProperties(updatedContent);*/
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * cru-D
     *
     * @param docId
     * @return
     */
    public boolean delete(String docId) {

        if (database == null) {
            Log.e("DatabaseWrapper", "database is null : DatabaseWrapper.delete(" + docId + ")");
            return false;
        }
        Document doc = null;
        // delete the document
        try {
            doc = database.getDocument(docId);
            doc.delete();
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }
        return doc.isDeleted();
    }
}