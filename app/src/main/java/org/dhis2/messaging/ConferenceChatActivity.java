package org.dhis2.messaging;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ToggleButton;

import org.dhis2.messaging.Models.IMMessageModel;
import org.dhis2.messaging.Utils.SharedPrefs;
import org.dhis2.messaging.Utils.XMPP.XMPPDataChanged;
import org.dhis2.messaging.Utils.XMPP.XMPPSessionStorage;
import org.dhis2.messaging.Utils.Adapters.IMChatAdapter;
import org.dhis2.messaging.Utils.XMPP.XMPPClient;
import org.dhis2.messaging.Utils.SwipeListener;

import java.util.List;

/**
 * Created by iNick on 20.10.14.
 */
public class ConferenceChatActivity extends Activity implements XMPPDataChanged {
    private EditText text;
    private ListView list;
    private Button send;
    private String conference;
    InputMethodManager inputManager;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.im_chat);
        View view = findViewById(R.id.imView);
        text = (EditText) this.findViewById(R.id.sendText);
        list = (ListView) this.findViewById(R.id.chat_List);
        send = (Button) this.findViewById(R.id.btnSend);
        Intent intent = getIntent();
        conference = intent.getStringExtra("id");
        fixActionBar();
        inputManager = (InputMethodManager) this.getSystemService(this.INPUT_METHOD_SERVICE);

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = text.getText().toString();
                if (message.trim().length() > 0) {
                        if(XMPPClient.getInstance().sendMucMessage(message))
                            text.setText("");

                    inputManager.hideSoftInputFromWindow(text.getWindowToken(), 0);
                }
            }
        });
        list.setOnTouchListener(new SwipeListener(this) {
            public void onSwipeLeft() {
                ((IMChatAdapter) list.getAdapter()).setShowDate(true);
            }

            public void onSwipeRight() {
                ((IMChatAdapter) list.getAdapter()).setShowDate(false);
            }
        });

        inputManager.hideSoftInputFromWindow(text.getWindowToken(), 0);
    }

    public void fixActionBar(){
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle("Leave chat");
        getActionBar().setSubtitle("Topic: " + XMPPSessionStorage.getInstance().getConference(conference).getTopic());
        actionBar.setDisplayUseLogoEnabled(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.conference, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.more:
                showAllUsersDialog();
                return true;
            case R.id.info:
                showInfo();
                return true;
            case R.id.delete:
                showDeleteConference();
                return true;

            case android.R.id.home:
                XMPPSessionStorage.getInstance().getConference(conference).setMessages(null);
                finish();
                ConferenceChatActivity.this.overridePendingTransition(R.anim.left_to_center, R.anim.center_to_right);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }



    public void setListAdapter() {
        List<IMMessageModel> messages = XMPPSessionStorage.getInstance().getConferenceChat(conference);
        IMChatAdapter adapter = new IMChatAdapter(this, R.layout.item_rest_conversation, messages);
        list.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }


    @Override
    public void notifyChanged() {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            public void run() {
                setListAdapter();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        XMPPClient.getInstance().leaveMUC();
        XMPPSessionStorage.getInstance().changeListener(null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        XMPPClient.getInstance().joinMUC(conference);
        XMPPSessionStorage.getInstance().changeListener(this);

        if(XMPPSessionStorage.getInstance().getConferenceChat(conference) != null) {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                public void run() {
                    setListAdapter();
                }
            });
        }
    }

    private void showAllUsersDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Users in conference");
        List<String> participants = XMPPSessionStorage.getInstance().getConference(conference).getParticipants();
        String[] array = new String[participants.size()];
        for (int j = 0; j < participants.size(); j++) {
            array[j] = participants.get(j);
        }
        builder.setItems(array, null);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
    private void showInfo(){

        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.conference_info, null);
        final TextView name = (TextView) view.findViewById(R.id.discussionName);
        final TextView id = (TextView) view.findViewById(R.id.id);
        final EditText subject = (EditText) view.findViewById(R.id.subject);
        final EditText description = (EditText) view.findViewById(R.id.desc);
        final TextView admin = (TextView) view.findViewById(R.id.admin);

        name.setText(XMPPSessionStorage.getInstance().getConference(conference).getName());
        id.setText(conference);
        subject.setText(XMPPSessionStorage.getInstance().getConference(conference).getTopic());
        description.setText(XMPPSessionStorage.getInstance().getConference(conference).getDescription());

        boolean isAdmin = XMPPSessionStorage.getInstance().getConference(conference).isAdmin();
        if(!isAdmin){
            subject.setEnabled(false);
            description.setEnabled(false);
            admin.setText(" No");
        }
        else
            admin.setText(" Yes");

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Conference info");
        builder.setView(view);

        if(isAdmin){
            builder.setPositiveButton("Save settings", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    XMPPClient.getInstance().updateMUC(subject.getText().toString(),description.getText().toString());
                    dialog.cancel();
                }
            });
        }

        builder.setNegativeButton("Close", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showDeleteConference() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        if(XMPPSessionStorage.getInstance().getConference(conference).isAdmin()) {
            builder.setTitle("Confirm to remove conference");


            builder.setNegativeButton("Remove", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    XMPPClient.getInstance().deleteConference(SharedPrefs.getXMPPUsername(getApplicationContext()));
                    dialog.cancel();
                    finish();
                    ConferenceChatActivity.this.overridePendingTransition(R.anim.left_to_center, R.anim.center_to_right);
                }
            });
        }
        else
            builder.setTitle("You need to be admin to delete");

        builder.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

}

