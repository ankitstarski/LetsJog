package com.example.ankit.gpstest;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;


public class MainActivity extends ActionBarActivity {

    private LocationManager  locationManager;
    Button upload;
    ProgressBar progressBar ;
    int bytesAvailable,totalBytes;

    double lat,lon;

    String uploadURL = "http://ankitstarski.phpzilla.net/secretupload.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        upload = (Button) findViewById(R.id.upload_button);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        //startService(new Intent(this, LocationService.class));

        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                locationManager = (LocationManager) getSystemService ( Context.LOCATION_SERVICE );
                Location current =  locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                lon =  current.getLongitude();
                lat = current.getLatitude();

                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("audio/*");
                //intent.addCategory(Intent.CATEGORY_APP_MUSIC);
                startActivityForResult(intent,1);

            }
        });




    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {

        // The ACTION_OPEN_DOCUMENT intent was sent with the request code
        // READ_REQUEST_CODE. If the request code seen here doesn't match, it's the
        // response to some other intent, and the code below shouldn't run at all.

        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.
            // Pull that URI using resultData.getData().

            Uri uri = null;
            if (resultData != null) {
                uri = resultData.getData();
                Log.i("fos", "Uri: " +
                        uri.toString());
                new UploadFormTask().execute(uri);

            }
        }
    }

    private class UploadFormTask extends AsyncTask<Uri, Integer, Boolean>{

        @Override
        protected void onPreExecute() {
            progressBar.setProgress(0);
            progressBar.setVisibility(ProgressBar.VISIBLE);
        }

        @Override
        protected Boolean doInBackground(Uri... uri) {
            try {


                HttpURLConnection connection = null;
                DataOutputStream outputStream = null;
                String lineEnd = "\r\n";
                String twoHyphens = "--";
                String boundary = "*****";
                int bytesRead, bytesAvailable, bufferSize;
                byte[] buffer;
                int maxBufferSize = 1 * 1024 * 1024;

                InputStream fileInputStream = getContentResolver().openInputStream(uri[0]);

                int serverResponseCode;


                URL url = new URL(uploadURL);
                connection = (HttpURLConnection) url.openConnection();

                // Allow Inputs & Outputs
                connection.setDoInput(true);
                connection.setDoOutput(true);
                connection.setUseCaches(false);

                // Enable POST method
                connection.setRequestMethod("POST");

                connection.setRequestProperty("Connection", "Keep-Alive");
                connection.setRequestProperty("Content-Type",     "multipart/form-data;boundary="+boundary);

                outputStream = new DataOutputStream(     connection.getOutputStream() );
                outputStream.writeBytes(twoHyphens + boundary + lineEnd);
                outputStream.writeBytes("Content-Disposition: form-data;     name=\"upload_file\";filename=\"" + System.currentTimeMillis() +"\"" + lineEnd);
                outputStream.writeBytes(lineEnd);

                bytesAvailable = fileInputStream.available();
                Log.v("Size", bytesAvailable + "");

                progressBar.setProgress(0);
                progressBar.setMax(bytesAvailable);
                //Log.v("Max",pb.getMax()+"");

                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];


                // Read file
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                while (bytesRead > 0)
                {
                    outputStream.write(buffer, 0, bufferSize);

                    bytesAvailable = fileInputStream.available();
                    Log.v("Available",bytesAvailable+"");

                    publishProgress();

                    bufferSize = Math.min(bytesAvailable,     maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0,      bufferSize);
                }

                outputStream.writeBytes(lineEnd);

                /* lat field */

                outputStream.writeBytes(twoHyphens + boundary + lineEnd);
                outputStream.writeBytes("Content-Disposition: form-data; name=\"" + "latitude" + "\"");
                outputStream.writeBytes(lineEnd);
                outputStream.writeBytes(lineEnd+""+lat+""+lineEnd);
                outputStream.flush();

                /* lon field */

                outputStream.writeBytes(twoHyphens + boundary + lineEnd);
                outputStream.writeBytes("Content-Disposition: form-data; name=\"" + "longitude" + "\"");
                outputStream.writeBytes(lineEnd);
                outputStream.writeBytes(lineEnd+""+lon+""+lineEnd);
                outputStream.flush();

                outputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                // Responses from the server (code and message)
                serverResponseCode = connection.getResponseCode();


                fileInputStream.close();
                outputStream.flush();
                outputStream.close();

                if(serverResponseCode==200){
                    return true;
                }else {
                    return false;
                }




            /*
            FormUploader x = new FormUploader(uploadURL, "UTF-8");
                x.addFilePart("upload_file", getContentResolver().openInputStream(uri[0]),"sadas.jpg");
                x.finish();*/
            }
            catch (Exception ex){
                return false;
            }


            //return true;
        }


        @Override
        protected void onProgressUpdate(Integer... values) {

            progressBar.setProgress(progressBar.getMax()-(int)(bytesAvailable/(double)totalBytes*100));
        }

        @Override
        protected void onPostExecute(Boolean s) {
            if(!s){
                Log.i("fos","error");
                Toast.makeText(MainActivity.this,"Error in Uploading",Toast.LENGTH_SHORT).show();
            }
            else{
                Log.i("fos","uploaded");
                Toast.makeText(MainActivity.this,"File Uploaded Successfully",Toast.LENGTH_SHORT).show();
            }

            progressBar.setVisibility(ProgressBar.INVISIBLE);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
