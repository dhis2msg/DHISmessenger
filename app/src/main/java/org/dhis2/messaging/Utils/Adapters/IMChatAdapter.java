package org.dhis2.messaging.Utils.Adapters;

import java.util.List;

import android.text.Html;

import org.dhis2.messaging.Models.IMMessageModel;
import org.dhis2.messaging.Utils.XMPP.XMPPSessionStorage;
import org.dhis2.messaging.R;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.dhis2.messaging.Utils.XMPP.XMPPClient;

public class IMChatAdapter extends ArrayAdapter<IMMessageModel>{
    private Context context;
    private boolean showDate;

    public IMChatAdapter(Context context, int textViewResourceId, List<IMMessageModel> chatMessages){
        super(context, textViewResourceId, chatMessages);
        this.context = context;
        showDate = false;
    }

    public void setShowDate(boolean value){
        showDate = value;
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        ChatHolder holder = new ChatHolder();
        if (row == null) {

            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.item_rest_conversation, parent, false);
            RelativeLayout wrapper = (RelativeLayout) row.findViewById(R.id.wrapper);
            TextView message = (TextView) row.findViewById(R.id.message);
            TextView date = (TextView) row.findViewById(R.id.date);
            ImageView left = (ImageView) row.findViewById(R.id.arrow_left);
            ImageView right = (ImageView) row.findViewById(R.id.arrow_right);

            holder.message = message;
            holder.wrapper = wrapper;
            holder.date = date;
            holder.left = left;
            holder.right = right;
            row.setTag(holder);
        }
        else
            holder = (ChatHolder) row.getTag();

        IMMessageModel model = getItem(position);

        //IF MESSAGE IS SENT FROM LOGGED IN USER
        String nickname = XMPPClient.getInstance().getMucNickname();
        if(model.JID == null || model.JID.equals(nickname))
        {
            holder.message.setBackgroundColor(Color.parseColor("#FFFFFF"));
            holder.wrapper.setGravity(Gravity.RIGHT);
            holder.left.setVisibility(View.GONE);
            holder.right.setVisibility(View.VISIBLE);
            holder.message.setText(model.text);
        }
        else
        {
            holder.message.setBackgroundColor(Color.parseColor("#e0eaff"));//#6699FF"));
            holder.wrapper.setGravity(Gravity.LEFT);
            holder.right.setVisibility(View.GONE);
            holder.left.setVisibility(View.VISIBLE);
            String out = "<b>" + XMPPSessionStorage.getInstance().getUsername(model.JID) + ": </b> " + model.text;
            holder.message.setText(Html.fromHtml(out));
        }

        holder.date.setText(model.date);
        if(showDate)
            holder.date.setVisibility(View.VISIBLE);
        else
            holder.date.setVisibility(View.GONE);
        return row;
    }
    private static class ChatHolder {
        public TextView message, date;
        public RelativeLayout wrapper;
        public ImageView right, left;
    }
}