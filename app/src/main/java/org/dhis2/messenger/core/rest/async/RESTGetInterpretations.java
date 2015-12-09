package org.dhis2.messenger.core.rest.async;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.dhis2.messenger.core.rest.RESTSessionStorage;
import org.dhis2.messenger.model.ChatModel;
import org.dhis2.messenger.model.InterpretationModel;
import org.dhis2.messenger.model.NameAndIDModel;
import org.dhis2.messenger.core.rest.APIPath;
import org.dhis2.messenger.core.rest.RESTClient;
import org.dhis2.messenger.core.rest.Response;
import org.dhis2.messenger.core.rest.callback.InterpretationCallback;
import org.dhis2.messenger.SharedPrefs;
import org.dhis2.messenger.gui.ToastMaster;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by iNick on 20.10.14.
 */
public class RESTGetInterpretations extends AsyncTask<Integer, String, Integer> {
    private static InterpretationCallback listener;
    String server, auth;
    Context context;
    private boolean gotFromCache = false;
    private boolean skipCache;

    private List<InterpretationModel> tempList;
    int page; //which page to get.
    //TODO: getInterpretationsPageSize from RESTSessionStorage
    static int pageSize = 5; // what size for the pages to request.

    public RESTGetInterpretations(InterpretationCallback listener, Context context, int page, boolean skipCache) {
        tempList = new ArrayList<InterpretationModel>();
        this.listener = listener;
        this.server = SharedPrefs.getServerURL(context);
        this.auth = SharedPrefs.getCredentials(context);
        this.context = context;
        this.page = page; //0
        this.skipCache = skipCache;
    }

    public List<InterpretationModel> getTempList(){
        return this.tempList;
    }

    protected Integer doInBackground(Integer... args) {
        tempList = RESTSessionStorage.getInstance().getInterpretationModelList(page);
        if (!skipCache && !tempList.isEmpty()) {
            gotFromCache = true;
        } else {
            gotFromCache = false;
            String api = server + APIPath.FIRST_PAGE_INTERPRETATIONS;
            try {
                JSONObject json;
                Response response;
                if (page == 1) {
                    response = RESTClient.get(api + APIPath.INTERPRETATIONS_FIELDS + "&pageSize=" + pageSize, auth);
                } else {
                    response = RESTClient.get(api + APIPath.INTERPRETATIONS_FIELDS + "&pageSize=" + pageSize + "&page=" + page, auth);
                }

                if (RESTClient.noErrors(response.getCode())) {
                    json = new JSONObject(response.getBody());
                    JSONObject pager = json.getJSONObject("pager");
                    page = Integer.parseInt(pager.getString("pageCount"));

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
                        if (!row.getString("type").equals("DATASET_REPORT")) {
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

                            if (type.equals("CHART")) {
                                JSONObject map = row.getJSONObject("chart");
                                typeid = map.getString("id");
                                pictureURL += "api/charts/" + typeid + "/data";
                                //picture = BitmapFactory.decodeResource(context.getResources(), R.drawable.chart);
                            } else if (type.equals("MAP")) {
                                JSONObject chart = row.getJSONObject("map");
                                typeid = chart.getString("id");
                                pictureURL += "api/maps/" + typeid + "/data";
                                //picture = BitmapFactory.decodeResource(context.getResources(), R.drawable.wordmap);
                            } else if (type.equals("REPORT_TABLE")) {
                                JSONObject reportTable = row.getJSONObject("reportTable");
                                typeid = reportTable.getString("id");
                                pictureURL += "api/reportTables/" + typeid + "/data.html";
                                //picture = BitmapFactory.decodeResource(context.getResources(), R.drawable.table);
                            }

                            //Get the original data from dhis2 server
                            //And show it on the interpretations fragment
                            URL url = new URL(pictureURL);
                            InputStream content = (InputStream)url.getContent();
                            picture = BitmapFactory.decodeStream(content);
                            tempList.add(new InterpretationModel(id, text, date, user, type, pictureURL, picture, comments, false));
                        }
                    }
                    return response.getCode();
                } else {
                    return response.getCode();
                }
            } catch (JSONException e) {
                e.printStackTrace();
                return -1;
            } catch (Exception e) {
                e.printStackTrace();
                return RESTClient.OK;
            }
        }
        //TODO: Probably not a good idea to have this return here:
        return RESTClient.OK;
    }

    protected void onPostExecute(final Integer code) {
        if (RESTClient.noErrors(code)) {
            //Check whether the interpretation list should be updated or not
            if(tempList.isEmpty()){
                new ToastMaster(context, "No interpretations available.", false);
            } else {
                if (!gotFromCache) {
                    RESTSessionStorage.getInstance().setInterpretatoinModelList(page, tempList);
                }
                listener.updateList(tempList, page);
            }
        } else if (code == -1) {
            new ToastMaster(context, "JSONException converting error", false);
        } else {
            new ToastMaster(context, RESTClient.getErrorMessage(code), false);
        }
    }
}


