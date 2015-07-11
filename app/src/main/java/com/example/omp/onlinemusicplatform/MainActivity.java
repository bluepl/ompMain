package com.example.omp.onlinemusicplatform;

import android.app.Fragment;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
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
import java.util.Timer;
import java.util.TimerTask;

import helper.AlertDialogManager;
import helper.ConnectionDetector;
import helper.JSONParser;


public class MainActivity extends ListActivity {
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;

    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    CustomDrawerAdapter adapter;

    List<DrawerItem> dataList;
    Intent i;
    String loggedInUser;
    String isAdmin;

    /*Loads Tracks*/
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);
        setContentView(R.layout.test_load_latest);
        Intent j = getIntent();
        loggedInUser = j.getStringExtra("loggedInUser");
        isAdmin = j.getStringExtra("isAdmin");

        // Initializing
        dataList = new ArrayList<DrawerItem>();
        mTitle = mDrawerTitle = getTitle();
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
                GravityCompat.START);


            dataList.add(new DrawerItem("Welcome, " + loggedInUser, R.drawable.ic_face_black_48dp));
            dataList.add(new DrawerItem("LATEST UPLOADS", R.drawable.ic_disc_full_black_18dp));
            dataList.add(new DrawerItem("RECORD SONG", R.drawable.ic_mic_none_black_18dp));
            dataList.add(new DrawerItem("UPLOAD SONG", R.drawable.ic_file_upload_black_24dp));
            dataList.add(new DrawerItem("CATEGORIES", R.drawable.ic_queue_music_black_18dp));
            dataList.add(new DrawerItem("SLEEP MODE", R.drawable.ic_av_timer_black_18dp));
            dataList.add(new DrawerItem("QUIT", R.drawable.ic_exit_to_app_black_18dp));
            if(isAdmin .equals("1")) {
                dataList.add(new DrawerItem("PORTAL", R.drawable.ic_settings_black_18dp));
        }

        adapter = new CustomDrawerAdapter(this, R.layout.custom_drawer_item, dataList);

        mDrawerList.setAdapter(adapter);
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        cd = new ConnectionDetector(getApplicationContext());

        // Check if Internet present
        if (!cd.isConnectingToInternet()) {
            // Internet Connection is not present
            alert.showAlertDialog(MainActivity.this, "Internet Connection Error",
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
                String song_name = ((TextView) view.findViewById(R.id.song_name)).getText().toString();
                String song_artist = ((TextView) view.findViewById(R.id.artist)).getText().toString();
                Toast.makeText(getApplicationContext(), "Song Id: " + song_id + ", Song Url: " + song_url, Toast.LENGTH_SHORT).show();

                i.putExtra("album_id", album_id);
                i.putExtra("song_url", song_url);
                i.putExtra("song_id", song_id);
                i.putExtra("song_name", song_name);
                i.putExtra("song_artist", song_artist);
                i.putExtra("isAdmin", "0");

                startActivity(i);
            }
        });

    }

    public void SelectItem(int position) {

        Fragment fragment = null;
        Bundle args = new Bundle();
        switch (position) {
            //New Songs
            case 0:

                break;
            case 1:

                i = new Intent(getApplicationContext(), MainActivity.class);
                i.putExtra("loggedInUser", loggedInUser);
                i.putExtra("isAdmin","0");
                startActivity(i);
                break;

            //Record a Song
            case 2:
                i = new Intent(getApplicationContext(), AudioCaptureActivity.class);
                i.putExtra("loggedInUser", loggedInUser);
                startActivity(i);
                break;
            //Upload a song
            case 3:
                i = new Intent(getApplicationContext(), UploadToServer.class);
                i.putExtra("loggedInUser", loggedInUser);
                startActivity(i);
                break;
            //My Playlist
            case 4:
                i = new Intent(getApplicationContext(), AlbumsActivity.class);
                i.putExtra("loggedInUser", loggedInUser);
                startActivity(i);
                break;
            //Sleep Mode
            case 5:
                AlertDialog.Builder alert = new AlertDialog.Builder(this);

                alert.setTitle("Sleep Mode");
                alert.setMessage("OMP will turn off after (mins):");

                // Set an EditText view to get user input
                final EditText input = new EditText(this);
                alert.setView(input);

                alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        int value = Integer.parseInt(input.getText().toString());
                        final Timer t = new Timer();
                        t.schedule(new TimerTask() {
                            public void run() {
                                finish();
                                System.exit(0);
                                t.cancel();
                            }
                        }, value * 60000);

                    }
                });

                alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Canceled.
                    }
                });

                alert.show();

                break;
            //Quit
            case 6:
                Intent intent = new Intent(this, login.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("Exit me", true);
                startActivity(intent);
                finish();

                //finish();
                //System.exit(0);
                break;
            //Setting
            case 7:
                if(isAdmin.equals("1")) {
                    i = new Intent(getApplicationContext(), AdminScreenTracks.class);
                    i.putExtra("loggedInUser", loggedInUser);
                    startActivity(i);
                    break;
                }else {
                    break;
                }
        }
/*
        fragment.setArguments(args);
        FragmentManager frgManager = getFragmentManager();
        frgManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
*/

        mDrawerLayout.closeDrawer(mDrawerList);

        mDrawerList.setItemChecked(position, true);
        setTitle(dataList.get(position).getItemName());

    }




    private class DrawerItemClickListener implements
            ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {
            SelectItem(position);

        }
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
            pDialog = new ProgressDialog(MainActivity.this);
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


            List<NameValuePair> params = new ArrayList<NameValuePair>();

            // post album id as GET parameter
            params.add(new BasicNameValuePair("IsActivated", "1"));
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
                            String created_at = c.getString("created_at");
                            String artist = c.getString("created_by");
                            // creating new HashMap
                            HashMap<String, String> map = new HashMap<String, String>();

                            // adding each child node to HashMap key => value
                            map.put("album_id", album_id);
                            map.put("song_id", song_id);
                            map.put("song_url", song_url);
                            map.put("track_no", track_no + ".");
                            map.put("name", name);
                            map.put("artist", artist);
                            map.put("created_at", created_at);

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
                            MainActivity.this, tracksList,
                            R.layout.list_item_latest, new String[]{"album_id", "name", "song_url", "track_no",
                             "created_at", "artist"}, new int[]{
                            R.id.album_id, R.id.song_name, R.id.song_url, R.id.track_no, R.id.song_created_at, R.id.artist});
                    // updating listview
                    setListAdapter(adapter);

                    // Change Activity Title with Album name
                    setTitle(album_name);
                }
            });

        }

    }

}
