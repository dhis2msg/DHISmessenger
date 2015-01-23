package org.dhis2.messaging;

import java.util.ArrayList;
import java.util.List;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.*;
import android.widget.*;

import org.dhis2.messaging.Models.NameAndIDModel;
import org.dhis2.messaging.Utils.AsyncTasks.Interfaces.RESTConversationCallback;
import org.dhis2.messaging.Utils.AsyncTasks.RESTConversationHandler;
import org.dhis2.messaging.Models.ChatModel;
import org.dhis2.messaging.Utils.Adapters.*;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import org.dhis2.messaging.Utils.*;

public class RESTChatActivity extends Activity implements RESTConversationCallback {
    private ListView listView;
    private EditText newMessage;
    private List<NameAndIDModel> members;
    public ProgressBar progressBar;

    //Conversation Id
    private String id, title;
    private boolean read;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rest_conversation);
        listView = (ListView) findViewById(R.id.chatList);
        newMessage = (EditText) findViewById(R.id.answerMessage);
        members = new ArrayList<NameAndIDModel>();
        progressBar = (ProgressBar) findViewById(R.id.loader);
        Button sendBtn = (Button) findViewById(R.id.btnSend);

        //Getting message context
        Intent i = getIntent();
        id = i.getStringExtra("id");
        read = i.getBooleanExtra("read", false);
        title = i.getStringExtra("subject");
        fixActionBar();
        new RESTConversationHandler(this).getInbox(getApplicationContext(), read, id);

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (newMessage.getText().toString().trim().length() > 0 && progressBar.getVisibility() != View.VISIBLE)
                    sendMessage();
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

    public void fixActionBar(){
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle("Back");
        getActionBar().setSubtitle(title);
        actionBar.setDisplayUseLogoEnabled(false);
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

    private void sendMessage() {
        progressBar.setVisibility(View.VISIBLE);
        RESTConversationHandler.sendMessage(getApplicationContext(), newMessage.getText().toString(), id);
    }

    private void setAdapter(List<ChatModel> list) {
        ChatAdapter adapter = new ChatAdapter(getApplicationContext(), R.layout.item_rest_conversation, list);
        listView.setAdapter(adapter);
    }

    @Override
    public void updateMessages(List<ChatModel> list) {
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
            RESTConversationHandler.getInbox(getApplicationContext(), read, id);
        } else
            Toast.makeText(this, "Could not send message!", Toast.LENGTH_SHORT).show();
        progressBar.setVisibility(View.GONE);
    }

    private void showAllUsersDialog() {
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
