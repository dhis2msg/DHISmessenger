package org.dhis2.messaging.REST;

import org.dhis2.messaging.Models.NameAndIDModel;
import org.dhis2.messaging.Models.ProfileModel;
import org.dhis2.messaging.REST.Interfaces.RESTDataChanged;

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




}

