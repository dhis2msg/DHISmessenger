package org.dhis2.messaging.REST;

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

    public void setInboxModelList(List<InboxModel> pageList, int page) {
        //this.inboxModelList.add(page, pageList);
        // TODO: double check if pages count from 0 or 1, if from 1, then use page -1 instead
        // such that storing the first page would result in index 0, sliding all the other elements back.
        int index = page * inboxPageSize;
        this.inboxModelList.addAll(index, pageList);
        //Duplicate entries ? ...hmm this needs more thought...
        // maybe such:
        // if pageList is to be added to the front:
        // examine if it contains the first element of the cache. (iteration over 25 elements ?)
        // then only add the new entries. (newer)
        // if to the back:
        // examine if it contains the last element of the list. (iteration over 25 elements ?)
        // then only add new entries (older).
        //I will come back to this after trying the new UI that Hans has pushed to the repo.
    }

    public List<InboxModel> getInboxModelList(int page) {
        //TODO: refactor to calculate paage to index interval.
        //return this.inboxModelList.get(page);
        return null;
    }



    public void setNameAndIdModelList(List<NameAndIDModel> lst) {

    }

    public List<NameAndIDModel> getNameAndIdModelList() {
        return null;
    }


}

