package org.dhis2.messenger.gui.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.transition.Fade;
import android.transition.Slide;
import android.transition.TransitionSet;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.dhis2.messenger.gui.activity.ConferenceChatActivity;
import org.dhis2.messenger.gui.activity.IMChatActivity;
import org.dhis2.messenger.model.ConferenceModel;
import org.dhis2.messenger.model.RosterModel;
import org.dhis2.messenger.R;
import org.dhis2.messenger.core.rest.RESTClient;
import org.dhis2.messenger.gui.adapter.ConferenceAdapter;
import org.dhis2.messenger.gui.adapter.RosterAdapter;
import org.dhis2.messenger.SharedPrefs;
import org.dhis2.messenger.gui.ToastMaster;
import org.dhis2.messenger.core.xmpp.XMPPDataChanged;
import org.dhis2.messenger.core.xmpp.XMPPClient;
import org.dhis2.messenger.core.xmpp.XMPPSessionStorage;
import org.jivesoftware.smack.packet.Presence;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RosterFragment extends Fragment implements XMPPDataChanged {
    private ListView userListView, groupsListView;
    private TextView message;
    private ProgressBar pb;
    private ImageView refresh;

    //Memory store
    private AsyncTask loginTask;

    public RosterFragment(){
        super();
        if(Build.VERSION.SDK_INT >= 21) {
            Slide slide = new Slide();
            slide.setDuration(500);
            Fade fade = new Fade();
            fade.setDuration(1000);
            TransitionSet transitionSet = new TransitionSet();
            transitionSet.addTransition(slide);
            transitionSet.addTransition(fade);
            setEnterTransition(transitionSet);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        setHasOptionsMenu(true);
        View view = inflater.inflate(R.layout.fragment_roster, container, false);
        userListView = (ListView) view.findViewById(R.id.onlineUsers_list);
        groupsListView = (ListView) view.findViewById(R.id.groups_list);
        message = (TextView) view.findViewById(R.id.infoMessage);
        pb = (ProgressBar) view.findViewById(R.id.loader);
        refresh = (ImageView) view.findViewById(R.id.refresh);

        setConferenceAdapter();
        setListAdapter(null);

        if (!XMPPClient.getInstance().checkConnection()) {
            pb.setVisibility(View.GONE);
            message.setVisibility(View.VISIBLE);
            refresh.setImageDrawable(getResources().getDrawable(R.drawable.offline));
        } else
            refresh.setImageDrawable(getResources().getDrawable(R.drawable.onnline));

        userListView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                RosterModel model = (RosterModel) userListView.getAdapter().getItem(position);
                Intent intent = new Intent(getActivity(), IMChatActivity.class);
                intent.putExtra("receiver", model.getJID());
                intent.putExtra("nickname", model.getUsername());
                getActivity().startActivity(intent);
                XMPPSessionStorage.getInstance().updateReadConversationNoCallback(model.getJID(), true);
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

        refresh.setOnClickListener(new ImageView.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!XMPPClient.getInstance().checkConnection()) {

                    //TODO:  get port from settings instead ?
                    loginXMPP(SharedPrefs.getXMPPHost(getActivity()), 5222,
                            SharedPrefs.getXMPPUsername(getActivity()), SharedPrefs.getXMPPPassword(getActivity()));
                    message.setVisibility(View.GONE);

                } else if (!RESTClient.isDeviceConnectedToInternet(getActivity())) {

                    message.setVisibility(View.VISIBLE);
                    refresh.setImageDrawable(getResources().getDrawable(R.drawable.offline));
                    new ToastMaster(getActivity(), "No internet connection", false);
                } else {
                    final Context context = getActivity();
                    Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {

                                XMPPClient.getInstance().destroy(context);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    thread.start();
                }
            }
        });
        return view;
    }

    @Override
    public void notifyChanged() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (XMPPClient.getInstance().checkConnection()) {
                    refresh.setImageDrawable(getResources().getDrawable(R.drawable.onnline));
                    pb.setVisibility(View.GONE);
                    setRoster();
                    setConferenceAdapter();
                } else {
                    refresh.setImageDrawable(getResources().getDrawable(R.drawable.offline));
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        XMPPSessionStorage.getInstance().setCallback(this);
        if (XMPPClient.getInstance().checkConnection()) {
            pb.setVisibility(View.GONE);
            XMPPClient.getInstance().getRosterList();
            setRoster();
            setConferenceAdapter();
        } else {
            message.setVisibility(View.VISIBLE);
            refresh.setImageDrawable(getResources().getDrawable(R.drawable.offline));
        }
    }

    @Override
    public void onPause() {
        super.onResume();
        XMPPSessionStorage.getInstance().setCallback(null);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (loginTask != null) {
            if (!loginTask.isCancelled())
                loginTask.cancel(true);
            loginTask = null;
        }
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
                userListView.setVisibility(View.GONE);
                groupsListView.setVisibility(View.VISIBLE);
                if (groupsListView == null)
                    pb.setVisibility(View.VISIBLE);

                if (XMPPClient.getInstance().checkConnection()) {
                    Runnable r = new Runnable() {
                        public void run() {
                            XMPPClient.getInstance().getAllMUCs();
                        }
                    };
                    new Thread(r).start();
                }

                return true;
            }
            case R.id.more: {
                if (XMPPClient.getInstance().checkConnection()) {
                    showStutusSettings();
                } else
                    new ToastMaster(getActivity(), "Not connected to chat server", false);

                return true;
            }
            case R.id.newConference:
                if (XMPPClient.getInstance().checkConnection()) {
                    showNewConference();
                } else
                    new ToastMaster(getActivity(), "Not connected to chat server", false);

                return true;
        }
        return true;
    }

    private void setListAdapter(List<RosterModel> list) {
        if (list == null)
            list = new ArrayList<RosterModel>();
        Collections.sort(list);
        RosterAdapter adapter = new RosterAdapter(getActivity(), R.layout.item_roster, list);
        userListView.setAdapter(adapter);
    }

    private void setConferenceAdapter() {
        List<ConferenceModel> multiUserChatRooms = XMPPSessionStorage.getInstance().getXMPPConferenceData();
        if (multiUserChatRooms == null)
            multiUserChatRooms = new ArrayList<ConferenceModel>();
        ConferenceAdapter groupAdapter = new ConferenceAdapter(getActivity(), R.layout.item_roster_conference, multiUserChatRooms);
        groupsListView.setAdapter(groupAdapter);
    }

    public void setRoster() {
        setListAdapter(XMPPSessionStorage.getInstance().getXMPPData());
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
                if (statusMessage.equals(""))
                    XMPPClient.getInstance().setPresenceModeAndStatus(type, mode, presence);
                else
                    XMPPClient.getInstance().setPresenceModeAndStatus(type, mode, statusMsg);

                SharedPrefs.setXMPPUserData(getActivity(), presence, statusMsg);
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
                if (!name.getText().toString().isEmpty() || !subject.getText().toString().isEmpty() || !description.getText().toString().isEmpty()) {
                    if (!XMPPSessionStorage.getInstance().conferenceExist(name.getText().toString())) {

                        int response = XMPPClient.getInstance()
                                .createConference(name.getText().toString(),
                                        subject.getText().toString(),
                                        description.getText().toString());

                        if (XMPPClient.noErrors(response)) {
                            dialog.cancel();
                            XMPPClient.getInstance().getAllMUCs();
                        } else
                            Toast.makeText(getActivity(), XMPPClient.getResponseMessage(response), Toast.LENGTH_LONG).show();
                    } else
                        Toast.makeText(getActivity(), "Conference name all ready exist - change the name", Toast.LENGTH_SHORT).show();
                } else
                    Toast.makeText(getActivity(), "Include all fields", Toast.LENGTH_SHORT).show();
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

    private void loginXMPP(final String host, final int port, final String username, final String password) {
        pb.setVisibility(View.VISIBLE);
        loginTask = new AsyncTask<String, String, Integer>() {
            @Override
            protected Integer doInBackground(String... args) {
                //TOOD: (useof).setConnection(...)get Server info from storage/settings
                return XMPPClient.getInstance().setConnection(getActivity(), host, port, username, password);
            }

            @Override
            protected void onPostExecute(Integer code) {
                pb.setVisibility(View.GONE);

                if (!XMPPClient.noErrors(code)) {
                    new ToastMaster(getActivity(), "Could not sign in", false);
                    message.setVisibility(View.VISIBLE);
                    refresh.setImageDrawable(getResources().getDrawable(R.drawable.offline));
                }
            }
        }.execute();
    }

}