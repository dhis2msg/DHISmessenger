package org.dhis2.messaging;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.dhis2.messaging.Utils.Adapters.ProfileAdapter;
import org.dhis2.messaging.Models.ProfileModel;
import org.dhis2.messaging.Utils.REST.APIPaths;
import org.dhis2.messaging.Utils.REST.RESTClient;
import org.dhis2.messaging.Utils.REST.Response;
import org.dhis2.messaging.Utils.SwipeListener;
import org.dhis2.messaging.Utils.SharedPrefs;

import java.util.ArrayList;

/**
 * Created by iNick on 18.10.14.
 */
public class ProfileActivity extends Activity {
    private ListView list;
    private String userid, username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.common_list_view);
        list = (ListView) findViewById(R.id.list);
        Intent intent = getIntent();
        userid = intent.getStringExtra("userid");
        username = intent.getStringExtra("username");
        fixActionBar();

        list.setOnTouchListener(new SwipeListener(this) {
            public void onSwipeRight() {
                finish();
                ProfileActivity.this.overridePendingTransition(R.anim.left_to_center, R.anim.center_to_right);
            }
        });
        getProfile();
    }

    public void fixActionBar(){
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle("Back");
        getActionBar().setSubtitle(username);
        actionBar.setDisplayUseLogoEnabled(false);
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
    private void getProfile() {
        new AsyncTask<String, String, Integer>() {
            ProfileModel model = null;
            String auth = SharedPrefs.getCredentials(getApplicationContext());
            String api = SharedPrefs.getServerURL(getApplicationContext()) + APIPaths.USERS + "/" +userid;

            @Override
            protected Integer doInBackground(String... args) {
                Response response = RESTClient.get(api, auth);
                if (RESTClient.noErrors(response.getCode()))
                    model = new Gson().fromJson(response.getBody(), ProfileModel.class);

                return response.getCode();
            }

            @Override
            protected void onPostExecute(final Integer code) {
                if (RESTClient.noErrors(code)) {
                    ArrayList<String> components = new ArrayList<String>();
                    components.add("First name:" + model.getFirstName());
                    components.add("Surname:" + model.getSurname());
                    components.add("Birthday:" + model.getBirthday());
                    components.add("Nationality:" + model.getNationality());
                    components.add("Education:" + model.getEducation());
                    components.add("Employer:" + model.getEmployer());
                    components.add("Job Title:" + model.getJobTitle());
                    components.add("Gender:" + model.getGender());
                    components.add("Interests:" + model.getInterests());
                    components.add("Languages:" + model.getLanguages());
                    components.add("Email:" + model.getEmail());
                    components.add("Phone:" + model.getPhoneNumber());
                    ProfileAdapter adapter = new ProfileAdapter(getApplicationContext(), R.layout.item_profile, components);
                    list.setAdapter(adapter);
                } else {
                    Toast.makeText(ProfileActivity.this, "Error:" + RESTClient.getErrorMessage(code), Toast.LENGTH_SHORT).show();
                }
            }
        }.execute();
    }
}
