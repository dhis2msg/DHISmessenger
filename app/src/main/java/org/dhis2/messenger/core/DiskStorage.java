package org.dhis2.messenger.core;

import com.couchbase.lite.Database;
import com.couchbase.lite.Manager;

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

    public static DiskStorage getInstance() {
        if(instance == null) {
            instance = new DiskStorage();
        }
        return instance;
    }

    public static void test() {
        //test stuff out.
    }

}
