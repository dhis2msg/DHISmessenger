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
public class RESTMarkRead extends AsyncTask<String, String, Integer> {

    private Context context;
    private UnreadMessagesCallback listener;

    public RESTMarkRead(Context context, UnreadMessagesCallback listener) {
        this.context = context;
        this.listener = listener;
    }

    @Override
    protected Integer doInBackground(String... args) {
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
