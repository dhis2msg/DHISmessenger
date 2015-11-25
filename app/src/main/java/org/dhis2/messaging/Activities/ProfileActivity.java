package org.dhis2.messaging.Activities;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.google.gson.Gson;

import org.dhis2.messaging.Models.ProfileModel;
import org.dhis2.messaging.R;
import org.dhis2.messaging.REST.APIPath;
import org.dhis2.messaging.REST.RESTClient;
import org.dhis2.messaging.REST.RESTSessionStorage;
import org.dhis2.messaging.REST.Response;
import org.dhis2.messaging.Utils.Adapters.ProfileAdapter;
import org.dhis2.messaging.Utils.SharedPrefs;
import org.dhis2.messaging.Utils.UserInterface.ToastMaster;
import org.dhis2.messaging.Interfaces.UpdateUnreadMsg;
import org.dhis2.messaging.XMPP.XMPPSessionStorage;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by iNick on 18.10.14.
 */
public class ProfileActivity extends Activity implements UpdateUnreadMsg {
    @Bind(R.id.list)
    ListView list;
    @Bind(R.id.loader)
    ProgressBar loader;

    // data :
    private String userId;
    private AsyncTask profileTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.common_list_view);
        ButterKnife.bind(this);

        Intent intent = getIntent();
        userId = intent.getStringExtra("userId");
        loader.setVisibility(View.VISIBLE);
        fixActionBar(intent.getStringExtra("username"));

        //TODO: vladislav/netcode?: find out what this addresses relate to. Maybe create entry in settings for it.
        if (SharedPrefs.getServerURL(getApplicationContext()).contains("197.243.37.125") || SharedPrefs.getServerURL(getApplicationContext()).contains("10.10.35.207"))
            ((ImageView) findViewById(R.id.rwlogo)).setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                ProfileActivity.this.overridePendingTransition(R.anim.left_to_center, R.anim.center_to_right);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        XMPPSessionStorage.getInstance().setHomeListener(null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        XMPPSessionStorage.getInstance().setHomeListener(this);
        getProfile();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (profileTask != null) {
            if (!profileTask.isCancelled())
                profileTask.cancel(true);
            profileTask = null;
        }
    }

    @Override
    public void updateUnreadMsg(int restNumber, int xmppNumber) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new ToastMaster(getApplicationContext(), "New Chat Message", true);
            }
        });
    }

    private void fixActionBar(String username) {
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle("Back");
        getActionBar().setSubtitle(username);
        actionBar.setDisplayUseLogoEnabled(false);
    }

    private void getProfile() {
        profileTask = new AsyncTask<String, String, Integer>() {
            ProfileModel model = RESTSessionStorage.getInstance().getProfileModel();
            String auth = SharedPrefs.getCredentials(getApplicationContext());
            String api = SharedPrefs.getServerURL(getApplicationContext()) + APIPath.USERS + "/" + userId;

            @Override
            protected Integer doInBackground(String... args) {
                if (model == null) {
                    Response response = RESTClient.get(api, auth);
                    if (RESTClient.noErrors(response.getCode())) {
                        model = new Gson().fromJson(response.getBody(), ProfileModel.class);
                        RESTSessionStorage.getInstance().setProfileModel(model);
                    }
                    return response.getCode();
                } else {
                    return RESTClient.OK;
                }
            }

            @Override
            protected void onPostExecute(final Integer code) {
                loader.setVisibility(View.GONE);
                if (RESTClient.noErrors(code)) {
                    ArrayList<String> components = new ArrayList<String>();
                    if (model.getFirstName() != null && !model.getFirstName().isEmpty()) {
                        components.add("First name:" + model.getFirstName());
                    }
                    if (model.getSurname() != null && !model.getSurname().isEmpty()) {
                        components.add("Surname:" + model.getSurname());
                    }
                    if (model.getBirthday() != null && !model.getBirthday().isEmpty()) {
                        components.add("Birthday:" + model.getBirthday());
                    }
                    if (model.getNationality() != null && !model.getNationality().isEmpty()) {
                        components.add("Nationality:" + model.getNationality());
                    }
                    if (model.getEmail() != null && !model.getEmail().isEmpty()) {
                        components.add("Email:" + model.getEmail());
                    }
                    if (model.getPhoneNumber() != null && !model.getPhoneNumber().isEmpty()) {
                        components.add("Phone:" + model.getPhoneNumber());
                    }
                    if (model.getEducation() != null && !model.getEducation().isEmpty()) {
                        components.add("Education:" + model.getEducation());
                    }
                    if (model.getEmployer() != null && !model.getNationality().isEmpty()) {
                        components.add("Employer:" + model.getEmployer());
                    }
                    if (model.getJobTitle() != null && !model.getJobTitle().isEmpty()) {
                        components.add("Job Title:" + model.getJobTitle());
                    }
                    if (model.getGender() != null && !model.getGender().isEmpty()) {
                        if (model.getGender().equals("gender_male")) {
                            components.add("Gender:" + "Male");
                        } else {
                            components.add("Gender:" + "Female");
                        }
                    }
                    if (model.getInterests() != null && !model.getInterests().isEmpty()) {
                        components.add("Interests:" + model.getInterests());
                    }
                    if (model.getLanguages() != null && !model.getLanguages().isEmpty()) {
                        components.add("Languages:" + model.getLanguages());
                    }
                    ProfileAdapter adapter = new ProfileAdapter(getApplicationContext(), R.layout.item_profile, components);
                    list.setAdapter(adapter);
                } else {
                    new ToastMaster(getApplicationContext(), "Something went wrong:\n" + RESTClient.getErrorMessage(code), false);
                }
            }
        }.execute();
    }
}