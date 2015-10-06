package org.dhis2.messaging.Utils.Adapters;

import android.content.Context;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by iNick on 26.11.14.
 */
public class AutoCompleteCharSearchAdapter extends SimpleAdapter implements Filterable {
    private ArrayList<HashMap<String, String>> data, visibleData;

    public AutoCompleteCharSearchAdapter(Context context, List data, int resource, String[] from, int[] to) {
        super(context, data, resource, from, to);
        visibleData = (ArrayList<HashMap<String, String>>) data;
        this.data = (ArrayList<HashMap<String, String>>) visibleData.clone();
    }

    @Override
    public Filter getFilter() {
        Filter nameFilter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                if (constraint != null) {
                    ArrayList<HashMap<String, String>> tmpData = data;
                    ArrayList<HashMap<String, String>> tmpVisibleData = visibleData;
                    tmpVisibleData.clear();
                    for (int i = 0; i < tmpData.size(); i++) {
                        if (tmpData.get(i).toString().toLowerCase().contains(constraint.toString().toLowerCase())) {
                            tmpVisibleData.add(tmpData.get(i));
                        }
                    }

                    FilterResults filterResults = new FilterResults();
                    filterResults.values = tmpVisibleData;
                    filterResults.count = tmpVisibleData.size();
                    return filterResults;
                } else {
                    return new FilterResults();
                }
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results != null && results.count > 0) {
                    try {
                        notifyDataSetChanged();
                    } catch (IndexOutOfBoundsException e) {
                    }
                }
            }
        };
        return nameFilter;
    }
}
