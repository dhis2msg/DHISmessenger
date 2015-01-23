package org.dhis2.messaging.Utils.Adapters;

import android.content.Context;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by iNick on 13.11.14.
 */
public class AutoCompleteTextAdapter{}/* extends SimpleAdapter implements Filterable {

    private ArrayList<HashMap<String, String>> mAllData, mDataShown;

    public AutoCompleteTextAdapter(Context context, List data, int resource, String[] from, int[] to) {
        super(context, data, resource, from, to);
        mDataShown = (ArrayList<HashMap<String, String>>) data;
        mAllData = (ArrayList<HashMap<String, String>>) mDataShown.clone();
    }

    @Override
    public Filter getFilter() {
        Filter nameFilter = new Filter(){

            @Override
            public String convertResultToString(Object resultValue) {
                return ((HashMap<String, String>)(resultValue)).get(Country.NAME);
            }

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                if(constraint != null)
                {
                    ArrayList<HashMap<String, String>> tmpAllData = mAllData;
                    ArrayList<HashMap<String, String>> tmpDataShown = mDataShown;
                    tmpDataShown.clear();
                    for(int i = 0; i < tmpAllData.size(); i++)
                    {
                        if(tmpAllData.get(i).get(Country.NAME).toLowerCase().startsWith(constraint.toString().toLowerCase()))
                        {
                            tmpDataShown.add(tmpAllData.get(i));
                        }
                    }

                    FilterResults filterResults = new FilterResults();
                    filterResults.values = tmpDataShown;
                    filterResults.count = tmpDataShown.size();
                    return filterResults;
                }
                else
                {
                    return new FilterResults();
                }
            }

            @Override
            protected void publishResults(CharSequence constraint,
                                          FilterResults results) {
                if(results != null && results.count > 0)
                {
                    notifyDataSetChanged();
                }
            }};

        return nameFilter;
    }
}*/