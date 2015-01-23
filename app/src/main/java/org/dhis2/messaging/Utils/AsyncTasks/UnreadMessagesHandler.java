package org.dhis2.messaging.Utils.AsyncTasks;

import android.content.Context;
import android.os.AsyncTask;

import org.dhis2.messaging.Utils.AsyncTasks.Interfaces.UnreadMessagesCallback;
import org.dhis2.messaging.Utils.REST.APIPaths;
import org.dhis2.messaging.Utils.REST.RESTClient;
import org.dhis2.messaging.Utils.REST.Response;
import org.dhis2.messaging.Utils.SharedPrefs;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by iNick on 20.10.14.
 */
public class UnreadMessagesHandler {

    private static UnreadMessagesCallback listener;

    public UnreadMessagesHandler(UnreadMessagesCallback listener) {
        this.listener = listener;
    }

    public static void getAllUnreadMessages(final Context context) {
        new AsyncTask<Integer, String, Integer>() {
            String pages = "500";
            String server = SharedPrefs.getServerURL(context);
            String api = server + APIPaths.FIRST_PAGE_MESSAGES + "?fields=read&pageSize=" + pages;
            String auth = SharedPrefs.getCredentials(context);
            int unreadMessages = 0;

            @Override
            protected Integer doInBackground(Integer... args) {
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
                    listener.unreadMessagesLoadComplete();
                }
            }
        }.execute();
    }
    public static void unreadOneMessage(Context context){
        int count = Integer.parseInt(SharedPrefs.getUnreadMessages(context));
        count--;
        SharedPrefs.setUnreadMessages(context,Integer.toString(count));
    }
}
