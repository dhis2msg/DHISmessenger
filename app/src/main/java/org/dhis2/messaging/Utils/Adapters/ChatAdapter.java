package org.dhis2.messaging.Utils.Adapters;

import java.util.List;

import android.text.Html;
import org.dhis2.messaging.R;
import org.dhis2.messaging.Models.ChatModel;

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
import org.dhis2.messaging.Utils.SharedPrefs;

public class ChatAdapter extends ArrayAdapter<ChatModel>{
    private Context context;
    private boolean showDate;
   // private List<ChatModel> messages = new ArrayList<ChatModel>();

	public ChatAdapter(Context context, int textViewResourceId, List<ChatModel> chatMessages){
		super(context, textViewResourceId, chatMessages);
		//this.messages = chatMessages;
        this.context = context;
        showDate = false;
	}

    public void setShowDate(boolean value){
        showDate = value;
        notifyDataSetChanged();
    }
	/*@Override
	public void add(ChatModel object) {
		messages.add(object);
		super.add(object);
	}

	@Override
	public int getCount() {
		return messages.size();
	}

	@Override	
	public ChatModel getItem(int index) {
		return messages.get(index);
	}*/
	 
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

		ChatModel model = getItem(position);

        //IF MESSAGE IS SENT FROM LOGGED IN USER
		if(model.user == null || model.user.getName().equals(SharedPrefs.getUserName(context)) || model.user.id.equals(SharedPrefs.getUserId(context)))
		{
            holder.message.setBackgroundColor(Color.parseColor("#FFFFFF"));
            holder.wrapper.setGravity(Gravity.RIGHT);
			holder.left.setVisibility(View.GONE);
			holder.right.setVisibility(View.VISIBLE);
            holder.message.setText(model.message);
		}
		else
		{
            holder.message.setBackgroundColor(Color.parseColor("#e0eaff"));//#6699FF"));
            holder.wrapper.setGravity(Gravity.LEFT);
			holder.right.setVisibility(View.GONE);
			holder.left.setVisibility(View.VISIBLE);
            String out = "<b>" + model.user.name + ": </b> " + model.message;
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