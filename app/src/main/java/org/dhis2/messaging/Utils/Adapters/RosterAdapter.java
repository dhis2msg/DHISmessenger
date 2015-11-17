package org.dhis2.messaging.Utils.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.dhis2.messaging.Models.RosterModel;
import org.dhis2.messaging.R;

import java.util.List;

public class RosterAdapter extends ArrayAdapter<RosterModel> {
    private Context context;

    public RosterAdapter(Context context, int textViewResourceId, List<RosterModel> roster) {
        super(context, textViewResourceId, roster);
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        Holder holder = new Holder();
        if (row == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE); ///(Activity)context
            row = inflater.inflate(R.layout.item_roster, parent, false);
            TextView nickname = (TextView) row.findViewById(R.id.nickname);
            TextView status = (TextView) row.findViewById(R.id.status);
            TextView lastPresence = (TextView) row.findViewById(R.id.lastpresence);
            ImageView dot = (ImageView) row.findViewById(R.id.dot);
            ImageView read = (ImageView) row.findViewById(R.id.read);

            holder.nickname = nickname;
            holder.status = status;
            holder.lastPresence = lastPresence;
            holder.dot = dot;
            holder.read = read;
            row.setTag(holder);
        } else
            holder = (Holder) row.getTag();

        RosterModel model = getItem(position);
        holder.nickname.setText(model.getUsername());
        holder.status.setText(model.getStatusMessage());
        holder.lastPresence.setText(model.getLastActivity());
        holder.lastPresence.setVisibility(model.isOnline() ? View.GONE : View.VISIBLE);
        holder.dot.setVisibility(model.isOnline() ? View.VISIBLE : View.GONE);
        holder.read.setVisibility(model.isReadConversation() ? View.GONE : View.VISIBLE);
        return row;
    }

    private static class Holder {
        public TextView nickname, status, lastPresence;
        public ImageView dot, read;
    }
}


