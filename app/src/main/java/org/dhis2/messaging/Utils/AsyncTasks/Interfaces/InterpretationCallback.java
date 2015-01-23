package org.dhis2.messaging.Utils.AsyncTasks.Interfaces;

import android.graphics.Bitmap;

import org.dhis2.messaging.Models.InterpretationModel;

import java.util.List;

/**
 * Created by iNick on 20.10.14.
 */
public interface InterpretationCallback {
    void updateList(List<InterpretationModel> list);
    void updatePages(int pages);
    void updateBitmap(Bitmap picture,String id);
}
