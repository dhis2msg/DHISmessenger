package org.dhis2.messenger.core.rest;

import org.dhis2.messenger.model.InboxModel;
import org.dhis2.messenger.model.InterpretationModel;
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
    private static final String TAG = "RESTSessionStorage";

    private RESTDataChanged callback = null;
    //current session:
    private static RESTSessionStorage currentRestSession = null;
    // a default username, to use for getInstance(), if no instance is active:
    public static String loginUsername;
    // a map of <username, cache>'s :
    private static HashMap<String, RESTSessionStorage> restSessions = new HashMap<>();

    private String username; //this instance's username

    private ProfileModel profile = null;
    private CacheList<InboxModel> inboxModelList = new CacheList<>();
    private CacheList<InterpretationModel> interpretationModelList = new CacheList<>();

    //InboxFragment list of pages(lists of InboxModels)
    //private List<ArrayList<InboxModel>> inboxModelList = new ArrayList<ArrayList<InboxModel>>();
    private int inboxCurrentPage = 1;
    private int inboxPageSize = 10;
    private int inboxTotalPages = 0;

    private boolean newConversation = false; //To indicate that a new conversation was started.
    private boolean newMessage = false; // new message of a conversation: ==> refresh the messeges. && set as not read ?

    //constructors for a singleton-like class:
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

    //------------------------Profile model setters/getters:-----------------------------
    public ProfileModel getProfileModel() {
        return profile;
    }

    public void setProfileModel(ProfileModel profile) {
        this.profile = profile;
    }

    //-----------------Page size, current, total set/get--------------------------------
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
     * Add a page of elements to the cache.
     * It is expected that you only add to the front or back (page = 1 or page = total+1)
     * @param page
     * @param newPageList
     */
    public synchronized void setInboxModelList(int page, List<InboxModel> newPageList) {
        inboxModelList.setListPage(page, newPageList);
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

        //Need to refresh the cache if we started a new conversation.
        if(page == 1 && startedNewConversation()) {
            startedNewConversation(false);
            return new ArrayList<>();
        } else {
            return inboxModelList.getListPage(page);
        }
    }

    //.........................other stuff....

    public void setNameAndIdModelList(List<NameAndIDModel> lst) {

    }

    public List<NameAndIDModel> getNameAndIdModelList() {
        return null;
    }


}

