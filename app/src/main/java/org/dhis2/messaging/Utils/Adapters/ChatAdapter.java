package org.dhis2.messaging.Utils.Adapters;

import android.content.Context;
import android.graphics.Color;
import android.text.Html;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.dhis2.messaging.Models.ChatModel;
import org.dhis2.messaging.R;
import org.dhis2.messaging.Utils.SharedPrefs;

import java.util.List;

public class ChatAdapter extends ArrayAdapter<ChatModel> {
    private Context context;
    private boolean showDate;

    public ChatAdapter(Context context, int textViewResourceId, List<ChatModel> chatMessages) {
        super(context, textViewResourceId, chatMessages);
        this.context = context;
        showDate = false;
    }

    public void setShowDate(boolean value) {
        showDate = value;
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        ChatHolder holder = new ChatHolder();
        if (row == null) {

            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.item_chat_new, parent, false);
            RelativeLayout wrapper = (RelativeLayout) row.findViewById(R.id.wrapper);
            TextView message = (TextView) row.findViewById(R.id.message);
            TextView timeRight = (TextView) row.findViewById(R.id.time_right);
            TextView timeLeft = (TextView) row.findViewById(R.id.time_left);
            TextView date = (TextView) row.findViewById(R.id.date);
            ImageView left = (ImageView) row.findViewById(R.id.arrow_left);
            ImageView right = (ImageView) row.findViewById(R.id.arrow_right);

            holder.message = message;
            holder.wrapper = wrapper;
            holder.date = date;
            holder.left = left;
            holder.right = right;
            holder.timeRight = timeRight;
            holder.timeLeft = timeLeft;
            row.setTag(holder);
        } else
            holder = (ChatHolder) row.getTag();

        ChatModel model = getItem(position);

        //IF MESSAGE IS SENT FROM LOGGED IN USER
        if (model.user == null || model.user.getName().equals(SharedPrefs.getUserName(context)) || model.user.id.equals(SharedPrefs.getUserId(context))) {
            holder.timeLeft.setText(model.time);
            holder.timeLeft.setVisibility(View.VISIBLE);
            holder.timeRight.setVisibility(View.GONE);
            holder.message.setBackgroundColor(Color.parseColor("#FFFFFF"));
            holder.wrapper.setGravity(Gravity.RIGHT);
            holder.left.setVisibility(View.GONE);
            holder.right.setVisibility(View.VISIBLE);
            holder.message.setText(model.message);
        } else {
            holder.message.setBackgroundColor(Color.parseColor("#e0eaff"));
            holder.wrapper.setGravity(Gravity.LEFT);
            holder.right.setVisibility(View.GONE);
            holder.left.setVisibility(View.VISIBLE);
            String out = "<b>" + model.user.name + ": </b> " + model.message;
            holder.message.setText(Html.fromHtml(out));
            holder.timeRight.setText(model.time);
            holder.timeRight.setVisibility(View.VISIBLE);
            holder.timeLeft.setVisibility(View.GONE);
        }

        holder.date.setText("- " + model.date + " -");
        holder.date.setVisibility(position == 0 || !getItem(position - 1).date.equals(model.date) ? View.VISIBLE : View.GONE);
        return row;
    }

    private static class ChatHolder {
        public TextView message, date, timeRight, timeLeft;
        public RelativeLayout wrapper;
        public ImageView right, left;
    }

}