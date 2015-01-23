package org.dhis2.messaging;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
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
import org.dhis2.messaging.Utils.REST.APIPaths;
import org.dhis2.messaging.Utils.REST.RESTClient;
import org.dhis2.messaging.Utils.REST.Response;
import org.dhis2.messaging.Utils.SharedPrefs;
import org.dhis2.messaging.Utils.XMPP.XMPPClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by iNick on 27.09.14.
 */
public class NewMessageActivity extends Activity {
    private EditText subject, content;
    private MultiAutoCompleteTextView recipients, units;
    public List<NameAndIDModel> users, orgUnits;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_message);
        subject = (EditText) findViewById(R.id.subject);
        content = (EditText) findViewById(R.id.content);
        recipients = (MultiAutoCompleteTextView) findViewById(R.id.recipients);
        units = (MultiAutoCompleteTextView) findViewById(R.id.orgunits);
        fixActionBar();
        getUsers();
        getOrgUnits();
    }

    public void fixActionBar(){
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle("Back");
        getActionBar().setSubtitle("Create new message");
        actionBar.setDisplayUseLogoEnabled(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_send, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.send:
                sendMessage();
                return true;
            case android.R.id.home:
                finish();
                NewMessageActivity.this.overridePendingTransition(R.anim.left_to_center, R.anim.center_to_right);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void setUserAdapter() {
        String[] array = new String[users.size()];
        for (int i = 0; i < array.length; i++) {
            array[i] = users.get(i).name;
        }
        //final String[] a = array;
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.item_dropdown, array);
        recipients.setAdapter(adapter);
        recipients.setThreshold(1);
        recipients.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
    }
    public void setOrgUnitsAdapter() {
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
        if(users == null){
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
                if (u.getName().equals(l)) {
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
            Toast.makeText(NewMessageActivity.this, "Could not send message to: " + stri, Toast.LENGTH_SHORT).show();
        }
        return recivers;
    }
    private List<NameAndIDModel> getSelectedOrgUnits() {

        if(orgUnits == null){
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
                if (u.getName().equals(l)) {
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
            Toast.makeText(NewMessageActivity.this, "Could not send message to: " + stri, Toast.LENGTH_SHORT).show();
        }
        return recivers;
    }

    private String createJSONMessage() {
        MessageModel model = new MessageModel(subject.getText().toString(),
                content.getText().toString(),
                getSelectedRecipients(), getSelectedOrgUnits());
        if(model.recipients.size() < 1 && model.organisations.size() < 1) {
            Toast.makeText(this, "Can only send messages to registered organisation units or users", Toast.LENGTH_LONG).show();
            return null;
        }
        return new Gson().toJson(model);
    }

    private boolean emptyStrings() {
        if (subject.getText().toString().isEmpty() || content.getText().toString().isEmpty() ||
           (recipients.getText().toString().isEmpty() && units.getText().toString().isEmpty()) ) {
                Toast.makeText(NewMessageActivity.this, "Requires some necessary fields", Toast.LENGTH_SHORT).show();
                return true;
        }

        return false;
    }

    private void sendMessage() {
        if (!emptyStrings()) {
            final String message = createJSONMessage();
            if(message != null) {
                new AsyncTask<Integer, String, Integer>() {
                    private ProgressDialog progressDialog;

                    @Override
                    protected void onPreExecute() {
                         super.onPreExecute();
                         progressDialog = new ProgressDialog( (NewMessageActivity.this) );
                         progressDialog.setMessage("Sending message");
                         progressDialog.setIndeterminate(false);
                         progressDialog.setCancelable(false);
                         progressDialog.show();

                    }
                    @Override
                    protected Integer doInBackground(Integer... args) {
                        String api = SharedPrefs.getServerURL(getApplicationContext()) + APIPaths.FIRST_PAGE_MESSAGES;
                        Response response = RESTClient.post(api, SharedPrefs.getCredentials(getApplicationContext()), message, "application/json");
                        return response.getCode();
                    }

                    @Override
                    protected void onPostExecute(Integer code) {
                        progressDialog.dismiss();
                        if (RESTClient.noErrors(code)) {
                            Toast.makeText(NewMessageActivity.this, "Message sent!", Toast.LENGTH_SHORT).show();
                            finish();
                            NewMessageActivity.this.overridePendingTransition(R.anim.left_to_center, R.anim.center_to_right);
                        } else
                        if(recipients.getText().toString().isEmpty()){
                            Toast.makeText(NewMessageActivity.this, "Could not send message - organisation-unit might be empty (no users)!", Toast.LENGTH_LONG).show();
                        }

                        else
                        Toast.makeText(NewMessageActivity.this, "Could not send message!", Toast.LENGTH_LONG).show();
                    }
                }.execute();
            }
        }
    }

    private void getUsers() {
        new AsyncTask<Integer, String, Integer>() {

            @Override
            protected Integer doInBackground(Integer... args) {
                String api = SharedPrefs.getServerURL(getApplicationContext()) + APIPaths.USERS + APIPaths.NAME_AND_ID_FIELDS;

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
                if(RESTClient.noErrors(code))
                    setUserAdapter();
            }
        }.execute();
    }

    private void getOrgUnits() {
        new AsyncTask<Integer, String, Integer>() {

            @Override
            protected Integer doInBackground(Integer... args) {
                String api = SharedPrefs.getServerURL(getApplicationContext()) + APIPaths.ORG_UNITS + APIPaths.NAME_AND_ID_FIELDS;

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
                if(RESTClient.noErrors(code))
                    setOrgUnitsAdapter();
            }
        }.execute();
    }

    //For JSON purposes only
    private class MessageModel {
        @SerializedName("organisationUnits")
        List<NameAndIDModel> organisations;
        @SerializedName("users")
        List<NameAndIDModel> recipients;
        @SerializedName("subject")
        String subject;
        @SerializedName("text")
        String text;

        public MessageModel(String subject, String text, List<NameAndIDModel> recipients, List<NameAndIDModel> organisations) {
            this.organisations =organisations;
            this.recipients = recipients;
            this.text = text;
            this.subject = subject;
        }
    }
}