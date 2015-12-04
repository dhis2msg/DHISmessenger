package org.dhis2.messenger.core.rest.async;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;

import org.dhis2.messenger.model.InterpretationModel;
import org.dhis2.messenger.core.rest.RESTClient;
import org.dhis2.messenger.core.rest.callback.InterpretationCallback;
import org.dhis2.messenger.SharedPrefs;

/**
 * Created by iNick on 23.02.15.
 */
public class RESTGetPicture extends AsyncTask<Integer, String, Bitmap> {
    private InterpretationCallback listener;
    private InterpretationModel model;
    private String auth;
    private int position;

    public RESTGetPicture(InterpretationCallback listener, Context context, InterpretationModel model, int position) {
        this.auth = SharedPrefs.getCredentials(context);
        this.model = model;
        this.listener = listener;
        this.position = position;
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
        listener.updateBitmap(picture, model.id, position);
    }
}
