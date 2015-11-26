package org.dhis2.messenger.gui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.dhis2.messenger.model.ConferenceModel;
import org.dhis2.messenger.R;

import java.util.List;

/**
 * Created by iNick on 20.10.14.
 */
public class ConferenceAdapter extends ArrayAdapter<ConferenceModel> {
    private Context context;

    public ConferenceAdapter(Context context, int textViewResourceId, List<ConferenceModel> groups) {
        super(context, textViewResourceId, groups);
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        Groupholder holder = new Groupholder();
        if (row == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.item_roster_conference, parent, false);
            TextView groupName = (TextView) row.findViewById(R.id.groupname);
            TextView participants = (TextView) row.findViewById(R.id.participants);
            TextView subject = (TextView) row.findViewById(R.id.subject);

            holder.groupname = groupName;
            holder.participants = participants;
            holder.subject = subject;
            row.setTag(holder);
        } else
            holder = (Groupholder) row.getTag();

        ConferenceModel conference = getItem(position);


        holder.groupname.setText(conference.getName());
        holder.subject.setText(conference.getTopic());
        holder.participants.setText(Integer.toString(conference.getOccupants()));

        return row;
    }

    private static class Groupholder {
        public TextView groupname, participants, subject;
    }
}
