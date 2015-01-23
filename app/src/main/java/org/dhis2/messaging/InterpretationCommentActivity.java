package org.dhis2.messaging;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.dhis2.messaging.Models.NameAndIDModel;
import org.dhis2.messaging.Utils.Adapters.ChatAdapter;
import org.dhis2.messaging.Models.ChatModel;
import org.dhis2.messaging.Utils.REST.APIPaths;
import org.dhis2.messaging.Utils.REST.RESTClient;
import org.dhis2.messaging.Utils.REST.Response;
import org.dhis2.messaging.Utils.SharedPrefs;
import org.dhis2.messaging.Utils.SwipeListener;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by iNick on 14.10.14.
 */
public class InterpretationCommentActivity extends Activity {
    private ListView listView;
    private EditText newMessage;
    private Button sendBtn;
    public ProgressBar progressBar, contentLoader;
    private String id, title;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rest_conversation);
        Intent i = getIntent();
        id = i.getStringExtra("id");
        title = i.getStringExtra("subject");
        listView = (ListView) findViewById(R.id.chatList);
        newMessage = (EditText) findViewById(R.id.answerMessage);
        sendBtn = (Button) findViewById(R.id.btnSend);
        progressBar = (ProgressBar) findViewById(R.id.loader);
        contentLoader = (ProgressBar) findViewById(R.id.contentLoader);
        fixActionBar();
        new FullRESTConversation().execute(id);

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (newMessage.getText().toString().trim().length() > 0 && progressBar.getVisibility() != View.VISIBLE)
                    sendMesage();
            }
        });
        listView.setOnTouchListener(new SwipeListener(this) {
            public void onSwipeLeft() {
                ((ChatAdapter) listView.getAdapter()).setShowDate(true);
            }
            public void onSwipeRight() {
                ((ChatAdapter) listView.getAdapter()).setShowDate(false);
            }
        });
    }

    public void fixActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle("Back");
        getActionBar().setSubtitle("Comments: " + title);
        actionBar.setDisplayUseLogoEnabled(false);
    }

   /* @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }*/

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            /*case R.id.more:
                return true;*/
            case android.R.id.home:
                finish();
                InterpretationCommentActivity.this.overridePendingTransition(R.anim.left_to_center, R.anim.center_to_right);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void sendMesage() {
        new SendMessage().execute(id);
    }


    private void setAdapter(List<ChatModel> list) {
        ChatAdapter adapter = new ChatAdapter(getApplicationContext(), R.layout.item_rest_conversation, list);
        listView.setAdapter(adapter);
    }

    public class FullRESTConversation extends AsyncTask<String, Integer, Integer> {
        private List<ChatModel> tempList = new ArrayList<ChatModel>();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if(listView.getAdapter() == null)
                contentLoader.setVisibility(View.VISIBLE);

        }
        @Override
        protected Integer doInBackground(String... interpretationID) {
            String api = SharedPrefs.getServerURL(getApplicationContext())
                    + APIPaths.FIRST_PAGE_INTERPRETATIONS
                    + "/"
                    + interpretationID[0]
                    + APIPaths.INTERPRETATIONS_COMMENT_FIELDS;

            Response response = RESTClient.get(api, SharedPrefs.getCredentials(getApplicationContext()));
            if (RESTClient.noErrors(response.getCode())) {
                try {
                    JSONObject json = new JSONObject(response.getBody());

                    //getting messages
                    JSONArray messages = new JSONArray(json.getString("comments"));
                    for (int i = 0; i < messages.length(); i++) {
                        JSONObject row = messages.getJSONObject(i);
                        String message = row.getString("text");                //Content of message (yes its called name)
                        String date = row.getString("lastUpdated");         //Correct to use last updated here?

                        JSONObject sender = row.getJSONObject("user");
                        String id = sender.getString("id");
                        String username = sender.getString("name");
                        NameAndIDModel user = new NameAndIDModel();
                        user.setId(id);
                        user.setName(username);
                        ChatModel model = new ChatModel(message, date, user);
                        tempList.add(model);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    return -1;
                }
            }
            return response.getCode();
        }

        protected void onPostExecute(final Integer code) {
            if (RESTClient.noErrors(code))
                setAdapter(tempList);
            else
                Toast.makeText(getApplicationContext(), "Could not get comments, " + RESTClient.getErrorMessage(code), Toast.LENGTH_LONG).show();
            contentLoader.setVisibility(View.GONE);
        }
    }

    public class SendMessage extends AsyncTask<String, Integer, Integer> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);

        }
        @Override
        protected Integer doInBackground(String... interpretationID) {
            String api = SharedPrefs.getServerURL(getApplicationContext())
                    + APIPaths.FIRST_PAGE_INTERPRETATIONS
                    + "/"
                    + interpretationID[0]
                    + "/comments";

            Response response = RESTClient.post(api, SharedPrefs.getCredentials(getApplicationContext()), newMessage.getText().toString(), "text/plain");
            System.out.println(RESTClient.getErrorMessage(response.getCode()));

            return response.getCode();
        }


        protected void onPostExecute(final Integer code) {
            if (RESTClient.noErrors(code)) {
                newMessage.setText("");
                new FullRESTConversation().execute(id);
            } else
                Toast.makeText(getApplicationContext(), code + ": " + RESTClient.getErrorMessage(code), Toast.LENGTH_LONG).show();
            progressBar.setVisibility(View.GONE);
        }
    }
}
