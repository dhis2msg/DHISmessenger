package org.dhis2.messaging.Utils.XMPP;

import android.content.Context;
import android.os.AsyncTask;

import org.apache.harmony.javax.security.sasl.SaslException;
import org.dhis2.messaging.Models.ConferenceModel;
import org.dhis2.messaging.Models.IMMessageModel;
import org.dhis2.messaging.Utils.ConvertSeconds;
import org.dhis2.messaging.Utils.SharedPrefs;
import org.jivesoftware.smack.*;
import org.jivesoftware.smack.filter.MessageTypeFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smackx.delay.packet.DelayInformation;
import org.jivesoftware.smackx.iqlast.LastActivityManager;
import org.jivesoftware.smackx.iqlast.packet.LastActivity;
import org.jivesoftware.smackx.muc.Affiliate;
import org.jivesoftware.smackx.muc.DiscussionHistory;
import org.jivesoftware.smackx.muc.HostedRoom;
import org.jivesoftware.smackx.muc.InvitationListener;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.RoomInfo;
import org.jivesoftware.smackx.vcardtemp.packet.VCard;
import org.jivesoftware.smackx.xdata.Form;
import org.jivesoftware.smackx.xdata.FormField;

import java.io.IOException;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class XMPPClient {
    private final int TIMEOUT = 500;

    //Listeners
    private IMRosterListener rosterListener = null;
    private IMPacketListener packetListener = null;
    private MUCPacketListener mucPacketListener = null;
    private MUCParticipantListener mucParticipantListener = null;

    //Instances
    private XMPPConnection connection = null;
    private static XMPPClient instance = null;
    private MultiUserChat muc = null;


    public synchronized static XMPPClient getInstance() {
        if (instance == null) {
            instance = new XMPPClient();
        }
        return instance;
    }

    public XMPPConnection getConnection() {
        return this.connection;
    }

    public boolean checkConnection() {
        if (connection == null)
            return false;

        return connection.isConnected();
    }


    public boolean setConnection(Context context, String HOST, String PORT, String USERNAME, String PASSWORD) {
        ConnectionConfiguration connectionConfig = new ConnectionConfiguration(HOST, Integer.parseInt(PORT), HOST);
        connectionConfig.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);
        connectionConfig.setRosterLoadedAtLogin(true);
        SmackConfiguration.setDefaultPacketReplyTimeout(TIMEOUT);
        connection = new XMPPTCPConnection(connectionConfig);

        try {
            SmackAndroid.init(context);
            connection.connect();
        } catch (XMPPException e) {
            return false;
        } catch (SaslException e) {
            return false;
        } catch (SmackException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            if(XMPPClient.getInstance().login(USERNAME, PASSWORD) ){
                String m = SharedPrefs.getXMPPMode(context);
                String s = SharedPrefs.getXMPPStatus(context);

                if (m.equals("Available")) {
                    XMPPClient.getInstance().setPresenceModeAndStatus(Presence.Type.available, Presence.Mode.available, s);
                } else if (m.equals("Away")) {
                    XMPPClient.getInstance().setPresenceModeAndStatus(Presence.Type.available, Presence.Mode.away, s);
                } else if (m.equals("Free to chat")) {
                    XMPPClient.getInstance().setPresenceModeAndStatus(Presence.Type.available, Presence.Mode.chat, s);
                } else if (m.equals("Do not disturb")) {
                    XMPPClient.getInstance().setPresenceModeAndStatus(Presence.Type.available, Presence.Mode.dnd, s);
                } else if (m.equals("Unavailable")) {
                    XMPPClient.getInstance().setPresenceModeAndStatus(Presence.Type.unavailable, Presence.Mode.dnd, s);
                }
                SharedPrefs.setXMPPData(context, HOST, PORT, HOST, USERNAME, PASSWORD);
                startPacketListener();
                getRosterList();
                getAllMUCs();
                setRosterListener();


            }
            else {
                destroy();
                return false;
            }
        }
        return true;
    }

    public boolean login(String username, String password) {
        try {
            connection.login(username, password);

        } catch (XMPPException e) {
            return false;
        } catch (SaslException e) {
            return false;
        } catch (SmackException e) {
            return false;
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    public void setPresenceModeAndStatus(Presence.Type type, Presence.Mode mode, String status) {
        try {
            Presence presence = new Presence(type);
            presence.setMode(mode);
            presence.setStatus(status);
            connection.sendPacket(presence);
        } catch (SmackException.NotConnectedException e) {
        }
    }

    public void getAllMUCs(){
        try {
            if (!MultiUserChat.getHostedRooms(connection,connection.getServiceName()).isEmpty()) {
                List<ConferenceModel> conferences = new ArrayList<ConferenceModel>();
                for (HostedRoom hr : MultiUserChat.getHostedRooms(connection, connection.getServiceName())) {

                    for (HostedRoom j : MultiUserChat.getHostedRooms(connection, hr.getJid())) {
                        RoomInfo roomInfo = MultiUserChat.getRoomInfo(connection,j.getJid());

                        if (j.getJid().indexOf("@") > 0) {

                            String id = j.getJid();
                            String name = j.getName();
                            String subject = roomInfo.getSubject();
                            int occupants = roomInfo.getOccupantsCount();
                            String description = roomInfo.getDescription();


                            if(XMPPSessionStorage.getInstance().MUCExist(id)) {
                                XMPPSessionStorage.getInstance().getConference(id).setName(name);
                                XMPPSessionStorage.getInstance().getConference(id).setTopic(subject);
                                XMPPSessionStorage.getInstance().getConference(id).setOccupants(occupants);
                                XMPPSessionStorage.getInstance().getConference(id).setDescription(description);
                            }
                            else
                                conferences.add(new ConferenceModel(id,name,subject,description,occupants,new ArrayList<String>(),false,null));

                        }
                    }
                }
                XMPPSessionStorage.getInstance().addConferenceList(conferences);

            }
        } catch (SmackException.NoResponseException e) {
            e.printStackTrace();
        } catch (XMPPException.XMPPErrorException e) {
            e.printStackTrace();
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        }
    }

    public void getRosterList(){
        new AsyncTask<String, String, List<RosterModel>>() {
            @Override
            protected List<RosterModel> doInBackground(String... args) {
                Roster roster = connection.getRoster();
                Collection<RosterEntry> entries = roster.getEntries();
                List<RosterModel> list = new ArrayList<RosterModel>();
                LastActivityManager lastActivityManager = LastActivityManager.getInstanceFor(connection);
                for (RosterEntry entry : entries) {
                    try {
                        LastActivity lastActivity = lastActivityManager.getLastActivity(entry.getUser());

                       System.out.println(entry.getStatus());
                        System.out.println(entry.getType().name());
                        Presence presence = roster.getPresence(entry.getUser());

                        String status = presence.getStatus();//lastActivity.getType().toString();//getStatusMessage();//presence.getStatus();
                        if (status == null) {
                            status = presence.getType().name();
                        }

                        if (status.equals("unavailable") && lastActivity.getIdleTime() < 1)
                            status = "Online";

                        lastActivity.getStatusMessage();
                        list.add(new RosterModel(entry.getName(),entry.getUser(), "", status,
                                new ConvertSeconds().convertToDHM(lastActivity.getIdleTime()),
                                presence.getMode(),lastActivity.getIdleTime() < 1 ? true : false,
                                true, null));
                    } catch (SmackException.NoResponseException e) {
                        e.printStackTrace();
                    } catch (XMPPException.XMPPErrorException e) {
                        e.printStackTrace();
                    } catch (SmackException.NotConnectedException e) {
                        e.printStackTrace();
                    }

                }
                return list;

            }

            @Override
            protected void onPostExecute(List<RosterModel> l) {
                XMPPSessionStorage.getInstance().addRosterList(l);
            }
        }.execute();
    }

    public void destroy() {
        try {
            if (connection != null && connection.isConnected()) {
                removePacketListener();
                removeRosterListener();
                if(muc != null)
                    leaveMUC();
                connection.disconnect();
            }
        } catch (SmackException.NotConnectedException e) {
        }
    }

    public List<RosterGroup> getGroups() {
        Roster roster = connection.getRoster();
        Collection<RosterGroup> entries = roster.getGroups();
        return new ArrayList<RosterGroup>(entries);
    }

    public String getMucNickname(){
        if(muc != null) {
            String[] sub = muc.getNickname().split("/");
            return sub[0];
        }
        return "";
    }

    public void startPacketListener() {
        PacketFilter filter = new MessageTypeFilter(Message.Type.chat);
        packetListener = new IMPacketListener();
       connection.addPacketListener(packetListener,filter);

    }

    public void removePacketListener(){
        connection.removePacketListener(packetListener);
    }

    public void setRosterListener(){
        rosterListener = new IMRosterListener();
        connection.getRoster().addRosterListener(rosterListener);
    }
    public void removeRosterListener(){
        connection.getRoster().removeRosterListener(rosterListener);
    }
    public void leaveMUC(){
        try {

            muc.leave();
            muc = null;
            mucPacketListener = null;
            mucParticipantListener = null;
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        }
    }


    public boolean removeFromConference(String JID){
        try{
            muc.revokeMembership(JID);
            XMPPSessionStorage.getInstance().removeConference(muc.getRoom());

        } catch (XMPPException.XMPPErrorException e) {
            e.printStackTrace();
            return false;
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
            return false;
        } catch (SmackException.NoResponseException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void updateMUC(String topic, String description) {
        try {
            muc.removeMessageListener(mucPacketListener);
            muc.removeParticipantStatusListener(mucParticipantListener);
            if(!muc.getSubject().equals(topic))
                muc.changeSubject(topic);
            Form submitForm = muc.getConfigurationForm().createAnswerForm();
            submitForm.setAnswer("muc#roomconfig_roomdesc", description);
            muc.sendConfigurationForm(submitForm);
        } catch (SmackException.NoResponseException e) {
            e.printStackTrace();
        } catch (XMPPException.XMPPErrorException e) {
            e.printStackTrace();
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        }finally {
            muc.addMessageListener(mucPacketListener);
            muc.addParticipantStatusListener(mucParticipantListener);
        }
    }

    public boolean deleteConference(String id){
        boolean ok = false;
        try{
            muc.removeMessageListener(mucPacketListener);
            muc.removeParticipantStatusListener(mucParticipantListener);
            muc.destroy("","");
            XMPPSessionStorage.getInstance().removeConference(muc.getRoom());
            ok = true;

        } catch (XMPPException.XMPPErrorException e) {
            e.printStackTrace();

        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();

        } catch (SmackException.NoResponseException e) {
            e.printStackTrace();

        }finally {
            muc.addMessageListener(mucPacketListener);
            muc.addParticipantStatusListener(mucParticipantListener);
        }

        return ok;
    }

    public boolean joinMUC(final String id){
        DiscussionHistory dh = new DiscussionHistory();
        if(XMPPSessionStorage.getInstance().getConference(id).getMessages() == null)
            dh.setMaxStanzas(30);
        else
            dh.setMaxStanzas(0);
        this.muc = new MultiUserChat(connection, id);

        try {
            AccountManager am = AccountManager.getInstance(connection);
            String nickname = am.getAccountAttribute("name");
            this.muc.join(nickname,"", dh, Long.valueOf(TIMEOUT));
            //Form form = muc.getConfigurationForm();

            //FormField role = form.getField("muc#role");
            //List<String> s = role.getValues();

             List<String> occupants = muc.getOccupants();
             for(String nick : occupants)
                XMPPSessionStorage.getInstance().addMUCParticipant(id, nick);

        } catch (XMPPException.XMPPErrorException e) {
            e.printStackTrace();
            return false;
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
            return false;
        } catch (SmackException.NoResponseException e) {
            e.printStackTrace();
            return false;
        }

        try{
            Collection<Affiliate> admins = muc.getAdmins();
            XMPPSessionStorage.getInstance().getConference(id).setAdmin(true);
        } catch (XMPPException.XMPPErrorException e) {
            e.printStackTrace();
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        } catch (SmackException.NoResponseException e) {
            e.printStackTrace();
        }


        if(muc.isJoined()) {

            List<IMMessageModel> list = new ArrayList<IMMessageModel>();
            while(true){
                Message message = muc.nextMessage(500);
                if(message == null)
                    break;
                else {
                    if (message.getBody() != null) {
                        DelayInformation inf;
                        try {
                            String username = message.getFrom();
                            String sub[] = username.split("/");
                            String nickname = sub[1];
                            inf = message.getExtension("x","jabber:x:delay");
                            if(inf!=null){
                                list.add(new IMMessageModel(message.getBody(), nickname, new SimpleDateFormat("yyyy.MM.dd HH:mm").format(inf.getStamp()) ) );
                            }
                            else
                                list.add(new IMMessageModel(message.getBody(), nickname, "" ) );

                        } catch (Exception e) {
                        }
                    }
                }
            }
            XMPPSessionStorage.getInstance().getConference(id).setMessages(list);
            mucPacketListener = new MUCPacketListener(id);
            mucParticipantListener = new MUCParticipantListener(id);
            muc.addMessageListener(mucPacketListener);
            muc.addParticipantStatusListener(mucParticipantListener);
        }
        return true;
    }

    public boolean sendMucMessage(String message){
        try {
            muc.sendMessage(message);
        } catch (XMPPException e) {
            e.printStackTrace();
            return false;
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /*public void addInvitationListener(){
        MultiUserChat.addInvitationListener(connection,
                new InvitationListener() {
                    @Override
                    public void invitationReceived(XMPPConnection xmppConnection, String conferenceName, String inviterName, String infoMessage, String s4, Message message) {
                        String hei =  conferenceName + infoMessage;
                    }
                });
    }*/

    public boolean createConference( String name, String subject, String description){

        MultiUserChat muc = new MultiUserChat(connection, name + "@conference." + connection.getServiceName());
        try {
            AccountManager am = AccountManager.getInstance(connection);
            String nickname = am.getAccountAttribute("name");
            String jid = am.getAccountAttribute("jid");
            muc.create(nickname);
            muc.changeSubject(subject);

            muc.grantAdmin(jid);
            muc.grantOwnership(jid);
            //muc.grantAdmin("Administrator");
            //muc.grantOwnership("Administrator");
            Form submitForm = muc.getConfigurationForm().createAnswerForm();
            submitForm.setAnswer("muc#roomconfig_publicroom", true);
            submitForm.setAnswer("muc#roomconfig_persistentroom", true);
            submitForm.setAnswer("muc#roomconfig_roomdesc", description);
            muc.sendConfigurationForm(submitForm);

        } catch (XMPPException.XMPPErrorException e) {
            e.printStackTrace();
            return false;
        } catch (SmackException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }
    /*
    //Customized code system
    //1= Connected to server
    //2= User logged in
    //3= XMPP exception
    //4= Sasl exception
    //5= Smack exception
    //6= Smack not connected exception
    //7= Smack not response exception
    //8= other exception
    public static boolean noErrors(int code) {
        return code == 1 || code == 2;
    }

    //Customized code system
    public static String getResponseMessage( int code) {
        switch (code) {
            case 1:
                return "Connected to server";
            case 2:
                return "User logged in";
            case 3:
                return "XMPP Error";
            case 4:
                return "Simple Authentication and Security Layer Error";
            case 5:
                return "Smack Error";
            case 6:
                return "Not connected..";
            case 7:
                return "No response from server..";
            default:
                return "Something went wrong, try again..";
        }
    }*/
}
