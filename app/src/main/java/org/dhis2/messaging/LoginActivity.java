package org.dhis2.messaging;

import android.app.ActionBar;
import android.text.AndroidCharacter;
import android.text.Editable;
import android.text.TextWatcher;
import android.webkit.URLUtil;
import android.widget.*;

import org.dhis2.messaging.Utils.Adapters.AutoCompleteCharSearchAdapter;
import org.dhis2.messaging.Utils.AsyncTasks.LoginHandler;
import org.dhis2.messaging.Utils.REST.RESTClient;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class LoginActivity extends Activity{
	//View items
	private EditText username, password;
	private Button signin;
    private ToggleButton autoLogin;
	private AutoCompleteTextView server;
	
	//Remember last login
	public static final String PREFS_NAME = "CredidentalsFile";
    private static final String PREF_SERVER = "server";
	private static final String PREF_USERNAME = "username";
	
	//private AsyncTask thread;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        //Creating custom action bar
        View custom = getLayoutInflater().inflate(R.layout.actionbar_login, null);
        getActionBar().setCustomView(custom);
        getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		setContentView(R.layout.activity_login);

		server = (AutoCompleteTextView) findViewById(R.id.serverInput);
		username = (EditText) findViewById(R.id.usernameInput);
		password = (EditText) findViewById(R.id.passwordInput);
		signin = (Button) findViewById(R.id.signinButton);
        autoLogin = (ToggleButton) findViewById(R.id.switchOnOff);
		
		//Imports previous log tempList credits
		SharedPreferences pref = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String srv = pref.getString(PREF_SERVER, null);
		String usr = pref.getString(PREF_USERNAME, null);
		if (server != null && usr != null ){
		    server.setText(srv);
            username.setText(usr);
            password.requestFocus();
		}

		setSuggestionServers();

        //ENABLES THE LOGIN BUTTON WHEN ALL FILEDS CONTAIN TEXT
        final TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void afterTextChanged(Editable edit) { }
            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) { }
            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                enableLogin();
            }
        };
        server.addTextChangedListener(textWatcher);
        username.addTextChangedListener(textWatcher);
        password.addTextChangedListener(textWatcher);

        //STARTS THE PROCESS OF LOGGING IN THE USER
        signin.setOnClickListener( new View.OnClickListener(){
            @Override
            public void onClick(View v){
                login();
            }
        });
	}

    //SETS SUGGESTED SERVER URL'S
    private void setSuggestionServers()
    {
        int[] icons = new int[]{R.drawable.united_nations, R.drawable.uganda, R.drawable.malawi,
                R.drawable.ghana, R.drawable.united_nations, R.drawable.rwanda, R.drawable.burkina_faso,
                R.drawable.liberia, R.drawable.tanzania, R.drawable.gambia, R.drawable.zambia, R.drawable.zimbabwe };
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
        AutoCompleteCharSearchAdapter adapter  = new AutoCompleteCharSearchAdapter(this,list,R.layout.item_suggestions,key,id);

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

	public void alert(String header, String text)
	{
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

    public boolean autoLogin()
    {
        return autoLogin.isChecked();
    }

    private void login(){
        if(!RESTClient.isDeviceConnectedToInternett(this)){
            alert("No internett connection", "Try again when you have a connection..");
        }
        else if(!URLUtil.isValidUrl(server.getText().toString())) {
            alert("Wrong URL", "Something is wrong with your server url..");
        }
        else{
            String[] params = new String[4];
            params[0] = server.getText().toString();
            params[1] = username.getText().toString();
            params[2] = password.getText().toString();
            if(autoLogin())
                params[3] = "indicator autologin";
           // thread = new LoginHandler(this).execute(params);
            new LoginHandler(this).execute(params);
        }
    }
}//End class LoginView
	

	

