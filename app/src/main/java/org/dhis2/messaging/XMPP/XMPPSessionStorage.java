package org.dhis2.messaging.XMPP;

import org.dhis2.messaging.Models.ConferenceModel;
import org.dhis2.messaging.Models.IMMessageModel;
import org.dhis2.messaging.Models.RosterModel;
import org.dhis2.messaging.XMPP.Interfaces.IMUpdateUnreadMessages;
import org.dhis2.messaging.XMPP.Interfaces.XMPPDataChanged;
import org.jivesoftware.smack.packet.Presence;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by iNick on 14.11.14.
 */
public class XMPPSessionStorage {
    public static XMPPSessionStorage xmppSession = null;

    private List<RosterModel> data = null;
    private List<ConferenceModel> conferenceData = null;

    public XMPPDataChanged callback = null;
    public IMUpdateUnreadMessages homeListener = null;

    public String nickname = null;
    public String JID = null;

    public synchronized static XMPPSessionStorage getInstance() {
        if (xmppSession == null) {
            xmppSession = new XMPPSessionStorage();
        }
        return xmppSession;
    }

    /*
     *  GET METHODS
     */
    public List<RosterModel> getXMPPData() {
        if (this.data == null)
            this.data = new ArrayList<RosterModel>();
        return this.data;
    }

    public List<ConferenceModel> getXMPPConferenceData() {
        if (this.conferenceData == null)
            this.conferenceData = new ArrayList<ConferenceModel>();
        return this.conferenceData;
    }

    public String getNickname() {
        return nickname;
    }

    public ConferenceModel getConference(String id) {
        if (getConferencePosition(id) > -1)
            return conferenceData.get(getConferencePosition(id));
        return null;
    }

    public List<IMMessageModel> getConferenceChat(String id) {
        if (getConferencePosition(id) > -1)
            return conferenceData.get(getConferencePosition(id)).getMessages();
        return new ArrayList<IMMessageModel>();
    }

    public int getConferencePosition(String id) {
        if (conferenceData == null)
            return -1;
        for (int i = 0; i < conferenceData.size(); i++) {
            if (conferenceData.get(i).getId().equals(id))
                return i;
        }
        return -1;
    }

    public int getUnreadMessages() {
        int amount = 0;
        if (data != null) {
            for (RosterModel model : data) {
                if (!model.isReadConversation())
                    amount++;
            }
        }
        return amount;
    }

    public RosterModel getRosterModel(String JID) {
        if (JIDExist(JID))
            return data.get(getRosterModelPosition(JID));
        return null;
    }

    public String getUsername(String JID) {
        RosterModel model = getRosterModel(JID);
        if (model != null)
            return model.getUsername();
        return JID;
    }

    public List<IMMessageModel> getConversation(String JID) {
        RosterModel model = data.get(getRosterModelPosition(JID));
        if (model.getConversations() == null)
            return new ArrayList<IMMessageModel>();
        return model.getConversations();
    }

    public int getRosterModelPosition(String JID) {
        if (JIDExist(JID)) {
            for (int i = 0; i < data.size(); i++) {
                if (data.get(i).getJID().equals(JID)) {
                    return i;
                }
            }
        }
        return -1;
    }

    /*
     *  ADD METHODS
     */
    public void addMUCParticipant(String mucId, String nickname) {
        String[] sub = nickname.split("/");
        if (!getConference(mucId).getParticipants().contains(sub[1])) {
            getConference(mucId).getParticipants().add(sub[1]);
            updateOccupants(mucId);
        }
    }

    public void addConference(ConferenceModel model) {
        if (getConferencePosition(model.getId()) < 0)
            getXMPPConferenceData().add(model);
    }

    public void addConferenceList(List<ConferenceModel> list) {
        getXMPPConferenceData().addAll(list);
        callback();
    }

    public void addConferenceMessage(String id, IMMessageModel message) {
        this.conferenceData.get(getConferencePosition(id)).addMessage(message);
        callback();
    }

    public void addRosterList(List<RosterModel> list) {
        for (RosterModel model : list) {
            addRosterModel(model);
        }
        callback();
    }

