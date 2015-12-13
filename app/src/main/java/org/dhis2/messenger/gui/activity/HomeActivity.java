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
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import org.dhis2.messenger.R;
import org.dhis2.messenger.SharedPrefs;
import org.dhis2.messenger.core.DiskStorage;
import org.dhis2.messenger.core.gcm.RegisterDevice;
import org.dhis2.messenger.core.rest.RESTSessionStorage;
import org.dhis2.messenger.core.rest.callback.UnreadMessagesCallback;
import org.dhis2.messenger.core.xmpp.XMPPClient;
import org.dhis2.messenger.core.xmpp.XMPPSessionStorage;
import org.dhis2.messenger.gui.Section;
import org.dhis2.messenger.gui.ToastMaster;
import org.dhis2.messenger.gui.fragment.InboxFragment;
import org.dhis2.messenger.gui.fragment.InterpretationsFragment;
import org.dhis2.messenger.gui.fragment.MyProfileFragment;
import org.dhis2.messenger.gui.fragment.RosterFragment;
import org.dhis2.messenger.gui.fragment.Stats;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class HomeActivity extends FragmentActivity implements UnreadMessagesCallback, UpdateUnreadMsg {

    private static final String SHOWN_SECTION_KEY = "shownSection";

    @Bind(R.id.drawer_layout)
    DrawerLayout drawerLayout;

    @Bind(R.id.left_drawer)
    ListView drawerListView;

    //Drawer variables (Future development should use MaterialDialog library or something..)
    private CharSequence charSequenceTitle;
    private String[] menuTitles;
    private int[] menuIcons;
    private ActionBarDrawerToggle actionBarDrawerToggle;

    private Section shownSection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        initializeDrawer(savedInstanceState);
        attachFragment(shownSection);
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
        outState.putSerializable(SHOWN_SECTION_KEY, shownSection);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void setTitle(CharSequence title) {
        charSequenceTitle = title;
        switch (shownSection) {
            case MESSAGES:
                getActionBar().setTitle(charSequenceTitle + " | " + SharedPrefs.getUnreadMessages(this));
                break;
            case CHAT:
                getActionBar().setTitle(charSequenceTitle + " | " + XMPPSessionStorage.getInstance().getUnreadMessages());
                break;
            default:
                getActionBar().setTitle(charSequenceTitle);
        }
    }

    private void updateUnreadMessages(int restMessages, int xmppMessages) {
        List<HashMap<String, String>> data = new ArrayList<HashMap<String, String>>();
        for (int i = 0; i < menuIcons.length; i++) {
            String titleTail = "";
            switch (Section.values()[i]) {
                case MESSAGES:
                    titleTail = " | " + restMessages;
                    break;
                case CHAT:
                    titleTail = " | " + xmppMessages;
            }
            HashMap<String, String> hm = new HashMap<String, String>();
            hm.put("titles", menuTitles[i] + titleTail);
            hm.put("icons", Integer.toString(menuIcons[i]));
            data.add(hm);
        }
        String[] key = {"icons", "titles"};
        int[] id = {R.id.listIcon, R.id.listTitle};

        ListAdapter adapter = new SimpleAdapter(this, data, R.layout.item_drawer, key, id);
        drawerListView.setAdapter(adapter);

        switch (shownSection) {
            case MESSAGES:
                getActionBar().setTitle(menuTitles[0] + " | " + restMessages);
                break;
            case CHAT:
                getActionBar().setTitle(menuTitles[1] + " | " + xmppMessages);
        }
    }

    @Override
    public void updateUnreadMsg(final int restNumber, final int xmppNumber) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                updateUnreadMessages(restNumber, xmppNumber);

                // TODO: this if statement is crazy, please look at it
                if (shownSection == Section.CHAT) {
                    Vibrator v = (Vibrator) getApplication().getSystemService(Context.VIBRATOR_SERVICE);
                    v.vibrate(150);
                } else {
                    new ToastMaster(getApplicationContext(), "New chat message", true);
                }
            }
        });
    }

    @Override
    public void updateDHISMessages() {
        int restMessages = Integer.valueOf(SharedPrefs.getUnreadMessages(this));
        int xmppMessages = XMPPSessionStorage.getInstance().getUnreadMessages();

        updateUnreadMessages(restMessages, xmppMessages);
    }

    @Override
    protected void onPause() {
        Log.v("HomeActivity", "onPause()");
        super.onPause();
        XMPPSessionStorage.getInstance().setHomeListener(null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (SharedPrefs.isUserLoggedIn(this)) {
            XMPPSessionStorage.getInstance().setHomeListener(this);
            updateDHISMessages();
        } else {
            logout();
        }
    }

    @Override
    public void onBackPressed() {
        Log.v("HomeActivity", "onBackPressed()");
        this.logout();
    }

    @Override
    protected void onDestroy() {
        Log.v("HomeActivity", "onDestroy()");
        super.onDestroy();
        XMPPClient.getInstance().destroy(this);
        RESTSessionStorage.getInstance().destroy();
        DiskStorage.getInstance().destroy();
    }

    @Override
    protected void onStop() {
        Log.v("HomeActivity", "onStop()");
        super.onStop();
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

        if (shownSection == null) {
            shownSection = Section.MESSAGES;
        } else {
            shownSection = (Section) savedInstanceState.get(SHOWN_SECTION_KEY);
        }
    }

    private void attachFragment(Section section) {
        shownSection = section;

        Fragment fragment;
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        switch (section) {
            case MESSAGES:
                fragment = getSupportFragmentManager().findFragmentByTag("inbox");
                if (fragment == null) {
                    fragment = new InboxFragment();
                }

                transaction.replace(R.id.content_frame, fragment, "inbox");
                transaction.addToBackStack(null);
                transaction.commit();

                break;
            case CHAT:
                fragment = new RosterFragment();
                transaction.replace(R.id.content_frame, fragment);
                transaction.commit();

                break;
            case INTERPRETATIONS:
                fragment = getSupportFragmentManager().findFragmentByTag("interpretations");
                if (fragment == null) {
                    fragment = new InterpretationsFragment();
                }

                transaction.replace(R.id.content_frame, fragment, "interpretations");
                transaction.addToBackStack(null);
                transaction.commit();

                break;
            case PROFILE:
                fragment = new MyProfileFragment();
                transaction.replace(R.id.content_frame, fragment);
                transaction.commit();

                break;
            case STATS:
                fragment = new Stats();
                transaction.replace(R.id.content_frame, fragment);
                transaction.commit();

                break;
            case SIGN_OUT:
                logout();
                break;
            default:
                throw new IllegalArgumentException("Unsupported section");
        }

        setTitle(menuTitles[shownSection.ordinal()]);
        drawerListView.setItemChecked(shownSection.ordinal(), true);
        drawerLayout.closeDrawer(drawerListView);
    }

    private void logout() {
        getApplication().getSharedPreferences(LoginActivity.PREFS_NAME, getApplication().MODE_PRIVATE).edit().remove("password").commit();
        RegisterDevice rd = new RegisterDevice(this);
        rd.removeGcmId();
        SharedPrefs.eraseData(this);
        XMPPClient.getInstance().destroy(this);
        XMPPSessionStorage.getInstance().destroy();
        RESTSessionStorage.getInstance().destroy();

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
            attachFragment(Section.values()[position]);
        }
    }

}
