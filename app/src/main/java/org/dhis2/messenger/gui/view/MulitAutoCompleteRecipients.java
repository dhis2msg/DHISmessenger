package org.dhis2.messenger.gui.view;

/*
 * Source-code inspired by: http://www.kpbird.com/2013/02/android-chips-edittext-token-edittext.html
 */

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.MultiAutoCompleteTextView;
import android.widget.TextView;

import org.dhis2.messenger.R;

public class MulitAutoCompleteRecipients extends MultiAutoCompleteTextView implements OnItemClickListener {

    public MulitAutoCompleteRecipients(Context context) {
        super(context);
        init(context);
    }

    public MulitAutoCompleteRecipients(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public MulitAutoCompleteRecipients(Context context, AttributeSet attrs,
                                       int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public void init(Context context) {
        setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        setResipients();
    }

    public void setResipients() {
        if (getText().toString().contains(",")) {

            String recipients[] = getText().toString().trim().split(",");
            SpannableStringBuilder stringBuilder = new SpannableStringBuilder(getText());


            int x = 0;

            for (String r : recipients) {

                LayoutInflater lf = (LayoutInflater) getContext().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
                TextView textView = (TextView) lf.inflate(R.layout.textview_recipient, null);
                textView.setTextSize(12);
                textView.setText(r);

                // capture bitmap of generated textview
                int spec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
                textView.measure(spec, spec);
                textView.layout(0, 0, textView.getMeasuredWidth(), textView.getMeasuredHeight());
                Bitmap b = Bitmap.createBitmap(textView.getWidth(), textView.getHeight(), Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(b);
                canvas.translate(-textView.getScrollX(), -textView.getScrollY());
                textView.draw(canvas);
                textView.setDrawingCacheEnabled(true);
                Bitmap cacheBitmap = textView.getDrawingCache();
                Bitmap viewBitmap = cacheBitmap.copy(Bitmap.Config.ARGB_8888, true);
                textView.destroyDrawingCache();

                // create bitmap drawable for imagespan
                BitmapDrawable bmpDrawable = new BitmapDrawable(getContext().getResources(), viewBitmap);
                bmpDrawable.setBounds(0, 0, bmpDrawable.getIntrinsicWidth(), bmpDrawable.getIntrinsicHeight());

                // create and set imagespan
                stringBuilder.setSpan(new ImageSpan(bmpDrawable), x, x + r.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                x = x + r.length() + 1;
            }

            setText(stringBuilder);
            setSelection(getText().length());
        }
    }

}