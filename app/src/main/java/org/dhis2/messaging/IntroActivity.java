package org.dhis2.messaging;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.TextView;

import org.dhis2.messaging.Utils.AsyncTasks.LoginHandler;
import org.dhis2.messaging.Utils.REST.RESTClient;

/**
 * Created by iNick on 26.09.14.
 */
public class IntroActivity extends Activity {
    public static final String PREFS_NAME = "CredidentalsFile";
    private static final String PREF_SERVER = "server";
    private static final String PREF_USERNAME = "username";
    private static final String PREF_PASSWORD = "password";

    @Override
    protected void onCreate(Bundle intro) {
        super.onCreate(intro);
        setContentView(R.layout.activity_intro);

        SharedPreferences pref = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        final String srv = pref.getString(PREF_SERVER, null);
        final String usr = pref.getString(PREF_USERNAME, null);
        final String psw = pref.getString(PREF_PASSWORD, null);
        TextView text = (TextView) findViewById(R.id.progress_text);

        Thread tid = new Thread() {
            public void run() {
                try {
                    sleep(2500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    Intent intent = new Intent(getApplication(), LoginActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        };

        if (psw != null) {
            if(!RESTClient.isDeviceConnectedToInternett(this)){
                Intent intent = new Intent(getApplication(), LoginActivity.class);
                startActivity(intent);
                finish();
            }
            if(!URLUtil.isValidUrl(srv.toString())) {
                Intent intent = new Intent(getApplication(), LoginActivity.class);
                startActivity(intent);
                finish();
            }
            else{
                text.setVisibility(View.VISIBLE);
                new LoginHandler(this).execute(srv, usr, psw);
            }
        } else
            tid.start();
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        finish();
    }
}

