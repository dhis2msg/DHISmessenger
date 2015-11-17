package org.dhis2.messaging.Fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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

import org.dhis2.messaging.Activities.HomeActivity;
import org.dhis2.messaging.Activities.NewMessageActivity;
import org.dhis2.messaging.Activities.RESTChatActivity;
import org.dhis2.messaging.Models.InboxModel;
import org.dhis2.messaging.R;
import org.dhis2.messaging.REST.APIPath;
import org.dhis2.messaging.REST.RESTClient;
import org.dhis2.messaging.REST.Response;
import org.dhis2.messaging.Utils.Adapters.InboxAdapter;
import org.dhis2.messaging.Utils.AsyncroniousTasks.RESTDeleteMessage;
import org.dhis2.messaging.Utils.AsyncroniousTasks.RESTMarkRead;
import org.dhis2.messaging.Utils.SharedPrefs;
import org.dhis2.messaging.Utils.UserInterface.ToastMaster;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class InboxFragment extends Fragment {
    private final String MESSAGES_PR_PAGE = "25";
    //Memory store
    AsyncTask asyncTask;
    private ListView listView;
    private ProgressBar loader;
    private View foot;
    private List<InboxModel> list;
    private int currentPage, totalPages;
    private BroadcastReceiver inboxReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    refresh(1);
                }
            });
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        list = new ArrayList<InboxModel>();
        //setAdapter();


        listView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                if (view == foot) {
                    currentPage++;
                    refresh(1);
                } else {
                    InboxModel model = (InboxModel) listView.getAdapter().getItem(position);
                    Intent intent = new Intent(getActivity(), RESTChatActivity.class);
                    intent.putExtra("id", model.getId());
                    intent.putExtra("subject", model.getSubject());
                    intent.putExtra("read", model.getRead());
                    getActivity().startActivity(intent);
                    getActivity().overridePendingTransition(R.anim.right_to_center, R.anim.center_to_left);
                }
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                                           int pos, long id) {
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

                        new RESTMarkRead(getActivity(), (HomeActivity) getActivity()).execute(im.getId());
                        popup.dismiss();
                    }
                });
                delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new RESTDeleteMessage(getActivity(), (HomeActivity) getActivity(), SharedPrefs.getUserId(getActivity())).execute(im.getId());
                        popup.dismiss();
                    }
                });
                if (im.getRead())
                    markRead.setVisibility(View.GONE);

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
    public void onResume() {
        super.onResume();
        list = new ArrayList<InboxModel>();
        refresh(1);
        getActivity().registerReceiver(inboxReceiver, new IntentFilter("org.dhis2.messaging.Activities.HomeActivity"));
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
                if (RESTClient.isDeviceConnectedToInternett(getActivity())) {
                    list = new ArrayList<InboxModel>();
                    refresh(1);
                } else
                    Toast.makeText(getActivity(), "No internet connection", Toast.LENGTH_SHORT).show();

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

    public void addToInboxList(List<InboxModel> list, int page) {
        for (InboxModel model : list) {
            this.list.add(model);
        }

        setMoreMessagesBtn();

        if (page == currentPage)
            setAdapter();

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
        } else
            listView.removeFooterView(foot);
    }

    public void setLoader(boolean on) {
        if (on)
            loader.setVisibility(View.VISIBLE);
        else
            loader.setVisibility(View.GONE);
    }

    public boolean morePages() {
        if (currentPage < totalPages)
            return true;
        return false;
    }

    private void refresh(int i) {
        if (i <= currentPage)
            getInboxElements(i);
    }

    private void getInboxElements(final int page) {
        asyncTask = new AsyncTask<Integer, String, Integer>() {
            List<InboxModel> tempList = new ArrayList<InboxModel>();
            String server = SharedPrefs.getServerURL(getActivity());
            String api = server + APIPath.FIRST_PAGE_MESSAGES + APIPath.INBOX_FIELDS;
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
                    JSONObject json;
                    Response response;
                    if (page == 1) {
                        response = RESTClient.get(api + "&pageSize=" + MESSAGES_PR_PAGE, auth);
                        list = new ArrayList<InboxModel>();
                    } else
                        response = RESTClient.get(api + "&pageSize=" + MESSAGES_PR_PAGE + "&page=" + page, auth);

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
                            boolean read = Boolean.parseBoolean(row.getString("read"));
                            tempList.add(new InboxModel(subject, date, id, lastSender, read));
                        }

                        return response.getCode();
                    } else
                        return response.getCode();
                } catch (JSONException e) {
                    e.printStackTrace();
                    return RESTClient.JSON_EXCEPTION;
                }
            }

            @Override
            protected void onPostExecute(Integer code) {
                setLoader(false);
                if (RESTClient.noErrors(code)) {
                    addToInboxList(tempList, page);
                    refresh(page + 1);
                } else if (code == RESTClient.JSON_EXCEPTION)
                    new ToastMaster(getActivity(), "Something went wrong. \nTry to refresh", false);
                    //Toast.makeText(getActivity(), "Something went wrong. \nTry to refresh", Toast.LENGTH_SHORT).show();
                else
                    new ToastMaster(getActivity(), "Error" + RESTClient.getErrorMessage(code), false);
                //Toast.makeText(getActivity(), "Error" + RESTClient.getErrorMessage(code), Toast.LENGTH_SHORT).show();
            }
        }.execute();
    }
}//End of class Conversation fragment