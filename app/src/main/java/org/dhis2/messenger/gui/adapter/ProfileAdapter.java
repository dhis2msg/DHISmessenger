package org.dhis2.messenger.gui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.dhis2.messenger.R;

import java.util.List;

/**
 * Created by iNick on 18.10.14.
 */
public class ProfileAdapter extends ArrayAdapter<String> {
    private Context context;

    public ProfileAdapter(Context context, int textViewResourceId, List<String> messages) {
        super(context, textViewResourceId, messages);
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        InboxHolder holder = new InboxHolder();
        if (row == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.item_profile, parent, false);
            TextView subject = (TextView) row.findViewById(R.id.subject);
            TextView data = (TextView) row.findViewById(R.id.data);
            holder.subject = subject;
            holder.data = data;
            row.setTag(holder);
        } else
            holder = (InboxHolder) row.getTag();

        String[] items = getItem(position).split(":");
        holder.subject.setText(items[0]);
        holder.data.setText(items[1]);

        return row;
    }

    private static class InboxHolder {
        public TextView subject, data;
    }
}

