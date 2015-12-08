package org.dhis2.messenger.gui.fragment;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.transition.Fade;
import android.transition.Slide;
import android.transition.TransitionSet;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.dhis2.messenger.model.ProfileModel;
import org.dhis2.messenger.R;
import org.dhis2.messenger.core.rest.APIPath;
import org.dhis2.messenger.core.rest.RESTClient;
import org.dhis2.messenger.core.rest.RESTSessionStorage;
import org.dhis2.messenger.core.rest.Response;
import org.dhis2.messenger.SharedPrefs;
import org.dhis2.messenger.gui.ToastMaster;

public class MyProfileFragment extends Fragment {
    private EditText phone, firstname, surname, email, jobTitle,
            nationality, education, employer, interests, languages;
    private TextView editText;
    private RelativeLayout layout;
    private DatePicker birthday;
    private ProgressBar loader;

    //Memory store
    private AsyncTask asyncTask, saveTask;

    public MyProfileFragment(){
        super();
        //Makes a phone with api 21 and higher use Slide and fade transitions
        if(Build.VERSION.SDK_INT >= 21) {
            Slide slide = new Slide();
            slide.setDuration(500);
            Fade fade = new Fade();
            fade.setDuration(500);
            TransitionSet transitionSet = new TransitionSet();
            transitionSet.addTransition(slide);
            transitionSet.addTransition(fade);
            setEnterTransition(transitionSet);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        View view = inflater.inflate(R.layout.fragment_my_profile, container, false);
        loader = (ProgressBar) view.findViewById(R.id.loader);
        phone = (EditText) view.findViewById(R.id.getPhoneNumber);
        firstname = (EditText) view.findViewById(R.id.getFirstName);
        surname = (EditText) view.findViewById(R.id.getSurname);
        email = (EditText) view.findViewById(R.id.getEmail);
        jobTitle = (EditText) view.findViewById(R.id.getJobTitle);
        birthday = (DatePicker) view.findViewById(R.id.getBirthday);
        nationality = (EditText) view.findViewById(R.id.getNationality);
        education = (EditText) view.findViewById(R.id.getEducation);
        employer = (EditText) view.findViewById(R.id.getEmployer);
        interests = (EditText) view.findViewById(R.id.getInterests);
        languages = (EditText) view.findViewById(R.id.getLanguages);
        editText = (TextView) view.findViewById(R.id.editBirthday);
        layout = (RelativeLayout) view.findViewById(R.id.someLayout);

        editText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                birthday.setVisibility(View.VISIBLE);
                editText.setVisibility(View.GONE);
            }
        });

        if (!RESTClient.isDeviceConnectedToInternet(getActivity())) {
            layout.setVisibility(View.GONE);
            new ToastMaster(getActivity(), "No internet connection", false);
        } else {
            getProfile();
        }
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
                if (RESTClient.isDeviceConnectedToInternet(getActivity()))
                    saveProfile();
                else
                    new ToastMaster(getActivity(), "No internet connection", false);
                return true;
            }
        }
        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (asyncTask != null)
            if (!asyncTask.isCancelled()) {
                asyncTask.cancel(true);
                asyncTask = null;
            }
        if (saveTask != null) {
            if (!saveTask.isCancelled())
                saveTask.cancel(true);
            saveTask = null;
        }
    }

    private void saveProfile() {
        //TODO: Vladislav: update local storage of profile. similiar to getProfile. to work with the cache.
        final ProfileModel newModel = new ProfileModel();
        newModel.setId(SharedPrefs.getUserId(getActivity()));
        newModel.setFirstName(firstname.getText().toString());
        newModel.setSurname(surname.getText().toString());
        newModel.setEmail(email.getText().toString());
        newModel.setPhoneNumber(phone.getText().toString());
        newModel.setJobTitle(jobTitle.getText().toString());
        newModel.setNationality(nationality.getText().toString());
        newModel.setEducation(education.getText().toString());
        newModel.setEmployer(employer.getText().toString());
        newModel.setInterests(interests.getText().toString());
        newModel.setLanguages(languages.getText().toString());

        if (editText.getVisibility() == View.GONE) {
            String date = birthday.getYear() + "-" + (birthday.getMonth() + 1) + "-" + (birthday.getDayOfMonth());
            newModel.setBirthday(date);
        }

        saveTask = new AsyncTask<String, String, Integer>() {
            String auth = SharedPrefs.getCredentials(getActivity());
            String api = SharedPrefs.getServerURL(getActivity()) + APIPath.USER_INFO + "/user-account";

            @Override
            protected Integer doInBackground(String... args) {
                Gson gson = new GsonBuilder().create();
                String s = gson.toJson(newModel);
                Response response = RESTClient.post(api, auth, s, "application/json");
                return response.getCode();
            }

            @Override
            protected void onPostExecute(final Integer code) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        public void run() {
                            if (RESTClient.noErrors(code)) {
                                // save to cache the successful model update here:
                                RESTSessionStorage.getInstance().setProfileModel(newModel);
                                new ToastMaster(getActivity(), "Profile Saved!", false);
                            } else {
                                new ToastMaster(getActivity(), "Not saved!\n" + RESTClient.getErrorMessage(code), false);
                            }
                        }
                    });
                }
            }
        }.execute();
    }

    private void getProfile() {
        asyncTask = new AsyncTask<String, String, Integer>() {
            // Modified to check if RESTSessionStorage has the model
            ProfileModel model = RESTSessionStorage.getInstance().getProfileModel();
            Boolean getModelFromCache = true;
            String auth = null;
            String api = null;

            @Override
            protected Integer doInBackground(String... args) {
                if (model == null) {
                    getModelFromCache = false;
                    auth = SharedPrefs.getCredentials(getActivity());
                    api = SharedPrefs.getServerURL(getActivity()) + APIPath.USER_INFO;

                    Response response = RESTClient.get(api, auth);
                    if (RESTClient.noErrors(response.getCode())) {
                        model = new Gson().fromJson(response.getBody(), ProfileModel.class);
                        RESTSessionStorage.getInstance().setProfileModel(model);
                    }
                    return response.getCode();
                } else {
                    getModelFromCache = true;
                    return null;
                }
            }

            @Override
            protected void onPostExecute(final Integer code) {
                loader.setVisibility(View.GONE);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        public void run() {
                            //If the model is from cache skip error checks.
                            if (getModelFromCache || RESTClient.noErrors(code)) {
                                firstname.setText(model.getFirstName());
                                surname.setText(model.getSurname());
                                email.setText(model.getEmail());
                                phone.setText(model.getPhoneNumber());
                                jobTitle.setText(model.getJobTitle());
                                //birthday.setText(model.getBirthday());
                                if (model.getBirthday() != null) {
                                    birthday.updateDate(Integer.parseInt(model.getBirthday().substring(0, 4)),
                                            Integer.parseInt(model.getBirthday().substring(5, 7)) - 1,
                                            Integer.parseInt(model.getBirthday().substring(8, 10)) + 1);
                                } else {
                                    editText.setText("Click to set your birthday");
                                }
                                nationality.setText(model.getNationality());
                                education.setText(model.getEducation());
                                employer.setText(model.getEmployer());
                                interests.setText(model.getInterests());
                                languages.setText(model.getLanguages());
                                // Demo cache use using toast notifications :
                                /*if (getModelFromCache) {
                                    new ToastMaster(getActivity(), "Model from cache!", false);
                                } else {
                                    new ToastMaster(getActivity(), "Did not get model from cache!", false);
                                } */
                            } else {
                                new ToastMaster(getActivity(), "Error - " + RESTClient.getErrorMessage(code), false);
                            }
                            layout.setVisibility(View.VISIBLE);
                        }
                    });
                }
            }
        }.execute();
    }
}
