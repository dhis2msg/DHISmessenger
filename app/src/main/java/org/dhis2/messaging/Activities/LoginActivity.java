package org.dhis2.messaging.Activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import org.dhis2.messaging.R;
import org.dhis2.messaging.REST.RESTClient;
import org.dhis2.messaging.Utils.Adapters.AutoCompleteCharSearchAdapter;
import org.dhis2.messaging.Utils.AsyncroniousTasks.RESTLogin;
import org.dhis2.messaging.Utils.UserInterface.ToastMaster;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LoginActivity extends Activity {
    //Remember last login
    public static final String PREFS_NAME = "CredidentalsFile";
    private static final String PREF_SERVER = "server";
    private static final String PREF_USERNAME = "username";
    @Bind(R.id.usernameInput)
    EditText username;
    @Bind(R.id.passwordInput)
    EditText password;
    @Bind(R.id.signinButton)
    Button signin;
    @Bind(R.id.about)
    ImageView about;
    @Bind(R.id.serverInput)
    AutoCompleteTextView server;
    //Memory store
    private AsyncTask loginHandler;

    @SuppressWarnings("unused")
    @OnClick(R.id.about)
    public void clickedAbout() {
        alert("Info", "- Be sure the DHIS url is the same as in the browser\n\n" +
                "- Requires at least DHIS version 2.17 \n\n" +
                "- DHIS chat requires a Open Fire server with a DHIS plugin\n\n" +
                "- Notifications requires a custom GCM implementation on the DHIS server");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        //Imports previous log tempList credits
        SharedPreferences pref = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String srv = pref.getString(PREF_SERVER, null);
        String usr = pref.getString(PREF_USERNAME, null);
        if (server != null && usr != null) {
            server.setText(srv);
            username.setText(usr);
            password.requestFocus();
        }

        setSuggestionServers();
        checkGooglePlay();

        final TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void afterTextChanged(Editable edit) {
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }

            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                enableLogin();
            }
        };
        server.addTextChangedListener(textWatcher);
        username.addTextChangedListener(textWatcher);
        password.addTextChangedListener(textWatcher);
        signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (loginHandler != null)
            if (!loginHandler.isCancelled())
                loginHandler.cancel(true);
        loginHandler = null;
        finish();
    }

    private void login() {
        if (!RESTClient.isDeviceConnectedToInternet(this)) {
            alert("No internett connection", "Try again when you have a connection..");
        } else if (!URLUtil.isValidUrl(server.getText().toString())) {
            alert("Wrong URL", "Something is wrong with your server url..");
        } else {
            String[] params = new String[4];
            params[0] = server.getText().toString();
            params[1] = username.getText().toString();
            params[2] = password.getText().toString();
            if (autoLogin())
                params[3] = "indicator autologin";

            loginHandler = new RESTLogin(this).execute(params);
        }
    }

    private void checkGooglePlay() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this, 0).show();
                //Toast.makeText(this, "Notification system not wokring, make sure you have the latest version of", Toast.LENGTH_LONG);
            } else {
                new ToastMaster(getApplicationContext(), "This device does not support GCM (Notifications trough Google Cloud Messaging)", false);
                //Toast.makeText(this, "This device does not support GCM (Notifications trough Google Cloud Messaging)", Toast.LENGTH_LONG);
            }
        }
    }

    private void setSuggestionServers() {
        int[] icons = new int[]{R.drawable.united_nations, R.drawable.uganda, R.drawable.malawi,
                R.drawable.ghana, R.drawable.united_nations, R.drawable.rwanda, R.drawable.rwanda, R.drawable.burkina_faso,
                R.drawable.liberia, R.drawable.tanzania, R.drawable.gambia, R.drawable.zambia, R.drawable.zimbabwe};
        String[] urls = getResources().getStringArray(R.array.server_array);
        List<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
        for (int i = 0; i < icons.length; i++) {
            HashMap<String, String> hm = new HashMap<String, String>();
            hm.put("titles", urls[i]);
            hm.put("icons", Integer.toString(icons[i]));
            list.add(hm);
        }
        String[] key = {"icons", "titles"};
        int[] id = {R.id.listIcon, R.id.listTitle};
        AutoCompleteCharSearchAdapter adapter = new AutoCompleteCharSearchAdapter(this, list, R.layout.item_suggestions, key, id);

        server.setAdapter(adapter);
        server.setThreshold(1);

    }

    private void enableLogin() {
        String serv = server.getText().toString();
        String user = username.getText().toString();
        String pass = password.getText().toString();

        if (serv.equals("") || user.equals("") || pass.equals("")) {
            signin.setEnabled(false);
        } else {
            signin.setEnabled(true);
        }
    }

    private boolean autoLogin() {
        return true;
    }

    public void alert(String header, String text) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(header);
        builder.setMessage(text);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}//End class LoginView




