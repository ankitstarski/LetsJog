package com.example.ankit.letsjog;


import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
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
 * Created by ankit on 1/19/15.
 */
public class PlaylistsFragment extends Fragment implements ListView.OnItemClickListener {

    private ArrayAdapter<String> adapter;
    private ListView listView;
    private ProgressBar pb;
    public static String[] playlists;
    public static String[] _ids;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =inflater.inflate(R.layout.fragment_playlists, container, false);

        listView = (ListView)view.findViewById(R.id.playlists);
        pb = (ProgressBar) view.findViewById(R.id.playlistLoading);
        listView.setOnItemClickListener(this);
        new PlaylistFetcher(true).execute();

        return  view;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        Intent i = new Intent(getActivity(),PlaylistSongView.class);
        i.putExtra("_id",_ids[position]);
        startActivity(i);
    }


    public class PlaylistFetcher extends AsyncTask<Void, Void, HttpResponse>{

        boolean location ;

        public PlaylistFetcher(boolean location){
            this.location = location;
        }

        @Override
        protected HttpResponse doInBackground(Void... params) {
            // Create a new HttpClient and Post Header
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(Global.PLAYLIST_URL);

            try {
                if(location) {
                    // Add your data
                    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
                    nameValuePairs.add(new BasicNameValuePair("lat", "" + LocationFinder.getLat()));
                    nameValuePairs.add(new BasicNameValuePair("lon", "" + LocationFinder.getLon()));
                    httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                }

                // Execute HTTP Post Request
                HttpResponse response = httpclient.execute(httppost);
                return response;

            } catch (ClientProtocolException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getActivity(), "Error occurred", Toast.LENGTH_LONG).show();
                    }
                });
            } catch (IOException e) {
                Log.i("fos", "Error: Your Internet might be down, please check your connection");
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(getActivity(), "Error: Your Internet might be down, please check your connection",
                                Toast.LENGTH_LONG).show();
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
                playlists = new String[len];
                _ids = new String[len];
                Log.i("fos",len+"" );
                for (int i=0;i<len;i++){
                    playlists[i]= in.nextLine();
                    _ids[i] = in.nextLine();
                }

            } catch (IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getActivity(), "Error occurred", Toast.LENGTH_LONG).show();
                        getFragmentManager().popBackStack();
                    }
                });
                return;
            }catch (NumberFormatException e){
                Toast.makeText(getActivity(),"Server might be having congestion, try again in sometime",
                        Toast.LENGTH_LONG).show();
            }

            if(len == 0){
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getActivity(),"We're sorry no playlists are found nearby you! " +
                                "Try creating a new playlist for this Location",Toast.LENGTH_LONG).show();
                        getFragmentManager().popBackStack();
                    }
                });
            }

            try {
                adapter = new ArrayAdapter<String>(getActivity(), R.layout.list_item_simple, playlists);
                listView.setAdapter(adapter);
                pb.setVisibility(View.GONE);
            }
            catch (Exception e){
                Log.i("fos","Activity modified in the background");
            }
        }
    }


}
