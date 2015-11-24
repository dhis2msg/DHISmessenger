package org.dhis2.messaging.REST;

import android.provider.Telephony;

import org.dhis2.messaging.Models.InboxModel;
import org.dhis2.messaging.Models.NameAndIDModel;
import org.dhis2.messaging.Models.ProfileModel;
import org.dhis2.messaging.REST.Interfaces.RESTDataChanged;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by vladislav on 11/20/15.
 * Analogious class to the XmppSessionStorage one.
 * Singleton. Used by the Rest client code to cache data instead of re-downloading it.
 * Thus cutting on amount of bandwidth wasted ?
 */
public class RESTSessionStorage {
    private static RESTSessionStorage restSession = null;
    //TODO: vladislav : compare to XMPPSessionStorage. From this it is obvious that the users of the storage have to implement the interface. Find if this is the only way / the best way to do a callback to inform the client (ui) that the data has changed.
    private RESTDataChanged callback = null;
    private ProfileModel profile = null;
    //InboxFragment list of pages(lists of InboxModels)
    //private List<ArrayList<InboxModel>> inboxModelList = new ArrayList<ArrayList<InboxModel>>();
    private int inboxPageSize = 25;
    private int inboxTotalPages = 0;
    private List<InboxModel> inboxModelList = new ArrayList<>();

    private List<NameAndIDModel> list; //will work on it later...


    private RESTSessionStorage() {}

    /**
     * Gets the singleton instance of this class.
     * If no instance exists it makes a new one.
     * @return RESTSession instance
     */
    public synchronized static RESTSessionStorage getInstance() {
        if (restSession == null) {
            //TODO: vladislav : Read stored values from disk here !
            restSession = new RESTSessionStorage();
        }
        return restSession;
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
        //this.restSession = null;
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


    public void setInboxModelList(int page, List<InboxModel> pageList) {
        //TODO: add code to resolve duplicates at start/end of list, by comparing pageList to first/last
        // These duplicates would be result of N new messages,
        // arriving and the messages shifting position by N with relation to the "pages"
        int index = (page -1)* this.getInboxPageSize();
        /*if (index == 0 ) { //I need to consider this more carefully later ...
            InboxModel first = inboxModelList.get(0);
            if (pageList.contains(first)) {

            }
        } else if (index == inboxModelList.size() && pageList.contains(inboxModelList.get(inboxModelList.size()))) {

        }
        for (int i = 0;  i < pageList.size(); i++) {
            if(inboxModelList.contains(pageList.get(i))) {
                //if inserting to the front insert all 0-i to inboxModeList
                //else (inserting to the end) add all from not containing to end. to inboxModelList
            }
        }*/
        inboxModelList.addAll(index, pageList);
    }

    public void setInboxTotalPages(int pages) {
        this.inboxTotalPages = pages;
    }
    public int getInboxTotalPages() {
        return this.inboxTotalPages;
    }

    public List<InboxModel> getInboxModelList(int page) {
        int index = (page -1)* this.getInboxPageSize();

        if (index > inboxModelList.size() || index + this.getInboxPageSize() > inboxModelList.size()) {
            return null;
        }
        return inboxModelList.subList(index, index  + this.getInboxPageSize());
        //return null;
    }


    //.........................

    public void setNameAndIdModelList(List<NameAndIDModel> lst) {

    }

    public List<NameAndIDModel> getNameAndIdModelList() {
        return null;
    }


}

