package org.dhis2.messenger.gui.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.transition.Fade;
import android.transition.Slide;
import android.transition.TransitionSet;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;

import org.dhis2.messenger.model.ChatModel;
import org.dhis2.messenger.model.CopyAttributes;
import org.dhis2.messenger.model.NameAndIDModel;
import org.dhis2.messenger.R;
import org.dhis2.messenger.core.rest.APIPath;
import org.dhis2.messenger.core.rest.RESTClient;
import org.dhis2.messenger.core.rest.Response;
import org.dhis2.messenger.gui.adapter.ChatAdapter;
import org.dhis2.messenger.SharedPrefs;
import org.dhis2.messenger.gui.SwipeListener;
import org.dhis2.messenger.gui.ToastMaster;
import org.dhis2.messenger.core.xmpp.XMPPSessionStorage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by iNick on 14.10.14.
 */
public class InterpretationCommentActivity extends Activity implements UpdateUnreadMsg {

    @Bind(R.id.chatList)
    ListView listView;
    @Bind(R.id.answerMessage)
    EditText newMessage;
    @Bind(R.id.btnSend)
    Button sendBtn;
    @Bind(R.id.loader)
    ProgressBar progressBar;
    @Bind(R.id.contentLoader)
    ProgressBar contentLoader;

    //Memory store
    private String id;
    private RESTInterpretationComments getComments;
    private SendInterpretationComment sendComment;

    @SuppressWarnings("unused")
    @OnClick(R.id.btnSend)
    public void sendClicked() {
        if (newMessage.getText().toString().trim().length() > 0 && progressBar.getVisibility() != View.VISIBLE) {
            if (RESTClient.isDeviceConnectedToInternet(getApplicationContext())) {
                sendComment = new SendInterpretationComment();
                sendComment.execute(id);
            } else
                new ToastMaster(getApplicationContext(), "No internet connection", false);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(Build.VERSION.SDK_INT >= 21){
            Slide slide = new Slide();
            slide.setDuration(300);
            Fade fade = new Fade();
            fade.setDuration(500);
            TransitionSet transitionSet = new TransitionSet();
            transitionSet.addTransition(slide);
            transitionSet.addTransition(fade);
            getWindow().setEnterTransition(transitionSet);
        }

        setContentView(R.layout.activity_rest_conversation);
        ButterKnife.bind(this);

        Intent i = getIntent();
        id = i.getStringExtra("id");
        fixActionBar(i.getStringExtra("subject"));

        listView.setOnTouchListener(new SwipeListener(this) {
            public void onSwipeLeft() {
                ((ChatAdapter) listView.getAdapter()).setShowDate(true);
            }

            public void onSwipeRight() {
                ((ChatAdapter) listView.getAdapter()).setShowDate(false);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                InterpretationCommentActivity.this.overridePendingTransition(R.anim.left_to_center, R.anim.center_to_right);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setAdapter(List<ChatModel> list) {
        ChatAdapter adapter = new ChatAdapter(getApplicationContext(), R.layout.item_rest_conversation, list);
        listView.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        XMPPSessionStorage.getInstance().setHomeListener(this);
        if (RESTClient.isDeviceConnectedToInternet(getApplicationContext())) {
            getComments = new RESTInterpretationComments();
            getComments.execute(id);
        } else
            new ToastMaster(getApplicationContext(), "No internet connection", false);
    }

    @Override
    protected void onStop() {
        super.onStop();
        removeHandlers();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_send, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void updateUnreadMsg(int restNumber, int xmppNumber) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new ToastMaster(getApplicationContext(), "New chat message", true);
            }
        });
    }

    private void fixActionBar(String title) {
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle("Back");
        getActionBar().setSubtitle("Comments: " + title);
        actionBar.setDisplayUseLogoEnabled(false);
    }

    private void removeHandlers() {
        if (getComments != null) {
            if (!getComments.isCancelled())
                getComments.cancel(true);
            getComments = null;
        }
        if (sendComment != null) {
            if (!sendComment.isCancelled())
                sendComment.cancel(true);
            sendComment = null;
        }
    }


    //______________________________Private class definitions________________________

    private class RESTInterpretationComments extends AsyncTask<String, Integer, Integer> {
        private List<ChatModel> tempList = new ArrayList<ChatModel>();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (listView.getAdapter() == null)
                contentLoader.setVisibility(View.VISIBLE);
        }

        @Override
        protected Integer doInBackground(String... interpretationID) {
            String api = SharedPrefs.getServerURL(getApplicationContext())
                    + APIPath.FIRST_PAGE_INTERPRETATIONS
                    + "/"
                    + interpretationID[0]
                    + APIPath.INTERPRETATIONS_COMMENT_FIELDS;

            Response response = RESTClient.get(api, SharedPrefs.getCredentials(getApplicationContext()));
            if (RESTClient.noErrors(response.getCode())) {
                try {
                    JSONObject json = new JSONObject(response.getBody());

                    //getting messages
                    JSONArray messages = new JSONArray(json.getString("comments"));
                    for (int i = 0; i < messages.length(); i++) {
                        JSONObject row = messages.getJSONObject(i);
                        String message = row.getString("text");
                        String date = row.getString("lastUpdated");

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
                new ToastMaster(getApplicationContext(), "Could not get comments, " + RESTClient.getErrorMessage(code), false);
            contentLoader.setVisibility(View.GONE);
        }
    }

    private class SendInterpretationComment extends AsyncTask<String, Integer, Integer> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Integer doInBackground(String... interpretationID) {
            String api = SharedPrefs.getServerURL(getApplicationContext())
                    + APIPath.FIRST_PAGE_INTERPRETATIONS
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
                new RESTInterpretationComments().execute(id);
            } else
                new ToastMaster(getApplicationContext(), code + ": " + RESTClient.getErrorMessage(code), false);
            progressBar.setVisibility(View.GONE);
        }
    }
}
