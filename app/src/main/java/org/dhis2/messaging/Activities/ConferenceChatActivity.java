package org.dhis2.messaging.Activities;

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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.dhis2.messaging.Models.IMMessageModel;
import org.dhis2.messaging.R;
import org.dhis2.messaging.Testing.SaveDataSqlLite;
import org.dhis2.messaging.Utils.SharedPrefs;
import org.dhis2.messaging.Utils.UserInterface.ToastMaster;
import org.dhis2.messaging.XMPP.Interfaces.IMUpdateUnreadMessages;
import org.dhis2.messaging.XMPP.Interfaces.XMPPDataChanged;
import org.dhis2.messaging.XMPP.XMPPSessionStorage;
import org.dhis2.messaging.Utils.Adapters.IMChatAdapter;
import org.dhis2.messaging.XMPP.XMPPClient;
import org.dhis2.messaging.Utils.UserInterface.SwipeListener;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by iNick on 20.10.14.
 */
public class ConferenceChatActivity extends Activity implements XMPPDataChanged, IMUpdateUnreadMessages {

    @Bind(R.id.sendText)
    EditText text;
    @Bind(R.id.chat_List)
    ListView list;
    @Bind(R.id.send)
    Button send;
    @Bind(R.id.loader)
    ProgressBar pb;
    @Bind(R.id.contentLoader)
    ProgressBar contentLoader;

    private String conferenceId;

    @SuppressWarnings("unused")
    @OnClick(R.id.send)
    public void sendClicked() {
        String message = text.getText().toString();
        if (message.trim().length() > 0) {
            if (XMPPClient.getInstance().checkConnection()) {
                pb.setVisibility(View.VISIBLE);
                int response = XMPPClient.getInstance().sendMucMessage(message);
                if (XMPPClient.noErrors(response)) {
                    SaveDataSqlLite db = new SaveDataSqlLite(ConferenceChatActivity.this);
                    db.open();
                    db.updateIMConferenceSent();
                    db.close();
                    text.setText("");
                } else
                    new ToastMaster(getApplicationContext(), XMPPClient.getResponseMessage(response), false);
            } else
                new ToastMaster(getApplicationContext(), "Lost connection..", false);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.im_chat);
        ButterKnife.bind(this);

        contentLoader.setVisibility(View.VISIBLE);

        Intent intent = getIntent();
        conferenceId = intent.getStringExtra("id");
        fixActionBar();

        list.setOnTouchListener(new SwipeListener(this) {
            public void onSwipeLeft() {
                ((IMChatAdapter) list.getAdapter()).setShowDate(true);
            }

            public void onSwipeRight() {
                ((IMChatAdapter) list.getAdapter()).setShowDate(false);
            }
        });
    }

    @Override
    public void updateIMMessages(int amount) {
        new ToastMaster(getApplicationContext(), "New chat message", true);
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
                finish();
                ConferenceChatActivity.this.overridePendingTransition(R.anim.left_to_center, R.anim.center_to_right);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void notifyChanged() {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            public void run() {
                pb.setVisibility(View.GONE);
                setListAdapter();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        XMPPSessionStorage.getInstance().setHomeListener(null);
    }

    @Override
    protected void onStop() {
        super.onDestroy();
        XMPPClient.getInstance().leaveMUC();
        XMPPSessionStorage.getInstance().changeListener(null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        XMPPSessionStorage.getInstance().changeListener(this);
        XMPPSessionStorage.getInstance().setHomeListener(this);

        if (!XMPPClient.getInstance().onlineOnMuc(conferenceId)) {
            if (XMPPSessionStorage.getInstance().getNickname() == null) {
                XMPPClient.getInstance().setMucNickname();
            }
            int response = XMPPClient.getInstance().joinMUC(conferenceId);
            if (!XMPPClient.noErrors(response)) {
                new ToastMaster(getApplicationContext(), XMPPClient.getResponseMessage(response), false);
            }
        }
        contentLoader.setVisibility(View.GONE);
    }

    private void fixActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle("Leave chat");
        getActionBar().setSubtitle("Topic: " + XMPPSessionStorage.getInstance().getConference(conferenceId).getTopic());
        actionBar.setDisplayUseLogoEnabled(false);
    }

    private void setListAdapter() {
        List<IMMessageModel> messages = XMPPSessionStorage.getInstance().getConferenceChat(conferenceId);
        if (messages == null)
            messages = new ArrayList<IMMessageModel>();

        IMChatAdapter adapter = new IMChatAdapter(this, R.layout.item_rest_conversation, messages);
        contentLoader.setVisibility(View.GONE);
        list.setAdapter(adapter);
    }

    private void showAllUsersDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Users in conference");
        List<String> participants = XMPPSessionStorage.getInstance().getConference(conferenceId).getParticipants();
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

        try {
            alertDialog.show();
        } catch (Exception e) {
        }
    }

    private void showInfo() {
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.conference_info, null);
        final TextView name = (TextView) view.findViewById(R.id.discussionName);
        final TextView id = (TextView) view.findViewById(R.id.id);
        final EditText subject = (EditText) view.findViewById(R.id.subject);
        final EditText description = (EditText) view.findViewById(R.id.desc);
        final TextView admin = (TextView) view.findViewById(R.id.admin);

        name.setText(XMPPSessionStorage.getInstance().getConference(conferenceId).getName());
        id.setText(conferenceId);
        subject.setText(XMPPSessionStorage.getInstance().getConference(conferenceId).getTopic());
        description.setText(XMPPSessionStorage.getInstance().getConference(conferenceId).getDescription());

        boolean isAdmin = XMPPSessionStorage.getInstance().getConference(conferenceId).isAdmin();
        if (!isAdmin) {
            subject.setEnabled(false);
            description.setEnabled(false);
            admin.setText(" No");
        } else
            admin.setText(" Yes");

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Conference info");
        builder.setView(view);

        if (isAdmin) {
            builder.setPositiveButton("Save settings", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    int i = XMPPClient.getInstance().updateMUC(subject.getText().toString(), description.getText().toString());
                    if (!XMPPClient.noErrors(i)) {
                        new ToastMaster(getApplicationContext(), XMPPClient.getResponseMessage(i), false);
                    }
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
        try {
            alertDialog.show();
        } catch (Exception e) {
        }
    }

    private void showDeleteConference() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        if (XMPPSessionStorage.getInstance().getConference(conferenceId).isAdmin()) {
            builder.setTitle("Confirm to remove conference");
            builder.setNegativeButton("Remove", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    int i = XMPPClient.getInstance().deleteConference(SharedPrefs.getXMPPUsername(getApplicationContext()));
                    if (!XMPPClient.noErrors(i)) {
                        new ToastMaster(getApplicationContext(), XMPPClient.getResponseMessage(i), false);
                    }
                    dialog.cancel();
                    finish();
                    ConferenceChatActivity.this.overridePendingTransition(R.anim.left_to_center, R.anim.center_to_right);
                }
            });
        } else
            builder.setTitle("You need to be admin to delete");

        builder.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        AlertDialog alertDialog = builder.create();
        try {
            alertDialog.show();
        } catch (Exception e) {
        }
    }
}

