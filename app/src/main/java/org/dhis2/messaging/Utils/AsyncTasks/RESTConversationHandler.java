package org.dhis2.messaging.Utils.AsyncTasks;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.google.gson.JsonObject;

import org.dhis2.messaging.Models.NameAndIDModel;
import org.dhis2.messaging.Utils.AsyncTasks.Interfaces.RESTConversationCallback;
import org.dhis2.messaging.Models.ChatModel;
import org.dhis2.messaging.Utils.REST.APIPaths;
import org.dhis2.messaging.Utils.REST.RESTClient;
import org.dhis2.messaging.Utils.REST.Response;
import org.dhis2.messaging.Utils.SharedPrefs;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by iNick on 20.10.14.
 */
public class RESTConversationHandler {

    private static RESTConversationCallback listener;

    public RESTConversationHandler(RESTConversationCallback listener) {
        this.listener = listener;
    }

    public static void getInbox(final Context context, final boolean read, final String id) {
        new AsyncTask<String, Integer, Integer>() {
            private List<ChatModel> tempList = new ArrayList<ChatModel>();
            private List<NameAndIDModel> tempMembers = new ArrayList<NameAndIDModel>();

            @Override
            protected Integer doInBackground(String... strings) {
                String api = SharedPrefs.getServerURL(context)
                        + APIPaths.FIRST_PAGE_MESSAGES
                        + "/"
                        + id;

                Response response = RESTClient.get(api + APIPaths.REST_CONVERSATION_FIELDS, SharedPrefs.getCredentials(context));
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
                            //mark as read!
                            JsonObject jout = new JsonObject();
                            jout.addProperty("id", id);
                            jout.addProperty("read", "true");
                            String s = jout.toString();
                            Response r = RESTClient.post(api, SharedPrefs.getCredentials(context),
                                    s, "application/json");

                            System.out.println(RESTClient.getErrorMessage(r.getCode()));//*/
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
                    Toast.makeText(context, "Could not load messages", Toast.LENGTH_LONG).show();
            }
        }.execute();
    }

    public static void sendMessage(final Context context, final String message, final String id) {
        new AsyncTask<String, Integer, Integer>() {
            @Override
            protected Integer doInBackground(String... strings) {
                String api = SharedPrefs.getServerURL(context)
                        + APIPaths.FIRST_PAGE_MESSAGES
                        + "/"
                        + id;

                Response response = RESTClient.post(api, SharedPrefs.getCredentials(context), message, "text/plain");
                System.out.println(RESTClient.getErrorMessage(response.getCode()));

                return response.getCode();
            }

            protected void onPostExecute(final Integer code) {
                listener.messageSent(RESTClient.noErrors(code));
            }
        }.execute();
    }
}
