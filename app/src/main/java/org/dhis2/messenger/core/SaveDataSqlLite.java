package org.dhis2.messenger.core;

/**
 * Created by iNick on 14.02.15.
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class SaveDataSqlLite {
    public static final String IM_MESSAGE_SENDT_COOUNT = "countim";
    public static final String IM_DISCUSSION_MESSAGE_SENDT_COOUNT = "countdiscussion";
    public static final String DHIS_MESSAGE_SENDT_COUNT = "countdhis";
    public static final String NOTIFICATION_COUNT = "countnotification";
    public static final String ONLINE_COUNT = "countonline";
    static final String TAG = "DatabaseHelper";
    static final String DB_TITEL = "data.db";
    static final String DATA_TABLE = "data";
    static final String ID = "id";//BaseColumns._ID;
    static final String NOTIFICATION_TABLE = "notification";
    static final String NOTIFICATION_ID = "notificationid";
    static final String NOTIFICATION_RECEIVED = "notificationreceived";
    static final String NOTIFICATION_READ = "notificationread";
    static final int DB_VERSION = 8;
    Context context;
    private DatabaseHelper dbHelper;
    private SQLiteDatabase db;

    public SaveDataSqlLite(Context context) {
        this.context = context;
        try {
            dbHelper = new DatabaseHelper(this.context);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public SaveDataSqlLite open() throws SQLException {
        db = dbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        if (dbHelper != null) {
            dbHelper.close();
        }
    }

    public void insertData(ContentValues values) {
        db.insert(DATA_TABLE, null, values);
    }

    public void insertNotification(ContentValues values) {
        db.insert(DATA_TABLE, null, values);
    }

    public boolean removeData(int id) {
        return db.delete(DATA_TABLE, ID + "='" + id + "'", null) > 0;
    }

    public boolean updateAllData(String imMsgSent, String imDiscSent, String dhisSent, String notificationCount, String onlineCount) {
        ContentValues cv = new ContentValues(2);
        cv.put(IM_MESSAGE_SENDT_COOUNT, imMsgSent);
        cv.put(IM_DISCUSSION_MESSAGE_SENDT_COOUNT, imDiscSent);
        cv.put(DHIS_MESSAGE_SENDT_COUNT, dhisSent);
        cv.put(NOTIFICATION_COUNT, notificationCount);
        cv.put(ONLINE_COUNT, onlineCount);
        return db.update(DATA_TABLE, cv, ID + "='" + 1 + "'", null) > 0;
    }

    public boolean updateIMMessageSent() {
        createNewRawIfEmpty();
        Cursor c = getDataRaw();
        c.moveToFirst();
        int count = Integer.parseInt(c.getString(c.getColumnIndex(IM_MESSAGE_SENDT_COOUNT)));
        count = count + 1;
        c.close();
        ContentValues cv = new ContentValues(1);
        cv.put(IM_MESSAGE_SENDT_COOUNT, String.valueOf(count));

        return db.update(DATA_TABLE, cv, ID + " = '" + 1 + "'", null) > 0;
    }

    public boolean updateIMConferenceSent() {
        createNewRawIfEmpty();
        Cursor c = getDataRaw();
        c.moveToFirst();
        int count = Integer.parseInt(c.getString(c.getColumnIndex(IM_DISCUSSION_MESSAGE_SENDT_COOUNT)));
        count = count + 1;
        c.close();
        ContentValues cv = new ContentValues(1);
        cv.put(IM_DISCUSSION_MESSAGE_SENDT_COOUNT, String.valueOf(count));
        return db.update(DATA_TABLE, cv, ID + " = '" + 1 + "'", null) > 0;
    }

    public boolean updateDHISMessageSent() {
        createNewRawIfEmpty();
        Cursor c = getDataRaw();
        c.moveToFirst();
        int count = Integer.parseInt(c.getString(c.getColumnIndex(DHIS_MESSAGE_SENDT_COUNT)));
        count = count + 1;
        c.close();
        ContentValues cv = new ContentValues(1);
        cv.put(DHIS_MESSAGE_SENDT_COUNT, String.valueOf(count));
        return db.update(DATA_TABLE, cv, ID + " = '" + 1 + "'", null) > 0;
    }

    public boolean updateNotificationCount() {
        createNewRawIfEmpty();
        Cursor c = getDataRaw();
        c.moveToFirst();
        int count = Integer.parseInt(c.getString(c.getColumnIndex(NOTIFICATION_COUNT)));
        count = count + 1;
        c.close();
        ContentValues cv = new ContentValues(1);
        cv.put(NOTIFICATION_COUNT, String.valueOf(count));
        return db.update(DATA_TABLE, cv, ID + " = '" + 1 + "'", null) > 0;
    }

    public boolean updateOnline(int seconds) {
        createNewRawIfEmpty();
        Cursor c = getDataRaw();
        c.moveToFirst();
        int count = Integer.parseInt(c.getString(c.getColumnIndex(ONLINE_COUNT)));
        count = count + seconds;
        c.close();
        ContentValues cv = new ContentValues(1);
        cv.put(ONLINE_COUNT, String.valueOf(count));
        return db.update(DATA_TABLE, cv, ID + " = '" + 1 + "'", null) > 0;
    }


    public boolean updateNotifications(String received, String read) {
        /*ContentValues cv = new ContentValues(2);
        cv.put(NAME, name);
        cv.put(LASTNAME, lastname);

        return db.update(TABLE, cv, LASTNAME + "='" + lastname + "' AND " + NAME + "='" + name + "'",null) > 0;*/
        return true;
    }

    public Cursor getDataRaw() {
        createNewRawIfEmpty();
        Cursor c;
        String[] cols = {IM_MESSAGE_SENDT_COOUNT, IM_DISCUSSION_MESSAGE_SENDT_COOUNT, DHIS_MESSAGE_SENDT_COUNT,
                NOTIFICATION_COUNT, ONLINE_COUNT};

        String sqlQuery = "SELECT * FROM " + DATA_TABLE + " WHERE id = '" + 1 + "'";
        c = db.rawQuery(sqlQuery, null);//DATA_TABLE, cols, ID + "='1'" , null, null, null, null);

        return c;
    }

    private void createNewRawIfEmpty() {
        String query = "Select * from " + DATA_TABLE + " where " + ID + " = '" + 1 + "'";
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.getCount() <= 0) {
            ContentValues cv = new ContentValues(2);
            cv.put(ID, 1);
            cv.put(IM_MESSAGE_SENDT_COOUNT, "0");
            cv.put(IM_DISCUSSION_MESSAGE_SENDT_COOUNT, "0");
            cv.put(DHIS_MESSAGE_SENDT_COUNT, "0");
            cv.put(NOTIFICATION_COUNT, "0");
            cv.put(ONLINE_COUNT, "0");
            insertData(cv);
        }
        cursor.close();
    }


    public Cursor getAllNotifications() {
        Cursor c;
        String[] cols = {NOTIFICATION_RECEIVED, NOTIFICATION_READ};
        c = db.query(NOTIFICATION_TABLE, cols, null, null, null, null, NOTIFICATION_RECEIVED);
        return c;
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context context) {
            super(context, DB_TITEL, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            String dataTable = "CREATE TABLE " + DATA_TABLE + " (" + ID + " INTEGER PRIMARY KEY, " +
                    IM_MESSAGE_SENDT_COOUNT + " TEXT, " +
                    IM_DISCUSSION_MESSAGE_SENDT_COOUNT + " TEXT, " +
                    DHIS_MESSAGE_SENDT_COUNT + " TEXT, " +
                    NOTIFICATION_COUNT + " TEXT, " +
                    ONLINE_COUNT + " TEXT); ";
            //NOTIFICATION_TIME_TO_READ_RATIO + "text);";
            String notificationTable = "create table " + NOTIFICATION_TABLE + " (" + NOTIFICATION_ID + " integer primary key, " +
                    NOTIFICATION_RECEIVED + " text, " +
                    NOTIFICATION_READ + " text);";


            db.execSQL(dataTable);

            // db.execSQL(notificationTable);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            if (oldVersion != newVersion)
                db.execSQL("drop table if exists " + DATA_TABLE);
            Log.d(TAG, "updated");
            onCreate(db);
        }
    }
}

