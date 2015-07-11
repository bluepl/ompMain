package com.example.omp.onlinemusicplatform;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import helper.AlertDialogManager;
import helper.ConnectionDetector;
import helper.JSONParser;

public class AdminScreenTracks extends ListActivity {
    // Connection detector
    ConnectionDetector cd;

    // Alert dialog manager
    AlertDialogManager alert = new AlertDialogManager();

    // Progress Dialog
    private ProgressDialog pDialog;

    // Creating JSON Parser object
    JSONParser jsonParser = new JSONParser();

    ArrayList<HashMap<String, String>> tracksList;

    // tracks JSONArray
    JSONArray albums = null;

    // Album id
    String album_id, album_name;
    Switch btn_tbisActivate;

    // tracks JSON url
    // id - should be posted as GET params to get track list (ex: id = 5)
    private static final String URL_ADMIN_ALBUMS = "http://192.168.0.108/ompAdminTracks.php";

    // ALL JSON node names
    private static final String TAG_SONGS = "songs";
    private static final String TAG_ID = "id";
    private static final String TAG_NAME = "name";
    private static final String TAG_ALBUM = "album";
    private static final String TAG_DURATION = "created_at";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_screen_tracks);

        cd = new ConnectionDetector(getApplicationContext());

        btn_tbisActivate = (Switch)findViewById(R.id.tb_isActivate);
        btn_tbisActivate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                new LoadTracks().execute();
            }

        });

        // Check if Internet present
        if (!cd.isConnectingToInternet()) {
            // Internet Connection is not present
            alert.showAlertDialog(AdminScreenTracks.this, "Internet Connection Error",
                    "Please connect to working Internet connection", false);
            // stop executing code by return
            return;
        }

        // Get album id
        Intent i = getIntent();
        album_id = i.getStringExtra("album_id");

        // Hashmap for ListView
        tracksList = new ArrayList<HashMap<String, String>>();

        // Loading tracks in Background Thread
        new LoadTracks().execute();

        // get listview
        ListView lv = getListView();

        /**
         * Listview on item click listener
         * SingleTrackActivity will be lauched by passing album id, song id
         * */
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View view, int arg2,
                                    long arg3) {
                // On selecting single track get song information
                Intent i = new Intent(getApplicationContext(), MediaPlayerActivity.class);

                // to get song information
                // both album id and song is needed
                String song_url = ((TextView) view.findViewById(R.id.song_url)).getText().toString();
                String song_id = ((TextView) view.findViewById(R.id.song_id)).getText().toString();
                String song_name = ((TextView) view.findViewById(R.id.album_name)).getText().toString();
                String song_active = ((TextView) view.findViewById(R.id.tv_isActivated)).getText().toString();
                String song_artist = ((TextView) view.findViewById(R.id.artist)).getText().toString();
                Toast.makeText(getApplicationContext(), "Song Id: " + song_id  + ", Song Url: " + song_url, Toast.LENGTH_SHORT).show();

                i.putExtra("album_id", album_id);
                i.putExtra("song_url", song_url);
                i.putExtra("song_id", song_id);
                i.putExtra("song_name", song_name);
                i.putExtra("song_faves", song_active);
                i.putExtra("song_artist", song_artist);
                i.putExtra("isAdmin", "1");

                startActivity(i);
            }
        });

    }

    /**
     * Background Async Task to Load all tracks under one album
     * */
    class LoadTracks extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(AdminScreenTracks.this);
            pDialog.setMessage("Loading songs ...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        /**
         * getting tracks json and parsing
         * */
        protected String doInBackground(String... args) {
            // Building Parameters

            String strIsActivated = "1";
            if((((Switch) findViewById(R.id.tb_isActivate)).isChecked())) {
                strIsActivated = "0";
            }

            List<NameValuePair> params = new ArrayList<NameValuePair>();

            // post album id as GET parameter
            params.add(new BasicNameValuePair("IsActivated", strIsActivated));
            // getting JSON string from URL
            String json = jsonParser.makeHttpRequest(URL_ADMIN_ALBUMS, "GET",
                    params);

            // Check your log cat for JSON reponse
            Log.d("Track List JSON: ", json);

            tracksList.clear();

            try {
                JSONObject jObj = new JSONObject(json);

                if (jObj != null) {
                    String album_id = jObj.getString("id");
                    album_name = jObj.getString("album");
                    albums = jObj.getJSONArray("songs");

                    if (albums != null) {
                        // looping through All songs
                        for (int i = 0; i < albums.length(); i++) {
                            JSONObject c = albums.getJSONObject(i);

                            // Storing each json item in variable
                            String song_id = c.getString("mid");
                            String song_url = c.getString("url");
                            // track no - increment i value
                            String track_no = String.valueOf(i + 1);
                            String name = c.getString("name");
                            String isActivated = c.getString("IsActivated");
                            String artist = c.getString("created_by");
                            // creating new HashMap
                            HashMap<String, String> map = new HashMap<String, String>();

                            // adding each child node to HashMap key => value
                            map.put("album_id", album_id);
                            map.put("song_id", song_id);
                            map.put("song_url", song_url);
                            map.put("track_no", track_no + ".");
                            map.put(TAG_NAME, name);
                            map.put("artist", artist);
                            map.put("song_active", isActivated);

                            // adding HashList to ArrayList

                            tracksList.add(map);
                        }
                    } else {
                        Log.d("Albums: ", "null");
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        /**
         * After completing background task Dismiss the progress dialog
         * **/
        protected void onPostExecute(String file_url) {
            // dismiss the dialog after getting all tracks
            pDialog.dismiss();
            // updating UI from Background Thread
            runOnUiThread(new Runnable() {
                public void run() {
                    /**
                     * Updating parsed JSON data into ListView
                     * */
                    ListAdapter adapter = new SimpleAdapter(
                            AdminScreenTracks.this, tracksList,
                            R.layout.list_item_admin_tracks, new String[] { "album_id", "song_id", "song_url", "track_no",
                            TAG_NAME, "song_active", "artist"}, new int[] {
                            R.id.album_id, R.id.song_id, R.id.song_url, R.id.track_no, R.id.album_name, R.id.tv_isActivated, R.id.artist });
                    // updating listview
                    setListAdapter(adapter);

                    // Change Activity Title with Album name
                    setTitle(album_name);
                }
            });

        }

    }
}