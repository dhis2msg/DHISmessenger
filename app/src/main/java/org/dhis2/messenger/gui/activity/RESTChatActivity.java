package org.dhis2.messenger.gui.activity;

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
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.transition.Slide;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;

import org.dhis2.messenger.core.rest.RESTSessionStorage;
import org.dhis2.messenger.core.xmpp.XMPPSessionStorage;
import org.dhis2.messenger.model.ChatModel;
import org.dhis2.messenger.model.NameAndIDModel;
import org.dhis2.messenger.R;
import org.dhis2.messenger.core.rest.RESTClient;
import org.dhis2.messenger.core.SaveDataSqlLite;
import org.dhis2.messenger.gui.adapter.ChatAdapter;
import org.dhis2.messenger.core.rest.callback.RESTConversationCallback;
import org.dhis2.messenger.core.rest.async.RESTGetConversation;
import org.dhis2.messenger.core.rest.async.RESTSendMessage;
import org.dhis2.messenger.gui.ToastMaster;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class RESTChatActivity extends Activity implements RESTConversationCallback, UpdateUnreadMsg {
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

    //Data:
    private String id;
    private int index;
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
            if (RESTClient.isDeviceConnectedToInternet(getApplication()))
                sendMessage();
            //intent refresh conversation ?
            else
                new ToastMaster(getApplicationContext(), "No internet connection", false);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(Build.VERSION.SDK_INT >= 21){
            Slide slide = new Slide();
            slide.setDuration(500);
            getWindow().setEnterTransition(slide);
        }

        setContentView(R.layout.activity_rest_conversation);
        ButterKnife.bind(this);

        //TODO: vladislav: storage: fetch this list from disk ?
        members = new ArrayList<NameAndIDModel>();

        //Getting message context
        Intent i = getIntent();
        id = i.getStringExtra("id");
        //get the index of the inboxModel from the intent :
        index = i.getIntExtra("index", -1);
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
        this.registerReceiver(messageReceiver, new IntentFilter("org.dhis2.messenger.gui.activity.RESTChatActivity"));
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
            //store in the cache that the conversation needs to be refreshed.
            RESTSessionStorage.getInstance().sentNewMessage(true);
            newMessage.setText("");

            SaveDataSqlLite db = new SaveDataSqlLite(this);
            db.open();
            db.updateDHISMessageSent();
            db.close();
            getMessages(true);
        } else {
            new ToastMaster(getApplicationContext(), "Could not send message!", false);
        }
        sendLoader.setVisibility(View.GONE);
    }

    @Override
    public void updateUnreadMsg(int restNumber, int xmppNumber) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new ToastMaster(getApplicationContext(), "New Chat Message", true);
            }
        });
    }

    private void removeHandler() {
        if (getConversation != null) {
            if (!getConversation.isCancelled()) {
                getConversation.cancel(true);
            }
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
        getConversation = new RESTGetConversation(this, this, read, id, index);
        getConversation.execute();
    }
}
