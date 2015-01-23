package org.dhis2.messaging.Utils.XMPP;

import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.packet.Presence;

import java.util.Collection;

/**
 * Created by iNick on 14.11.14.
 */
public class IMRosterListener implements RosterListener{


    @Override
    public void entriesAdded(Collection<String> strings) {

    }

    @Override
    public void entriesUpdated(Collection<String> strings) {

    }

    @Override
    public void entriesDeleted(Collection<String> strings) {

    }

    @Override
    public void presenceChanged(Presence presence) {
        String[] split = presence.getFrom().split("/");
        String JID = split[0];
        XMPPSessionStorage.getInstance().updateMode(JID, presence.getMode());
        XMPPSessionStorage.getInstance().updateStatus(JID, presence.getStatus());
        XMPPSessionStorage.getInstance().updateAvailability(JID, presence.isAvailable());
        XMPPSessionStorage.getInstance().updateLastActivty(JID, "");
    }
}
