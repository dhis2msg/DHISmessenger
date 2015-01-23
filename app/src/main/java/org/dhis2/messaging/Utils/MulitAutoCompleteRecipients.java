package org.dhis2.messaging.Utils;

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

import org.dhis2.messaging.R;

import java.util.Arrays;

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

        //addTextChangedListener(watcher);
    }


    /*private TextWatcher watcher = new TextWatcher() {
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
           // if (count >= 1) {
             //   if (s.charAt(start) == ',')
               //     editOutput();
            //}
            String textString = getText().toString();

            if( textString.length() > 0  && textString.charAt(textString.length() -1 ) == ',') {
                String recipients[] = textString.trim().split(",");
                String correct = "";
                for(String t : recipients )
                {
                    if(!t.equals(recipients[recipients.length ]))
                        correct += t;
                }
                //setText(textString.substring(0, textString.length() - 1));
                setText(correct);
                setSelection(textString.length() - 1);
                editOutput();
            }
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };*/

    public void setResipients() {
        if (getText().toString().contains(","))
        {

            String recipients[] = getText().toString().trim().split(",");

            /*if(checkIfRecipientExist(recipients)){
                String[] fixed = getText().toString().split(",");
                fixed[fixed.length -1] = "";
                String f = "";
                for(String e :fixed){
                    if(!e.equals(""))
                        f += e + ",";
                }
                setText(f);
            }*/
            SpannableStringBuilder stringBuilder = new SpannableStringBuilder(getText());


            int x = 0;

            for (String r : recipients) {

                LayoutInflater lf = (LayoutInflater) getContext().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
                TextView textView = (TextView) lf.inflate(R.layout.textview_recipient, null);
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

    //Noe feil her
    private boolean checkIfRecipientExist(String[] recipients) {
        int size = recipients.length;
        String last = recipients[size - 1 ];
        String[] test = new String[size-1];
        int i = 0;
        if(size != 1){
        for(String l : recipients)
        {
            test[i] = l;
            i++;
        }}
        test[size - 1] =  "";
        boolean in = Arrays.asList(test).contains(last);
        return in;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        setResipients();
    }
}