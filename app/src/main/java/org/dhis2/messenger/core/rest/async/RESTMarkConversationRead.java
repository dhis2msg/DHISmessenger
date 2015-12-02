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
import org.dhis2.messenger.model.InboxModel;
import org.json.JSONArray;

/**
 * Created by iNick on 28.02.15.
 *
 */
public class RESTMarkConversationRead extends AsyncTask<String, String, Integer> {

    private Context context;
    private UnreadMessagesCallback listener;
    private int inboxModelIndex;

    public RESTMarkConversationRead(Context context, UnreadMessagesCallback listener, int inboxModelIndex) {
        this.context = context;
        this.listener = listener;
        this.inboxModelIndex = inboxModelIndex;
    }

    @Override
    protected Integer doInBackground(String... args) {
        //update the cache :
        InboxModel conversation = RESTSessionStorage.getInstance().getInboxModel(inboxModelIndex);
        conversation.setRead(true);

        //TODO: Evaluate if RESTMarkConversationRead is still relevant. And how we use read.
        // Since the read field is no longer used server side is this still relevant to  do ?
        // Is there a reason to distinguish between server read and client read ?
        String id = args[0];
        String url = SharedPrefs.getServerURL(context)
                + APIPath.FIRST_PAGE_MESSAGES
                + "/read";
        JSONArray array = new JSONArray();
        array.put(id);
        Response r = RESTClient.post(url, SharedPrefs.getCredentials(context), array.toString(), "application/json");
        if (RESTClient.noErrors(r.getCode())) {
            int temp = Integer.parseInt(SharedPrefs.getUnreadMessages(context));
            if (temp > 0)
                SharedPrefs.setUnreadMessages(context, String.valueOf(temp - 1));

        }
        return r.getCode();
    }

    protected void onPostExecute(final Integer code) {
        if (RESTClient.noErrors(code)) {
            //Maybe cache this ?
            listener.updateDHISMessages();
        } else
            new ToastMaster(context, "Could not mark read, try again..", false);
    }
}
