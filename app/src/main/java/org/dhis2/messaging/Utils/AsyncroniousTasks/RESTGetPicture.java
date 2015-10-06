package org.dhis2.messaging.Utils.AsyncroniousTasks;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;

import org.dhis2.messaging.Models.InterpretationModel;
import org.dhis2.messaging.Utils.AsyncroniousTasks.Interfaces.InterpretationCallback;
import org.dhis2.messaging.REST.RESTClient;
import org.dhis2.messaging.Utils.SharedPrefs;

/**
 * Created by iNick on 23.02.15.
 */
public class RESTGetPicture extends AsyncTask<Integer, String, Bitmap> {
    private InterpretationCallback listener;
    private InterpretationModel model;
    private String auth;

    public RESTGetPicture(InterpretationCallback listener, Context context, InterpretationModel model) {
        this.auth = SharedPrefs.getCredentials(context);
        this.model = model;
        this.listener = listener;
    }

    @Override
    protected Bitmap doInBackground(Integer... args) {
        Bitmap picture = null;
        if (model.type.equals("chart") || model.type.equals("map")) {
            picture = RESTClient.getPicture(model.pictureUrl + ".png", auth);
        }
        return picture;
    }

    @Override
    protected void onPostExecute(Bitmap picture) {
        listener.updateBitmap(picture, model.id);
    }
}
