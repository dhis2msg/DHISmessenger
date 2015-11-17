package org.dhis2.messaging.Utils.Google.CloudMesaaging;

import android.content.Context;
import android.os.AsyncTask;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.dhis2.messaging.Activities.HomeActivity;
import org.dhis2.messaging.REST.APIPath;
import org.dhis2.messaging.REST.RESTClient;
import org.dhis2.messaging.REST.Response;
import org.dhis2.messaging.Utils.SharedPrefs;
import org.dhis2.messaging.Utils.UserInterface.ToastMaster;

import java.io.IOException;

/**
 * Created by iNick on 04.10.14.
 */
public class RegisterDevice {
    private GoogleCloudMessaging gcm;
    private Context context;
    private String regid;

    public RegisterDevice(Context context) {
        this.context = context;
        regid = "";
    }

    public void checkRegistration() {
        gcm = GoogleCloudMessaging.getInstance(context);
        regid = getRegistrationId();

        if (regid.equals("")) {
            registerInBackground();
        }
    }

    public boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(context);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, (HomeActivity) context, 0).show();
            } else {
                new ToastMaster(context, "This device does not support GCM (Notificatons trough google)", false);
            }
            return false;
        }
        return true;
    }

    public String getRegistrationId() {
        if (!regid.equals("")) {
            return regid;
        }
        String registrationId = SharedPrefs.getGCMRegistrationId(context);
        return registrationId;
    }

    private void registerInBackground() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(context);
                    }
                    regid = gcm.register(APIPath.GCMID);
                    msg = "Device registered, registration ID=" + regid;

                    sendRegistrationIdToBackend();

                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                }
                return msg;
            }
        }.execute(null, null, null);
    }

    private void sendRegistrationIdToBackend() {
        new AsyncTask<Integer, String, Integer>() {
            protected Integer doInBackground(Integer... args) {
                String server = SharedPrefs.getServerURL(context);
                String api = server + APIPath.USER_INFO + APIPath.ADD_GCM_ID + regid;
                String auth = SharedPrefs.getCredentials(context);
                Response response = RESTClient.post(api, auth, "", "application/json");
                return response.getCode();
            }

            protected void onPostExecute(final Integer code) {
                if (RESTClient.noErrors(code)) {
                    SharedPrefs.setGCMData(context, regid);
                } else {
                    new ToastMaster(context, "Notifications not working" + RESTClient.getErrorMessage(code), false);
                    SharedPrefs.eraseGCM(context);
                }
            }
        }.execute();
    }

    public void removeGcmId() {
        final String server = SharedPrefs.getServerURL(context);
        final String api = server + APIPath.USER_INFO + APIPath.REMOVE_GCM_ID + SharedPrefs.getGCMRegistrationId(context);
        final String auth = SharedPrefs.getCredentials(context);
        new AsyncTask<Integer, String, Integer>() {
            protected Integer doInBackground(Integer... args) {
                String s = "";
                Response response = RESTClient.post(api, auth, s, "application/json");
                return response.getCode();
            }

            protected void onPostExecute(final Integer code) {
                if (RESTClient.noErrors(code)) {
                    if (!SharedPrefs.getGCMRegistrationId(context).equals("")) {
                        SharedPrefs.eraseGCM(context);
                    }
                }
            }
        }.execute();
    }
}