package com.example.omp.onlinemusicplatform;

import java.util.ArrayList;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import com.example.omp.onlinemusicplatform.R;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("NewApi")
public class login extends Activity {
    EditText un, pw;
    TextView error;
    Button login, bypassLogin;
    private String resp;
    private String errorMsg;
    private Dao Dao = new Dao();

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        un = (EditText) findViewById(R.id.et_un);
        pw = (EditText) findViewById(R.id.et_pw);
        login = (Button) findViewById(R.id.btn_login);
        bypassLogin = (Button) findViewById(R.id.btn_bypassLogin);
        error = (TextView) findViewById(R.id.tv_error);

        login.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                /** According with the new StrictGuard policy,  running long tasks on the Main UI thread is not possible
                 So creating new thread to create and execute http operations */
                String UserName = un.getText().toString();
                String HashedPassword = new String(Hex.encodeHex(DigestUtils.sha(pw.getText().toString())));
                try
                {
                    if(Dao.CheckLogin(UserName,HashedPassword))
                    {
                        sendMessage(v);
                //Login success -> redirect to the Main activity
                //Intent launchActivity= new Intent(login.this,MainActivity.class);
                //startActivity(launchActivity);
            }
        } catch (Exception e) {
            e.printStackTrace();
            errorMsg = e.getMessage();
        }



                try {
                    /** wait a second to get response from server */
                    Thread.sleep(1000);
                    /** Inside the new thread we cannot update the main thread
                     So updating the main thread outside the new thread */

                    error.setText(resp);

                    if (null != errorMsg && !errorMsg.isEmpty()) {
                        error.setText(errorMsg);
                    }
                } catch (Exception e) {
                    error.setText(e.getMessage());
                }
            }
        });
        bypassLogin.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Byspass Login -> redirect to the Main activity immediately

                Intent i = new Intent(getApplicationContext(), MainActivity.class);


                Toast.makeText(getApplicationContext(), "Login Bypassed.", Toast.LENGTH_SHORT).show();
                /*
                    i.putExtra("file_desc", outputFile);
                    i.putExtra("file_name", audName);
                */
                startActivity(i);

            }
        });

    }

    public void sendMessage(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}