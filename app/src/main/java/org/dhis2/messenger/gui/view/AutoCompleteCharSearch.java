package org.dhis2.messenger.gui.view;

/**
 * Created by iNick on 26.11.14.
 */

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;

public class AutoCompleteCharSearch extends AutoCompleteTextView implements AdapterView.OnItemClickListener {
    public AutoCompleteCharSearch(Context context) {
        super(context);
        init(context);
    }

    public AutoCompleteCharSearch(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public AutoCompleteCharSearch(Context context, AttributeSet attrs,
                                  int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public void init(Context context) {
        setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        editOutput();
    }

    public void editOutput() {
        if (getText().toString().contains(",")) {
            String split[] = getText().toString().trim().split("=");
            String again[] = split[1].split(",");
            String out = again[0];
            setText(out);
        }
    }
}

