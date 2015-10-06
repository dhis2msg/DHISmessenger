package org.dhis2.messaging.XMPP.Listeners;

import org.dhis2.messaging.XMPP.XMPPSessionStorage;
import org.jivesoftware.smackx.muc.ParticipantStatusListener;

/**
 * Created by iNick on 24.11.14.
 */
public class MUCParticipantListener implements ParticipantStatusListener {

    private String id;

    public MUCParticipantListener(String id) {
        this.id = id;
    }

    @Override
    public void joined(String s) {
        XMPPSessionStorage.getInstance().addMUCParticipant(id, s);

    }

    @Override
    public void left(String s) {
        XMPPSessionStorage.getInstance().removeMUCParticipant(id, s);
    }

    @Override
    public void kicked(String s, String s2, String s3) {

    }

    @Override
    public void voiceGranted(String s) {

    }

    @Override
    public void voiceRevoked(String s) {

    }

    @Override
    public void banned(String s, String s2, String s3) {

    }

    @Override
    public void membershipGranted(String s) {

    }

    @Override
    public void membershipRevoked(String s) {

    }

    @Override
    public void moderatorGranted(String s) {

    }

    @Override
    public void moderatorRevoked(String s) {

    }

    @Override
    public void ownershipGranted(String s) {

    }

    @Override
    public void ownershipRevoked(String s) {

    }

    @Override
    public void adminGranted(String s) {

    }

    @Override
    public void adminRevoked(String s) {

    }

    @Override
    public void nicknameChanged(String s, String s2) {

    }
}
