package org.dhis2.messenger.core.rest;

import android.content.SharedPreferences;

import org.dhis2.messenger.gui.activity.LoginActivity;
import org.dhis2.messenger.model.InboxModel;
import org.dhis2.messenger.model.NameAndIDModel;
import org.dhis2.messenger.model.ProfileModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by vladislav on 11/20/15.
 * Analogious class to the XmppSessionStorage one.
 * Singleton. Used by the Rest client code to cache data instead of re-downloading it.
 * Thus cutting on amount of bandwidth wasted ?
 */
public class RESTSessionStorage {
    // a default username, to use for getInstance(), if no instance is active.
    public static String loginUsername;
    // a map of <username, cache>'s :
    private static HashMap<String, RESTSessionStorage> restSessions = new HashMap<>();
    private static RESTSessionStorage currentRestSession = null;
    private String username;

    private RESTDataChanged callback = null;
    private ProfileModel profile = null;
    //InboxFragment list of pages(lists of InboxModels)
    //private List<ArrayList<InboxModel>> inboxModelList = new ArrayList<ArrayList<InboxModel>>();
    private int inboxCurrentPage = 1;
    private int inboxPageSize = 10;
    private int inboxTotalPages = 0;
    private List<InboxModel> inboxModelList = new ArrayList<>();

    private List<NameAndIDModel> list; //will work on it later...

    private static final String TAG = "RESTSessionStorage";

    private RESTSessionStorage() {}
    private RESTSessionStorage(String username) {
        this.username = username;
    }

    /**
     * A method to specify which user Session will be used.
     * Separate users use separate sessions, because they require separate caches.
     *
     * @param username The name of the currently active user.
     */
    public synchronized static RESTSessionStorage setActiveSession(String username) {
        if (currentRestSession != null &&
                currentRestSession.username != null &&
                currentRestSession.username.equals(username)) {
            return currentRestSession;
        } else {//no session selected/other selected
            //See if it exists:
            RESTSessionStorage session = restSessions.get(username);
            if (session != null) {
                currentRestSession = session;
            } else { //doesn't exist: create new !
                session = new RESTSessionStorage(username);
                restSessions.put(username, session);
                currentRestSession = session;
            }
            return  currentRestSession;
        }
    }

    /**
     * Gets the singleton instance of this class.
     * If no instance exists it returns/creates if it doesn't exist the default one.
     *
     * @return RESTSession instance
     */
    public synchronized static RESTSessionStorage getInstance() {
        if ( currentRestSession == null) {

            //TODO: vladislav : Read stored values from disk here !
            RESTSessionStorage session = restSessions.get(loginUsername);
            if (session != null) {
                currentRestSession = session;
            } else { //doesn't exist: create new !
                session = new RESTSessionStorage( loginUsername);
                restSessions.put(loginUsername, session);
                currentRestSession = session;
            }
            return  currentRestSession;
        }
        return currentRestSession;
    }

    public void setCallback(RESTDataChanged callback) {
        this.callback = callback;
    }

    /*public void setHomeListener(UpdateUnreadMsg homeListener) {
        this.homeListener = homeListener;
    }*/

    /**
     * Destroy the XmppStorage instance.
     */
    public void destroy() {
        //TODO: vladislav: store vars to disk before exiting from here !
        //this.currentRestSession = null;
    }

    //__________________________Setters & getters_________________________________________
    //Lists of:
    // NameAndIDModel members (from RESTChatActivity).
    //NameAndIDModel oruUits (from newMessageActivity)
    //NameAndIDModel users
    // ProflieModel profile (not a list)


    public ProfileModel getProfileModel() {
        return profile;
    }

    public void setProfileModel(ProfileModel profile) {
        this.profile = profile;
    }

    public void setInboxPageSize(int size) {
        this.inboxPageSize = size;
    }

    public int getInboxPageSize() {
        return this.inboxPageSize;
    }

    public void setInboxCurrentPage(int p) {
        this.inboxCurrentPage = p;
    }

    public int getInboxCurrentPage() {
        return this.inboxCurrentPage;
    }

    public void setInboxTotalPages(int pages) {
        this.inboxTotalPages = pages;
    }
    public int getInboxTotalPages() {
        return this.inboxTotalPages;
    }

    /**
     * Add a page of elements to the cache.
     * It is expected that you only add to the front or back (page = 1 or page = total+1)
     * @param page
     * @param newPageList
     */
    public synchronized void setInboxModelList(int page, List<InboxModel> newPageList) {
        int index = (page - 1) * inboxPageSize;
        //TODO: Vladislav: if entries already exist and read variable is different modify the cached one!
        //Note: detecting overlap on both sides is necessary,
        // because the pages move with relation to the first item chronologically.
        //Thus what might have been the last page in cache might become the last partial page in cache...etc

        if(inboxModelList.isEmpty()) {
            inboxModelList.addAll(index, newPageList);
        } else if (page == 1) { //insert at the front:
            int overlapIx = newPageList.indexOf(inboxModelList.get(0));
            if (overlapIx > -1) {
                inboxModelList.addAll(newPageList.subList(0, overlapIx));
            } else {
                inboxModelList.addAll(index, newPageList);
            }
        } else {
            //new page at the end
            if (page > inboxTotalPages) {
                inboxTotalPages = page;
            }
            int last = page * inboxPageSize;
            if (last > inboxModelList.size()) {
                last = inboxModelList.size();
            }
            int overlapIx = newPageList.indexOf(inboxModelList.get(last - 1));
            if (overlapIx > -1) { // there is overlap:
                    inboxModelList.addAll(newPageList.subList(overlapIx + 1, newPageList.size()));
            } else {
                    inboxModelList.addAll(index, newPageList);
            }
        }
        //Log.v(TAG, "(INSERT) page = " + page + " index=" + index + " inboxModelList.size() = " + inboxModelList.size());
    }

    /**
     * Returns a list of conversations on the page.
     * If the page is not in cache (empty) the returned list will be empty.
     * If the page is not of page size only the available elements are returned.
     *
     * @param page
     * @return list of conversations
     */
    public synchronized List<InboxModel> getInboxModelList(int page) {
        int index = (page - 1) * inboxPageSize;
        // if cache is empty return empty list.
        if (index < 0 || inboxTotalPages == 0 || page > inboxTotalPages) {
            return inboxModelList.subList(0, 0); // ie empty list
        }

        // For partial end pages:
        if (index + inboxPageSize > inboxModelList.size()) {
            //Log.v(TAG, "page = " + page + " index=" + index + " index + inboxPageSize = " + (index + inboxPageSize) + " inboxModelList.size() = " + inboxModelList.size());
            if (index > inboxModelList.size()) {
                return inboxModelList.subList(0,0);
            }
            List<InboxModel> toReturn = inboxModelList.subList(index, inboxModelList.size());
            //return inboxModelList.subList(index, inboxModelList.size());
            return toReturn;
        } else {// full page:
            return inboxModelList.subList(index, index + inboxPageSize);
        }
    }

    // For the RESTGetConversation: functions to get inboxModel by index:

    /**
     * Returns the inbox model at that index.
     * @param index 0 < index < size or you get an exception !
     * @return the model
     * Throws indexOutOfBounds if index is out of bounds
     */
    public InboxModel getInboxModel(int index) {
        return inboxModelList.get(index);
    }


    //.........................

    public void setNameAndIdModelList(List<NameAndIDModel> lst) {

    }

    public List<NameAndIDModel> getNameAndIdModelList() {
        return null;
    }


}