    public boolean addRosterModel(RosterModel model) {
        if (data == null) {
            data = new ArrayList<RosterModel>();
            data.add(model);
        } else {
            if (JIDExist(model.getJID()))
                data.get(getRosterModelPosition(model.getJID())).updateUserinfo(model);
            else
                data.add(model);
        }
        return true;
    }

    public void addMessage(String JID, IMMessageModel message) {
        if (data == null) {
            getXMPPData();
            List<IMMessageModel> temp = new ArrayList<IMMessageModel>();
            temp.add(message);
            RosterModel model = new RosterModel("", JID, "", "", "", null, false, false, temp);
            addRosterModel(model);
        } else {
            int i = getRosterModelPosition(JID);
            if (data.get(i) != null)
                data.get(i).addConversation(message);
            else {
                List<IMMessageModel> temp = new ArrayList<IMMessageModel>();
                temp.add(message);
                RosterModel model = new RosterModel("", JID, "", "", "", null, false, false, temp);
                addRosterModel(model);
            }
        }
        updateAmountUnreadMessages();
        callback();
    }

    /*
     *  UPDATE METHODS
     */
    public void updateOccupants(String mucid) {
        int amount = getConference(mucid).getParticipants().size();
        getConference(mucid).setOccupants(amount);
    }

    public void updateStatus(String JID, String status) {
        int i = getRosterModelPosition(JID);
        data.get(i).setStatusMessage(status);
        callback();
    }

    public void updateAvailability(String JID, boolean online) {
        int i = getRosterModelPosition(JID);
        data.get(i).setOnline(online);
        callback();
    }

    public void updateMode(String JID, Presence.Mode mode) {
        int i = getRosterModelPosition(JID);
        if (data.get(i) != null)
            data.get(i).setMode(mode);
        callback();
    }

    public void updateReadConversation(String JID, boolean readConversation) {
        int i = getRosterModelPosition(JID);
        data.get(i).setReadConversation(readConversation);
        callback();
        updateAmountUnreadMessages();
    }

    public void updateReadConversationNoCallback(String JID, boolean readConversation) {
        int i = getRosterModelPosition(JID);
        data.get(i).setReadConversation(readConversation);
    }

    public void updateLastActivty(String JID, String lastActivity) {
        int i = getRosterModelPosition(JID);
        data.get(i).setLastActivity(lastActivity);
        callback();
    }

    private void updateAmountUnreadMessages() {
        if (homeListener != null) {
            homeListener.updateIMMessages(getUnreadMessages());
        }
    }

    /*
     *  SET METHODS
     */
    public void setNickname(String name) {
        this.nickname = name;
    }

    public void setConferences(String id, List<IMMessageModel> messages) {
        if (getConference(id) != null) {
            getConference(id).setMessages(messages);
            callback();
        }
    }

    /*
     *  REMOVE METHODS
     */
    public void removeConference(String id) {
        conferenceData.remove(getConferencePosition(id));
        callback();
    }

    public void removeMUCParticipant(String mucId, String nickname) {
        String[] sub = nickname.split("/");
        getConference(mucId).getParticipants().remove(sub[1]);
        updateOccupants(mucId);
    }


    /*
     *  EXISTS METHODS
     */
    public boolean conferenceExist(String name) {
        if (conferenceData == null)
            return false;
        for (int i = 0; i < conferenceData.size(); i++) {
            if (conferenceData.get(i).getName().equals(name))
                return true;
        }
        return false;
    }

    public boolean JIDExist(String JID) {
        if (data != null) {
            for (RosterModel temp : data) {
                if (temp.getJID().equals(JID)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean MUCExist(String JID) {
        if (conferenceData != null) {
            for (ConferenceModel temp : conferenceData) {
                if (temp.getId().equals(JID)) {
                    return true;
                }
            }
        }
        return false;
    }

    /*
    *  CALLBACK AND LISTENER METHODS
    */
    public void changeListener(XMPPDataChanged callback) {
        this.callback = callback;
    }

    public void callback() {
        if (callback != null) {
            callback.notifyChanged();
        }
    }

    public void setHomeListener(IMUpdateUnreadMessages homeListener) {
        this.homeListener = homeListener;
    }

    public void destroy() {
        this.data = null;
        this.conferenceData = null;
        this.callback = null;
        this.homeListener = null;
        this.xmppSession = null;
    }
}
