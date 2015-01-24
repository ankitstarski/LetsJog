package com.example.ankit.letsjog;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;


public class UploadActivity extends ActionBarActivity {

    Intent intent;
    ProgressBar progressBar;
    TextView txtPercentage;
    TextView songName;
    ImageView coverArt;
    Long totalSize;

    NotificationManager mNotifyManager;
    NotificationCompat.Builder mBuilder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        // UI Items
        txtPercentage = (TextView)findViewById(R.id.percentage);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        coverArt = (ImageView)findViewById(R.id.coverArt);
        songName = (TextView) findViewById(R.id.songName);

        // Upload Intent
        intent = getIntent();




        /*
        mNotifyManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setContentTitle("Uploading Song")
                .setContentText("Starting upload")
                .setSmallIcon(android.R.drawable.ic_menu_upload);
        mBuilder.setOngoing(true);


        // Launch Upload Task
        UploadFileToServer asyncTask = new UploadFileToServer();


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
        else {
            asyncTask.execute();
        }
        */
//
//        Intent service= new Intent(this,UploadingService.class);
//        service.putExtras(intent.getExtras());
//        startService(service);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_upload, menu);
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }


    public class UploadProgressReciever extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            int progress = intent.getIntExtra("progress",0);

            progressBar.setProgress(progress);
            progressBar.setIndeterminate(false);
            txtPercentage.setText(progress + "%");
            Log.i("upload", progress + "%");
        }
    }




    private class UploadFileToServer extends AsyncTask<Void, Integer, String> {

        File sourceFile;
        Uri albumArtUri;
        String songTitle;
        int _id;
        String playlistId;

        String responseString = null;

        @Override
        protected void onPreExecute() {
            // setting progress bar to zero
            sourceFile = new File(intent.getStringExtra("uri"));
            albumArtUri = Uri.parse(intent.getStringExtra("coverUri"));
            songTitle = intent.getStringExtra("title");
            playlistId = intent.getStringExtra("playlist");
            _id = intent.getIntExtra("_id",-1);

            Global.uploadingList.add(_id);

            setBitmapFromUri(albumArtUri);
            songName.setText(songTitle);

            progressBar.setProgress(0);

            mBuilder.setContentText("Uploading :" + songTitle);
        }

        @Override
        protected String doInBackground(Void... params) {
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(Global.FORM_URL);

            try {
                MultipartUploader entity = new MultipartUploader(
                        new MultipartUploader.ProgressListener() {
                            @Override
                            public void transferred(long num) {
                                publishProgress((int) ((num / (float) totalSize) * 100));
                            }
                        }
                );

                // Adding file data to http body
                entity.addPart("upload_file", new FileBody(sourceFile));
                // Adding Name of song to the form
                entity.addPart("title", new StringBody(songTitle));

                // Extra parameters if you want to pass to server
                entity.addPart("lat", new StringBody(""+LocationFinder.getLat()));
                entity.addPart("lon", new StringBody(""+LocationFinder.getLon()));
                entity.addPart("playlist", new StringBody(""+playlistId));

                totalSize = entity.getContentLength();
                httppost.setEntity(entity);

                // Making server call
                HttpResponse response = httpclient.execute(httppost);
                HttpEntity r_entity = response.getEntity();

                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == 200) {
                    // Server response
                    responseString = EntityUtils.toString(r_entity);
                } else {
                    responseString = "Error occurred! Http Status Code: "
                            + statusCode;
                }

            } catch (ClientProtocolException e) {
                responseString = e.toString();
            } catch (IOException e) {
                //Log.e("fos",e.getMessage());
                responseString = e.toString();
            }
            return responseString;
        }

        @Override
        protected void onPostExecute(String result) {
            Log.e("fos", "Response from server: " + result);

            // showing the server response in an alert dialog
            showAlert(result);

            mBuilder.setContentText(result)
                    .setProgress(0,0,false);
            mBuilder.setOngoing(false);
            mNotifyManager.notify(_id+100, mBuilder.build());

            int i;
            if((i=Global.uploadingList.indexOf(_id))!=-1){
                Global.uploadingList.remove(i);
            }

            super.onPostExecute(result);
        }

        private void publishProgress(final int progress){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // Activity part
                    progressBar.setProgress(progress);
                    progressBar.setIndeterminate(false);
                    txtPercentage.setText(progress + "%");
                    Log.i("upload", progress + "%");

                    // Notification part
                    mBuilder.setProgress(100, progress, false);
                    mBuilder.setContentInfo(progress + "% uploaded");
                    // Issues the notification
                    mNotifyManager.notify(_id+100, mBuilder.build());
                }
            });
        }
    }


    /**
     * Method to show alert dialog
     * */
    private void showAlert(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message).setTitle("LetsJog")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // do nothing
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }


    private void setBitmapFromUri(Uri albumArtUri){
        Bitmap bitmap = null;
        try {
            Log.i("fos",albumArtUri.toString());
            bitmap = MediaStore.Images.Media.getBitmap(
                    UploadActivity.this.getContentResolver(), albumArtUri);
            bitmap = Bitmap.createScaledBitmap(bitmap, 240, 240, true);

        } catch (FileNotFoundException exception) {
            exception.printStackTrace();
            bitmap = BitmapFactory.decodeResource(
                    UploadActivity.this.getResources(),
                    R.drawable.ic_coverart);
        } catch (IOException e) {
            Log.i("fos","IOException Here");
        }

        coverArt.setImageBitmap(bitmap);
    }

}
