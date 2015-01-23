package org.dhis2.messaging.Fragments;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Bitmap;
import android.widget.ImageView;

import org.dhis2.messaging.Utils.Adapters.InterpretationAdapter;
import org.dhis2.messaging.Utils.AsyncTasks.Interfaces.InterpretationCallback;
import org.dhis2.messaging.Utils.AsyncTasks.InterpretationHandler;
import org.dhis2.messaging.Models.InterpretationModel;
import org.dhis2.messaging.R;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;

public class InterpretationsFragment extends Fragment implements InterpretationCallback {
    private ListView listView;
    private ProgressBar loader;
    private ImageView moreInterpretations;
    private View foot;
    private int currentPage, totalPages;
    private List<InterpretationModel> list = new ArrayList<InterpretationModel>();

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_interpretation, container, false);
        listView = (ListView) view.findViewById(R.id.list);
        loader = (ProgressBar) view.findViewById(R.id.loader);
        foot = inflater.inflate(R.layout.listview_footer, null);
        moreInterpretations = (ImageView) foot.findViewById(R.id.moreMessages);
        currentPage = totalPages = 1;

        moreInterpretations.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (morePages()) {
                    currentPage++;
                    getInterpretations(currentPage);
                }
            }
        });
        if (list.isEmpty())
            getInterpretations(currentPage);
        else
            setAdapter();
        return view;
    }

    @Override
    public void updateList(List<InterpretationModel> list) {
        this.list = list;
        setAdapter();
        setLoader(false);
        setMoreInterpretationsBtn();

        if (currentPage == 1) {
            if (list.get(0) != null)
                new InterpretationHandler(this).addPicture(getActivity(), list.get(0));
        } else if (list.get((currentPage - 1) * 5) != null)
            new InterpretationHandler(this).addPicture(getActivity(), list.get((currentPage - 1) * 5));
    }

    @Override
    public void updatePages(int totalPages) {
        this.totalPages = totalPages;
    }

    @Override
    public void updateBitmap(Bitmap picture, String id) {
        InterpretationModel model = null;
        int position = -1;
        for (InterpretationModel m : list) {
            if (m.id.equals(id)) {
                model = m;
                position = ((InterpretationAdapter) listView.getAdapter()).getPosition(model);
                break;
            }
        }
        if (picture != null) {
            model.picture = picture;
            updateListItem(model, position);
            updateListView(model, position);
        }
        if (position < (list.size() - 1)) {
            InterpretationModel nextModel = list.get(position + 1);
            new InterpretationHandler(this).addPicture(getActivity(), nextModel);
        }
    }

    public void setAdapter() {
        InterpretationAdapter adapter = new InterpretationAdapter(getActivity(), R.layout.item_rest_inbox, list);
        adapter.setNotifyOnChange(true);
        listView.setAdapter(adapter);
    }

    public void setMoreInterpretationsBtn() {
        if (morePages() && listView.getFooterViewsCount() < 1) {
            listView.addFooterView(foot);
        } else if (!morePages() && listView.getFooterViewsCount() > 0)
            listView.removeFooterView(foot);
    }

    public void setLoader(boolean on) {
        if (on)
            loader.setVisibility(View.VISIBLE);
        else
            loader.setVisibility(View.GONE);
    }

    public void getInterpretations(int page) {
        setLoader(true);
        new InterpretationHandler(this).getInterpretations(getActivity(), page);
    }

    /*
    Updates content in listView and keep scroll position
     */
    public void updateListView(InterpretationModel model, int position) {
        int index = listView.getFirstVisiblePosition();
        View view = listView.getChildAt(0);
        int top = (view == null) ? 0 : view.getTop();
        ((InterpretationAdapter) listView.getAdapter()).remove(model);
        ((InterpretationAdapter) listView.getAdapter()).insert(model, position);
        ((InterpretationAdapter) listView.getAdapter()).notifyDataSetChanged();
        listView.setSelectionFromTop(index, top);
    }

    public void updateListItem(InterpretationModel model, int i) {
        list.set(i, model);
    }

    public boolean morePages() {
        if ((currentPage + 1) < totalPages)
            return true;
        return false;
    }
}//End of class Conversation fragment