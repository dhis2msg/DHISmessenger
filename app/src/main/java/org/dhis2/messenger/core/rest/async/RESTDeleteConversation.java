package org.dhis2.messenger.core.rest.async;

import android.content.Context;
import android.os.AsyncTask;

import org.dhis2.messenger.core.rest.APIPath;
import org.dhis2.messenger.core.rest.RESTClient;
import org.dhis2.messenger.core.rest.RESTSessionStorage;
import org.dhis2.messenger.core.rest.Response;
import org.dhis2.messenger.core.rest.callback.UnreadMessagesCallback;
import org.dhis2.messenger.SharedPrefs;
import org.dhis2.messenger.gui.ToastMaster;

/**
 * Created by iNick on 28.02.15.
 * Modified/Refactored by Vladislav on 02.12.2015
 *
 * An asynchronous task to delete a conversation in the background.
 */
public class RESTDeleteConversation extends AsyncTask<String, String, Integer> {

    private Context context;
    private UnreadMessagesCallback listener;
    private String userId;
    private int inboxModelIndex;

    public RESTDeleteConversation(Context context, UnreadMessagesCallback listener, String userId, int inboxModelIndex) {
        this.context = context;
        this.listener = listener;
        this.userId = userId;
        this.inboxModelIndex = inboxModelIndex;
    }

    @Override
    protected Integer doInBackground(String... args) {
        String id = args[0];
        String url = SharedPrefs.getServerURL(context)
                + APIPath.FIRST_PAGE_MESSAGES
                + "/" + id + "/" + userId;

        Response r = RESTClient.delete(url, SharedPrefs.getCredentials(context), "application/json");
        if (RESTClient.noErrors(r.getCode())) {
            int temp = Integer.parseInt(SharedPrefs.getUnreadMessages(context));
            if (temp > 0)
                SharedPrefs.setUnreadMessages(context, String.valueOf(temp - 1));
            //? I don't get it why decrease the unread messages ?
            // How do you know that the message that you are deleting is unread ?
        }
        return r.getCode();
    }

    protected void onPostExecute(final Integer code) {
        if (RESTClient.noErrors(code)) {
            //update the cache : (this way we don't have to refresh the page)
            RESTSessionStorage.getInstance().removeInboxModel(inboxModelIndex);
            listener.updateDHISMessages();
        } else
            new ToastMaster(context, "Could not delete message, try again..", false);
    }
}
