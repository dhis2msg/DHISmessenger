package org.dhis2.messenger.gui.fragment;

import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.transition.Fade;
import android.transition.Slide;
import android.transition.TransitionSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;

import org.dhis2.messenger.core.rest.RESTSessionStorage;
import org.dhis2.messenger.model.InterpretationModel;
import org.dhis2.messenger.R;
import org.dhis2.messenger.gui.adapter.InterpretationAdapter;
import org.dhis2.messenger.core.rest.callback.InterpretationCallback;
import org.dhis2.messenger.core.rest.async.RESTGetInterpretations;
import org.dhis2.messenger.core.rest.async.RESTGetPicture;

import java.util.ArrayList;
import java.util.List;

public class InterpretationsFragment extends Fragment implements InterpretationCallback {
    private ListView listView;
    private ProgressBar loader;
    private ImageView moreInterpretations;
    private View foot;

    //Memory store
    private int currentPage, totalPages;
    private int pageSize = 5;
    private List<InterpretationModel> list;
    private RESTGetInterpretations getInterpretations;
    private RESTGetPicture getPicture;

    public InterpretationsFragment(){
        super();
        //Makes a phone with api 21 and higher use Slide and fade transitions
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
        RESTSessionStorage.getInstance().setInterpretationsPageSize(pageSize);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_interpretation, container, false);
        listView = (ListView) view.findViewById(R.id.list);
        loader = (ProgressBar) view.findViewById(R.id.loader);
        foot = inflater.inflate(R.layout.listview_footer, null);
        moreInterpretations = (ImageView) foot.findViewById(R.id.moreMessages);
        currentPage = totalPages = 1;
        list = new ArrayList<>();

        //TODO: The morePages button is never shown.
        //TODO: Add a refresh button.
        moreInterpretations.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (morePages()) {
                    currentPage++;
                    getInterpretations(currentPage);
                }
            }
        });
        if (list.isEmpty()) {
            getInterpretations(currentPage);
        }else {
            setAdapter();
        }
        return view;
    }

    /**
     * Replaces the this class list with the provided and gets the images to display:
     * @param list
     * @param page
     */
    @Override
    public void updateList(List<InterpretationModel> list, int page) {
        //int index = (currentPage - 1) * pageSize;
        this.list.addAll(list);
        setAdapter();
        setLoader(false);
        setMoreInterpretationsBtn();

        // the following do not seem to have any effect atm:
        /*if (currentPage == 1) {
            if (list.get(index) != null) {
                getPicture = new RESTGetPicture(this, getActivity(), list.get(index), index);
                getPicture.execute();
            }
        } else if (list.get(index) != null) {
            getPicture = new RESTGetPicture(this, getActivity(), list.get(index), index);
            getPicture.execute();
        }*/
    }

    @Override
    public void updatePages(int totalPages) {
        this.totalPages = totalPages;
        RESTSessionStorage.getInstance().setInterpretationsTotalPages(totalPages);
    }

    @Override
    public void updateBitmap(Bitmap picture, String id, int index) {
        InterpretationModel model = null;
        Log.v("updateBitmap:", "index= " + index);
        if (!(index > -1) || index > list.size()) {// invalid index:
            for (InterpretationModel m : list) {
                if (m.id.equals(id)) {
                    model = m;
                    index = ((InterpretationAdapter) listView.getAdapter()).getPosition(model);
                    break;
                }
            }
        }
        if (picture != null) {
            model.picture = picture;
            //updateListItem(model, index);
            list.set(index, model);
            updateListView(model, index);
        }
        if (index < (list.size() - 1)) {
            InterpretationModel nextModel = list.get(index + 1);
            //TODO: Get picture from cache instead of the server ? model.picture == null ? skipCache == true ?
            getPicture = new RESTGetPicture(this, getActivity(), nextModel, index + 1);
            getPicture.execute();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        removeHandlers();
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
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
        Log.v("InterpretationFragment", "getInterpretations page=" + page);

        getInterpretations = new RESTGetInterpretations(this, getActivity(), page, true); //skip the cache for now.
        setLoader(!getInterpretations.getTempList().isEmpty());
        getInterpretations.execute();
    }

    public void updateListView(InterpretationModel model, int position) {
        int index = listView.getFirstVisiblePosition();
        View view = listView.getChildAt(0);
        int top = (view == null) ? 0 : view.getTop();
        ((InterpretationAdapter) listView.getAdapter()).remove(model);
        ((InterpretationAdapter) listView.getAdapter()).insert(model, position);
        ((InterpretationAdapter) listView.getAdapter()).notifyDataSetChanged();
        listView.setSelectionFromTop(index, top);
    }

    public boolean morePages() {
        if ((currentPage + 1) < totalPages) {
            return true;
        }
        return false;
    }

    private void removeHandlers() {
        if (getInterpretations != null) {
            if (!getInterpretations.isCancelled())
                getInterpretations.cancel(true);
            getInterpretations = null;
        }
        if (getPicture != null) {
            if (!getPicture.isCancelled())
                getPicture.cancel(true);
            getPicture = null;
        }
    }
}//End of class Conversation fragment