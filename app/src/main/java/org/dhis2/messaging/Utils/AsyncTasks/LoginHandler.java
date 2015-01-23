package org.dhis2.messaging.Utils.AsyncTasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Base64;

import org.dhis2.messaging.HomeActivity;
import org.dhis2.messaging.IntroActivity;
import org.dhis2.messaging.LoginActivity;
import org.dhis2.messaging.Utils.*;
import org.dhis2.messaging.Utils.REST.APIPaths;
import org.dhis2.messaging.Utils.REST.RESTClient;
import org.dhis2.messaging.Utils.REST.Response;
import org.dhis2.messaging.Utils.XMPP.XMPPClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by iNick on 23.09.14.
 */
	/*
	 * Class: Login
	 * Description: An asynchronous class to sign tempList the user with a background process
	 */
public class LoginHandler extends AsyncTask<String, String, Integer>
{
    //Creates a progress dialog (spinner with text)
    private ProgressDialog progressDialog;
    private Context context;
    public LoginHandler(Context context)
    {
        this.context =  context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if(context instanceof LoginActivity) {
            progressDialog = new ProgressDialog(context);
            progressDialog.setMessage("Signing in");
            progressDialog.setIndeterminate(false);
            progressDialog.setCancelable(false);
            progressDialog.show();
        }
    }

    //Calls for sign tempList method tempList the ConectionManager class
    protected Integer doInBackground(String... args) {
        String formatServer = args[0];
        String formatCredentials = String.format("%s:%s", args[1], args[2]);
        String server = formatServer + (formatServer.endsWith("/") ? "" : "/");
        String credentials = Base64.encodeToString(formatCredentials.getBytes(), Base64.NO_WRAP);

        String api = server + APIPaths.USER_INFO;
        Response response = RESTClient.get(api, credentials);
        String id = "";

        if(RESTClient.noErrors(response.getCode()) ) {
            try {
                JSONObject userinfo = new JSONObject(response.getBody());
                id = userinfo.getString("id");
                //Stores context, credentials, username, userid and server
                SharedPrefs.setSessionData(context,
                                           credentials,
                                           args[1],
                                           id,
                                           server);
                SharedPreferences session = context.getSharedPreferences(LoginActivity.PREFS_NAME, context.MODE_PRIVATE);
                session.edit()
                        .putString("server", args[0])
                        .putString("username", args[1]).commit();

                if(args.length > 3)
                    session.edit().putString("password", args[2]).commit();



                URL tempURL= new URL(server);
                String domain = tempURL.getHost();
                String port = "5222";
                XMPPClient.getInstance().setConnection(context,domain,port,args[1],args[2]);
            }
            catch (JSONException e) {
                e.printStackTrace();
                return -1;
            }catch (MalformedURLException e){
                e.printStackTrace();
            }
        }
        return response.getCode();
    }



    protected void onPostExecute(final Integer code) {

        if(context instanceof LoginActivity) {
            if (RESTClient.noErrors(code)) {
                Intent intent = new Intent(context, HomeActivity.class);
                context.startActivity(intent);
                ((LoginActivity) context).finish();

            }
            else
                ((LoginActivity) context).alert("Error", RESTClient.getErrorMessage(code));
            progressDialog.dismiss();
        }
        else{
            if (RESTClient.noErrors(code)) {
                Intent intent = new Intent(context, HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                ((IntroActivity)context).finish();

            }
            else {
                Intent intent = new Intent(context, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                ((IntroActivity)context).finish();
            }
        }
    }
}//End class DhisLogin AsyncTask