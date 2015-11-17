package org.dhis2.messaging.Models;

import org.jivesoftware.smack.packet.Presence;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by iNick on 14.11.14.
 */
public class RosterModel implements Comparable<RosterModel> {
    String username;
    String JID;
    String nickname;
    String statusMessage;
    String lastActivity;
    Presence.Mode mode;
    boolean online;
    boolean readConversation;
    List<IMMessageModel> conversations;

    public RosterModel(String username, String JID, String nickname, String statusMessage, String lastActivity,
                       Presence.Mode mode, boolean online, boolean readConversation, List<IMMessageModel> conversations) {
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

    public void updateUserinfo(RosterModel model) {
        this.username = model.username;
        this.nickname = model.nickname;
        this.statusMessage = model.statusMessage;
        this.lastActivity = model.lastActivity;
        this.mode = model.getMode();
        this.online = model.online;
    }

    public String getJID() {
        return JID;
    }

    public void setJID(String JID) {
        this.JID = JID;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public Presence.Mode getMode() {
        return mode;
    }

    public void setMode(Presence.Mode mode) {
        this.mode = mode;
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

    public void setReadConversation(boolean readConversation) {
        this.readConversation = readConversation;
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

    public void addConversation(IMMessageModel model) {
        if (conversations == null)
            conversations = new ArrayList<IMMessageModel>();
        if (model.JID != null)
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
