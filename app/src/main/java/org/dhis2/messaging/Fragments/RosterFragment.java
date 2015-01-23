package org.dhis2.messaging.Fragments;

import java.util.Collections;
import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import org.dhis2.messaging.ConferenceChatActivity;
import org.dhis2.messaging.IMChatActivity;
import org.dhis2.messaging.Models.ConferenceModel;
import org.dhis2.messaging.Utils.XMPP.RosterModel;
import org.dhis2.messaging.Utils.XMPP.XMPPDataChanged;
import org.dhis2.messaging.Utils.XMPP.XMPPSessionStorage;
import org.dhis2.messaging.Utils.Adapters.ConferenceAdapter;
import org.dhis2.messaging.Utils.Adapters.RosterAdapter;
import org.dhis2.messaging.R;
import org.dhis2.messaging.Utils.XMPP.XMPPClient;

import org.dhis2.messaging.Utils.SharedPrefs;
import org.jivesoftware.smack.packet.Presence;

import android.support.v4.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;
import android.widget.ToggleButton;

public class RosterFragment extends Fragment implements XMPPDataChanged {
    private ListView userListView, groupsListView;
    private TextView message;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        setHasOptionsMenu(true);
        View view = inflater.inflate(R.layout.fragment_roster, container, false);
        userListView = (ListView) view.findViewById(R.id.onlineUsers_list);
        groupsListView = (ListView) view.findViewById(R.id.groups_list);
        message = (TextView) view.findViewById(R.id.infoMessage);

        if (!XMPPClient.getInstance().checkConnection()) {
            Toast.makeText(getActivity(), "Not connected.. Try turning on 'Live Chat' in Settings", Toast.LENGTH_LONG).show();
            message.setVisibility(View.VISIBLE);
        }
        else {
            userListView.setOnItemClickListener(new OnItemClickListener() {
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                    RosterModel model = (RosterModel) userListView.getAdapter().getItem(position);
                    Intent intent = new Intent(getActivity(), IMChatActivity.class);
                    intent.putExtra("receiver", model.getJID());
                    intent.putExtra("nickname", model.getUsername());
                    getActivity().startActivity(intent);
                    XMPPSessionStorage.getInstance().updateReadConversation(model.getJID(), true);
                }
            });

