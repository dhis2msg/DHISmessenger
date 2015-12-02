package org.dhis2.messenger.core.rest.async;

import android.content.Context;
import android.os.AsyncTask;

import org.dhis2.messenger.core.rest.RESTSessionStorage;
import org.dhis2.messenger.model.ChatModel;
import org.dhis2.messenger.model.InboxModel;
import org.dhis2.messenger.model.NameAndIDModel;
import org.dhis2.messenger.core.rest.APIPath;
import org.dhis2.messenger.core.rest.RESTClient;
import org.dhis2.messenger.core.rest.Response;
import org.dhis2.messenger.core.rest.callback.RESTConversationCallback;
import org.dhis2.messenger.SharedPrefs;
import org.dhis2.messenger.gui.ToastMaster;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by iNick on 20.10.14.
 */
public class RESTGetConversation extends AsyncTask<String, Void, Integer> {
    private List<ChatModel> tempMessages = new ArrayList<ChatModel>();
    private List<NameAndIDModel> tempMembers = new ArrayList<NameAndIDModel>();
    private RESTConversationCallback listener;
    private Context context;
    private boolean read;
    private String id; // this is the id of the conversation
    private int inboxModelIndex;
    private InboxModel inboxModel;
    private boolean fromCache = false;

    /**
     * A constructor for this task.
     * To make this skip the cache simply pass read == false as an argument.
     * Or set the inboxModel.read == false & update the cache (?) from InboxFragment,HomeActivity, REESTChatActivity...etc up the chain before this instance is created.
     * (As of the writing of this comment the second won't probably work as I have to modify the caching of the inboxModel lists for inboxFragment to modify the existing entries' .read variable..)
     *
     * @param listener
     * @param context
     * @param read
     * @param id
     * @param inboxModelIndex
     */
    public RESTGetConversation(RESTConversationCallback listener, Context context, boolean read, String id, int inboxModelIndex) {
        this.listener = listener;
        this.context = context;
        this.read = read;
        this.id = id;
        this.inboxModelIndex = inboxModelIndex;
    }

    @Override
    protected Integer doInBackground(String... strings) {
        this.inboxModel = RESTSessionStorage.getInstance().getInboxModel(inboxModelIndex);
        if (read && !inboxModel.messages.isEmpty() && !inboxModel.members.isEmpty()) { //get from cache.
            tempMembers = inboxModel.members;
            tempMessages = inboxModel.messages;
            fromCache = true;
            return RESTClient.OK;
        } else {
            fromCache = false;
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
                        tempMessages.add(model);
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
    }

    protected void onPostExecute(final Integer code) {
        if (RESTClient.noErrors(code)) {
            if (!fromCache) {
                inboxModel.members.clear();
                inboxModel.members.addAll(tempMembers);
                inboxModel.messages.clear();
                inboxModel.messages.addAll(tempMessages);
            }
            //System.out.println("-------------Are the inbox models still the same ? " + inboxModel.equals(RESTSessionStorage.getInstance().getInboxModel(inboxModelIndex)));

            listener.updateMessages(tempMessages);
            listener.updateUsers(tempMembers);

        } else
            new ToastMaster(context, "Could not load messages:\n" + RESTClient.getErrorMessage(code), false);
    }
}