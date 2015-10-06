package org.dhis2.messaging.Utils.Adapters;

import java.util.List;

import org.dhis2.messaging.*;
import org.dhis2.messaging.Models.InboxModel;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class InboxAdapter extends ArrayAdapter<InboxModel> {
    private Context context;

    public InboxAdapter(Context context, int textViewResourceId, List<InboxModel> messages) {
        super(context, textViewResourceId, messages);
        this.context = context;
    }

    @Override
    public View getView(final int position, final View convertView, ViewGroup parent) {
        View row = convertView;
        InboxHolder holder = new InboxHolder();
        TextView subject, date, lastSender;


        if (row == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.item_rest_inbox, parent, false);
            subject = (TextView) row.findViewById(R.id.subject);
            date = (TextView) row.findViewById(R.id.date);
            lastSender = (TextView) row.findViewById(R.id.lastSender);
            ImageView mail = (ImageView) row.findViewById(R.id.open);
            ImageView line = (ImageView) row.findViewById(R.id.line);
            holder.subject = subject;
            holder.date = date;
            holder.lastSender = lastSender;
            holder.mail = mail;
            holder.line = line;
            row.setTag(holder);
        } else
            holder = (InboxHolder) row.getTag();

        InboxModel item = getItem(position);
        holder.date.setText(item.getDate());
        holder.date.setVisibility(position == 0 || !getItem(position - 1).getDate().equals(item.getDate()) ? View.VISIBLE : View.GONE);
        holder.line.setVisibility(position == 0 || !getItem(position - 1).getDate().equals(item.getDate()) ? View.VISIBLE : View.GONE);
        holder.subject.setText(item.getSubject());
        holder.lastSender.setText("Last sender " + item.getLastSender());
        holder.lastSender.setTextColor(item.getRead() ? Color.parseColor("#000000") : Color.parseColor("#1d5288"));
        holder.subject.setTextColor(item.getRead() ? Color.parseColor("#000000") : Color.parseColor("#1d5288"));
        holder.mail.setVisibility(item.getRead() ? View.GONE : View.VISIBLE);

        return row;
    }

    private static class InboxHolder {
        public TextView subject, lastSender, date;
        public ImageView mail, line;
    }
}