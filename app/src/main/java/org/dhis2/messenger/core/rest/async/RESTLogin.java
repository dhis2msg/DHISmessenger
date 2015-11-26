package org.dhis2.messenger.core.rest.async;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Base64;

import org.dhis2.messenger.gui.activity.HomeActivity;
import org.dhis2.messenger.gui.activity.IntroActivity;
import org.dhis2.messenger.gui.activity.LoginActivity;
import org.dhis2.messenger.core.rest.APIPath;
import org.dhis2.messenger.core.rest.RESTClient;
import org.dhis2.messenger.core.rest.Response;
import org.dhis2.messenger.SharedPrefs;
import org.dhis2.messenger.core.xmpp.XMPPClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;

public class RESTLogin extends AsyncTask<String, String, Integer> {

    private ProgressDialog progressDialog;
    private Context context;

    public RESTLogin(Context context) {
        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (context instanceof LoginActivity) {
            progressDialog = new ProgressDialog(context);
            progressDialog.setMessage("Signing in");
            progressDialog.setIndeterminate(false);
            progressDialog.setCancelable(false);
            progressDialog.show();
        }
    }

    protected Integer doInBackground(String... args) {
        String formatServer = args[0];
        String username = args[1];
        String password = args[2];

        String formatCredentials = String.format("%s:%s", username, password);
        String server = formatServer + (formatServer.endsWith("/") ? "" : "/");
        String credentials = Base64.encodeToString(formatCredentials.getBytes(), Base64.NO_WRAP);
        String api = server + APIPath.USER_INFO;
        Response response = RESTClient.get(api, credentials);

        if (RESTClient.noErrors(response.getCode())) {
            try {
                JSONObject userinfo = new JSONObject(response.getBody());
                String id = userinfo.getString("id");

                if (SharedPrefs.getUserId(context) != id) {
                    if (!SharedPrefs.getGCMRegistrationId(context).equals("")) {
                        Response r = RESTClient.post(api + APIPath.REMOVE_GCM_ID +
                                SharedPrefs.getGCMRegistrationId(context), credentials, "", "application/json");
                        if (RESTClient.noErrors(r.getCode())) {
                            SharedPrefs.eraseGCM(context);
                        }
                    }
                }

                SharedPrefs.setSessionData(context, credentials, username, id, server);
                SharedPreferences session = context.getSharedPreferences(LoginActivity.PREFS_NAME, context.MODE_PRIVATE);
                session.edit()
                        .putString("server", formatServer)
                        .putString("username", username).commit();

                if (args.length > 3) {
                    session.edit().putString("password", password).commit();
                }

                URL tempURL = new URL(server);
                String domain = tempURL.getHost();
                String port = "5222";

                XMPPClient.getInstance().setConnection(context, domain, port, username, password);
            } catch (JSONException e) {
                e.printStackTrace();
                return RESTClient.JSON_EXCEPTION;
            } catch (MalformedURLException e) {
                e.printStackTrace();
                return RESTClient.MALFORMED_URL_EXCEPTION;
            }
        }
        return response.getCode();
    }

    protected void onPostExecute(final Integer code) {
        if (context instanceof LoginActivity) {
            removeDialogSafely();
            if (RESTClient.noErrors(code)) {
                Intent intent = new Intent(context, HomeActivity.class);
                context.startActivity(intent);
                ((LoginActivity) context).finish();

            } else {
                ((LoginActivity) context).alert("Error", RESTClient.getErrorMessage(code));
            }
        } else {
            if (RESTClient.noErrors(code)) {
                Intent intent = new Intent(context, HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                ((IntroActivity) context).finish();
            } else {
                Intent intent = new Intent(context, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                ((IntroActivity) context).finish();
            }
        }
    }

    protected void removeDialogSafely() {
        try {
            if ((this.progressDialog != null) && this.progressDialog.isShowing()) {
                this.progressDialog.dismiss();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            this.progressDialog = null;
        }
    }

}
