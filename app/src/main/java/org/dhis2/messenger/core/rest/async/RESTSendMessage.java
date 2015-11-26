package org.dhis2.messenger.core.rest.async;

import android.content.Context;
import android.os.AsyncTask;

import org.dhis2.messenger.core.rest.APIPath;
import org.dhis2.messenger.core.rest.RESTClient;
import org.dhis2.messenger.core.rest.Response;
import org.dhis2.messenger.core.rest.callback.RESTConversationCallback;
import org.dhis2.messenger.SharedPrefs;

/**
 * Created by iNick on 23.02.15.
 */
public class RESTSendMessage extends AsyncTask<String, Integer, Integer> {
    private RESTConversationCallback listener;
    private Context context;
    private String message;
    private String api;

    public RESTSendMessage(RESTConversationCallback listener, Context context, String message, String id) {
        this.listener = listener;
        this.context = context;
        this.message = message;
        api = SharedPrefs.getServerURL(context)
                + APIPath.FIRST_PAGE_MESSAGES
                + "/"
                + id;
    }

    @Override
    protected Integer doInBackground(String... strings) {
        Response response = RESTClient.post(api, SharedPrefs.getCredentials(context), message, "text/plain");
        return response.getCode();
    }

    protected void onPostExecute(final Integer code) {
        listener.messageSent(RESTClient.noErrors(code));
    }
}