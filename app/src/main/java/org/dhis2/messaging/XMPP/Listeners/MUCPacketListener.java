package org.dhis2.messaging.XMPP.Listeners;

import org.dhis2.messaging.Models.IMMessageModel;
import org.dhis2.messaging.Utils.CurrentTime;
import org.dhis2.messaging.XMPP.XMPPSessionStorage;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;

/**
 * Created by iNick on 24.11.14.
 */
public class MUCPacketListener implements PacketListener {

    private String id;

    public MUCPacketListener(String id) {
        this.id = id;
    }

    @Override
    public void processPacket(Packet packet) throws SmackException.NotConnectedException {
        IMMessageModel messageModel = null;
        String nickname = "";

        Message message = (Message) packet;
        if (message.getBody() != null) {
            String username = message.getFrom();
            String sub[] = username.split("/");
            nickname = sub[1];
            messageModel = new IMMessageModel(message.getBody(), nickname, new CurrentTime().getCurrentTime());
        }
        XMPPSessionStorage.getInstance().addConferenceMessage(id, messageModel);
    }
}