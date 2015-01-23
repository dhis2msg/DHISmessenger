package org.dhis2.messaging.Utils.AsyncTasks;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.dhis2.messaging.Models.NameAndIDModel;
import org.dhis2.messaging.Utils.AsyncTasks.Interfaces.InterpretationCallback;
import org.dhis2.messaging.Models.ChatModel;
import org.dhis2.messaging.Models.InterpretationModel;
import org.dhis2.messaging.R;
import org.dhis2.messaging.Utils.REST.APIPaths;
import org.dhis2.messaging.Utils.REST.RESTClient;
import org.dhis2.messaging.Utils.REST.Response;
import org.dhis2.messaging.Utils.SharedPrefs;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by iNick on 20.10.14.
 */
public class InterpretationHandler {

    private static InterpretationCallback listener;

    public InterpretationHandler(InterpretationCallback listener) {
        this.listener = listener;
    }

    public void getInterpretations(final Context context, final int page) {
        new AsyncTask<Integer, String, Integer>() {
            List<InterpretationModel> tempList = new ArrayList<InterpretationModel>();
            String server = SharedPrefs.getServerURL(context);
            String api = server + APIPaths.FIRST_PAGE_INTERPRETATIONS;
            String auth = SharedPrefs.getCredentials(context);

            int pages = 0;

            protected Integer doInBackground(Integer... args) {
                try {
                    JSONObject json;
                    Response response;
                    if (page == 1) {
                        response = RESTClient.get(api + APIPaths.INTERPRETATIONS_FIELDS + "&pageSize=5", auth);
                    } else {
                        response = RESTClient.get(api + APIPaths.INTERPRETATIONS_FIELDS + "&pageSize=5&page=" + page, auth);
                    }

                    if (RESTClient.noErrors(response.getCode())) {
                        json = new JSONObject(response.getBody());
                        JSONObject pager = json.getJSONObject("pager");
                        pages = Integer.parseInt(pager.getString("pageCount"));

                        JSONArray allConversations = new JSONArray(json.getString("interpretations"));

                    /* working fast code
                    Gson gson = new Gson();
                    Type listType = new TypeToken<List<InterpretationModel>>() {
                    }.getType();
                    String coms = allConversations.toString();
                    tempList = gson.fromJson(coms, listType);*/

                        for (int i = 0; i < allConversations.length() && i < 5 ; i++) {


                            JSONObject row = allConversations.getJSONObject(i);
                            //Dataset-reports is not auited for this application and will not be added to the view
                            if (!row.getString("type").equals("dataSetReport")) {
                                String id = row.getString("id");
                                String date = row.getString("lastUpdated");
                                String text = row.getString("text");
                                String type = row.getString("type");
                                NameAndIDModel user = new Gson().fromJson(row.getString("user"), NameAndIDModel.class);

                                Gson gson = new Gson();
                                Type listType = new TypeToken<List<ChatModel>>() {
                                }.getType();
                                String coms = row.getJSONArray("comments").toString();
                                List<ChatModel> comments = gson.fromJson(coms, listType);

                                String typeid = "";
                                Bitmap picture = null;
                                String pictureURL = server;
                                if (type.equals("chart")) {
                                    JSONObject map = row.getJSONObject("chart");
                                    typeid = map.getString("id");
                                    pictureURL += "api/charts/" + typeid + "/data";
                                } else if (type.equals("map")) {
                                    JSONObject chart = row.getJSONObject("map");
                                    typeid = chart.getString("id");
                                    pictureURL += "api/maps/" + typeid + "/data";
                                } else if (type.equals("reportTable")) {
                                    JSONObject reportTable = row.getJSONObject("reportTable");
                                    typeid = reportTable.getString("id");
                                    pictureURL += "api/reportTables/" + typeid + "/data.html";
                                    //picture = BitmapFactory.decodeResource(context.getResources(), R.drawable.dhis2_logo);
                                    picture = BitmapFactory.decodeResource(context.getResources(), R.drawable.table);

                                }
                                /*else if (type.equals("dataSetReport")) {
                                JSONObject dataSet = row.getJSONObject("dataSet");
                                typeid = dataSet.getString("id");
                                }*/

                                tempList.add(new InterpretationModel(id, text, date, user, type, pictureURL, picture, comments));

                            }
                        }

                        return response.getCode();
                    } else
                        return response.getCode();
                } catch (JSONException e) {
                    e.printStackTrace();
                    return -1;
                }
            }

            protected void onPostExecute(final Integer code) {

                if (RESTClient.noErrors(code)) {
                    listener.updateList(tempList);
                } else if (code == -1) {
                    Toast.makeText(context, "JSONException converting error", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(context, RESTClient.getErrorMessage(code), Toast.LENGTH_LONG).show();
                }
                listener.updatePages(pages);
            }
        }.execute();
    }

    public void addPicture(final Context context, final InterpretationModel model) {
        new AsyncTask<Integer, String, Bitmap>() {
            @Override
            protected Bitmap doInBackground(Integer... args) {
                String auth = SharedPrefs.getCredentials(context);
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
        }.execute();
    }
}
