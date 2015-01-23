package org.dhis2.messaging.Utils.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.dhis2.messaging.Models.NameAndIDModel;
import org.dhis2.messaging.R;

import java.util.List;

/**
 * Created by iNick on 16.10.14.
 */
public class UserDropdownAdapter extends ArrayAdapter<NameAndIDModel> {
    private Context context;

    public UserDropdownAdapter(Context context, int textViewResourceId, List<NameAndIDModel> users){
        super(context, textViewResourceId, users);
        this.context = context;
    }

    @Override
    public View getView(int position, final View convertView, ViewGroup parent) {
        View row = convertView;
        UserHolder holder = new UserHolder();
        if (row == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE); //Her er det noe feil
            row = inflater.inflate(R.layout.textview_recipient, parent, false);
            TextView username = (TextView) row;
            holder.username = username;
            row.setTag(holder);

        }else
            holder = (UserHolder) row.getTag();

        if(!holder.selected) {
            NameAndIDModel item = getItem(position);
            holder.username.setText(item.getName());
            holder.username.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    UserHolder holder = (UserHolder) convertView.getTag();
                    holder.selected = true;
                }
            });
        }

        return row;
    }
    private static class UserHolder {
      public TextView username;
      public Boolean selected;
    }
}
