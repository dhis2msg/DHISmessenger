package org.dhis2.messaging.Fragments;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.os.AsyncTask;
import android.support.v4.view.MenuItemCompat;
import android.view.*;
import android.widget.*;

import org.dhis2.messaging.HomeActivity;
import org.dhis2.messaging.NewMessageActivity;
import org.dhis2.messaging.RESTChatActivity;
import org.dhis2.messaging.Utils.Adapters.InboxAdapter;
import org.dhis2.messaging.Models.InboxModel;
import org.dhis2.messaging.R;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.widget.AdapterView.OnItemClickListener;

import org.dhis2.messaging.Utils.REST.APIPaths;
import org.dhis2.messaging.Utils.REST.RESTClient;
import org.dhis2.messaging.Utils.REST.Response;
import org.dhis2.messaging.Utils.SharedPrefs;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
//import android.widget.SearchView;
//import android.widget.SearchView.OnQueryTextListener;

public class InboxFragment extends Fragment { // implements OnQueryTextListener {
    private final String MESSAGES_PR_PAGE = "20";
    private ListView listView;
    private ProgressBar loader;
    private View foot;
    private List<InboxModel> list;
    private int currentPage, totalPages;
    //private TextView searchBar;

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

        if (list == null) {
            list = new ArrayList<InboxModel>();
            setLoader(true);
            getInboxPage();
        } else
            setAdapter();

        listView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                if (view == foot) {
                    currentPage++;
                    getInboxPage();
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

        return root;
    }

    public void addToInboxList(List<InboxModel> list) {
        for (InboxModel model : list) {
            this.list.add(model);
        }
        setMoreMessagesBtn();
        setAdapter();
        setLoader(false);
    }

    public void setPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public void setAdapter() {
        InboxAdapter adapter = new InboxAdapter(getActivity(), R.layout.item_rest_inbox, list);
        listView.setAdapter(adapter);
    }

    public void setMoreMessagesBtn() {
        if (morePages() && listView.getFooterViewsCount() < 1) {
            listView.addFooterView(foot);
        }
        else if (morePages() && listView.getFooterViewsCount() == 1) {
        }
        else
            listView.removeFooterView(foot);
    }

    public void setLoader(boolean on) {
        if (on)
            loader.setVisibility(View.VISIBLE);
        else
            loader.setVisibility(View.GONE);
    }

    public boolean morePages() {
        if (currentPage  < totalPages)
            return true;
        return false;
    }

    public void getInboxPage() {
        if (list == null) {
            setLoader(true);
        }
        getInboxElements(currentPage);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_inbox, menu);

        /*SearchView searchView = (SearchView) menu.findItem(R.id.mi_search).getActionView();
        searchView.setOnQueryTextListener( this);*/

        /*MenuItem item = menu.findItem(R.id.mi_search);
        SearchView sv = new SearchView(((HomeActivity) getActivity()).getSupportActionBar().getThemedContext());
        MenuItemCompat.setShowAsAction(item, MenuItemCompat.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW | MenuItemCompat.SHOW_AS_ACTION_IF_ROOM);
        MenuItemCompat.setActionView(item, sv);
        sv.setOnQueryTextListener(new OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                System.out.println("search query submit");
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                System.out.println("tap");
                return false;
            }
        });*/
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.refresh: {
                list = new ArrayList<InboxModel>();
                getInboxElements(1);
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
            /*case R.id.mi_search:{
                MenuItem i = menu.add("Search");
                SearchView sv = new SearchView(getActionBar().getThemedContext());
                item.setActionView(sv);
                item.setIcon(R.drawable.abc_ic_search);
                item.setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW
                        | MenuItem.SHOW_AS_ACTION_IF_ROOM);
            }*/
        }
        return true;
    }

    private void getInboxElements(final int page) {
        new AsyncTask<Integer, String, Integer>() {
            List<InboxModel> tempList = new ArrayList<InboxModel>();
            String server = SharedPrefs.getServerURL(getActivity());
            String api = server + APIPaths.FIRST_PAGE_MESSAGES + APIPaths.INBOX_FIELDS;
            String auth = SharedPrefs.getCredentials(getActivity());

            @Override
            protected Integer doInBackground(Integer... args) {
                try {
                    JSONObject json;
                    Response response;
                    if (page == 1)
                        response = RESTClient.get(api + "&pageSize=" + MESSAGES_PR_PAGE, auth);
                    else
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

                            if(!row.isNull("lastSenderFirstname") ||!row.isNull("lastSenderSurname"))
                                lastSender = row.getString("lastSenderFirstname") + " " + row.getString("lastSenderSurname");
                            boolean read = Boolean.parseBoolean(row.getString("read"));
                            tempList.add(new InboxModel(subject, date, id, lastSender, read));
                        }

                        return response.getCode();
                    }
                    else
                        return response.getCode();
                } catch (JSONException e) {
                    e.printStackTrace();
                    return -1;
                }
            }

            @Override
            protected void onPostExecute(Integer code) {
                setLoader(false);
                if (RESTClient.noErrors(code))
                    addToInboxList(tempList);
                else if (code == -1)
                    Toast.makeText(getActivity(), "Error - JSONException converting error", Toast.LENGTH_LONG).show();
                else
                    Toast.makeText(getActivity(), "Error" + RESTClient.getErrorMessage(code), Toast.LENGTH_LONG).show();
            }
        }.execute();
    }

   /* @Override
    public boolean onQueryTextSubmit(String query) {
        //newText = newText.isEmpty() ? "" : "Query so far: " + newText;
        //mSearchText.setText(newText);
        //mSearchText.setTextColor(Color.GREEN);
        Toast.makeText(getActivity(), "submit", Toast.LENGTH_LONG).show();
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        //Toast.makeText(this, "Searching for: " + query + "...", Toast.LENGTH_SHORT).show();
        //mSearchText.setText("Searching for: " + query + "...");
        //mSearchText.setTextColor(Color.RED);
        //return true;
        Toast.makeText(getActivity(), "change", Toast.LENGTH_LONG).show();
        return true;
    }*/

}//End of class Conversation fragment