package com.example.ankit.letsjog;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.widget.Toast;

/**
 * Created by ankit on 1/19/15.
 */

public class MainActivity extends ActionBarActivity implements NavigationDrawerCallbacks {

    private Toolbar mToolbar;
    private NavigationDrawerFragment mNavigationDrawerFragment;
    SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        mNavigationDrawerFragment = (NavigationDrawerFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_drawer);
        mNavigationDrawerFragment.setup(R.id.fragment_drawer,
                (DrawerLayout) findViewById(R.id.drawer), mToolbar);

        sp= getSharedPreferences(Global.PREFERENCES_FILE,MODE_PRIVATE);

        // if internet is not available prompt for internet
        if(!isNetworkAvailable()){
            buildAlertMessageNoInternet();
        }

        // for first run
        if(!sp.contains("letsjog")){
            sp.edit().putBoolean("letsjog",true).commit();
        }


        // Start the LocationService
        this.startService(new Intent(this, LocationService.class));


        Intent i = getIntent();

        String frag = i.getStringExtra("fragment");

        if(frag!=null) {
            if (frag.equals("playlists")) {
                showPlaylistsFragment();
            } else if (frag.equals("songs")) {
                showSongsListFragment();
            }
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        Toast.makeText(this, "Menu item selected -> " + position, Toast.LENGTH_SHORT).show();

        switch (position){
            case 0 :
                showMainFragment();
                break;
            case 1 :
                showSongsListFragment();
                break;
            case 2 :
                showPlaylistsFragment();
                break;
            default:
                if(Global.uploadingList.isEmpty())
                    finish();
                else
                    moveTaskToBack(true);
                break;

        }
    }

    void showMainFragment(){
        // Create fragment and give it an argument specifying the article it should show
        MainFragment newFragment = new MainFragment();

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        // Set title
        try {
            getSupportActionBar().setTitle("Lets Jog");
        }
        catch (Exception e){
            Log.i("fos","Activity not yet started");
        }
        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack so the user can navigate back
        transaction.replace(R.id.container, newFragment);
        transaction.addToBackStack(null);

        // Commit the transaction
        transaction.commit();
    }

    void showSongsListFragment(){
        // Create fragment and give it an argument specifying the article it should show
        SongsListFragment newFragment = new SongsListFragment();

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        // Set title
        getSupportActionBar().setTitle("Songs on your device");

        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack so the user can navigate back
        transaction.replace(R.id.container, newFragment);
        transaction.addToBackStack(null);
        // Commit the transaction
        transaction.commit();
    }
    void showPlaylistsFragment(){
        // Create fragment and give it an argument specifying the article it should show
        PlaylistsFragment newFragment = new PlaylistsFragment();

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        // Set title
        getSupportActionBar().setTitle("Playlists created here");

        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack so the user can navigate back
        transaction.replace(R.id.container, newFragment);
        transaction.addToBackStack(null);
        // Commit the transaction
        transaction.commit();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private  void buildAlertMessageNoInternet() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("An active internet connection is required to use this app, enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        startActivity(new Intent(Settings.ACTION_NETWORK_OPERATOR_SETTINGS));

                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        android.os.Process.killProcess(android.os.Process.myPid());
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void onBackPressed() {
        if (mNavigationDrawerFragment.isDrawerOpen())
            mNavigationDrawerFragment.closeDrawer();
        else
            mNavigationDrawerFragment.openDrawer();
//            super.onBackPressed();
    }
}
