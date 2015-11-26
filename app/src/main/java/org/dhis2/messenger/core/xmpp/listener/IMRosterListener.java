package org.dhis2.messenger.core.xmpp.listener;

import org.dhis2.messenger.core.xmpp.XMPPSessionStorage;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.roster.RosterListener;

import java.util.Collection;

public class IMRosterListener implements RosterListener {

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
        XMPPSessionStorage.getInstance().getXMPPData();
        XMPPSessionStorage.getInstance().updateMode(JID, presence.getMode());
        if (presence.getStatus() == null) {
            XMPPSessionStorage.getInstance().updateStatus(JID, "unavailable");
        } else if (presence.isAvailable()) {
            XMPPSessionStorage.getInstance().updateStatus(JID, "Online");
        } else {
            XMPPSessionStorage.getInstance().updateStatus(JID, (presence.getStatus()));
        }
        XMPPSessionStorage.getInstance().updateAvailability(JID, presence.isAvailable());
        XMPPSessionStorage.getInstance().updateLastActivty(JID, "");
    }

}
