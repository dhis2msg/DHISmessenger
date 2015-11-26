package org.dhis2.messenger.gui.activity;

/**
 * An interface that activities in the messanger app are expected to implement.
 * The rest and DHIS2 message sub-apps/sub-clients, have setters/getters to the current active Activity,
 * that implements this interface.
 *
 * In short when new message comes, the xmpp/rest messenger client uses this method's implementation to
 * inform of the event of new messages having happened.
 */
public interface UpdateUnreadMsg {
    /**
     * A method to inform the user/current active activity that new messages
     * from particular client (xmpp or rest) have arrived.
     *
     * @param restNumber of new messages
     * @param xmppNumber of new messages
     */
    void updateUnreadMsg(int restNumber, int xmppNumber);
}
