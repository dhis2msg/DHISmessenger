package org.dhis2.messenger.gui.adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.ActivityOptionsCompat;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.dhis2.messenger.gui.activity.InterpretationCommentActivity;
import org.dhis2.messenger.gui.activity.ProfileActivity;
import org.dhis2.messenger.model.InterpretationModel;
import org.dhis2.messenger.R;
import org.dhis2.messenger.core.rest.APIPath;
import org.dhis2.messenger.core.rest.RESTClient;
import org.dhis2.messenger.core.rest.Response;
import org.dhis2.messenger.SharedPrefs;
import org.dhis2.messenger.gui.ToastMaster;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

public class InterpretationAdapter extends ArrayAdapter<InterpretationModel> {
    private Context context;
    private InterpretationHolder holder;
    public InterpretationAdapter(Context context, int textViewResourceId, List<InterpretationModel> interpretations) {
        super(context, textViewResourceId, interpretations);
        this.context = context;
    }

    @Override
    public View getView(final int position, final View convertView, ViewGroup parent) {
        View row = convertView;

        if (row == null) {
            holder = new InterpretationHolder();
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.item_interpretation, parent, false);

            TextView text = (TextView) row.findViewById(R.id.text);
            TextView date = (TextView) row.findViewById(R.id.date);
            TextView user = (TextView) row.findViewById(R.id.user);
            ImageView image = (ImageView) row.findViewById(R.id.typeImage);
            RelativeLayout comment = (RelativeLayout) row.findViewById(R.id.comment_layout);
            RelativeLayout expand = (RelativeLayout) row.findViewById(R.id.expand_layout);
            ProgressBar spinner = (ProgressBar) row.findViewById(R.id.loader);
            holder.spinner = spinner;
            holder.text = text;
            holder.date = date;
            holder.user = user;
            holder.image = image;
            holder.comment = comment;
            holder.expand = expand;

            row.setTag(holder);
        } else
            holder = (InterpretationHolder) row.getTag();

        final InterpretationModel item = getItem(position);
        holder.text.setText(item.text);
        holder.date.setText(item.date);
        holder.user.setText(item.user.name);
        holder.spinner.setVisibility(item.picture != null ? View.GONE : View.VISIBLE);

        //Make a new thread to disable the loader if there is no data to show
        Thread t = new Thread(){
            public void run() {
                try {
                    long startTime = System.nanoTime();
                    //Waiting for 10 seconds before disable the loader
                    while ((System.nanoTime() - startTime) / Math.pow(10, 9) < 10);
                    holder.spinner.setVisibility(View.GONE);
                } catch (Exception e) {
                    return;
                }
            }
        };
        t.start();
        holder.image.setImageBitmap(item.picture);

        holder.comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Activity activity = (Activity) context;
                ActivityOptionsCompat compat = ActivityOptionsCompat.makeSceneTransitionAnimation(activity, null);
                Intent intent = new Intent(context, InterpretationCommentActivity.class);
                intent.putExtra("id", item.id);
                intent.putExtra("subject", item.text);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                if(Build.VERSION.SDK_INT >= 16) {
                    context.startActivity(intent, compat.toBundle());
                }else{
                    context.startActivity(intent);
                }
            }
        });

        holder.image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showBitmap(item.pictureUrl);
            }
        });

        holder.user.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Activity activity = (Activity) context;
                ActivityOptionsCompat compat = ActivityOptionsCompat.makeSceneTransitionAnimation(activity, null);
                Intent intent = new Intent(context, ProfileActivity.class);
                intent.putExtra("userid", item.user.getId());
                intent.putExtra("username", item.user.getName());
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                if(Build.VERSION.SDK_INT >= 16) {
                    context.startActivity(intent, compat.toBundle());
                }else{
                    context.startActivity(intent);
                }
            }
        });

        holder.expand.setVisibility((item.user.getId().equals(SharedPrefs.getUserId(context))) ? View.VISIBLE : View.INVISIBLE);

        if (holder.expand.getVisibility() == View.VISIBLE) {
            holder.expand.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        InterpretationHolder holder = (InterpretationHolder) convertView.getTag();
                        PopupMenu popup = new PopupMenu(context, holder.expand);
                        popup.getMenuInflater().inflate(R.menu.popup, popup.getMenu());
                        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem i) {
                                switch (i.getItemId()) {

                                    case R.id.delete: {
                                        new AsyncTask<Integer, String, Integer>() {
                                            @Override
                                            protected Integer doInBackground(Integer... args) {
                                                Response response = (RESTClient.delete(SharedPrefs.getServerURL(context) + APIPath.FIRST_PAGE_INTERPRETATIONS + "/" + item.id, SharedPrefs.getCredentials(context), "application/json"));
                                                return response.getCode();
                                            }

                                            @Override
                                            protected void onPostExecute(Integer code) {
                                                if (RESTClient.noErrors(code)) {
                                                    new ToastMaster(context, "Deleted", false);
                                                    remove(item);
                                                    notifyDataSetChanged();
                                                } else
                                                    new ToastMaster(context, "Could not delete interpretation!", false);
                                            }
                                        }.execute();
                                        return true;
                                    }

                                    case R.id.edit: {
                                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                        final EditText input = new EditText(context);
                                        input.setText(item.text);
                                        builder.setView(input);
                                        builder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                if (!input.getText().toString().equals(item.text)) {
                                                    new AsyncTask<Integer, String, Integer>() {
                                                        @Override
                                                        protected Integer doInBackground(Integer... args) {
                                                            JSONObject object = new JSONObject();
                                                            try {
                                                                object.put("text", input.getText().toString());
                                                            } catch (JSONException e) {
                                                            }
                                                            Response response = RESTClient.put(SharedPrefs.getServerURL(context) + APIPath.FIRST_PAGE_INTERPRETATIONS + "/" + item.id, SharedPrefs.getCredentials(context), input.getText().toString(), "text/plain");
                                                            return response.getCode();
                                                        }

                                                        @Override
                                                        protected void onPostExecute(Integer code) {
                                                            if (RESTClient.noErrors(code)) {
                                                                new ToastMaster(context, "Edited", false);
                                                                item.text = input.getText().toString();
                                                                remove(item);
                                                                insert(item, position);
                                                                notifyDataSetChanged();
                                                            } else
                                                                new ToastMaster(context, "Could not edit subject!", false);
                                                        }
                                                    }.execute();
                                                }
                                                dialog.cancel();
                                            }
                                        });
                                        AlertDialog alertDialog = builder.create();
                                        alertDialog.show();
                                        return true;
                                    }
                                }
                                return true;
                            }
                        });
                        popup.show();
                    } catch (NullPointerException e) {
                    }
                }
            });
        }
        return row;
    }

    private void showBitmap(String url) {
        AlertDialog.Builder alert = new AlertDialog.Builder(context);
        WebView webView = new WebView(context);
        webView.getSettings().setBuiltInZoomControls(true);
        HashMap<String, String> map = new HashMap<String, String>();
        String authorization = "Basic " + SharedPrefs.getCredentials(context);
        map.put("Authorization", authorization);
        webView.loadUrl(url, map);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });
        webView.zoomIn();
        alert.setView(webView);
        alert.setNegativeButton("Close", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        alert.show();
    }

    private static class InterpretationHolder {
        private TextView text, date, user;
        private RelativeLayout comment, expand;
        private ProgressBar spinner;
        private ImageView image;
    }
}

