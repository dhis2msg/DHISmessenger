package org.dhis2.messenger.core.rest.callback;

import android.graphics.Bitmap;

import org.dhis2.messenger.model.InterpretationModel;

import java.util.List;

/**
 * Created by iNick on 20.10.14.
 */
public interface InterpretationCallback {
    void updateList(List<InterpretationModel> list);

    void updatePages(int pages);

    void updateBitmap(Bitmap picture, String id);
}
