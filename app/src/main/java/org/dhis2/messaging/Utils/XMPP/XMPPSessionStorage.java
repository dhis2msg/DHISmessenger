package org.dhis2.messaging.Utils.XMPP;

import org.dhis2.messaging.Models.ConferenceModel;
import org.dhis2.messaging.Models.IMMessageModel;
import org.jivesoftware.smack.packet.Presence;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by iNick on 14.11.14.
 */
public class XMPPSessionStorage {
    private List<RosterModel> data = null;
    private List<ConferenceModel> conferenceData = null;

    public XMPPDataChanged callback = null;
    public IMUpdateUnreadMessages homeListener = null;
    public static XMPPSessionStorage xmppSession = null;

    public synchronized static XMPPSessionStorage getInstance() {
        if (xmppSession == null) {
            xmppSession = new XMPPSessionStorage();
        }
        return xmppSession;
    }
    public List<RosterModel> getXMPPData() {
        if(this.data == null)
            this.data = new ArrayList<RosterModel>();
        return this.data;
    }
    public List<ConferenceModel> getXMPPConferenceData() {
        if(this.conferenceData == null)
            this.conferenceData = new ArrayList<ConferenceModel>();
        return this.conferenceData;
    }


    public void addMUCParticipant(String mucId, String nickname) {
        String[] sub = nickname.split("/");
        if (!getConference(mucId).getParticipants().contains(sub[1])){
            getConference(mucId).getParticipants().add(sub[1]);
            updateOccupants(mucId);
        }
    }
    public void removeMUCParticipant(String mucId, String nickname){
        String[] sub = nickname.split("/");
        getConference(mucId).getParticipants().remove(sub[1]);
        updateOccupants(mucId);
    }

    public void updateOccupants(String mucid){
        int amount = getConference(mucid).getParticipants().size();
        getConference(mucid).setOccupants(amount);
    }

    public void removeConference(String id){
        conferenceData.remove(getConferencePosition(id));
        callback();
    }

    public void addConference(ConferenceModel model){
        if(getConferencePosition(model.getId()) < 0)
            getXMPPConferenceData().add(model);
    }

    public ConferenceModel getConference(String id){
        if(getConferencePosition(id) > -1)
            return conferenceData.get(getConferencePosition(id));
        return null;
    }

    public void addConferenceList(List<ConferenceModel> list){
        getXMPPConferenceData().addAll(list);
        callback();
    }


    public List<IMMessageModel> getConferenceChat(String id){
        if(getConferencePosition(id) > -1)
            return conferenceData.get(getConferencePosition(id)).getMessages();
        return new ArrayList<IMMessageModel>();
    }

    public int getConferencePosition(String id){
        if(conferenceData == null)
            return -1;
        for(int i = 0; i < conferenceData.size(); i++){
            if(conferenceData.get(i).getId().equals(id))
                return i;
        }
        return -1;
    }

    public void addConferenceMessage(String id, IMMessageModel message){
        this.conferenceData.get(getConferencePosition(id)).addMessage(message);
        callback();
    }

    public void updateStatus(String JID, String status){
        int i = getRosterModelPosition(JID);
        data.get(i).setStatusMessage(status);
        callback();
    }
    public void updateAvailability(String JID, boolean online){
        int i = getRosterModelPosition(JID);
        data.get(i).setOnline(online);
        callback();
    }
    public void updateMode(String JID, Presence.Mode mode){
        int i = getRosterModelPosition(JID);
        data.get(i).setMode(mode);
        callback();
    }
    public void updateReadConversation(String JID, boolean readConversation){
        int i = getRosterModelPosition(JID);
        data.get(i).setReadConversation(readConversation);
        callback();
        updateAmountUnreadMessages();
    }

    public void updateReadConversationNoCallback(String JID, boolean readConversation){
        int i = getRosterModelPosition(JID);
        data.get(i).setReadConversation(readConversation);
    }

    public void updateLastActivty(String JID, String lastActivity){
        int i = getRosterModelPosition(JID);
        data.get(i).setLastActivity(lastActivity);
        callback();
    }

    public RosterModel getRosterModel(String JID){
        if(JIDExist(JID))
            return data.get(getRosterModelPosition(JID));
        return null;
    }

    public String getUsername(String JID){
        RosterModel model = getRosterModel(JID);
        if(model != null)
            return model.getUsername();
        return JID;
    }

    public void addRosterList(List<RosterModel> list){
        for(RosterModel model :list){
            addRosterModel(model);
        }
        callback();
    }

    public boolean addRosterModel(RosterModel model){
        if(data == null) {
            data = new ArrayList<RosterModel>();
            data.add(model);
        }
        else{
            if(JIDExist(model.JID))
                return false;
            else
                data.add(model);
        }
        return true;
    }

    public List<IMMessageModel> getConversation(String JID){
        RosterModel model = data.get(getRosterModelPosition(JID));
        if(model.getConversations() == null )
            return new ArrayList<IMMessageModel>();
        return model.getConversations();
    }

    public void addMessage(String JID, IMMessageModel message){
        int i = getRosterModelPosition(JID);
        data.get(i).addConversation(message);

        updateAmountUnreadMessages();
        callback();
    }

    public int getRosterModelPosition(String JID){
        if(JIDExist(JID)){
            for (int i = 0; i < data.size(); i++) {
                if (data.get(i).getJID().equals(JID)) {
                    return i;
                }
            }
        }
        return -1;
    }

    public boolean JIDExist(String JID) {
      if(data != null) {
          for (RosterModel temp : data) {
              if (temp.getJID().equals(JID)) {
                  return true;
              }
          }
      }
      return false;
    }
    public boolean MUCExist(String JID) {
        if(conferenceData != null) {
            for (ConferenceModel temp : conferenceData) {
                if (temp.getId().equals(JID)) {
                    return true;
                }
            }
        }
        return false;
    }

    public int getUnreadMessages(){
        int amount = 0;
        if(data != null) {
            for (RosterModel model : data) {
                if (!model.isReadConversation())
                    amount++;
            }
        }
        return amount;
    }

    public void changeListener(XMPPDataChanged callback){
        this.callback = callback;
    }

    private void callback(){
        if(callback != null)
        {
            callback.notifyChanged();
        }
    }
    public void setHomeListener(IMUpdateUnreadMessages homeListener){
        this.homeListener = homeListener;
    }

    private void updateAmountUnreadMessages(){
        if(homeListener != null) {
            homeListener.updateUnreadMessages(getUnreadMessages());
        }
    }
    public void destroy(){
        this.data = null;
        this.conferenceData = null;
        this.callback = null;
        this.homeListener = null;
        this.xmppSession = null;
    }
}
