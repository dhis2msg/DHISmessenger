package org.dhis2.messaging.Activities;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;

import org.dhis2.messaging.Models.ChatModel;
import org.dhis2.messaging.Models.NameAndIDModel;
import org.dhis2.messaging.R;
import org.dhis2.messaging.REST.RESTClient;
import org.dhis2.messaging.Testing.SaveDataSqlLite;
import org.dhis2.messaging.Utils.Adapters.ChatAdapter;
import org.dhis2.messaging.Utils.AsyncroniousTasks.Interfaces.RESTConversationCallback;
import org.dhis2.messaging.Utils.AsyncroniousTasks.RESTGetConversation;
import org.dhis2.messaging.Utils.AsyncroniousTasks.RESTSendMessage;
import org.dhis2.messaging.Utils.UserInterface.ToastMaster;
import org.dhis2.messaging.XMPP.Interfaces.IMUpdateUnreadMessages;
import org.dhis2.messaging.XMPP.XMPPSessionStorage;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class RESTChatActivity extends Activity implements RESTConversationCallback, IMUpdateUnreadMessages {
    @Bind(R.id.chatList)
    ListView listView;
    @Bind(R.id.answerMessage)
    EditText newMessage;
    @Bind(R.id.loader)
    ProgressBar sendLoader;
    @Bind(R.id.contentLoader)
    ProgressBar receiveLoader;
    @Bind(R.id.btnSend)
    Button sendButton;

    //Handlers
    private RESTGetConversation getConversation;
    private RESTSendMessage sendMessage;

    //Memory store
    private String id;
    private List<NameAndIDModel> members;
    private BroadcastReceiver messageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    getMessages(false);
                    Vibrator v = (Vibrator) getApplication().getSystemService(Context.VIBRATOR_SERVICE);
                    v.vibrate(500);
                }
            });
        }
    };

    @SuppressWarnings("unused")
    @OnClick(R.id.btnSend)
    public void clickedSend() {
        if (newMessage.getText().toString().trim().length() > 0 && sendLoader.getVisibility() != View.VISIBLE) {
            if (RESTClient.isDeviceConnectedToInternett(getApplication()))
                sendMessage();
            else
                new ToastMaster(getApplicationContext(), "No internet connection", false);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rest_conversation);
        ButterKnife.bind(this);

        //TODO: vladislav: storage: fetch this list from disk ?
        members = new ArrayList<NameAndIDModel>();

        //Getting message context
        Intent i = getIntent();
        id = i.getStringExtra("id");
        fixActionBar(i.getStringExtra("subject"));
        getMessages(i.getBooleanExtra("read", false));
        receiveLoader.setVisibility(View.VISIBLE);

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                                           int pos, long id) {
                ChatModel chat = (ChatModel) listView.getAdapter().getItem(pos);
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("simple text", chat.message);
                clipboard.setPrimaryClip(clip);
                new ToastMaster(getApplicationContext(), "Message is copied", false);
                return true;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.persons, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.more:
                showAllUsersDialog();
                return true;
            case android.R.id.home:
                finish();
                RESTChatActivity.this.overridePendingTransition(R.anim.left_to_center, R.anim.center_to_right);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        this.registerReceiver(messageReceiver, new IntentFilter("org.dhis2.messaging.Activities.RESTChatActivity"));
        if (getConversation != null)
            if (getConversation.isCancelled())
                getMessages(true);
        // TODO : vladislav:  why xmppSessionStorage  here ?
        XMPPSessionStorage.getInstance().setHomeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.unregisterReceiver(messageReceiver);
        // TODO : vladislav:  why xmppSessionStorage  here ?
        XMPPSessionStorage.getInstance().setHomeListener(null);
        removeHandler();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (sendMessage != null)
            if (!sendMessage.isCancelled())
                sendMessage.cancel(true);
        sendMessage = null;
    }

    @Override
    public void updateMessages(List<ChatModel> list) {
        receiveLoader.setVisibility(View.INVISIBLE);
        setAdapter(list);

    }

    @Override
    public void updateUsers(List<NameAndIDModel> list) {
        members = list;
    }

    @Override
    public void messageSent(boolean sent) {
        if (sent) {
            newMessage.setText("");

            SaveDataSqlLite db = new SaveDataSqlLite(this);
            db.open();
            db.updateDHISMessageSent();
            db.close();
            getMessages(true);
        } else
            new ToastMaster(getApplicationContext(), "Could not send message!", false);

        sendLoader.setVisibility(View.GONE);
    }

    @Override
    public void updateIMMessages(int amount) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new ToastMaster(getApplicationContext(), "New Chat Message", true);
            }
        });
    }

    private void removeHandler() {
        if (getConversation != null) {
            if (!getConversation.isCancelled())
                getConversation.cancel(true);
            getConversation = null;
        }
    }

    private void fixActionBar(String title) {
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle("Back");
        getActionBar().setSubtitle(title);
        actionBar.setDisplayUseLogoEnabled(false);
    }

    private void sendMessage() {
        sendLoader.setVisibility(View.VISIBLE);
        sendMessage = new RESTSendMessage(this, this, newMessage.getText().toString(), id);
        sendMessage.execute();
    }

    private void setAdapter(List<ChatModel> list) {
        ChatAdapter adapter = new ChatAdapter(getApplicationContext(), R.layout.item_rest_conversation, list);
        listView.setAdapter(adapter);
        listView.setSelection(listView.getCount() - 1);
    }

    private void showAllUsersDialog() {
        if (members.size() > 0) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Users in this conversation");
            String[] array = new String[members.size()];
            for (int j = 0; j < members.size(); j++) {
                array[j] = members.get(j).getName();
            }
            builder.setItems(array, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    NameAndIDModel model = members.get(i);
                    Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
                    intent.putExtra("userid", model.getId());
                    intent.putExtra("username", model.getName());
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    getApplicationContext().startActivity(intent);
                }
            });

            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }
    }

    private void getMessages(boolean read) {
        removeHandler();
        getConversation = new RESTGetConversation(this, this, read, id);
        getConversation.execute();
    }
}
