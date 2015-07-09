package com.example.omp.onlinemusicplatform;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
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


public class register extends ActionBarActivity {

    InputStream is = null;

    String line = null;
    String result = null;
    int code;

    EditText etUn = null;
    EditText etPw = null;
    EditText etMail = null;
    Button btnRegister = null;

    String un = null;
    String pw = null;
    String mail = null;

    Context context = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        context = this;

        etUn = (EditText) findViewById(R.id.et_un);
        etPw = (EditText) findViewById(R.id.et_pw);
        etMail = (EditText) findViewById(R.id.et_mail);
        btnRegister = (Button) findViewById(R.id.btn_register);

        btnRegister.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                un = etUn.getText().toString();
                pw = new String(Hex.encodeHex(DigestUtils.sha(etPw.getText().toString())));
                mail = etMail.getText().toString();
                new insertDATA().execute("");
            }
        });

    }

    class insertDATA extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... arg0) {
            ArrayList<NameValuePair> values = new ArrayList<NameValuePair>();

            values.add(new BasicNameValuePair("name", un));
            values.add(new BasicNameValuePair("pw", pw));
            values.add(new BasicNameValuePair("email", mail));
            values.add(new BasicNameValuePair("isActivated", "1"));
            try {
                DefaultHttpClient httpclient = new DefaultHttpClient();
                HttpPost httppost = new HttpPost("http://192.168.0.108/insertReg.php");
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
                    //Toast.makeText(getApplicationContext(), "Data Successfully Inserted", Toast.LENGTH_SHORT).show();

//Data Successfully Inserted
                } else {
//Data Not Inserted
                }
            } catch (Exception e) {
                Log.i("TAG", e.toString());
                //Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show();

            }
            return null;
        }

    }

}