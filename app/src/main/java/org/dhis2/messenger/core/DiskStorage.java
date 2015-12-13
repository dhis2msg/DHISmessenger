package org.dhis2.messenger.core;

import android.content.Context;
import android.util.Log;

import com.couchbase.lite.Database;
import com.couchbase.lite.Manager;
import com.couchbase.lite.NetworkReachabilityManager;
import com.couchbase.lite.android.AndroidContext;
import com.couchbase.lite.storage.SQLiteStorageEngineFactory;
import com.google.gson.Gson;

import org.dhis2.messenger.core.rest.RESTSessionStorage;
import org.dhis2.messenger.core.xmpp.XMPPSessionStorage;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Vladislav on 12/10/15.
 * A wrapper class for couchbase to write the caches to disk.
 * This class is roughly based on the Couchbase example available here:https://github.com/iraycd/TestCouchLiteAndroid/blob/master/app/src/main/java/com/iraycd/testcouchdblite/models/DatabaseWrapper.java
 *
 */
public class DiskStorage {
    public static DiskStorage instance;
    static Manager manager;
    static Database database;
    private DatabaseWrapper currentDatabase;
   /// private String currentUsername;
    private static Context c;



    public static DiskStorage getInstance() {
        if(instance == null) {
            instance = new DiskStorage();
        }
        return instance;
    }

    /*public static DiskStorage getInstance(String username) {
        if (instance == null) {
            instance = new DiskStorage();
            //switched user:
        } else if (!instance.currentUsername.equals(username)) {
            instance.destroy();

        }
        return instance;
    }*/

    /**
     * Must be called before getInstance().
     * @param context
     */
    public static void setContext(Context context) {
        c = context;
    }

    private DiskStorage() {
        if (c == null) {
            Log.e("DiskStorage constructor", "Context was never set! Please call setContext, before trying to create new objects !");
        }
        currentDatabase = new DatabaseWrapper("dhis2messangerstorage", c);
    }

    public void destroy() {
        currentDatabase.close();
    }

    public static void test(Context context) {

        try {
            //test stuff out.
            manager = new Manager(new AndroidContext(context), Manager.DEFAULT_OPTIONS);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Saves a RESTSessionStorage object to the disk.
     * It overwrites any previous saved object with that username.
     * @param username
     * @param session the object to save.
     */
    public void saveRESTSession(String username, RESTSessionStorage session) {

        Map<String, Object> m = new HashMap<String, Object>();
        Gson gson = new Gson();
        String serialized = gson.toJson(session);
        m.put(username, serialized);
        Log.v("saveRestSession:", "" + serialized);
        currentDatabase.delete(username + "rest");
        currentDatabase.create(m ,username + "rest");
    }

    /**
     * Retrieves a RESTSessionStorage object from the disk. (Or null)
     * @param username
     * @return null if not found.
     */
    public RESTSessionStorage retrieveRESTSession(String username) {
        Map<String, Object> m = currentDatabase.retrieve(username + "rest");
        String sessionJson = (String) m.get(username);
        Log.v("retrieveRESTSession", "" + sessionJson);
        Gson gson = new Gson();
        RESTSessionStorage session = gson.fromJson(sessionJson, RESTSessionStorage.class);
        return session;
    }

    /**
     * Saves a XMPPSessionStorage object to the disk.
     * @param username
     * @param session the object to save.
     */
    public void saveXMPPSession(String username, XMPPSessionStorage session) {

    }

    /**
     * Retrieves a XMPPSessionStorage object from the disk.
     * @param username
     * @return null if not found.
     */
    public XMPPSessionStorage getXMPPSession(String username) {
        return null;
    }


}
