package org.dhis2.messaging.Fragments;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.dhis2.messaging.Models.ProfileModel;
import org.dhis2.messaging.Utils.*;
import org.dhis2.messaging.R;
import org.dhis2.messaging.Utils.REST.APIPaths;
import org.dhis2.messaging.Utils.REST.RESTClient;
import org.dhis2.messaging.Utils.REST.Response;

import android.support.v4.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

public class MyProfileFragment extends Fragment {
    private EditText phone, firstname, surname, email, jobTitle,
            birthday, nationality, education, employer, interests, languages;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        View view = inflater.inflate(R.layout.fragment_my_profile, container, false);
        phone = (EditText) view.findViewById(R.id.getPhoneNumber);
        firstname = (EditText) view.findViewById(R.id.getFirstName);
        surname = (EditText) view.findViewById(R.id.getSurname);
        email = (EditText) view.findViewById(R.id.getEmail);
        jobTitle = (EditText) view.findViewById(R.id.getJobTitle);
        birthday = (EditText) view.findViewById(R.id.getBirthday);
        nationality = (EditText) view.findViewById(R.id.getNationality);
        education = (EditText) view.findViewById(R.id.getEducation);
        employer = (EditText) view.findViewById(R.id.getEmployer);
        interests = (EditText) view.findViewById(R.id.getInterests);
        languages = (EditText) view.findViewById(R.id.getLanguages);

        getProfile();
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_save, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save: {
                saveProfile();
                return true;
            }
        }
        return true;
    }

    private void saveProfile() {
        final ProfileModel newModel = new ProfileModel();
        newModel.setId(SharedPrefs.getUserId(getActivity()));
        newModel.setFirstName(firstname.getText().toString());
        newModel.setSurname(surname.getText().toString());
        newModel.setEmail(email.getText().toString());
        newModel.setPhoneNumber(phone.getText().toString());
        newModel.setJobTitle(jobTitle.getText().toString());
        newModel.setBirthday(birthday.getText().toString());
        newModel.setNationality(nationality.getText().toString());
        newModel.setEducation(education.getText().toString());
        newModel.setEmployer(employer.getText().toString());
        newModel.setInterests(interests.getText().toString());
        newModel.setLanguages(languages.getText().toString());

        new AsyncTask<String, String, Integer>() {
            String auth = SharedPrefs.getCredentials(getActivity());
            String api = SharedPrefs.getServerURL(getActivity()) + APIPaths.USER_INFO + "/user-account";

            @Override
            protected Integer doInBackground(String... args) {
                Gson gson = new GsonBuilder().create();
                String s = gson.toJson(newModel);
                Response response = RESTClient.post(api, auth, s, "application/json");
                return response.getCode();
            }

            @Override
            protected void onPostExecute(final Integer code) {
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        if (RESTClient.noErrors(code)) {
                            Toast.makeText(getActivity(), "Profile Saved!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getActivity(), "Could not save new profile information", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }.execute();
    }

    private void getProfile() {
        new AsyncTask<String, String, Integer>() {
            ProfileModel model = null;
            String auth = SharedPrefs.getCredentials(getActivity());
            String api = SharedPrefs.getServerURL(getActivity()) + APIPaths.USER_INFO;

            @Override
            protected Integer doInBackground(String... args) {
                Response response = RESTClient.get(api, auth);

                if (RESTClient.noErrors(response.getCode()))
                    model = new Gson().fromJson(response.getBody(), ProfileModel.class);

                return response.getCode();
            }

            @Override
            protected void onPostExecute(final Integer code) {
               if(getActivity() != null) {
                   getActivity().runOnUiThread(new Runnable() {
                       public void run() {
                           if (RESTClient.noErrors(code)) {
                               firstname.setText(model.getFirstName());
                               surname.setText(model.getSurname());
                               email.setText(model.getEmail());
                               phone.setText(model.getPhoneNumber());
                               jobTitle.setText(model.getJobTitle());
                               birthday.setText(model.getBirthday());
                               nationality.setText(model.getNationality());
                               education.setText(model.getEducation());
                               employer.setText(model.getEmployer());
                               interests.setText(model.getInterests());
                               languages.setText(model.getLanguages());
                           } else
                               Toast.makeText(getActivity(), "Error - " + RESTClient.getErrorMessage(code), Toast.LENGTH_SHORT).show();
                       }
                   });
               }
            }
        }.execute();
    }
}
