package org.dhis2.messaging.Utils.AsyncroniousTasks;

import android.content.Context;
import android.os.AsyncTask;

import org.dhis2.messaging.Models.NameAndIDModel;
import org.dhis2.messaging.Utils.AsyncroniousTasks.Interfaces.RESTConversationCallback;
import org.dhis2.messaging.Models.ChatModel;
import org.dhis2.messaging.REST.APIPath;
import org.dhis2.messaging.REST.RESTClient;
import org.dhis2.messaging.REST.Response;
import org.dhis2.messaging.Utils.SharedPrefs;
import org.dhis2.messaging.Utils.UserInterface.ToastMaster;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by iNick on 20.10.14.
 */
public class RESTGetConversation extends AsyncTask<String, Void, Integer> {
    private List<ChatModel> tempList = new ArrayList<ChatModel>();
    private List<NameAndIDModel> tempMembers = new ArrayList<NameAndIDModel>();
    private RESTConversationCallback listener;
    private Context context;
    private boolean read;
    private String id;

    public RESTGetConversation(RESTConversationCallback listener, Context context, boolean read, String id) {
        this.listener = listener;
        this.context = context;
        this.read = read;
        this.id = id;
    }

    @Override
    protected Integer doInBackground(String... strings) {
        String api = SharedPrefs.getServerURL(context)
                + APIPath.FIRST_PAGE_MESSAGES
                + "/"
                + id;

        Response response = RESTClient.get(api + APIPath.REST_CONVERSATION_FIELDS, SharedPrefs.getCredentials(context));
        if (RESTClient.noErrors(response.getCode())) {
            try {
                JSONObject json = new JSONObject(response.getBody());

                //getting users
                JSONArray users = new JSONArray(json.getString("userMessages"));
                for (int i = 0; i < users.length(); i++) {
                    JSONObject row = users.getJSONObject(i);
                    JSONObject user = row.getJSONObject("user");        //All members of conversation
                    NameAndIDModel model = new NameAndIDModel();
                    model.setId(user.getString("id"));
                    model.setName(user.getString("name"));
                    tempMembers.add(model);
                }

                //getting messages
                JSONArray messages = new JSONArray(json.getString("messages"));
                for (int i = 0; i < messages.length(); i++) {
                    JSONObject row = messages.getJSONObject(i);
                    String message = row.getString("text");                //Content of message (yes its called name)
                    String date = row.getString("lastUpdated");         //Correct to use last updated here?

                    JSONObject sender = row.getJSONObject("sender");
                    String id = sender.getString("id");
                    String username = sender.getString("name");
                    NameAndIDModel user = new NameAndIDModel();
                    user.setId(id);
                    user.setName(username);

                    ChatModel model = new ChatModel(message, date, user);
                    tempList.add(model);
                }
                if (read == false) {
                    JSONArray array = new JSONArray();
                    array.put(id);

                    String url = SharedPrefs.getServerURL(context)
                            + APIPath.FIRST_PAGE_MESSAGES
                            + "/read";

                    Response r = RESTClient.post(url, SharedPrefs.getCredentials(context), array.toString(), "application/json");
                    if (RESTClient.noErrors(r.getCode())) {
                        int temp = Integer.parseInt(SharedPrefs.getUnreadMessages(context));
                        if (temp > 0)
                            SharedPrefs.setUnreadMessages(context, String.valueOf(temp - 1));

                    }

                }

            } catch (JSONException e) {
                e.printStackTrace();
                return -1;
            }
        }
        return response.getCode();
    }

    protected void onPostExecute(final Integer code) {
        if (RESTClient.noErrors(code)) {
            listener.updateMessages(tempList);
            listener.updateUsers(tempMembers);
        } else
            new ToastMaster(context, "Could not load messages:\n" + RESTClient.getErrorMessage(code), false);
    }
}