package org.dhis2.messaging.XMPP;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import org.apache.harmony.javax.security.sasl.SaslException;
import org.dhis2.messaging.Activities.HomeActivity;
import org.dhis2.messaging.Models.ConferenceModel;
import org.dhis2.messaging.Models.IMMessageModel;
import org.dhis2.messaging.Models.RosterModel;
import org.dhis2.messaging.Testing.DataCaptureOnline;
import org.dhis2.messaging.Testing.SaveDataSqlLite;
import org.dhis2.messaging.Utils.ConvertSeconds;
import org.dhis2.messaging.Utils.SharedPrefs;
import org.dhis2.messaging.Utils.UserInterface.ToastMaster;
import org.dhis2.messaging.XMPP.Listeners.IMPacketListener;
import org.dhis2.messaging.XMPP.Listeners.IMRosterListener;
import org.dhis2.messaging.XMPP.Listeners.MUCPacketListener;
import org.dhis2.messaging.XMPP.Listeners.MUCParticipantListener;
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
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.RoomInfo;
import org.jivesoftware.smackx.xdata.Form;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class XMPPClient {
    private final int TIMEOUT = 3000;
    private final int CONFERENCE_HISTORY_MESSAGES = 35;

    //Customized code system
    private final static int CONNECTED_TO_SERVER = 0;
    private final static int USER_LOGGED_IN = 1;
    private final static int SUCCESS = 2;
    private final static int MESSAGE_SENT = 3;
    private final static int CONFERENCE_CREATED = 4;
    private final static int XMPP_EXCEPTION = 15;
    private final static int SASL_EXCEPTION = 13;
    private final static int SMACK_NOT_CONNECTED_EXCEPTION = 5;
    private final static int SMACK_NO_RESPONSE_EXCEPTION = 6;
    private final static int SMACK_NOT_LOGGED_IN_EXCEPTION = 7;
    private final static int SMACK_EXCEPTION = 8;
    private final static int IO_EXCEPTION = 9;
    private final static int SOCKET_TIMEOUT_EXCEPTION = 10;
    private final static int OTHER_EXCEPTION = 14;
    private final static int UNABLE_TO_JOIN_CONFERENCE = 12;

    //Listeners
    private IMRosterListener rosterListener = null;
    private IMPacketListener packetListener = null;
    private MUCPacketListener mucPacketListener = null;
    private MUCParticipantListener mucParticipantListener = null;

    //Instances
    private XMPPConnection connection = null;
    private static XMPPClient instance = null;
    private MultiUserChat muc = null;
    private DataCaptureOnline data;

    //Tasks
    private AsyncTask asyncTask;

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

    public int setConnection(Context context, String HOST, String PORT, String USERNAME, String PASSWORD) {
        //Midlertidig
        HOST = "172.169.1.64";
        //  PORT = "5222";
        USERNAME = "admin";
        PASSWORD = "admin";

        ConnectionConfiguration connectionConfig = new ConnectionConfiguration(HOST, Integer.parseInt(PORT), HOST);
        connectionConfig.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);
        connectionConfig.setRosterLoadedAtLogin(true);
        SmackConfiguration.setDefaultPacketReplyTimeout(TIMEOUT);
        SharedPrefs.setXMPPData(context, HOST, PORT, HOST, USERNAME, PASSWORD);
        connection = new XMPPTCPConnection(connectionConfig);

        try {
            SmackAndroid.init(context);
            connection.connect();

        } catch (SocketTimeoutException e) {
            return SOCKET_TIMEOUT_EXCEPTION;
        } catch (XMPPException e) {
            return XMPP_EXCEPTION;
        } catch (SaslException e) {
            return SASL_EXCEPTION;
        } catch (SmackException.NotConnectedException e) {
            return SMACK_NO_RESPONSE_EXCEPTION;
        } catch (SmackException.ConnectionException e) {
            return SMACK_EXCEPTION;
        } catch (SmackException e) {
            return SMACK_NO_RESPONSE_EXCEPTION;
        } catch (IOException e) {
            return IO_EXCEPTION;
        } catch (Exception e) {
            return OTHER_EXCEPTION;
        } finally {
            int loggInCode = XMPPClient.getInstance().login(USERNAME, PASSWORD);
            if (noErrors(loggInCode)) {
                data = new DataCaptureOnline();

                String m = SharedPrefs.getXMPPMode(context);
                String s = SharedPrefs.getXMPPStatus(context);

                String[] tmp = connection.getUser().split("/");
                XMPPSessionStorage.getInstance().JID = tmp[0];

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

                startPacketListener(context);
                getRosterList();
                getAllMUCs();
                setRosterListener();
                setMucNickname();
            } else {

                if (context instanceof HomeActivity) {
                    final Context fin = context;
                    ((HomeActivity) context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            new ToastMaster(fin, "Error during login, is your username or password correct?", false);
                        }
                    });
                }
                destroy(null);
                return loggInCode;
            }
        }

        return CONNECTED_TO_SERVER;
    }

    public int login(String username, String password) {
        try {
            connection.login(username, password);

        } catch (XMPPException e) {
            return XMPP_EXCEPTION;
        } catch (SaslException e) {
            return SASL_EXCEPTION;
        } catch (SmackException.NoResponseException e) {
            return SMACK_NOT_CONNECTED_EXCEPTION;
        } catch (SmackException.NotConnectedException e) {
            return SMACK_NOT_CONNECTED_EXCEPTION;
        } catch (SmackException e) {
            return SMACK_EXCEPTION;
        } catch (IOException e) {
            return IO_EXCEPTION;
        } catch (Exception e) {
            return OTHER_EXCEPTION;
        }
        return USER_LOGGED_IN;
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

    public void getAllMUCs() {
        try {
            if (!MultiUserChat.getHostedRooms(connection, connection.getServiceName()).isEmpty()) {
                List<ConferenceModel> conferences = new ArrayList<ConferenceModel>();
                for (HostedRoom hr : MultiUserChat.getHostedRooms(connection, connection.getServiceName())) {

                    for (HostedRoom j : MultiUserChat.getHostedRooms(connection, hr.getJid())) {
                        RoomInfo roomInfo = MultiUserChat.getRoomInfo(connection, j.getJid());

                        if (j.getJid().indexOf("@") > 0) {

                            String id = j.getJid();
                            String name = j.getName();
                            String subject = roomInfo.getSubject();
                            int occupants = roomInfo.getOccupantsCount();
                            String description = roomInfo.getDescription();


                            if (XMPPSessionStorage.getInstance().MUCExist(id)) {
                                XMPPSessionStorage.getInstance().getConference(id).setName(name);
                                XMPPSessionStorage.getInstance().getConference(id).setTopic(subject);
                                XMPPSessionStorage.getInstance().getConference(id).setOccupants(occupants);
                                XMPPSessionStorage.getInstance().getConference(id).setDescription(description);
                            } else
                                conferences.add(new ConferenceModel(id, name, subject, description, occupants, new ArrayList<String>(), false, null));

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

    public void getRosterList() {
        asyncTask = new AsyncTask<String, String, List<RosterModel>>() {
            @Override
            protected List<RosterModel> doInBackground(String... args) {
                Roster roster = connection.getRoster();
                Collection<RosterEntry> entries = roster.getEntries();
                List<RosterModel> list = new ArrayList<RosterModel>();
                LastActivityManager lastActivityManager = LastActivityManager.getInstanceFor(connection);
                for (RosterEntry entry : entries) {
                    try {
                        LastActivity lastActivity = lastActivityManager.getLastActivity(entry.getUser());
                        Presence presence = roster.getPresence(entry.getUser());

                        String status = presence.getStatus();//lastActivity.getType().toString();//getStatusMessage();//presence.getStatus();
                        if (status == null) {
                            status = presence.getType().name();
                        }

                        if (XMPPSessionStorage.getInstance().getRosterModel(entry.getUser()) != null && lastActivity.getIdleTime() < 1) {
                            if (!XMPPSessionStorage.getInstance().getRosterModel(entry.getUser()).getStatusMessage().equals("unavailable"))
                                status = XMPPSessionStorage.getInstance().getRosterModel(entry.getUser()).getStatusMessage();
                            else
                                status = "Online";
                        } else if ((status.equals("unavailable") && lastActivity.getIdleTime() < 1) || ((status.equals("") && lastActivity.getIdleTime() < 1)))
                            status = "Online";

                        lastActivity.getStatusMessage();
                        list.add(new RosterModel(entry.getName(), entry.getUser(), "", status,
                                new ConvertSeconds().convertToDHM(lastActivity.getIdleTime()),
                                presence.getMode(), lastActivity.getIdleTime() < 1 ? true : false,
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

    public void setMucNickname() {
        try {
            AccountManager am = AccountManager.getInstance(connection);
            XMPPSessionStorage.getInstance().setNickname(am.getAccountAttribute("name"));
        } catch (XMPPException.XMPPErrorException e) {
            e.printStackTrace();
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        } catch (SmackException.NoResponseException e) {
            e.printStackTrace();
        }
    }

    public void startPacketListener(Context context) {
        PacketFilter filter = new MessageTypeFilter(Message.Type.chat);
        packetListener = new IMPacketListener(context);
        connection.addPacketListener(packetListener, filter);
        context.startService(new Intent(context, IMPacketListener.class));

    }

    public void removePacketListener() {
        try {
            connection.removePacketListener(packetListener);
        } catch (NullPointerException e) {
        }
    }

    public void setRosterListener() {
        rosterListener = new IMRosterListener();
        connection.getRoster().addRosterListener(rosterListener);
    }

    public void removeRosterListener() {
        if (rosterListener != null && connection != null)
            connection.getRoster().removeRosterListener(rosterListener);
    }

    public boolean removeFromConference(String JID) {
        try {
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

    public int updateMUC(String topic, String description) {
        try {
            muc.removeMessageListener(mucPacketListener);
            muc.removeParticipantStatusListener(mucParticipantListener);
            muc.changeSubject(topic);

            Form submitForm = muc.getConfigurationForm().createAnswerForm();
            submitForm.setAnswer("muc#roomconfig_roomdesc", description);
            muc.sendConfigurationForm(submitForm);
            muc.addMessageListener(mucPacketListener);
            muc.addParticipantStatusListener(mucParticipantListener);

            XMPPSessionStorage.getInstance().getConference(muc.getRoom()).setTopic(topic);
            XMPPSessionStorage.getInstance().getConference(muc.getRoom()).setDescription(description);
        } catch (SmackException.NoResponseException e) {
            return SMACK_NO_RESPONSE_EXCEPTION;
        } catch (XMPPException.XMPPErrorException e) {
            return XMPP_EXCEPTION;
        } catch (SmackException.NotConnectedException e) {
            return SMACK_NOT_CONNECTED_EXCEPTION;
        } catch (Exception e) {
            return OTHER_EXCEPTION;
        }
        return SUCCESS;
    }

    public int deleteConference(String id) {
        try {
            muc.removeMessageListener(mucPacketListener);
            muc.removeParticipantStatusListener(mucParticipantListener);
            muc.destroy("Deleted by user " + id, null);
            XMPPSessionStorage.getInstance().removeConference(muc.getRoom());
            muc.addMessageListener(mucPacketListener);
            muc.addParticipantStatusListener(mucParticipantListener);
        } catch (SmackException.NotConnectedException e) {
            return SMACK_NOT_CONNECTED_EXCEPTION;
        } catch (XMPPException.XMPPErrorException e) {
            return XMPP_EXCEPTION;

        } catch (SmackException.NoResponseException e) {
            return SMACK_NO_RESPONSE_EXCEPTION;

        } catch (Exception e) {
            return OTHER_EXCEPTION;
        }

        return SUCCESS;
    }

    public boolean onlineOnMuc(String id) {
        if (muc != null)
            if (muc.getRoom().equals(id))
                return muc.isJoined();
        return false;
    }

    public int joinMUC(final String id) throws NullPointerException {
        DiscussionHistory dh = new DiscussionHistory();
        dh.setMaxStanzas(CONFERENCE_HISTORY_MESSAGES);

        this.muc = new MultiUserChat(connection, id);

        try {
            if (XMPPSessionStorage.getInstance().getNickname() != null)
                this.muc.join(XMPPSessionStorage.getInstance().getNickname(), "", dh, Long.valueOf(TIMEOUT));
            else
                this.muc.join("no-nickname", "", dh, Long.valueOf(TIMEOUT));
            List<String> occupants = muc.getOccupants();
            for (String nick : occupants)
                XMPPSessionStorage.getInstance().addMUCParticipant(id, nick);
            if (muc.isJoined()) {


                for (Affiliate info : muc.getOwners()) {
                    if (info.getJid().equals(XMPPSessionStorage.getInstance().JID)) //info.getNick().equals(XMPPSessionStorage.getInstance().getNickname()))
                        XMPPSessionStorage.getInstance().getConference(id).setAdmin(true);
                }
                List<IMMessageModel> list = new ArrayList<IMMessageModel>();
                while (true) {
                    Message message = muc.nextMessage(500);
                    if (message == null)
                        break;
                    else {
                        if (message.getBody() != null) {
                            DelayInformation inf;
                            String username = message.getFrom();
                            String sub[] = username.split("/");
                            String nickname = sub[1];
                            inf = message.getExtension("x", "jabber:x:delay");
                            if (inf != null) {
                                list.add(new IMMessageModel(message.getBody(), nickname, new SimpleDateFormat("yyyy.MM.dd HH:mm").format(inf.getStamp())));
                            } else
                                list.add(new IMMessageModel(message.getBody(), nickname, ""));


                        }
                    }
                }
                XMPPSessionStorage.getInstance().setConferences(id, list);
                mucPacketListener = new MUCPacketListener(id);
                mucParticipantListener = new MUCParticipantListener(id);
                muc.addMessageListener(mucPacketListener);
                muc.addParticipantStatusListener(mucParticipantListener);
            } else
                return UNABLE_TO_JOIN_CONFERENCE;


            return SUCCESS;

        } catch (XMPPException.XMPPErrorException e) {
            return XMPP_EXCEPTION;
        } catch (SmackException.NotConnectedException e) {
            return SMACK_NOT_CONNECTED_EXCEPTION;
        } catch (SmackException.NoResponseException e) {
            return SMACK_NO_RESPONSE_EXCEPTION;
        } catch (Exception e) {
            return OTHER_EXCEPTION;
        }
    }

    public int sendMucMessage(String message) {
        try {
            muc.sendMessage(message);
        } catch (XMPPException e) {
            return XMPP_EXCEPTION;
        } catch (SmackException.NotConnectedException e) {
            return SMACK_NOT_CONNECTED_EXCEPTION;
        } catch (Exception e) {
            return OTHER_EXCEPTION;
        }

        return MESSAGE_SENT;
    }

    public int createConference(String name, String subject, String description) {
        MultiUserChat muc = new MultiUserChat(connection, name + "@conference." + connection.getServiceName());
        try {
            String[] tmp = connection.getUser().split("/");
            String jid = tmp[0];
            muc.create(jid);
            muc.changeSubject(subject);

            Form submitForm = muc.getConfigurationForm().createAnswerForm();
            submitForm.setAnswer("muc#roomconfig_publicroom", true);
            submitForm.setAnswer("muc#roomconfig_persistentroom", true);
            submitForm.setAnswer("muc#roomconfig_enablelogging", true);
            submitForm.setAnswer("muc#roomconfig_roomdesc", description);
            muc.sendConfigurationForm(submitForm);

        } catch (SmackException.NoResponseException e) {
            return SMACK_NO_RESPONSE_EXCEPTION;
        } catch (XMPPException.XMPPErrorException e) {
            return XMPP_EXCEPTION;
        } catch (SmackException e) {
            return SMACK_EXCEPTION;
        } catch (Exception e) {
            return OTHER_EXCEPTION;
        }

        return CONFERENCE_CREATED;
    }

    public void leaveMUC() {
        if (muc != null) {
            try {

                muc.leave();
                muc = null;
                mucPacketListener = null;
                mucParticipantListener = null;
            } catch (SmackException.NotConnectedException e) {
                e.printStackTrace();
            }
        }
    }

    public void destroy(Context context) {
        if (context != null && data != null) {
            SaveDataSqlLite db = new SaveDataSqlLite(context);
            db.open();
            db.updateOnline(data.stopDateAndGetRatio());
            db.close();
        }
        if (asyncTask != null) {
            if (!asyncTask.isCancelled())
                asyncTask.cancel(true);
            asyncTask = null;
        }

        new AsyncTask<Integer, String, String>() {
            @Override
            protected String doInBackground(Integer... args) {
                try {
                    if (connection != null && connection.isConnected()) {
                        removePacketListener();
                        removeRosterListener();
                        if (muc != null)
                            leaveMUC();
                        connection.disconnect();
                    }
                } catch (SmackException.NotConnectedException e) {
                }
                return "";
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                XMPPSessionStorage.getInstance().callback();
            }
        }.execute();
    }

    public static boolean noErrors(int code) {
        return code == CONNECTED_TO_SERVER || code == USER_LOGGED_IN ||
                code == SUCCESS || code == MESSAGE_SENT ||
                code == CONFERENCE_CREATED;
    }

    public static String getResponseMessage(int code) {
        switch (code) {
            case CONNECTED_TO_SERVER:
                return "Connected to server";
            case USER_LOGGED_IN:
                return "Successfully logged in";
            case SUCCESS:
                return "";
            case MESSAGE_SENT:
                return "Message sent!";
            case CONFERENCE_CREATED:
                return "Successfully created conference";
            case XMPP_EXCEPTION:
                return "XMPP Error";
            case SASL_EXCEPTION:
                return "Simple Authentication and Security Layer Error";
            case SMACK_NOT_CONNECTED_EXCEPTION:
                return "Not connected to chat server..";
            case SMACK_NO_RESPONSE_EXCEPTION:
                return "No response from chat server..";
            case SMACK_NOT_LOGGED_IN_EXCEPTION:
                return "Not logged in on chat server..";
            case SMACK_EXCEPTION:
                return "Something wrong with chat server..";
            case IO_EXCEPTION:
                return "Try refresh!";
            case SOCKET_TIMEOUT_EXCEPTION:
                return "Connection timeout - try again..";
            case UNABLE_TO_JOIN_CONFERENCE:
                return "Unable to join conference";
            case OTHER_EXCEPTION:
                return "Something went wrong..";

            default:
                return "";
        }
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

    /*public List<RosterGroup> getGroups() {
        Roster roster = connection.getRoster();
        Collection<RosterGroup> entries = roster.getGroups();
        return new ArrayList<RosterGroup>(entries);
    }*/
}
