package org.dhis2.messenger.core.rest.async;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.dhis2.messenger.model.ChatModel;
import org.dhis2.messenger.model.InterpretationModel;
import org.dhis2.messenger.model.NameAndIDModel;
import org.dhis2.messenger.R;
import org.dhis2.messenger.core.rest.APIPath;
import org.dhis2.messenger.core.rest.RESTClient;
import org.dhis2.messenger.core.rest.Response;
import org.dhis2.messenger.core.rest.callback.InterpretationCallback;
import org.dhis2.messenger.SharedPrefs;
import org.dhis2.messenger.gui.ToastMaster;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by iNick on 20.10.14.
 */
public class RESTGetInterpretation extends AsyncTask<Integer, String, Integer> {
    private static InterpretationCallback listener;
    String server, auth;
    Context context;
    int pages;
    private List<InterpretationModel> tempList;

    public RESTGetInterpretation(InterpretationCallback listener, Context context, int pages) {
        tempList = new ArrayList<InterpretationModel>();
        this.listener = listener;
        this.server = SharedPrefs.getServerURL(context);
        this.auth = SharedPrefs.getCredentials(context);
        this.context = context;
        this.pages = 0;//pages
    }

    protected Integer doInBackground(Integer... args) {
        String api = server + APIPath.FIRST_PAGE_INTERPRETATIONS;
        try {
            JSONObject json;
            Response response;
            if (pages == 1) {
                response = RESTClient.get(api + APIPath.INTERPRETATIONS_FIELDS + "&pageSize=5", auth);
            } else {
                response = RESTClient.get(api + APIPath.INTERPRETATIONS_FIELDS + "&pageSize=5&page=" + pages, auth);
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

                for (int i = 0; i < allConversations.length() && i < 5; i++) {


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
        } catch (Exception e) {
            e.printStackTrace();
        }
        //TODO: Probably not a good idea to have this return here:
        return RESTClient.OK;
    }

    protected void onPostExecute(final Integer code) {

        if (RESTClient.noErrors(code)) {
            listener.updateList(tempList);
        } else if (code == -1) {
            new ToastMaster(context, "JSONException converting error", false);
        } else {
            new ToastMaster(context, RESTClient.getErrorMessage(code), false);
        }
        listener.updatePages(pages);
    }
}


