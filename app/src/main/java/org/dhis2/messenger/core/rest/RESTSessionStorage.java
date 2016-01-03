package org.dhis2.messenger.core.rest;

import android.content.Context;

import org.dhis2.messenger.core.DiskStorage;
import org.dhis2.messenger.model.InboxModel;
import org.dhis2.messenger.model.InterpretationModel;
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
    private static final String TAG = "RESTSessionStorage";

    private RESTDataChanged callback = null;
    //current session:
    private static RESTSessionStorage currentRestSession = null;
    // a default username, to use for getInstance(), if no instance is active:
    public static String loginUsername;
    // a map of <username, cache>'s :
    private static HashMap<String, RESTSessionStorage> restSessions = new HashMap<>();

    private String username; //this instance's username

    private int nrUnreadLastCall = 0; //gets reset after commit to settings

    private ProfileModel profile = null;
    private CacheList<InboxModel> inboxModelList = new CacheList<>();

    private boolean newConversation = false; //To indicate that a new conversation was started.
    private boolean newMessage = false; // new message of a conversation: ==> refresh the messeges. && set as not read ?

    private CacheList<InterpretationModel> interpretationModelList = new CacheList<>();
    //private int interpretationsCurrentPage = 1;
    //private int interpretationsPageSize = 5;
    //private int interpretationsTotalPages = 0;

    private static Context appContext;

    //constructors for a singleton-like class:
    private RESTSessionStorage() {}
    private RESTSessionStorage(String username) {
        this.username = username;
    }

    /**
     * A method to specify which user Session will be used.
     * Separate users use separate sessions, because they require separate caches.
     *
     * @param context
     * @param username The name of the currently active user.
     */
    public synchronized static RESTSessionStorage setActiveSession(Context context, String username) {
        appContext = context;
        //DiskStorage.test(context);
        DiskStorage.setContext(context);

        if (currentRestSession != null &&
                currentRestSession.username != null &&
                currentRestSession.username.equals(username)) {
            return currentRestSession;
        } else {//no session selected/other selected
            /* pre-storage (NoSql) code using the hash map of sessions:
            //See if it exists:
            RESTSessionStorage session = restSessions.get(username);
            if (session != null) {
                currentRestSession = session;
            } else { //doesn't exist: create new !
                session = new RESTSessionStorage(username);
                restSessions.put(username, session);
                currentRestSession = session;
            }
            */
            //post-storage (NoSql) code, using DiskStorage to get instance or creates new one.
            RESTSessionStorage session = DiskStorage.getInstance().retrieveRESTSession(username);
            if (session != null) {
                currentRestSession = session;
            } else { //doesn't exist: create new !
                session = new RESTSessionStorage(username);
                currentRestSession = session;
                DiskStorage.getInstance().saveRESTSession(username, currentRestSession);
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
        /*  pre-disk storage:
        if ( currentRestSession == null) {
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
       }*/

        //post disk storage:
        if (currentRestSession != null) {
            return currentRestSession;
        } else if (loginUsername != null && appContext != null){
            return setActiveSession(appContext, loginUsername);

        }
        //if all else fails null (?) [probably won't happen...]
        return currentRestSession;
    }

    /*public void setCallback(RESTDataChanged callback) {
        this.callback = callback;
    }

    public void setHomeListener(UpdateUnreadMsg homeListener) {
        this.homeListener = homeListener;
    }*/

    /**
     * Destroy the RESTStorage instance.
     */
    public void destroy() {
        //this.currentRestSession = null;
        DiskStorage.getInstance().saveRESTSession(username, currentRestSession);
    }

    //----------------------Unread messages calls-------------------------------------

    public int getNrUnreadLastCall() {
        //Log.d("RESTSessionStorage", "GET-Number of new unread: " + nrUnreadLastCall);
        return nrUnreadLastCall;
    }

    //------------------------Profile model setters/getters:-----------------------------
    public ProfileModel getProfileModel() {
        return profile;
    }

    public void setProfileModel(ProfileModel profile) {
        this.profile = profile;
    }

    //-----------------Page size, current, total set/get--------------------------------
    public void setInboxPageSize(int size) {
        inboxModelList.setPageSize(size);
    }

    public int getInboxPageSize() {
        return inboxModelList.getPageSize();
    }

    public void setInboxCurrentPage(int p) {
        inboxModelList.setCurrentPage(p);
    }

    public int getInboxCurrentPage() {
        return inboxModelList.getCurrentPage();
    }

    public void setInboxTotalPages(int pages) {
        inboxModelList.setTotalPages(pages);
    }
    public int getInboxTotalPages() {
       return inboxModelList.getTotalPages();
    }

    public int getInterpretationsCurrentPage() {
        return interpretationModelList.getCurrentPage();
    }
    public void setInterpretationsCurrentPage(int page) {
        interpretationModelList.setCurrentPage(page);
    }

    public int getInterpretationsPageSize() {
        return interpretationModelList.getPageSize();
    }

    public void setInterpretationsPageSize(int pageSize) {
        interpretationModelList.setPageSize(pageSize);
    }

    public int getInterpretationsTotalPages() {
        return interpretationModelList.getTotalPages();
    }
    public void setInterpretationsTotalPages(int pages) {
        interpretationModelList.setTotalPages(pages);
    }
    //--------------NewConversation/SentNewMessage setters/getters: ------------------------

    public void startedNewConversation(boolean newConversation) {
        this.newConversation = newConversation;
    }

    public boolean startedNewConversation() {
        return newConversation;
    }

    public void sentNewMessage(boolean newMessage) {
        this.newMessage = newMessage;
    }

    public boolean sentNewMessage() {
        return newMessage;
    }

    //------------InboxModel------------get index------remove at index-----------
    /**
     * Returns the inbox model at that index.
     * @param index 0 < index < size or you get an exception !
     * @return the model
     * Throws indexOutOfBounds if index is out of bounds
     */
    public InboxModel getInboxModel(int index) {
        return inboxModelList.getElement(index);
    }

    /**
     * Removes the InboxModel from the cached ones. (conversations).
     * @param index
     */
    public void removeInboxModel(int index) {
        inboxModelList.removeElement(index);
    }

    //---------InboxModel----------set page / get page ---------------------------------------

    /**
     * Called upon a refresh (skipCache == true).
     * It saves the old cache & merges it with the new one.
     */
    public synchronized void refreshInboxModelList() {
        inboxModelList.refresh();
    }
    /**
     * Add a page of elements to the cache.
     * It is expected that you only add to the front or back (page = 1 or page = total+1)
     * @param page
     * @param newPageList
     * @return the number of new entries added to the list.
     */
    public synchronized int setInboxModelList(int page, List<InboxModel> newPageList) {
        nrUnreadLastCall = inboxModelList.setListPage(page, newPageList);
        return nrUnreadLastCall;
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
        int index = (page - 1) * inboxModelList.getPageSize();

        //Need to refresh the cache if we started a new conversation.
        if(page == 1 && startedNewConversation()) {
            startedNewConversation(false);
            return new ArrayList<>();
        } else {
            return inboxModelList.getListPage(page);
        }
    }

    //----------- the following are what the wrappers for the interpretations should roughly look like:
    public InterpretationModel getInterpretationModel(int index) {
        return interpretationModelList.getElement(index);
    }

    public void removeInterpretationModel(int index) {
        interpretationModelList.removeElement(index);
    }

    public synchronized List<InterpretationModel> getInterpretationModelList(int page) {
        return interpretationModelList.getListPage(page);
    }


    public synchronized void setInterpretatoinModelList( int page, List<InterpretationModel> list) {
        interpretationModelList.setListPage(page, list);
    }



}

