package org.dhis2.messenger.gui.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.transition.Fade;
import android.transition.Slide;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.transition.TransitionSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.dhis2.messenger.core.rest.async.RESTMarkConversationRead;
import org.dhis2.messenger.gui.activity.HomeActivity;
import org.dhis2.messenger.gui.activity.NewMessageActivity;
import org.dhis2.messenger.gui.activity.RESTChatActivity;
import org.dhis2.messenger.model.InboxModel;
import org.dhis2.messenger.R;
import org.dhis2.messenger.core.rest.APIPath;
import org.dhis2.messenger.core.rest.RESTClient;
import org.dhis2.messenger.core.rest.RESTSessionStorage;
import org.dhis2.messenger.core.rest.Response;
import org.dhis2.messenger.gui.adapter.InboxAdapter;
import org.dhis2.messenger.core.rest.async.RESTDeleteConversation;
import org.dhis2.messenger.SharedPrefs;
import org.dhis2.messenger.gui.ToastMaster;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class InboxFragment extends Fragment {
    private final int MESSAGES_PR_PAGE = 10;

    //gui elements:
    private ListView listView;
    private ProgressBar loader;
    private View foot;
    //data:
    private List<InboxModel> list;
    private int currentPage, totalPages;

    AsyncTask asyncTask;

    public InboxFragment(){
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

    private BroadcastReceiver inboxReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    currentPage = RESTSessionStorage.getInstance().getInboxCurrentPage();
                    list = new ArrayList<InboxModel>();
                    refresh(1, false);
                }
            });
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        RESTSessionStorage.getInstance().setInboxPageSize(MESSAGES_PR_PAGE);

        if(Build.VERSION.SDK_INT >= 21){
            TransitionInflater infalter = TransitionInflater.from(getActivity());
            Transition transition = infalter.inflateTransition(R.transition.transition_inbox_exit);
            getActivity().getWindow().setExitTransition(transition);
        }

        setRetainInstance(true);
        setHasOptionsMenu(true);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.common_list_view, container, false);
        listView = (ListView) root.findViewById(R.id.list);
        loader = (ProgressBar) root.findViewById(R.id.loader);
        foot = inflater.inflate(R.layout.listview_footer, null);
        currentPage = totalPages = 1;
        totalPages = RESTSessionStorage.getInstance().getInboxTotalPages();
        list = new ArrayList<InboxModel>();
        //setAdapter();

        listView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                if (view == foot) {
                    currentPage++;
                    RESTSessionStorage.getInstance().setInboxCurrentPage(currentPage);
                    list = new ArrayList<InboxModel>();
                    refresh(1, false);
                } else {
                    InboxModel model = (InboxModel) listView.getAdapter().getItem(position);

                    // Update the read field if the conversation was unread
                    if(!model.getRead()) {
                        Context context = getActivity();
                        int unreadMessages = Integer.parseInt(SharedPrefs.getUnreadMessages(context));
                        unreadMessages--;
                        SharedPrefs.setUnreadMessages(context, Integer.toString(unreadMessages));
                    }

                    model.setRead(true);

                    // Creating the RESTChatActivity
                    Intent intent = new Intent(getActivity(), RESTChatActivity.class);
                    intent.putExtra("id", model.getId());
                    intent.putExtra("subject", model.getSubject());
                    intent.putExtra("read", model.getRead());
                    // pass the index of the item:
                    intent.putExtra("index", position);


                    // Transition animation
                    ActivityOptionsCompat compat = ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(),null);

                    if(Build.VERSION.SDK_INT >= 16) {
                        getActivity().startActivity(intent, compat.toBundle());
                    }else{
                        getActivity().startActivity(intent);
                    }
                    getActivity().overridePendingTransition(R.anim.right_to_center, R.anim.center_to_left);
                }
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                                           final int pos, long id) {
                // TODO Auto-generated method stub
                final PopupWindow popup = new PopupWindow(getActivity());
                final InboxModel im = (InboxModel) listView.getAdapter().getItem(pos);
                RelativeLayout viewGroup = (RelativeLayout) getActivity().findViewById(R.id.someLayout);
                LayoutInflater layoutInflater = (LayoutInflater) getActivity().getSystemService(getActivity().LAYOUT_INFLATER_SERVICE);
                View layout = layoutInflater.inflate(R.layout.popupwindow_inbox, viewGroup);
                TextView tv = (TextView) layout.findViewById(R.id.name);
                tv.setText("Message: " + im.getSubject());
                Button markRead = (Button) layout.findViewById(R.id.markRead);
                Button delete = (Button) layout.findViewById(R.id.delete);
                markRead.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        new RESTMarkConversationRead(getActivity(), (HomeActivity) getActivity(), pos).execute(im.getId());
                        popup.dismiss();
                    }
                });
                delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new RESTDeleteConversation(getActivity(), (HomeActivity) getActivity(), SharedPrefs.getUserId(getActivity()), pos).execute(im.getId());
                        popup.dismiss();
                    }
                });
                if (im.getRead()) {
                    markRead.setVisibility(View.GONE);
                }

                popup.setWidth(450);
                popup.setHeight(150);
                popup.setContentView(layout);
                popup.setFocusable(true);
                popup.setBackgroundDrawable(getResources().getDrawable(R.drawable.border_transparent));
                popup.showAtLocation(getView(), Gravity.CENTER, 0, 0);
                popup.setTouchable(true);

                return true;
            }
        });
        return root;
    }

    @Override
    public void onPause() {
        super.onPause();
        //list = new ArrayList<InboxModel>();
        RESTSessionStorage.getInstance().setInboxCurrentPage(currentPage);
        RESTSessionStorage.getInstance().setInboxTotalPages(totalPages);

    }

    @Override
    public void onResume() {
        super.onResume();
        list = new ArrayList<InboxModel>();
        currentPage = RESTSessionStorage.getInstance().getInboxCurrentPage();
        totalPages = RESTSessionStorage.getInstance().getInboxTotalPages();

        refresh(1, false);
        getActivity().registerReceiver(inboxReceiver, new IntentFilter("org.dhis2.messenger.gui.activity.HomeActivity"));
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_inbox, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(inboxReceiver);
        if (asyncTask != null) {
            if (!asyncTask.isCancelled())
                asyncTask.cancel(true);
            asyncTask = null;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.refresh: {
                if (!RESTClient.isDeviceConnectedToInternet(getActivity())) {
                    Toast.makeText(getActivity(), "No internet connection", Toast.LENGTH_SHORT).show();
                } //since we are caching ? access to the internet to display things isn't nessesary. ?
                list = new ArrayList<InboxModel>();
                refresh(1, true); //skip the cache and force refresh!
                return true;
            }
            case R.id.new_message: {
                Intent intent = new Intent(getActivity(), NewMessageActivity.class);
                startActivity(intent);
                return true;
            }
            case R.id.sort: {
                Collections.sort(list);
                setAdapter();
                return true;
            }
        }
        return true;
    }

    //----------------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------------

    public void addToInboxList(List<InboxModel> list, int page) {
        // Vladislav: This is a workaround.
        // The problem: addinboxList gets called twice for each page.
        // As a consequence each page is displayed twice.
        // I couldn't find a solution.
        if(page == 1) {
            this.list.clear();
        }
        this.list.addAll(list);
        setMoreMessagesBtn();

        if (page == currentPage) {
            setAdapter();
        }
        setLoader(false);
    }

    public void setPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public void setAdapter() {
        if (list != null && getActivity() != null) {
            InboxAdapter adapter = new InboxAdapter(getActivity(), R.layout.item_rest_inbox, list);
            listView.setAdapter(adapter);
        }
        /*else if (listView.getAdapter() !=null){
            ((InboxAdapter) listView.getAdapter()).clear();
            ((InboxAdapter) listView.getAdapter()).addAll(list); //update your adapter's data
            ((InboxAdapter) listView.getAdapter()).notifyDataSetChanged();
        }*/
    }

    public void setMoreMessagesBtn() {
        if (morePages() && listView.getFooterViewsCount() < 1) {
            listView.addFooterView(foot);
        } else if (morePages() && listView.getFooterViewsCount() == 1) {
            //listView.addFooterView(foot); ///edit ?
        } else {
            listView.removeFooterView(foot);
        }
    }

    public void setLoader(boolean on) {
        if (on) {
            loader.setVisibility(View.VISIBLE);
        } else {
            loader.setVisibility(View.GONE);
        }
    }

    public boolean morePages() {
        if (currentPage < totalPages) {
            return true;
        } else {
            return false;
        }
    }

    private void refresh(int i, boolean skipCache) {
        if (i <= currentPage && i > 0) {
            getInboxElements(i, skipCache);
        }
    }

    private void getInboxElements(final int page, final boolean skipCache) {
        //TODO: Needs to be informed by GCM ! about changes to the lists!

        asyncTask = new AsyncTask<Integer, String, Integer>() {
            Boolean gotListFromCache = false;
            List<InboxModel> cached = null;
            List<InboxModel> tempList = new ArrayList<InboxModel>();
            String server = SharedPrefs.getServerURL(getActivity());

            // Root path to message conversations
            String mcAPIRootPath = server + APIPath.FIRST_PAGE_MESSAGES;
            String mcAPIPath = mcAPIRootPath + APIPath.INBOX_FIELDS;

            String auth = SharedPrefs.getCredentials(getActivity());

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                if (listView.getAdapter() == null) {
                    setLoader(true);
                }
            }

            @Override
            protected Integer doInBackground(Integer... args) {
                try {
                    int responseCode;
                    JSONObject json;
                    Response response;
                    int unreadMessages = 0;

                    /* check if we have the page in cache */
                    cached = RESTSessionStorage.getInstance().getInboxModelList(page);
                    //TODO: Use Google Cloud Messaging to find out that cache is outdated !
                    if (!cached.isEmpty() && !skipCache) { // pageList is cached:
                        gotListFromCache = true;
                        tempList.addAll(cached);
                        totalPages = RESTSessionStorage.getInstance().getInboxTotalPages();
                        //Log.v("InboxFragment", "from cache");
                        responseCode = RESTClient.OK;
                    } else { //get it from the server
                        gotListFromCache = false;
                        //Log.v("InboxFragment", "NOT FROM CACHE! cached is empty? " + cached.isEmpty() + " skipCache? " + skipCache + " page= " + page);
                        /*if (page == 1) {//this seems unnecessary :
                            response = RESTClient.get(mcAPIPath + "&pageSize=" + MESSAGES_PR_PAGE, auth);
                            list = new ArrayList<>();
                        } else {*/

                            response = RESTClient.get(mcAPIPath + "&pageSize=" + MESSAGES_PR_PAGE + "&page=" + page, auth);
                        responseCode = response.getCode();

                        //}
                        // parse response into page (list of models):
                        if (RESTClient.noErrors(response.getCode())) {
                            json = new JSONObject(response.getBody());
                            JSONObject pager = json.getJSONObject("pager");
                            JSONArray allConversations = new JSONArray(json.getString("messageConversations"));
                            setPages(Integer.parseInt(pager.getString("pageCount")));

                            for (int i = 0; i < allConversations.length(); i++) {
                                JSONObject row = allConversations.getJSONObject(i);
                                String id = row.getString("id");
                                String subject = row.getString("name");
                                String date = row.getString("lastMessage");
                                String lastSender = "";

                                if (!row.isNull("lastSenderFirstname") || !row.isNull("lastSenderSurname"))
                                    lastSender = row.getString("lastSenderFirstname") + " " + row.getString("lastSenderSurname");

                                // Find out if the conversation is unread
                                Response mcResponse = RESTClient.get(mcAPIRootPath + "/" + id + "?fields=read", auth);
                                JSONObject messageConversation = new JSONObject(mcResponse.getBody());

                                Boolean read = Boolean.parseBoolean(messageConversation.getString("read"));
                                tempList.add(new InboxModel(subject, date, id, lastSender, read));

                                // Count the number of read messages
                                if (!read)
                                    unreadMessages++;
                            }

                            // Save the number of unread messages
                            Context context = getActivity();
                            SharedPrefs.setUnreadMessages(context, Integer.toString(unreadMessages));
                        }
                    }

                    return responseCode;

                } catch (JSONException e) {
                    e.printStackTrace();
                    return RESTClient.JSON_EXCEPTION;
                } /*catch (Exception e) {
                //TODO: remove this after testing. catching all exceptions is not good here.
                // As some of them might be meant for other parts of the code (?)
                    e.printStackTrace();
                    return RESTClient.OK; // ???
                }*/
            }

            @Override
            protected void onPostExecute(Integer code) {
                setLoader(false);
                if (gotListFromCache || RESTClient.noErrors(code)) {
                    if (!gotListFromCache) {
                        RESTSessionStorage.getInstance().setInboxModelList(page, tempList);
                        RESTSessionStorage.getInstance().setInboxTotalPages(totalPages);
                    }

                    // Update the number of unread messages
                    ((HomeActivity) getActivity()).updateDHISMessages();

                    new ToastMaster(getActivity(), "Page: " + currentPage + "/ " + totalPages, false);
                    addToInboxList(tempList, page);
                    refresh(page + 1, skipCache);
                } else if (code == RESTClient.JSON_EXCEPTION)
                    new ToastMaster(getActivity(), "Something went wrong. \nTry to refresh", false);
                    //Toast.makeText(getActivity(), "Something went wrong. \nTry to refresh", Toast.LENGTH_SHORT).show();
                else
                    new ToastMaster(getActivity(), "Error" + RESTClient.getErrorMessage(code), false);
                //Toast.makeText(getActivity(), "Error" + RESTClient.getErrorMessage(code), Toast.LENGTH_SHORT).show();
            }
        }.execute();
    }
}