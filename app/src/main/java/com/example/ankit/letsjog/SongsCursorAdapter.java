package com.example.ankit.letsjog;


import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created by ankit on 1/20/15.
 */

public class SongsCursorAdapter extends CursorAdapter implements AdapterView.OnItemClickListener,
        AdapterView.OnLongClickListener {

    private LruCache<Integer, Bitmap> mMemoryCache;
    private final Context context;
    private final Cursor cursor;
    AsyncTask<ViewHolder, Void, Void> asyncTask;

    public SongsCursorAdapter(Context context, Cursor cursor) {
        super(context,cursor);
        this.context = context;
        this.cursor = cursor;
        // Get max available VM memory, exceeding this amount will throw an
        // OutOfMemory exception. Stored in kilobytes as LruCache takes an
        // int in its constructor.
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

        // Use 1/8th of the available memory for this memory cache.
        final int cacheSize = maxMemory / 8;

        mMemoryCache = new LruCache<Integer, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(Integer key, Bitmap bitmap) {
                // The cache size will be measured in kilobytes rather than
                // number of items.
                return (bitmap.getRowBytes() * bitmap.getHeight())/ 1024;
            }
        };
    }

    public void addBitmapToMemoryCache(Integer key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            mMemoryCache.put(key, bitmap);
        }
    }

    public Bitmap getBitmapFromMemCache(Integer key) {
        return mMemoryCache.get(key);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.songs_list_row, parent, false);
        bindView(view, context, cursor);
        return view;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void bindView(View rowView, final Context context, Cursor cursor) {
        // Assign views
        TextView songTitle = (TextView) rowView.findViewById(R.id.firstLine);
        TextView songEtc = (TextView) rowView.findViewById(R.id.secondLine);
        TextView id = (TextView) rowView.findViewById(R.id.id);
        ImageView imageCover = (ImageView) rowView.findViewById(R.id.icon);
        final int position = cursor.getPosition();
        ViewHolder viewHolder =new ViewHolder(songTitle,songEtc,imageCover,position);


        // Fetch values from cursor
        String artist = cursor.getString(cursor
                .getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
        String album = cursor.getString(cursor
                .getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));
        String track = cursor.getString(cursor
                .getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
        Long albumId = cursor.getLong(cursor
                .getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID));
        int rowId = cursor.getInt(cursor.getColumnIndexOrThrow("_id"));



        // Set Views
        songTitle.setText(track);
        songEtc.setText(artist+"-"+album);
        id.setText(rowId+"");


        Uri sArtworkUri=  Uri
                .parse("content://media/external/audio/albumart");
        final Uri albumArtUri = ContentUris.withAppendedId(sArtworkUri, albumId);




        // Using an AsyncTask to load the slow images in a background thread
        asyncTask =  new AsyncTask<ViewHolder, Void, Void>() {
            private ViewHolder v;

            @Override
            protected Void doInBackground(ViewHolder... params) {
                v = params[0];

               // Log.i("fos", this.getStatus().toString());

                Bitmap bitmap = null;
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(
                            context.getContentResolver(), albumArtUri);
                    bitmap = Bitmap.createScaledBitmap(bitmap, 96, 96, true);

                } catch (FileNotFoundException exception) {
                    exception.printStackTrace();
                    bitmap = BitmapFactory.decodeResource(context.getResources(),
                            R.drawable.ic_coverart);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                addBitmapToMemoryCache(position,bitmap);
                //Log.i("fos", position+"");

                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                super.onPostExecute(result);
                if(v.position==position)
                    v.icon.setImageBitmap(getBitmapFromMemCache(position));
            }
        };

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, viewHolder);
        }
        else {
            asyncTask.execute(viewHolder);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterViewCompat, View view, int pos, long id) {
        cursor.moveToPosition(pos);
        int rowId = cursor.getInt(cursor.getColumnIndexOrThrow("_id"));
        String songUri = cursor.getString(cursor
                .getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));

        Log.i("fos", songUri);
        Intent intent = new Intent();
        intent.setAction(android.content.Intent.ACTION_VIEW);
        File file = new File(songUri);
        intent.setDataAndType(Uri.fromFile(file), "audio/*");

        context.startActivity(intent);
    }

    @Override
    public boolean onLongClick(View v) {
        return false;
    }

    static class ViewHolder {
        TextView title;
        TextView album;
        ImageView icon;
        int position;

        ViewHolder(TextView title, TextView album, ImageView icon, int position) {
            this.title = title;
            this.album = album;
            this.icon = icon;
            this.position =position;
        }
    }
}
