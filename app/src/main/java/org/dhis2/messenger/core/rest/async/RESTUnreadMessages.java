package org.dhis2.messenger.core.rest.async;

import android.content.Context;
import android.os.AsyncTask;

import org.dhis2.messenger.core.rest.APIPath;
import org.dhis2.messenger.core.rest.RESTClient;
import org.dhis2.messenger.core.rest.Response;
import org.dhis2.messenger.core.rest.callback.UnreadMessagesCallback;
import org.dhis2.messenger.SharedPrefs;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by iNick on 20.10.14.
 */
public class RESTUnreadMessages extends AsyncTask<Void, Void, Integer> {

    public static UnreadMessagesCallback listener;
    private Context context;
    private String api, auth = "";
    private int unreadMessages = 0;

    public RESTUnreadMessages(UnreadMessagesCallback listener, Context context) {
        this.listener = listener;
        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        String pages = "500";
        String server = SharedPrefs.getServerURL(context);
        api = server + APIPath.FIRST_PAGE_MESSAGES + "?fields=read&pageSize=" + pages;
        auth = SharedPrefs.getCredentials(context);
        unreadMessages = 0;
    }

    @Override
    protected Integer doInBackground(Void... args) {
        try {
            Response response = RESTClient.get(api, auth);
            if (RESTClient.noErrors(response.getCode())) {
                JSONObject json = new JSONObject(response.getBody());
                JSONArray allConversations = new JSONArray(json.getString("messageConversations"));

                for (int i = 0; i < allConversations.length(); i++) {
                    JSONObject row = allConversations.getJSONObject(i);
                    boolean read = Boolean.parseBoolean(row.getString("read"));
                    if (!read)
                        unreadMessages++;
                }

                return response.getCode();
            } else
                return response.getCode();
        } catch (JSONException e) {
            e.printStackTrace();
            return -1;
        }
    }

    @Override
    protected void onPostExecute(Integer code) {
        if (RESTClient.noErrors(code)) {
            SharedPrefs.setUnreadMessages(context, Integer.toString(unreadMessages));
            listener.updateDHISMessages();
        }
    }
}
