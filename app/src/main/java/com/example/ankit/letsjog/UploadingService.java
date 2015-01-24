package com.example.ankit.letsjog;


import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

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
import java.io.IOException;

/**
 * Created by ankit on 1/24/15.
 */
public class UploadingService extends Service {


    Intent intent;
    Long totalSize;

    NotificationManager mNotifyManager;
    NotificationCompat.Builder mBuilder;
    final int NOTIFICATION_ID = 1011;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        this.intent = intent;

        mNotifyManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(this);

        Intent notificationIntent = new Intent(this, UploadActivity.class);
        notificationIntent.putExtras(intent.getExtras());
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        mBuilder.setContentTitle("Uploading Music ")
                .setContentText("Starting upload")
                .setSmallIcon(android.R.drawable.ic_menu_upload)
                .setOngoing(true);
                //.setContentIntent(pendingIntent);

        int _id = intent.getIntExtra("_id",-1);

        if(!Global.uploadingList.contains(_id)) {
            new Thread(new UploadFileToServer()).start();
            startForeground(_id, mBuilder.build());
        }

        return START_REDELIVER_INTENT;
    }


    private class UploadFileToServer implements Runnable{

        File sourceFile;
        Uri albumArtUri;
        String songTitle;
        int _id;
        String playlistId;

        String responseString = null;

        @Override
        public void run() {
            preExecute();
            background();
            postExecute();
        }

        private void preExecute(){
            // setting progress bar to zero
            sourceFile = new File(intent.getStringExtra("uri"));
            albumArtUri = Uri.parse(intent.getStringExtra("coverUri"));
            songTitle = intent.getStringExtra("title");
            playlistId = intent.getStringExtra("playlist");
            _id = intent.getIntExtra("_id",-1);

            Global.uploadingList.add(_id);
            mBuilder.setContentTitle("Uploading Music ")
                    .setContentText("Starting upload")
                    .setSmallIcon(android.R.drawable.ic_menu_upload)
                    .setOngoing(true);
            mNotifyManager.notify(_id,mBuilder.build());
        }

        private void background(){
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
                entity.addPart("lat", new StringBody(""+ LocationFinder.getLat()));
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
        }

        private void postExecute(){
            Log.e("fos", "Response from server: " + responseString);

            if(responseString.toLowerCase().equals("success")){

                mBuilder.setContentTitle("Upload complete")
                        .setContentText("")
                        .setProgress(0,0,false);
                mBuilder.setOngoing(false);
                mNotifyManager.notify(_id, mBuilder.build());
            }
            else{
                mBuilder.setContentText("Error in uploading, try again")
                        .setContentTitle("Error")
                        .setProgress(0,0,false);
                mBuilder.setOngoing(false);
                mNotifyManager.notify(_id, mBuilder.build());
            }

            // showing the server response in an alert dialog
            int i;
            if((i=Global.uploadingList.indexOf(_id))!=-1){
                Global.uploadingList.remove(i);
            }

            if(Global.uploadingList.isEmpty()){
                stopSelf();
            }
        }

        private void publishProgress(final int progress){
            Log.i("upload", progress + "%");

            Intent intent = new Intent();
            intent.putExtra("progress",progress);
            intent.setAction("com.ankit.example.letsjog.uploadbroadcast");
            sendBroadcast(intent);

            // Notification part
            mBuilder.setProgress(100, progress, false);
            mBuilder.setContentInfo(progress + "% uploaded");
            mBuilder.setContentText("Uploading "+songTitle);
            // Issues the notification
            mNotifyManager.notify(_id, mBuilder.build());
        }
    }

    @Override
    public void onDestroy() {
        mBuilder.setOngoing(false);
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}