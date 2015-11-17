package org.dhis2.messaging.Utils.UserInterface;

import android.content.Context;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.dhis2.messaging.R;

/**
 * Created by iNick on 28.02.15.
 */
public class ToastMaster {
    private Toast toast;
    private LayoutInflater inflater;

    public ToastMaster(Context context, String text, boolean message) {
        toast = new Toast(context);
        inflater = LayoutInflater.from(context);
        if (message)
            createMessageToast(text);
        else
            createInfoToast(text);
    }

    private void createMessageToast(String message) {
        View layout = inflater.inflate(R.layout.toast_new_message, null);
        layout = inflater.inflate(R.layout.toast_new_message, (ViewGroup) layout.findViewById(R.id.toast_new_message_layout));
        ImageView image = (ImageView) layout.findViewById(R.id.img);
        image.setImageDrawable(layout.getResources().getDrawable(R.drawable.ic_action_email));
        TextView text = (TextView) layout.findViewById(R.id.toastText);
        text.setText(message);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.show();

        Vibrator v = (Vibrator) layout.getContext().getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(500);
    }

    private void createInfoToast(String message) {
        View layout = inflater.inflate(R.layout.toast_info_message, null);
        layout = inflater.inflate(R.layout.toast_info_message, (ViewGroup) layout.findViewById(R.id.toast_info_layout));
        ImageView image = (ImageView) layout.findViewById(R.id.img);
        image.setImageDrawable(layout.getResources().getDrawable(R.drawable.ic_action_about));
        TextView text = (TextView) layout.findViewById(R.id.toastText);
        text.setText(message);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.show();
    }
}
