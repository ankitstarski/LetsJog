package com.example.ankit.letsjog;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
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
public class MainFragment extends Fragment implements ImageButton.OnClickListener {
    EditText userInput;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =inflater.inflate(R.layout.fragment_main, container, false);

        ImageButton button = (ImageButton) view.findViewById(R.id.create_playlist);

        button.setOnClickListener(this);


        return  view;
    }

    @Override
    public void onClick(View v) {
        // get prompts.xml view
        LayoutInflater li = LayoutInflater.from(getActivity());
        View promptsView = li.inflate(R.layout.create_playlist_layout, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                getActivity());

        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);

        userInput  = (EditText) promptsView
                .findViewById(R.id.newPlaylist);

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                // get user input and set it to result
                                // edit text

                                new PlaylistCreator().execute();

                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                dialog.cancel();
                            }
                        }
                );

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    public class PlaylistCreator extends AsyncTask<Void,Void,HttpResponse> {
        ProgressDialog pd;
        @Override
        protected void onPreExecute() {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    pd = new ProgressDialog(getActivity());
                    pd.setMessage("Creating playlist...");
                    pd.show();
                }
            });

        }

        @Override
        protected HttpResponse doInBackground(Void... params) {
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(Global.PLAYLIST_CREATE_URL);

            try {
                // Add your data
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(3);
                nameValuePairs.add(new BasicNameValuePair("name", ""+userInput.getText()));
                nameValuePairs.add(new BasicNameValuePair("lat", "" + LocationFinder.getLat()));
                nameValuePairs.add(new BasicNameValuePair("lon", "" + LocationFinder.getLon()));
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));


                // Execute HTTP Post Request
                HttpResponse response = httpclient.execute(httppost);
                return response;

            } catch (ClientProtocolException e) {getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getActivity(), "Error occurred",
                            Toast.LENGTH_LONG).show();
                    pd.dismiss();
                }
            });
            } catch (IOException e) {

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getActivity(), "Error: Your Internet might be down, " +
                                        "please check your connection",
                                Toast.LENGTH_LONG).show();
                        pd.dismiss();
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

            pd.dismiss();

            String message = "" ;
            Scanner in;

            try {
                in = new Scanner(response.getEntity().getContent());
                message = in.nextLine();
                Log.i("fos", message + "");

                if(message.equals("success")){
                    Toast.makeText(getActivity(),"Playlist successfully created",
                            Toast.LENGTH_SHORT).show();
                }
                else{
                    throw new IOException();
                }

            } catch (IOException e) {
                Toast.makeText(getActivity(),"Error occurred",Toast.LENGTH_LONG).show();
                return;
            }

        }
    }

}
