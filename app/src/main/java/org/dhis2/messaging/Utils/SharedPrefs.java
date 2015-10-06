package org.dhis2.messaging.Utils;

import android.content.Context;
import android.content.SharedPreferences.Editor;

public class SharedPrefs {
    //App data
    private static final String APP_DATA = "APP_DATA";
    private static final String LOGGED_IN = "loggedIn";

    //User data
    private static final String USER_DATA = "USER_DATA";
    private static final String CREDENTIALS = "credentials";
    private static final String USER_NAME = "userName";
    private static final String USER_ID = "userId";
    private static final String URL = "url";

    //GCM
    private static final String GCM_DATA = "GCM_DATA";
    private static final String GCMID = "gcmid";

    //XMPP data - temporary
    private static final String XMPP_DATA = "XMPP_DATA";
    private static final String HOST = "host";
    private static final String PORT = "port";
    private static final String SERVER = "server";
    private static final String XMPP_USER_ID = "xmppUserId";
    private static final String XMPP_PASSWORD = "xmppPassword";

    private static final String XMPP_USER_DATA = "XMPP_USER_DATA";
    private static final String MODE = "mode";
    private static final String STATUS = "status";

    //Globals
    private static final String UNREAD_MESSAGES = "unreadMessages";

    private SharedPrefs() {
    }

    public static void setSessionData(Context context, String creds, String username, String userid, String url) {
        Editor userData = context.getSharedPreferences(USER_DATA, Context.MODE_PRIVATE).edit();
        userData.putString(CREDENTIALS, creds);
        userData.putString(USER_NAME, username);
        userData.putString(USER_ID, userid);
        userData.putString(URL, url);

        userData.commit();

        Editor appData = context.getSharedPreferences(APP_DATA, Context.MODE_PRIVATE).edit();
        appData.putBoolean(LOGGED_IN, true);
        appData.commit();
    }

    public static void setGCMData(Context context, String id) {
        Editor userData = context.getSharedPreferences(GCM_DATA, Context.MODE_PRIVATE).edit();
        userData.putString(GCMID, id);
        userData.commit();
    }

    public static void setXMPPData(Context context, String host, String port, String server, String username, String password) {
        Editor userData = context.getSharedPreferences(XMPP_DATA, Context.MODE_PRIVATE).edit();
        userData.putString(HOST, host);
        userData.putString(PORT, port);
        userData.putString(SERVER, server);
        userData.putString(XMPP_USER_ID, username);
        userData.putString(XMPP_PASSWORD, password);
        userData.commit();
    }

    public static void setXMPPUserData(Context context, String mode, String status) {
        Editor userData = context.getSharedPreferences(XMPP_USER_DATA, Context.MODE_PRIVATE).edit();
        userData.putString(MODE, mode);
        userData.putString(STATUS, status);
        userData.commit();
    }

    public static void setUnreadMessages(Context context, String unreadMessages) {
        Editor userData = context.getSharedPreferences(UNREAD_MESSAGES, Context.MODE_PRIVATE).edit();
        userData.putString(UNREAD_MESSAGES, unreadMessages);
        userData.commit();
    }

    public static boolean isUserLoggedIn(Context context) {
        return context.getSharedPreferences(APP_DATA, Context.MODE_PRIVATE).getBoolean(LOGGED_IN, false);
    }

    public static String getCredentials(Context context) {
        return context.getSharedPreferences(USER_DATA, Context.MODE_PRIVATE).getString(CREDENTIALS, null);
    }

    public static String getServerURL(Context context) {
        return context.getSharedPreferences(USER_DATA, Context.MODE_PRIVATE).getString(URL, null);
    }

    public static String getGCMRegistrationId(Context context) {
        return context.getSharedPreferences(GCM_DATA, Context.MODE_PRIVATE).getString(GCMID, "");
    }

    public static String getUserName(Context context) {
        return context.getSharedPreferences(USER_DATA, Context.MODE_PRIVATE).getString(USER_NAME, null);
    }

    public static String getUserId(Context context) {
        return context.getSharedPreferences(USER_DATA, Context.MODE_PRIVATE).getString(USER_ID, null);
    }

    public static String getXMPPHost(Context context) {
        return context.getSharedPreferences(XMPP_DATA, Context.MODE_PRIVATE).getString(HOST, null);
    }

    public static String getXMPPUsername(Context context) {
        return context.getSharedPreferences(XMPP_DATA, Context.MODE_PRIVATE).getString(XMPP_USER_ID, null);
    }

    public static String getXMPPPassword(Context context) {
        return context.getSharedPreferences(XMPP_DATA, Context.MODE_PRIVATE).getString(XMPP_PASSWORD, null);
    }

    public static String getXMPPMode(Context context) {
        return context.getSharedPreferences(XMPP_USER_DATA, Context.MODE_PRIVATE).getString(MODE, "Available");
    }

    public static String getXMPPStatus(Context context) {
        return context.getSharedPreferences(XMPP_USER_DATA, Context.MODE_PRIVATE).getString(STATUS, "");
    }

    public static String getUnreadMessages(Context context) {
        String amount = context.getSharedPreferences(UNREAD_MESSAGES, Context.MODE_PRIVATE).getString(UNREAD_MESSAGES, null);
        return amount == null ? "0" : amount;
    }

    public static void eraseGCM(Context context) {
        context.getSharedPreferences(GCM_DATA, Context.MODE_APPEND).edit().clear().commit();
    }

    public static void eraseData(Context context) {
        context.getSharedPreferences(USER_DATA, Context.MODE_PRIVATE).edit().clear().commit();
        context.getSharedPreferences(APP_DATA, Context.MODE_PRIVATE).edit().clear().commit();
        context.getSharedPreferences(XMPP_DATA, Context.MODE_PRIVATE).edit().clear().commit();
        context.getSharedPreferences(XMPP_USER_DATA, Context.MODE_PRIVATE).edit().clear().commit();
        context.getSharedPreferences(UNREAD_MESSAGES, Context.MODE_PRIVATE).edit().clear().commit();
        context.getSharedPreferences(GCM_DATA, Context.MODE_APPEND).edit().clear().commit();

    }
}