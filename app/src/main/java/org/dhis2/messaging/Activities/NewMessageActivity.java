package org.dhis2.messaging.Activities;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.MultiAutoCompleteTextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import org.dhis2.messaging.Models.NameAndIDModel;
import org.dhis2.messaging.R;
import org.dhis2.messaging.Testing.SaveDataSqlLite;
import org.dhis2.messaging.REST.APIPath;
import org.dhis2.messaging.REST.RESTClient;
import org.dhis2.messaging.REST.Response;
import org.dhis2.messaging.Utils.SharedPrefs;
import org.dhis2.messaging.Utils.UserInterface.ToastMaster;
import org.dhis2.messaging.XMPP.Interfaces.IMUpdateUnreadMessages;
import org.dhis2.messaging.XMPP.XMPPSessionStorage;
import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by iNick on 27.09.14.
 */
public class NewMessageActivity extends Activity implements IMUpdateUnreadMessages {
    @Bind(R.id.subject)
    EditText subject;
    @Bind(R.id.content)
    EditText content;
    @Bind(R.id.recipients)
    MultiAutoCompleteTextView recipients;
    @Bind(R.id.orgunits)
    MultiAutoCompleteTextView units;

    //Memory store
    private List<NameAndIDModel> users, orgUnits;
    private AsyncTask orgTask, userTask, msgTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_message);
        ButterKnife.bind(this);

        fixActionBar();
        getUsers();
        getOrgUnits();
    }

    @Override
    protected void onPause() {
        super.onPause();
        XMPPSessionStorage.getInstance().setHomeListener(null);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (orgTask != null) {
            if (!orgTask.isCancelled())
                orgTask.cancel(true);
            orgTask = null;
        }
        if (userTask != null) {
            if (!userTask.isCancelled())
                userTask.cancel(true);
            userTask = null;
        }
        if (msgTask != null) {
            if (!msgTask.isCancelled())
                msgTask.cancel(true);
            msgTask = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        XMPPSessionStorage.getInstance().setHomeListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_send, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void updateIMMessages(int amount) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new ToastMaster(getApplicationContext(), "New Chat Message", true);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.send:
                if (RESTClient.isDeviceConnectedToInternett(getApplicationContext()))
                    sendMessage();
                else
                    new ToastMaster(getApplicationContext(), "No internet connection", false);

                return true;
            case android.R.id.home:
                finish();
                NewMessageActivity.this.overridePendingTransition(R.anim.left_to_center, R.anim.center_to_right);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void fixActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle("Back");
        getActionBar().setSubtitle("Create new message");
        actionBar.setDisplayUseLogoEnabled(false);
    }

    private void setUserAdapter() {
        String[] array = new String[users.size()];
        for (int i = 0; i < array.length; i++) {
            array[i] = users.get(i).name;
        }
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.item_dropdown, array);
        recipients.setAdapter(adapter);
        recipients.setThreshold(1);
        recipients.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
    }

    private void setOrgUnitsAdapter() {
        String[] array = new String[orgUnits.size()];
        for (int i = 0; i < array.length; i++) {
            array[i] = orgUnits.get(i).name;
        }
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.item_dropdown, array);
        units.setAdapter(adapter);
        units.setThreshold(1);
        units.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
    }

    private List<NameAndIDModel> getSelectedRecipients() {
        if (users == null) {
            return new ArrayList<NameAndIDModel>();
        }

        String text = recipients.getText().toString();
        List<String> list = Arrays.asList(text.split(","));

        List<NameAndIDModel> recivers = new ArrayList<NameAndIDModel>();
        List<String> notRecivers = new ArrayList<String>();
        for (String l : list) {
            l = l.trim();
            boolean exist = false;
            searching:
            for (NameAndIDModel u : users) {
                if (u.getName().trim().equals(l)) {
                    NameAndIDModel model = new NameAndIDModel();
                    model.setId(u.getId());
                    recivers.add(model);
                    exist = true;
                    break searching;

                }
            }
            if (!exist && !l.trim().isEmpty())
                notRecivers.add(l);
        }

        if (!notRecivers.isEmpty()) {
            String stri = "";
            for (String s : notRecivers) {
                stri += s + ", ";
            }
            new ToastMaster(getApplicationContext(), "Could not send message to: " + stri, false);
        }
        return recivers;
    }

    private List<NameAndIDModel> getSelectedOrgUnits() {

        if (orgUnits == null) {
            return new ArrayList<NameAndIDModel>();
        }

        String text = units.getText().toString();
        List<String> list = Arrays.asList(text.split(","));

        List<NameAndIDModel> recivers = new ArrayList<NameAndIDModel>();
        List<String> notRecivers = new ArrayList<String>();
        for (String l : list) {
            l = l.trim();
            boolean exist = false;
            searching:
            for (NameAndIDModel u : orgUnits) {
                if (u.getName().trim().equals(l)) {
                    NameAndIDModel model = new NameAndIDModel();
                    model.setId(u.getId());
                    recivers.add(model);
                    exist = true;
                    break searching;

                }
            }
            if (!exist && !l.trim().isEmpty())
                notRecivers.add(l);
        }

        if (!notRecivers.isEmpty()) {
            String stri = "";
            for (String s : notRecivers) {
                stri += s + ", ";
            }
            new ToastMaster(getApplicationContext(), "Could not send message to: " + stri, false);
        }
        return recivers;
    }

    private String createJSONMessage() {
        MessageModel model = new MessageModel(subject.getText().toString(),
                content.getText().toString(),
                getSelectedRecipients(), getSelectedOrgUnits());
        if (model.recipients.size() < 1 && model.organisations.size() < 1) {
            new ToastMaster(getApplicationContext(), "Can only send messages to registered organisation units or users", false);
            return null;
        }
        return new Gson().toJson(model);
    }

    private boolean emptyStrings() {
        if (subject.getText().toString().isEmpty() || content.getText().toString().isEmpty() ||
                (recipients.getText().toString().isEmpty() && units.getText().toString().isEmpty())) {
            new ToastMaster(getApplicationContext(), "Requires some necessary fields", false);

            return true;
        }

        return false;
    }

    private void sendMessage() {
        if (!emptyStrings()) {
            final String message = createJSONMessage();
            if (message != null) {
                msgTask = new AsyncTask<Integer, String, Integer>() {
                    private ProgressDialog progressDialog;

                    @Override
                    protected void onPreExecute() {
                        super.onPreExecute();
                        progressDialog = new ProgressDialog((NewMessageActivity.this));
                        progressDialog.setMessage("Sending message");
                        progressDialog.setIndeterminate(false);
                        progressDialog.setCancelable(false);
                        progressDialog.show();

                    }

                    @Override
                    protected Integer doInBackground(Integer... args) {
                        String api = SharedPrefs.getServerURL(getApplicationContext()) + APIPath.FIRST_PAGE_MESSAGES;
                        Response response = RESTClient.post(api, SharedPrefs.getCredentials(getApplicationContext()), message, "application/json");
                        return response.getCode();
                    }

                    @Override
                    protected void onPostExecute(Integer code) {
                        progressDialog.dismiss();
                        if (RESTClient.noErrors(code)) {
                            new ToastMaster(getApplicationContext(), "Message sent!", false);
                            SaveDataSqlLite db = new SaveDataSqlLite(NewMessageActivity.this);
                            db.open();
                            db.updateDHISMessageSent();
                            db.close();
                            finish();
                            NewMessageActivity.this.overridePendingTransition(R.anim.left_to_center, R.anim.center_to_right);
                        } else if (recipients.getText().toString().isEmpty()) {
                            new ToastMaster(getApplicationContext(), "Could not send message - organisation-unit might be empty (no users)!", false);

                        } else
                            new ToastMaster(getApplicationContext(), "Could not send message!", false);

                    }
                }.execute();
            }
        }
    }

    private void getUsers() {
        userTask = new AsyncTask<Integer, String, Integer>() {

            @Override
            protected Integer doInBackground(Integer... args) {
                String api = SharedPrefs.getServerURL(getApplicationContext()) + APIPath.USERS + APIPath.NAME_AND_ID_FIELDS;

                Response response = RESTClient.get(api, SharedPrefs.getCredentials(getApplicationContext()));
                if (RESTClient.noErrors(response.getCode())) {
                    try {
                        JSONObject json = new JSONObject(response.getBody());
                        JSONArray array = json.getJSONArray("users");
                        Type listType = new TypeToken<ArrayList<NameAndIDModel>>() {
                        }.getType();
                        users = new Gson().fromJson(array.toString(), listType);


                    } catch (Exception e) {

                    }
                }
                return response.getCode();
            }

            @Override
            protected void onPostExecute(Integer code) {
                if (RESTClient.noErrors(code))
                    setUserAdapter();
            }
        }.execute();
    }

    private void getOrgUnits() {
        orgTask = new AsyncTask<Integer, String, Integer>() {

            @Override
            protected Integer doInBackground(Integer... args) {
                String api = SharedPrefs.getServerURL(getApplicationContext()) + APIPath.ORG_UNITS + APIPath.NAME_AND_ID_FIELDS;

                Response response = RESTClient.get(api, SharedPrefs.getCredentials(getApplicationContext()));
                if (RESTClient.noErrors(response.getCode())) {
                    try {
                        JSONObject json = new JSONObject(response.getBody());
                        JSONArray array = json.getJSONArray("organisationUnits");
                        Type listType = new TypeToken<ArrayList<NameAndIDModel>>() {
                        }.getType();
                        orgUnits = new Gson().fromJson(array.toString(), listType);


                    } catch (Exception e) {

                    }
                }
                return response.getCode();
            }

            @Override
            protected void onPostExecute(Integer code) {
                if (RESTClient.noErrors(code))
                    setOrgUnitsAdapter();
            }
        }.execute();
    }

    private class MessageModel {
        //For JSON purposes only
        @SerializedName("organisationUnits")
        List<NameAndIDModel> organisations;
        @SerializedName("users")
        List<NameAndIDModel> recipients;
        @SerializedName("subject")
        String subject;
        @SerializedName("text")
        String text;

        public MessageModel(String subject, String text, List<NameAndIDModel> recipients, List<NameAndIDModel> organisations) {
            this.organisations = organisations;
            this.recipients = recipients;
            this.text = text;
            this.subject = subject;
        }
    }
}