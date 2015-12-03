package org.dhis2.messenger.gui.activity;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import org.dhis2.messenger.gui.fragment.InboxFragment;
import org.dhis2.messenger.gui.fragment.InterpretationsFragment;
import org.dhis2.messenger.gui.fragment.MyProfileFragment;
import org.dhis2.messenger.gui.fragment.RosterFragment;
import org.dhis2.messenger.R;
import org.dhis2.messenger.gui.fragment.Stats;
import org.dhis2.messenger.core.rest.callback.UnreadMessagesCallback;
import org.dhis2.messenger.core.gcm.RegisterDevice;
import org.dhis2.messenger.SharedPrefs;
import org.dhis2.messenger.gui.ToastMaster;
import org.dhis2.messenger.core.xmpp.XMPPClient;
import org.dhis2.messenger.core.xmpp.XMPPSessionStorage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;


public class HomeActivity extends FragmentActivity implements UnreadMessagesCallback, UpdateUnreadMsg {

    private static final String SELECTED_FRAGMENT_POSITION = "selectedFragmentPos";

    @Bind(R.id.drawer_layout)
    DrawerLayout drawerLayout;
    @Bind(R.id.left_drawer)
    ListView drawerListView;

    //Drawer variables (Future development should use MaterialDialog library or something..)
    private CharSequence charSequenceTitle;
    private String[] menuTitles;
    private int[] menuIcons;
    private int drawerSelection;
    private ActionBarDrawerToggle actionBarDrawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        initializeDrawer(savedInstanceState);
        attachFragment(drawerSelection);
        checkGCM();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (drawerLayout != null && actionBarDrawerToggle != null) {
            actionBarDrawerToggle.syncState();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (drawerLayout != null && actionBarDrawerToggle != null) {
            actionBarDrawerToggle.onConfigurationChanged(newConfig);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (actionBarDrawerToggle.onOptionsItemSelected(item))
            return true;

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(SELECTED_FRAGMENT_POSITION, drawerSelection);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void setTitle(CharSequence title) {
        charSequenceTitle = title;
        if (drawerSelection == 0)
            getActionBar().setTitle(charSequenceTitle + " | " + SharedPrefs.getUnreadMessages(this));
        else if (drawerSelection == 1)
            getActionBar().setTitle(charSequenceTitle + " | " + XMPPSessionStorage.getInstance().getUnreadMessages());
        else
            getActionBar().setTitle(charSequenceTitle);
    }

    public void updateTitle() {
        getActionBar().setTitle(menuTitles[0] + " | " + SharedPrefs.getUnreadMessages(this));
    }

    @Override
    public void updateUnreadMsg(final int restNumber, int xmppNumber) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                List<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
                for (int i = 0; i < menuIcons.length; i++) {
                    HashMap<String, String> hm = new HashMap<String, String>();
                    if (i == 0)
                        hm.put("titles", menuTitles[i] + " | " + SharedPrefs.getUnreadMessages(getApplicationContext()));
                    else if (i == 1)
                        hm.put("titles", menuTitles[i] + " | " + restNumber);
                    else
                        hm.put("titles", menuTitles[i]);
                    hm.put("icons", Integer.toString(menuIcons[i]));
                    list.add(hm);
                }
                String[] key = {"icons", "titles"};
                int[] id = {R.id.listIcon, R.id.listTitle};
                drawerListView.setAdapter(new SimpleAdapter(getApplicationContext(), list, R.layout.item_drawer, key, id));

                if (drawerSelection == 1) {
                    getActionBar().setTitle(menuTitles[1] + " | " + restNumber);
                    Vibrator v = (Vibrator) getApplication().getSystemService(Context.VIBRATOR_SERVICE);
                    v.vibrate(150);
                } else {
                    new ToastMaster(getApplicationContext(), "New chat message", true);
                }
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        XMPPSessionStorage.getInstance().setHomeListener(null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (SharedPrefs.isUserLoggedIn(this)) {
            XMPPSessionStorage.getInstance().setHomeListener(this);
            // unreadMessagesHandler = new RESTUnreadMessages(this, this);
            // unreadMessagesHandler.execute();
            updateDHISMessages();

            if (XMPPClient.getInstance().checkConnection())
                manualUpdate(XMPPSessionStorage.getInstance().getUnreadMessages());
        } else
            logout();
    }

    @Override
    public void onBackPressed() {
        XMPPSessionStorage.getInstance().destroy();
        XMPPClient.getInstance().destroy(this);
        this.finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        XMPPClient.getInstance().destroy(this);
    }

    private void initializeDrawer(Bundle savedInstanceState) {
        charSequenceTitle = getTitle();
        menuTitles = getResources().getStringArray(R.array.view_array);
        menuIcons = new int[]{R.drawable.ic_mail_outline_white, R.drawable.ic_chat_white, R.drawable.ic_image_white,
                R.drawable.ic_person_white, R.drawable.ic_info_outline_white, R.drawable.ic_lock_outline_white};

        List<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
        for (int i = 0; i < menuIcons.length; i++) {
            HashMap<String, String> hm = new HashMap<String, String>();
            hm.put("titles", menuTitles[i]);
            hm.put("icons", Integer.toString(menuIcons[i]));
            list.add(hm);
        }
        String[] key = {"icons", "titles"};
        int[] id = {R.id.listIcon, R.id.listTitle};
        drawerListView.setAdapter(new SimpleAdapter(this, list, R.layout.item_drawer, key, id));
        drawerListView.setOnItemClickListener(new DrawerItemClickListener());
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setIcon(R.drawable.home);
        getActionBar().setHomeButtonEnabled(true);

        if (drawerLayout != null) {
            actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.drawable.ic_view_headline_white,
                    R.string.drawer_open, R.string.drawer_close) {
                public void onDrawerClosed(View view) {
                    super.onDrawerClosed(view);
                    setTitle(charSequenceTitle);
                    invalidateOptionsMenu();
                }

                public void onDrawerOpened(View drawerView) {
                    super.onDrawerOpened(drawerView);
                    getActionBar().setTitle("");
                    invalidateOptionsMenu();
                }
            };
            drawerLayout.setDrawerListener(actionBarDrawerToggle);
            drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        }

        if (savedInstanceState == null)
            drawerSelection = 0;

        else
            drawerSelection = savedInstanceState.getInt(SELECTED_FRAGMENT_POSITION);
    }

    public void updateDHISMessages() {
        List<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
        for (int i = 0; i < menuIcons.length; i++) {
            HashMap<String, String> hm = new HashMap<String, String>();
            if (i == 0)
                hm.put("titles", menuTitles[i] + " | " + SharedPrefs.getUnreadMessages(this));
            else if (i == 1)
                hm.put("titles", menuTitles[i] + " | " + XMPPSessionStorage.getInstance().getUnreadMessages());
            else
                hm.put("titles", menuTitles[i]);
            hm.put("icons", Integer.toString(menuIcons[i]));
            list.add(hm);
        }
        String[] key = {"icons", "titles"};
        int[] id = {R.id.listIcon, R.id.listTitle};
        drawerListView.setAdapter(new SimpleAdapter(this, list, R.layout.item_drawer, key, id));

        if (drawerSelection == 0) {
            getActionBar().setTitle(menuTitles[0] + " | " + SharedPrefs.getUnreadMessages(this));

            // Uhhm, removed this due to eternal loop
            //if (getSupportFragmentManager().findFragmentByTag("inbox") != null)
            //    getSupportFragmentManager().findFragmentByTag("inbox").onResume();
        }
    }

    private void manualUpdate(final int amount) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                List<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
                for (int i = 0; i < menuIcons.length; i++) {
                    HashMap<String, String> hm = new HashMap<String, String>();
                    if (i == 0)
                        hm.put("titles", menuTitles[i] + " | " + SharedPrefs.getUnreadMessages(getApplicationContext()));
                    else if (i == 1)
                        hm.put("titles", menuTitles[i] + " | " + amount);
                    else
                        hm.put("titles", menuTitles[i]);
                    hm.put("icons", Integer.toString(menuIcons[i]));
                    list.add(hm);
                }
                String[] key = {"icons", "titles"};
                int[] id = {R.id.listIcon, R.id.listTitle};
                drawerListView.setAdapter(new SimpleAdapter(getApplicationContext(), list, R.layout.item_drawer, key, id));

                if (drawerSelection == 1)
                    getActionBar().setTitle(menuTitles[1] + " | " + amount);

            }
        });
    }

    private void attachFragment(int position) {
        Fragment fragment;
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (position == 0) {
            InboxFragment f = (InboxFragment) getSupportFragmentManager().findFragmentByTag("inbox");
            if (f == null)
                f = new InboxFragment();

            transaction.replace(R.id.content_frame, f, "inbox");
            transaction.addToBackStack(null);
            transaction.commit();
            drawerSelection = 0;
        } else if (position == 1) {
            fragment = new RosterFragment();
            transaction.replace(R.id.content_frame, fragment);
            transaction.commit();
            drawerSelection = 1;
        } else if (position == 2) {
            InterpretationsFragment f = (InterpretationsFragment) getSupportFragmentManager().findFragmentByTag("interpretations");
            if (f == null)
                f = new InterpretationsFragment();

            transaction.replace(R.id.content_frame, f, "interpretations");
            transaction.addToBackStack(null);
            transaction.commit();
            drawerSelection = 2;
        } else if (position == 3) {
            fragment = new MyProfileFragment();
            transaction.replace(R.id.content_frame, fragment);
            transaction.commit();
            drawerSelection = 3;
        } else if (position == 4) {
            fragment = new Stats();
            transaction.replace(R.id.content_frame, fragment);
            transaction.commit();
            drawerSelection = 4;
        } else {
            logout();

        }
        setTitle(menuTitles[position]);
        drawerListView.setItemChecked(position, true);
        drawerLayout.closeDrawer(drawerListView);
    }

    private void logout() {
        getApplication().getSharedPreferences(LoginActivity.PREFS_NAME, getApplication().MODE_PRIVATE).edit().remove("password").commit();
        RegisterDevice rd = new RegisterDevice(this);
        rd.removeGcmId();
        SharedPrefs.eraseData(this);
        XMPPClient.getInstance().destroy(this);
        XMPPSessionStorage.getInstance().destroy();

        Intent intent = new Intent(this, LoginActivity.class);
        this.startActivity(intent);
        finish();
    }

    protected void checkGCM() {
        RegisterDevice gcm = new RegisterDevice(this);

        if (gcm.checkPlayServices()) {
            gcm.checkRegistration();
        }
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            attachFragment(position);
        }
    }
}//End of class Home