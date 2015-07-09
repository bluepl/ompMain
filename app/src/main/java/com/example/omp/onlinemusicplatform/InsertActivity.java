package com.example.omp.onlinemusicplatform;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class InsertActivity extends Activity {

    String uid_text = null;
    String name_text = null;
    String city_text = null;

    InputStream is = null;

    String line = null;
    String result = null;
    int code;

    EditText uid = null;
    EditText name = null;
    EditText city = null;
    Button submit = null;

    Context context = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_insert);

        context = this;

        uid = (EditText) findViewById(R.id.uid);
        name = (EditText) findViewById(R.id.name);
        city = (EditText) findViewById(R.id.city);
        submit = (Button) findViewById(R.id.submit);

        submit.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                uid_text = uid.getText().toString();
                name_text = name.getText().toString();
                city_text = city.getText().toString();
                new insertDATA().execute("");
            }
        });

    }

    class insertDATA extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... arg0) {
            ArrayList<NameValuePair> values = new ArrayList<NameValuePair>();

            values.add(new BasicNameValuePair("uid", uid_text));
            values.add(new BasicNameValuePair("name", name_text));
            values.add(new BasicNameValuePair("city", city_text));

            try {
                DefaultHttpClient httpclient = new DefaultHttpClient();
                HttpPost httppost = new HttpPost("http://rifeinblu.com/AndroidFileUpload/insertDATA.php");
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
                        new InputStreamReader(is, "iso-8859-1"), 8);
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

}