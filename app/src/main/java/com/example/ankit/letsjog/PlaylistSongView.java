package com.example.ankit.letsjog;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Created by ankit on 1/22/15.
 */
public class PlaylistSongView extends ActionBarActivity {

    ListView listView;
    ProgressBar pb;
    Intent intent;

    String[] songs,_ids;
    ArrayAdapter<String> adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.songs_list_activity);
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Uploaded Songs");


        intent = getIntent();

        listView = (ListView)findViewById(R.id.playlists);
        pb = (ProgressBar)findViewById(R.id.playlistLoading);

        new PlaylistSongFetcher(false).execute();

    }



    public class PlaylistSongFetcher extends AsyncTask<Void, Void, HttpResponse> {

        boolean location ;

        public PlaylistSongFetcher(boolean location){
            this.location = location;
        }

        @Override
        protected HttpResponse doInBackground(Void... params) {
            // Create a new HttpClient and Post Header
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(Global.PLAYLIST_SONGS_URL);

            try {
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                if(location) {
                    // Add your lat lon
                    nameValuePairs.add(new BasicNameValuePair("lat", "" + LocationFinder.getLat()));
                    nameValuePairs.add(new BasicNameValuePair("lon", "" + LocationFinder.getLon()));
                }

                //Log.i("fos","_id="+intent.getStringExtra("_id"));

                nameValuePairs.add(new BasicNameValuePair("_id",intent.getStringExtra("_id")+""));


                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                // Execute HTTP Post Request
                HttpResponse response = httpclient.execute(httppost);
                return response;

            } catch (ClientProtocolException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getBaseContext() , "Error occurred",
                                Toast.LENGTH_LONG).show();
                        finish();
                    }
                });
            } catch (IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getBaseContext(), "Error: Your Internet might be down, please check your connection",
                                Toast.LENGTH_LONG).show();
                        finish();
                    }
                });
            }
            return null;
        }

        @Override
        protected void onPostExecute(HttpResponse response) {
            if(response == null){
                return;
            }

            int len =0 ;
            Scanner in;

            try {
                in = new Scanner(response.getEntity().getContent());
                len = Integer.parseInt(in.nextLine());
                songs = new String[len];
                _ids = new String[len];
                Log.i("fos", len + "");
                for (int i=0;i<len;i++){
                    songs[i]= in.nextLine();
                    _ids[i] = in.nextLine();
                }

            } catch (IOException e) {
                Toast.makeText(getBaseContext(),"Error occurred",Toast.LENGTH_LONG).show();
                return;
            } catch (NumberFormatException e){
                Toast.makeText(getBaseContext(),"Server might be having congestion, try again in sometime",
                        Toast.LENGTH_LONG).show();
            }

            if(len == 0){
                Toast.makeText(getBaseContext(),"We're sorry no Songs in this Playlist! " +
                        "Try uploading a new song to this playlist",Toast.LENGTH_LONG).show();
                finish();
            }

            try {
                adapter = new ArrayAdapter<String>(getBaseContext(), R.layout.list_item_simple, songs);
                listView.setAdapter(adapter);
                pb.setVisibility(View.GONE);
            }
            catch (Exception e){
                Log.i("fos","Activity modified in the background");
            }
        }
    }

}
