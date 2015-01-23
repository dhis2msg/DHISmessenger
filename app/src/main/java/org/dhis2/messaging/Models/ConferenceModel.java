package org.dhis2.messaging.Models;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by iNick on 15.11.14.
 */
public class ConferenceModel {

    String id;
    String name;
    String topic;
    String description;
    int occupants;
    List<String> participants;
    boolean admin;
    List<IMMessageModel> messages;

    public void setParticipants(List<String> participants) {
        this.participants = participants;
    }

    public List<String> getParticipants() {

        return participants;
    }

    public ConferenceModel(String id, String name, String topic,String description, int occupants,List<String> participants, boolean admin, List<IMMessageModel> messages) {
        this.id = id;
        this.name =name;
        this.topic = topic;
        this.description = description;
        this.occupants = occupants;
        this.participants = participants;

        this.admin = admin;
        this.messages = messages;
    }

    public String getName() {
        return name;
    }

    public void setMessages(List<IMMessageModel> messages) {

        this.messages = messages;
    }

    public void setName(String name) {

        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setOccupants(int occupants) {
        this.occupants = occupants;
    }

    public String getDescription() {

        return description;
    }

    public int getOccupants() {
        return occupants;
    }

    public String getId() {
        return id;
    }

    public String getTopic() {
        return topic;
    }

    public boolean isAdmin() {
        return admin;
    }

    public List<IMMessageModel> getMessages() {
        return messages;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public void addMessage(IMMessageModel message) {
        if(this.messages == null)
            this.messages = new ArrayList<IMMessageModel>();
        this.messages.add(message);
    }
    public void minusOccupant(){
        occupants = occupants -1;
    }
    public void plussOccupant(){
        occupants = occupants + 1;
    }
}
