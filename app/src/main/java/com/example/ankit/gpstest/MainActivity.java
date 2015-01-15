package com.example.ankit.gpstest;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;


public class MainActivity extends ActionBarActivity {

    static TextView tv ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv = (TextView) findViewById(R.id.tv);
        //startService(new Intent(this, LocationService.class));
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.setType("image/*");
        i.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        Intent c = Intent.createChooser(i, "Select soundfile");
        startActivityForResult(c,1);
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

    private class UploadFormTask extends AsyncTask<Uri, Void, Boolean>{

        @Override
        protected Boolean doInBackground(Uri... uri) {
            try {

            FormUploader x = new FormUploader("http://ankitstarski.phpzilla.net/secretupload.php", "UTF-8");
                x.addFilePart("upload_file", getContentResolver().openInputStream(uri[0]),"sadas.jpg");
                x.finish();
            }
            catch (Exception ex){
                return false;
            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean s) {
            if(!s){
                Log.i("fos","error");
                Toast.makeText(getApplicationContext(),"Error in Uploading",Toast.LENGTH_SHORT).show();
            }
            else{
                Log.i("fos","uploaded");
                Toast.makeText(getApplicationContext(),"File Uploaded Successfully",Toast.LENGTH_SHORT).show();
            }
        }
    }

    static void printLoc(String x){
        tv.setText(x);
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