            groupsListView.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    ConferenceModel conference = (ConferenceModel) groupsListView.getAdapter().getItem(i);
                    Intent intent = new Intent(getActivity(), ConferenceChatActivity.class);
                    intent.putExtra("id", conference.getId());
                    getActivity().startActivity(intent);

                }
            });
        }

        return view;
    }

    private void setListAdapter(List<RosterModel> list) {
        Collections.sort(list);
        RosterAdapter adapter = new RosterAdapter(getActivity(), R.layout.item_roster, list);
        userListView.setAdapter(adapter);
    }

    private void setConferenceAdapter() {
        List<ConferenceModel> multiUserChatRooms = XMPPSessionStorage.getInstance().getXMPPConferenceData();
        ConferenceAdapter groupAdapter = new ConferenceAdapter(getActivity(), R.layout.item_roster_conference, multiUserChatRooms);
        groupsListView.setAdapter(groupAdapter);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.roster_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.person: {
                userListView.setVisibility(View.VISIBLE);
                groupsListView.setVisibility(View.GONE);
                return true;
            }
            case R.id.conference: {
                if(XMPPClient.getInstance().checkConnection()) {
                    userListView.setVisibility(View.GONE);
                    groupsListView.setVisibility(View.VISIBLE);
                    XMPPClient.getInstance().getAllMUCs();
                }
                else
                    Toast.makeText(getActivity(),"Need to be connected to XMPP server", Toast.LENGTH_LONG).show();
                return true;
            }
            case R.id.more: {
                if(XMPPClient.getInstance().checkConnection()) {
                    showStutusSettings();
                }else
                    Toast.makeText(getActivity(),"Need to be connected to XMPP server", Toast.LENGTH_LONG).show();

                return true;
            }
            case R.id.newConference:
                if(XMPPClient.getInstance().checkConnection()) {
                    showNewConference();
                }
                else
                    Toast.makeText(getActivity(),"Need to be connected to XMPP server", Toast.LENGTH_LONG).show();

                return true;
            case R.id.settings: {
                showSettings();
                return true;
            }
        }
        return true;
    }

    public void setRoster(){
        setListAdapter(XMPPSessionStorage.getInstance().getXMPPData());
    }

    private void showSettings(){

        LayoutInflater inflater = getLayoutInflater(getArguments());
        View view = inflater.inflate(R.layout.fragment_settings, null);
        final ToggleButton chatToggle = (ToggleButton) view.findViewById(R.id.toggleChat);
        final EditText server = (EditText) view.findViewById(R.id.serverField);
        final EditText username = (EditText) view.findViewById(R.id.usernameField);
        final EditText password = (EditText) view.findViewById(R.id.passwordField);

        if (SharedPrefs.getXMPPHost(getActivity()) != null) {
            server.setText(SharedPrefs.getXMPPServer(getActivity()));
            username.setText(SharedPrefs.getXMPPUsername(getActivity()));
            password.setText(SharedPrefs.getXMPPPassword(getActivity()));
        }

        if (XMPPClient.getInstance().checkConnection()) {
            chatToggle.setChecked(true);
        }

        chatToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (!server.getText().toString().isEmpty() && !username.getText().toString().isEmpty() && !password.getText().toString().isEmpty() ) {
                        loginXMPP(server.getText().toString(), "5222",
                             username.getText().toString(), password.getText().toString());
                        message.setVisibility(View.GONE);
                    } else
                        chatToggle.setChecked(false);
                } else {
                    Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {

                                XMPPClient.getInstance().destroy();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    thread.start();
                }
            }
        });
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Edit chat settings");
        builder.setView(view);
        builder.setPositiveButton("Close", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showStutusSettings() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Edit status");
        String m = SharedPrefs.getXMPPMode(getActivity());
        String s = SharedPrefs.getXMPPStatus(getActivity());

        LayoutInflater inflater = getLayoutInflater(getArguments());
        View view = inflater.inflate(R.layout.dialog_status, null);
        final Spinner picker = (Spinner) view.findViewById(R.id.status);
        final EditText statusMessage = (EditText) view.findViewById(R.id.statusMessage);
        statusMessage.setText(s);

        final ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(), R.array.status_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        picker.setAdapter(adapter);

        if (m.equals("Available")) {
            picker.setSelection(0);
        } else if (m.equals("Away")) {
            picker.setSelection(1);
        } else if (m.equals("Free to chat")) {
            picker.setSelection(2);
        } else if (m.equals("Do not disturb")) {
            picker.setSelection(3);
        } else if (m.equals("Unavailable")) {
            picker.setSelection(4);
        }

        builder.setView(view);
        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                String presence = picker.getSelectedItem().toString();
                String statusMsg = statusMessage.getText().toString();
                Presence.Mode mode = null;
                Presence.Type type = Presence.Type.available;

                if (presence.equals("Available")) {
                    mode = Presence.Mode.available;
                } else if (presence.equals("Away")) {
                    mode = Presence.Mode.away;
                } else if (presence.equals("Free to chat")) {
                    mode = Presence.Mode.chat;
                } else if (presence.equals("Do not disturb")) {
                    mode = Presence.Mode.dnd;
                } else if (presence.equals("Unavailable")) {
                    type = Presence.Type.unavailable;
                    mode = Presence.Mode.dnd;
                }
                XMPPClient.getInstance().setPresenceModeAndStatus(type, mode, statusMsg);
                SharedPrefs.setXMPPUserData(getActivity(),presence,statusMsg);
                dialog.cancel();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showNewConference() {
        LayoutInflater inflater = getLayoutInflater(getArguments());
        View view = inflater.inflate(R.layout.create_conference, null);
        final EditText name = (EditText) view.findViewById(R.id.discussionName);
        final EditText subject = (EditText) view.findViewById(R.id.subject);
        final EditText description = (EditText) view.findViewById(R.id.desc);


        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Create new conference");
        builder.setView(view);


        builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    if(!name.getText().toString().isEmpty() || !subject.getText().toString().isEmpty() || !description.getText().toString().isEmpty()) {
                        boolean ok = XMPPClient.getInstance().createConference(name.getText().toString(),
                                                                    subject.getText().toString(),
                                                                    description.getText().toString());
                        if(ok) {
                            dialog.cancel();
                            XMPPClient.getInstance().getAllMUCs();
                        }
                        else
                            Toast.makeText(getActivity(),"Could not create conference, try again..", Toast.LENGTH_LONG).show();
                    }
                    else
                        Toast.makeText(getActivity(),"Include all fields", Toast.LENGTH_SHORT).show();
                }
            });
        builder.setNegativeButton("Close", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void loginXMPP(final String HOST,final String PORT, final String USERNAME, final String PASSWORD) {
        new AsyncTask<String, String, Boolean>() {
            @Override
            protected Boolean doInBackground(String... args) {
                return XMPPClient.getInstance().setConnection(getActivity(),HOST,PORT,USERNAME,PASSWORD);

            }
            @Override
            protected void onPostExecute(Boolean worked) {
                if(worked)
                    Toast.makeText(getActivity(), "Successfully logged in", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(getActivity(), "Could not sign in..", Toast.LENGTH_LONG).show();
            }
        }.execute();
    }

    @Override
    public void notifyChanged() {
        if(XMPPClient.getInstance().checkConnection()) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setRoster();
                    setConferenceAdapter();
                }
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        XMPPSessionStorage.getInstance().changeListener(this);
        if(XMPPClient.getInstance().checkConnection()) {
            setRoster();
            setConferenceAdapter();
        }
        else
            Toast.makeText(getActivity(), "Not connected.. Try turning on 'Live Chat' in Settings", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPause() {
        super.onResume();
        XMPPSessionStorage.getInstance().changeListener(null);
    }
}