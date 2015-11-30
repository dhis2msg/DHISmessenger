package org.dhis2.messenger.core.rest.async;

import android.content.Context;
import android.os.AsyncTask;

import org.dhis2.messenger.core.rest.APIPath;
import org.dhis2.messenger.core.rest.RESTClient;
import org.dhis2.messenger.core.rest.Response;
import org.dhis2.messenger.core.rest.callback.UnreadMessagesCallback;
import org.dhis2.messenger.SharedPrefs;
import org.dhis2.messenger.gui.ToastMaster;
import org.json.JSONArray;

/**
 * Created by iNick on 28.02.15.
 */
public class RESTDeleteMessage extends AsyncTask<String, String, Integer> {

    private Context context;
    private UnreadMessagesCallback listener;
    private String userid;

    public RESTDeleteMessage(Context context, UnreadMessagesCallback listener, String userid) {
        this.context = context;
        this.listener = listener;
        this.userid = userid;
    }

    @Override
    protected Integer doInBackground(String... args) {
        //TODO: Vladislav : also delete from cache!
        String id = args[0];
        String url = SharedPrefs.getServerURL(context)
                + APIPath.FIRST_PAGE_MESSAGES
                + "/" + id + "/" + userid;

        JSONArray array = new JSONArray();
        array.put(id);
        Response r = RESTClient.delete(url, SharedPrefs.getCredentials(context), "application/json");
        if (RESTClient.noErrors(r.getCode())) {
            int temp = Integer.parseInt(SharedPrefs.getUnreadMessages(context));
            if (temp > 0)
                SharedPrefs.setUnreadMessages(context, String.valueOf(temp - 1));

        }
        return r.getCode();
    }

    protected void onPostExecute(final Integer code) {
        if (RESTClient.noErrors(code)) {
            listener.updateDHISMessages();
        } else
            new ToastMaster(context, "Could not delete message, try again..", false);
    }
}