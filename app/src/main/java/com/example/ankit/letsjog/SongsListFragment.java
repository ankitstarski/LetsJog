package com.example.ankit.letsjog;

import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.io.File;

/**
 * Created by ankit on 1/19/15.
 */
public class SongsListFragment extends Fragment {


    private ListView listview ;
    private Cursor cursor;

    private final int CONTEXT_ACTION_PLAY = 0;
    private final int CONTEXT_ACTION_UPLOAD = 1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View fragmentView = inflater.inflate(R.layout.fragment_songs_list, container, false);
        listview = (ListView) fragmentView.findViewById(R.id.songs_list);

        //Some audio may be explicitly marked as not being music
        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";
        String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";

        String[] projection = {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.DATA,
        };

        // Cursor for SongList
        cursor =  getActivity().getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                null,
                sortOrder
        );

        final SongsCursorAdapter adapter = new SongsCursorAdapter(getActivity(),cursor);
        listview.setAdapter(adapter);
        listview.setOnItemClickListener(adapter);
        registerForContextMenu(listview);

        return fragmentView;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        if (v.getId() == R.id.songs_list) {
            ListView lv = (ListView) v;
            AdapterView.AdapterContextMenuInfo acmi = (AdapterView.AdapterContextMenuInfo) menuInfo;
            int pos = acmi.position;

            String track = cursor.getString(cursor
                    .getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));

            cursor.moveToPosition(pos);
            menu.setHeaderTitle(track);
            menu.add(0,CONTEXT_ACTION_PLAY,0,"Play");
            menu.add(0,CONTEXT_ACTION_UPLOAD,1,"Share with other Joggers");
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info;

        Log.i("fos", "" + item.getItemId());
        switch (item.getItemId()) {
            case CONTEXT_ACTION_PLAY:
                info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                playSongAtPos(info.position);
                return true;
            case CONTEXT_ACTION_UPLOAD:
                info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                uploadSongAtPos(info.position);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }


    public void uploadSongAtPos(int pos){
        Intent intent = new Intent(getActivity(),UploadActivity.class);

        cursor.moveToPosition(pos);
        int rowId = cursor.getInt(cursor.getColumnIndexOrThrow("_id"));
        String songUri = cursor.getString(cursor
                .getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
        String artist = cursor.getString(cursor
                .getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
        String album = cursor.getString(cursor
                .getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));
        String track = cursor.getString(cursor
                .getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
        Long albumId = cursor.getLong(cursor
                .getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID));


        Uri sArtworkUri=  Uri
                .parse("content://media/external/audio/albumart");
        final Uri albumArtUri = ContentUris.withAppendedId(sArtworkUri, albumId);


        // Data to pass to the upload activity
        intent.putExtra("title",track);
        intent.putExtra("uri",songUri);
        intent.putExtra("coverUri",albumArtUri.toString());
        intent.putExtra("_id",rowId);

        //intent.putExtra("lat",);
        startActivity(intent);


    }

    void playSongAtPos(int pos){

        Intent intent = new Intent();

        cursor.moveToPosition(pos);
        int rowId = cursor.getInt(cursor.getColumnIndexOrThrow("_id"));
        String songUri = cursor.getString(cursor
                .getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
        String artist = cursor.getString(cursor
                .getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
        String album = cursor.getString(cursor
                .getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));
        String track = cursor.getString(cursor
                .getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
        Long albumId = cursor.getLong(cursor
                .getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID));

        intent.setAction(android.content.Intent.ACTION_VIEW);
        File file = new File(songUri);
        intent.setDataAndType(Uri.fromFile(file), "audio/*");
        startActivity(intent);
    }

}
