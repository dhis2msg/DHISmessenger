package org.dhis2.messaging.Utils.XMPP;

import org.dhis2.messaging.Models.IMMessageModel;
import org.jivesoftware.smack.packet.Presence;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by iNick on 14.11.14.
 */
public class RosterModel implements Comparable<RosterModel>{
    String username; //before@
    String JID;
    String nickname;
    String statusMessage;
    String lastActivity;
    Presence.Mode mode;
    boolean online;
    boolean readConversation;
    List<IMMessageModel> conversations;

    public RosterModel(String username, String JID, String nickname, String statusMessage, String lastActivity,
                       Presence.Mode mode, boolean online, boolean readConversation, List<IMMessageModel> conversations){
        this.username = username;
        this.JID = JID;
        this.nickname = nickname;
        this.statusMessage = statusMessage;
        this.lastActivity = lastActivity;
        this.mode = mode;
        this.online = online;
        this.readConversation = readConversation;
        this.conversations = conversations;

    }

    public String getJID() {
        return JID;
    }

    public String getNickname() {
        return nickname;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setMode(Presence.Mode mode) {
        this.mode = mode;
    }

    public boolean isOnline() {
        return online;

    }
    public Presence.Mode getMode() {
        return mode;
    }

    public String getLastActivity() {
        return lastActivity;
    }

    public void setLastActivity(String lastActivity) {
        this.lastActivity = lastActivity;
    }


    public boolean isReadConversation() {
        return readConversation;
    }

    public List<IMMessageModel> getConversations() {
        return conversations;
    }

    public String getUsername() {

        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setJID(String JID) {
        this.JID = JID;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public void setReadConversation(boolean readConversation) {
        this.readConversation = readConversation;
    }

    public void addConversation(IMMessageModel model) {
       if(conversations == null)
           conversations = new ArrayList<IMMessageModel>();
       if(model.JID != null)
           setReadConversation(false);
       conversations.add(model);
    }
    public int compareTo(RosterModel model) {
        if (!online && model.online) {
            return 1;
        }
        if (online && !model.online) {
            return -1;
        }
        return 0;
    }
}
