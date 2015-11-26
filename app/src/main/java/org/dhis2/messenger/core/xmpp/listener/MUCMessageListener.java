package org.dhis2.messenger.core.xmpp.listener;

import org.dhis2.messenger.CurrentTime;
import org.dhis2.messenger.core.xmpp.XMPPSessionStorage;
import org.dhis2.messenger.model.IMMessageModel;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.packet.Message;

public class MUCMessageListener implements MessageListener {

    private String id;

    public MUCMessageListener(String id) {
        this.id = id;
    }

    @Override
    public void processMessage(Message message) {
        IMMessageModel messageModel = null;
        String nickname = "";

        if (message.getBody() != null) {
            String username = message.getFrom();
            String sub[] = username.split("/");
            nickname = sub[1];
            messageModel = new IMMessageModel(message.getBody(), nickname, new CurrentTime().getCurrentTime());
        }
        XMPPSessionStorage.getInstance().addConferenceMessage(id, messageModel);
    }

}
