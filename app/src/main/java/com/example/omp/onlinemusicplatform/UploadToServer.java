package com.example.omp.onlinemusicplatform;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
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
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;


public class UploadToServer extends Activity implements OnClickListener{

    private TextView messageText;
    private Button uploadButton, btnselectpic;
    //private ImageView imageview;
    private int serverResponseCode = 0;
    private ProgressDialog dialog = null;



    InputStream is = null;
    String line = null;
    String result = null;
    int code;
    Context context = null;
    /**
     * *******  File Path ************
     */
    String loggedInUser = "";
    String file_desc = "";
    String file_id = "";
    String file_name = "";
    String file_locale = "";
    Button update = null;
    private String upLoadServerUri = null;
    private String audiopath=null;


    String uid_text = null;
    String name_text = null;
    String category_text = null;
    EditText uid = null;
    EditText name = null;
    EditText category = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_to_server);


        uploadButton = (Button)findViewById(R.id.uploadButton);
        messageText  = (TextView)findViewById(R.id.messageText);
        btnselectpic = (Button)findViewById(R.id.button_selectpic);
        //imageview = (ImageView)findViewById(R.id.imageView_pic);
        Intent i = getIntent();
        file_desc = i.getStringExtra("file_desc");
        file_name =  i.getStringExtra("file_name");
        //file_locale =  i.getStringExtra("file_desc");
        //file_id = "1";
        btnselectpic.setOnClickListener(this);
        uploadButton.setOnClickListener(this);
        upLoadServerUri = "http://rifeinblu.com/AndroidFileUpload/UploadToServer.php";
        //upLoadServerUri = "http://192.168.0.108/UploadToServer.php";


        update = (Button) findViewById(R.id.btn_updateDb);



        //uid = (EditText) findViewById(R.id.uid);
        name = (EditText) findViewById(R.id.name);
        category = (EditText) findViewById(R.id.category);

        update.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // uid_text = uid.getText().toString(); //name
                name_text = name.getText().toString(); //genre
                category_text = category.getText().toString(); //category == genre
                new insertDATA().execute("");
            }
        });
    }

    class insertDATA extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... arg0) {
            ArrayList<NameValuePair> values = new ArrayList<NameValuePair>();

            String extractedFileName = "";
            int rightPosition = 0;
            for(int i=0; i < file_desc.length(); i++){
                if(file_desc.charAt(i) == '/') {
                    rightPosition = file_desc.length() - i;
                }
            }
            extractedFileName = file_desc.substring(file_desc.length() - rightPosition);

            // values.add(new BasicNameValuePair("id", null));
            values.add(new BasicNameValuePair("name", name_text));
            values.add(new BasicNameValuePair("url", extractedFileName));
            values.add(new BasicNameValuePair("created_by", loggedInUser));
            values.add(new BasicNameValuePair("isActivated", "1"));
            values.add(new BasicNameValuePair("cid", category_text));

            try {
                DefaultHttpClient httpclient = new DefaultHttpClient();
                HttpPost httppost = new HttpPost("http://192.168.0.108/insertDATA.php");
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

    @Override
    public void onClick(View arg0) {
        if(arg0==btnselectpic)
        {
            Intent intent = new Intent();
            intent.setType("audio/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Complete action using"), 1);
        }
        else if (arg0==uploadButton) {

            dialog = ProgressDialog.show(UploadToServer.this, "", "Uploading file...", true);
            messageText.setText("uploading started.....");
            new Thread(new Runnable() {
                public void run() {

                    //uploadFile(file_desc);
                    if(audiopath==null)
                    {
                        uploadFile(file_desc);
                        uid_text = "";
                        name_text = name.getText().toString();
                        category_text = category.getText().toString();
                        new insertDATA().execute("");

                    }else {
                        file_desc = audiopath;
                        uploadFile(file_desc);
                        uid_text = "";
                        name_text = name.getText().toString();
                        category_text = category.getText().toString();
                        new insertDATA().execute("");
                    }
                }
            }).start();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 1 && resultCode == RESULT_OK) {
            //Bitmap photo = (Bitmap) data.getData().getPath();

            Uri selectedAudioUri = data.getData();
            audiopath = getPath(selectedAudioUri);
            //Bitmap bitmap=BitmapFactory.decodeFile(audiopath);
            //imageview.setImageBitmap(bitmap);
            messageText.setText("Uploading file path:" +audiopath);

        }
    }
    public String getPath(Uri uri) {
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    public int uploadFile(String sourceFileUri) {


        String fileName = sourceFileUri;

        HttpURLConnection conn = null;
        DataOutputStream dos = null;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;
        File sourceFile = new File(sourceFileUri);

        if (!sourceFile.isFile()) {

            dialog.dismiss();

            Log.e("uploadFile", "Source File not exist :" + file_desc);

            runOnUiThread(new Runnable() {
                public void run() {
                    messageText.setText("Source File not exist :" + file_desc);
                }
            });

            return 0;

        } else {
            try {

                // open a URL connection to the Servlet
                FileInputStream fileInputStream = new FileInputStream(sourceFile);
                URL url = new URL(upLoadServerUri);

                // Open a HTTP  connection to  the URL
                conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true); // Allow Inputs
                conn.setDoOutput(true); // Allow Outputs
                conn.setUseCaches(false); // Don't use a Cached Copy
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                conn.setRequestProperty("uploaded_file", fileName);

                dos = new DataOutputStream(conn.getOutputStream());

                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\""
                        + fileName + "\"" + lineEnd);

                dos.writeBytes(lineEnd);

                // create a buffer of  maximum size
                bytesAvailable = fileInputStream.available();

                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];

                // read file and write it into form...
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                while (bytesRead > 0) {

                    dos.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                }

                // send multipart form data necesssary after file data...
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                // Responses from the server (code and message)
                serverResponseCode = conn.getResponseCode();
                String serverResponseMessage = conn.getResponseMessage();

                Log.i("uploadFile", "HTTP Response is : "
                        + serverResponseMessage + ": " + serverResponseCode);

                if (serverResponseCode == 200) {

                    runOnUiThread(new Runnable() {
                        public void run() {
                            String msg = "File Upload Completed.\n\n See uploaded file here : \n\n"
                                    + file_desc;
                            messageText.setText(msg);
                            Toast.makeText(UploadToServer.this, "File Upload Complete.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                //close the streams //
                fileInputStream.close();
                dos.flush();
                dos.close();

            } catch (MalformedURLException ex) {

                dialog.dismiss();
                ex.printStackTrace();

                runOnUiThread(new Runnable() {
                    public void run() {
                        messageText.setText("MalformedURLException Exception : check script url.");
                        Toast.makeText(UploadToServer.this, "MalformedURLException", Toast.LENGTH_SHORT).show();
                    }
                });

                Log.e("Upload file to server", "error: " + ex.getMessage(), ex);
            } catch (Exception e) {

                dialog.dismiss();
                e.printStackTrace();

                runOnUiThread(new Runnable() {
                    public void run() {
                        messageText.setText("Got Exception : see logcat ");
                        Toast.makeText(UploadToServer.this, "Got Exception : see logcat ", Toast.LENGTH_SHORT).show();
                    }
                });
                Log.e("Upload Exception", "Exception : " + e.getMessage(), e);
            }
            dialog.dismiss();
            return serverResponseCode;

        } // End else block
    }

}