package org.dhis2.messaging.Utils.XMPP;

import org.dhis2.messaging.Models.IMMessageModel;
import org.dhis2.messaging.Utils.CurrentTime;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smackx.delay.packet.DelayInformation;

import java.text.SimpleDateFormat;

/**
 * Created by iNick on 14.11.14.
 */
public class IMPacketListener implements PacketListener {
    @Override
    public void processPacket(Packet packet) throws SmackException.NotConnectedException {
        IMMessageModel messageModel = null;
        String JID =  "";
        Message message = (Message) packet;
        DelayInformation inf;
        if (message.getBody() != null) {
            String username = message.getFrom();
            String sub[] = username.split("/");
            JID = sub[0];
            inf = message.getExtension("x","jabber:x:delay");
            if(inf!=null) {
                messageModel = new IMMessageModel(message.getBody(), JID , new SimpleDateFormat("yyyy.MM.dd HH:mm").format(inf.getStamp()));
            }
            else
                messageModel = new IMMessageModel(message.getBody(), JID , new CurrentTime().getCurrentTime());
        }
        System.out.println(message.getBody());
        XMPPSessionStorage.getInstance().addMessage(JID, messageModel);
    }
}
