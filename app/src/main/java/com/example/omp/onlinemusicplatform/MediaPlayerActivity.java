package com.example.omp.onlinemusicplatform;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class MediaPlayerActivity extends Activity {

    static MediaPlayer mediaPlayer;
    public TextView songName, duration, songArtist;
    private double timeElapsed = 0, finalTime = 0;
    private int forwardTime = 2000, backwardTime = 2000;
    private Handler durationHandler = new Handler();
    ImageButton btn_addLikes;
    private SeekBar seekbar;
    private Button btn_activate;
    String shortUrl, url, song_id, song_name, song_faves, song_artist, isAdmin;
    String urlHead = "http://rifeinblu.com/AndroidFileUpload/uploads";
    InputStream is = null;
    String line = null;
    String result = null;
    int code;
    Context context = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //hosannatelugu.mp3
        Intent i = getIntent();
        song_name = i.getStringExtra("song_name");
        song_id = i.getStringExtra("song_id");
        shortUrl = i.getStringExtra("song_url");
        song_faves = i.getStringExtra("song_faves");
        song_artist = i.getStringExtra("song_artist");
        isAdmin = i.getStringExtra("isAdmin");
        url = urlHead + shortUrl;

        btn_activate = (Button) findViewById(R.id.btnActivate);
        btn_addLikes = (ImageButton) findViewById(R.id.btnAddLikes);


        //set the layout of the Activity
        if(isAdmin.equals("1")) {
            setContentView(R.layout.activity_admin_media_player);
        }else{
            setContentView(R.layout.activity_media_player);
        }

        //initialize views
        initializeViews();
    }

    public void initializeViews(){
        songName = (TextView) findViewById(R.id.songName);
        songArtist= (TextView) findViewById(R.id.songArtist);
        finalTime = 24000;
        duration = (TextView) findViewById(R.id.songDuration);
        seekbar = (SeekBar) findViewById(R.id.seekBar);
        songName.setText(song_name);
        seekbar.setMax((int) finalTime);
        seekbar.setClickable(false);
        songArtist.setText(song_artist);

    }

    // play mp3 song
    public void play(View view) {
        if(mediaPlayer==null || !mediaPlayer.isPlaying()) {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            try {
                mediaPlayer.setDataSource(url);
            } catch (IllegalArgumentException e) {
                Toast.makeText(getApplicationContext(), "You might not set the URI correctly!", Toast.LENGTH_LONG).show();
            } catch (SecurityException e) {
                Toast.makeText(getApplicationContext(), "You might not set the URI correctly!", Toast.LENGTH_LONG).show();
            } catch (IllegalStateException e) {
                Toast.makeText(getApplicationContext(), "You might not set the URI correctly!", Toast.LENGTH_LONG).show();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                mediaPlayer.prepare();
                seekbar.setMax(mediaPlayer.getDuration());
            } catch (IllegalStateException e) {
                Toast.makeText(getApplicationContext(), "Prep: You might not set the URI correctly!", Toast.LENGTH_LONG).show();
            } catch (IOException e) {
                Toast.makeText(getApplicationContext(), "Prep: You might not set the URI correctly!", Toast.LENGTH_LONG).show();
            }
            mediaPlayer.start();
            finalTime = mediaPlayer.getDuration();
            seekbar.setProgress((int) timeElapsed);
            durationHandler.postDelayed(updateSeekBarTime, 100);
        }
    }

    //handler to change seekBarTime
    private Runnable updateSeekBarTime = new Runnable() {
        public void run() {
            //get current position
            timeElapsed = mediaPlayer.getCurrentPosition();
            //set seekbar progress
            seekbar.setProgress((int) timeElapsed);
            //set time remaing
            double timeRemaining = finalTime - timeElapsed;
            duration.setText(String.format("%d min, %d sec", TimeUnit.MILLISECONDS.toMinutes((long) timeRemaining), TimeUnit.MILLISECONDS.toSeconds((long) timeRemaining) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) timeRemaining))));

            //repeat yourself that again in 100 miliseconds
            durationHandler.postDelayed(this, 100);
        }
    };

    // pause mp3 song
    public void pause(View view) {
        if(mediaPlayer!=null && mediaPlayer.isPlaying()){
            mediaPlayer.pause();
        }
    }

    // go forward at forwardTime seconds
    public void forward(View view) {
        //check if we can go forward at forwardTime seconds before song endes
        if ((timeElapsed + forwardTime) <= finalTime) {
            timeElapsed = timeElapsed + forwardTime;

            //seek to the exact second of the track
            mediaPlayer.seekTo((int) timeElapsed);
        }
    }

    // go backwards at backwardTime seconds
    public void rewind(View view) {
        //check if we can go back at backwardTime seconds after song starts
        if ((timeElapsed - backwardTime) > 0) {
            timeElapsed = timeElapsed - backwardTime;

            //seek to the exact second of the track
            mediaPlayer.seekTo((int) timeElapsed);
        }
    }
    // add likes
    public void addLikes(View view) {
        new insertDATA().execute("");
    }

    class insertDATA extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... arg0) {
            ArrayList<NameValuePair> values = new ArrayList<NameValuePair>();
            String query = null;
            if(isAdmin.equals("1")){
                query = "http://192.168.0.108/markTracks.php";
            }else{
                query = "http://192.168.0.108/insertLikes.php";
            }
            values.add(new BasicNameValuePair("mid", song_id));

            try {
                DefaultHttpClient httpclient = new DefaultHttpClient();
                HttpPost httppost = new HttpPost(query);
                httppost.setEntity(new UrlEncodedFormEntity(values));
                HttpResponse response = httpclient.execute(httppost);
                HttpEntity entity = response.getEntity();
                is = entity.getContent();
                Log.i("TAG", "Connection Successful");
            } catch (Exception e) {
                Log.i("TAG", e.toString());
                //Invalid Address
            }

            try {
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(is, "UTF-8"), 8);
                StringBuilder sb = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    sb.append(line + "\n");
                }
                is.close();
                result = sb.toString();
                Log.i("TAG", "Result Retrieved");
            } catch (Exception e) {
                Log.i("TAG", e.toString());
            }

            try {
                JSONObject json = new JSONObject(result);
                code = (json.getInt("code"));
                if (code == 1) {
                    Log.i("msg", "Data Successfully Inserted");
//Data Successfully Inserted
                } else {
//Data Not Inserted
                }
            } catch (Exception e) {
                Log.i("TAG", e.toString());
            }
            return null;
        }

    }

    protected void onDestroy() {
        // TODO Auto-generated method stub
        if (mediaPlayer != null) {
            mediaPlayer.pause();
        }
        super.onDestroy();
    }

}