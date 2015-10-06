package org.dhis2.messaging.Activities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;

import org.dhis2.messaging.Models.IMMessageModel;
import org.dhis2.messaging.R;
import org.dhis2.messaging.Testing.SaveDataSqlLite;
import org.dhis2.messaging.Models.RosterModel;
import org.dhis2.messaging.Utils.UserInterface.ToastMaster;
import org.dhis2.messaging.XMPP.Interfaces.XMPPDataChanged;
import org.dhis2.messaging.XMPP.XMPPSessionStorage;
import org.dhis2.messaging.Utils.Adapters.IMChatAdapter;
import org.dhis2.messaging.Utils.CurrentTime;
import org.dhis2.messaging.Utils.UserInterface.SwipeListener;
import org.dhis2.messaging.XMPP.XMPPClient;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.packet.Message;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class IMChatActivity extends Activity implements XMPPDataChanged {
    @Bind(R.id.sendText)
    EditText text;
    @Bind(R.id.chat_List)
    ListView listView;
    @Bind(R.id.btnSend)
    Button send;
    @Bind(R.id.loader)
    ProgressBar pb;

    //Memory store
    private String receiver;

    @SuppressWarnings("unused")
    @OnClick(R.id.send)
    public void sendClicked() {
        String message = text.getText().toString();
        if (message.trim().length() > 0) {
            if (XMPPClient.getInstance().checkConnection()) {
                pb.setVisibility(View.VISIBLE);
                Message msg = new Message(receiver, Message.Type.chat);
                msg.setBody(message);
                try {
                    text.setText("");
                    XMPPClient.getInstance().getConnection().sendPacket(msg);
                    XMPPSessionStorage.getInstance().addMessage(receiver, new IMMessageModel(message, null, new CurrentTime().getCurrentTime()));
                    SaveDataSqlLite db = new SaveDataSqlLite(IMChatActivity.this);
                    db.open();
                    db.updateIMMessageSent();
                    db.close();

                } catch (NotConnectedException e) {
                    new ToastMaster(getApplicationContext(), "Sending message to " + receiver + " FAILED!", false);
                    text.setText(msg.getBody());
                }
                pb.setVisibility(View.GONE);
            } else
                new ToastMaster(getApplicationContext(), "Not connected to chat server", false);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.im_chat);
        ButterKnife.bind(this);

        Intent intent = getIntent();
        receiver = intent.getStringExtra("receiver");
        fixActionBar(intent.getStringExtra("nickname"));

        listView.setOnTouchListener(new SwipeListener(this) {
            public void onSwipeLeft() {
                ((IMChatAdapter) listView.getAdapter()).setShowDate(true);
            }

            public void onSwipeRight() {
                ((IMChatAdapter) listView.getAdapter()).setShowDate(false);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.person, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.more:
                showUserInfo();
                return true;
            case android.R.id.home:
                finish();
                IMChatActivity.this.overridePendingTransition(R.anim.left_to_center, R.anim.center_to_right);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void notifyChanged() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setListAdapter();
            }
        });
        XMPPSessionStorage.getInstance().updateReadConversationNoCallback(receiver, true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        XMPPSessionStorage.getInstance().changeListener(null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        XMPPSessionStorage.getInstance().changeListener(this);
        setListAdapter();
    }

    private void fixActionBar(String nickname) {
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle("Back");
        getActionBar().setSubtitle(nickname);
        actionBar.setDisplayUseLogoEnabled(false);
    }

    private void setListAdapter() {
        List<IMMessageModel> list = XMPPSessionStorage.getInstance().getConversation(receiver);
        IMChatAdapter adapter = new IMChatAdapter(this, R.layout.item_rest_conversation, list);
        listView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    private void showUserInfo() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        RosterModel model = XMPPSessionStorage.getInstance().getRosterModel(receiver);
        ListView view = new ListView(getApplicationContext());
        List<Map<String, String>> list = new ArrayList<Map<String, String>>();

        Map<String, String> in = new HashMap<String, String>(2);
        in.put("title", "JID");
        in.put("content", model.getJID());
        list.add(in);
        in = new HashMap<String, String>(2);
        in.put("title", "Username");
        in.put("content", model.getUsername());
        list.add(in);
        in = new HashMap<String, String>(2);
        in.put("title", "Online");
        in.put("content", model.isOnline() == true ? "yes" : "no");
        list.add(in);
        in = new HashMap<String, String>(2);
        in.put("title", "Status");
        in.put("content", model.getStatusMessage());
        list.add(in);

        SimpleAdapter adapter = new SimpleAdapter(this, list, android.R.layout.simple_list_item_2,
                new String[]{"title", "content"}, new int[]{android.R.id.text1, android.R.id.text2});
        view.setAdapter(adapter);
        builder.setView(view);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}