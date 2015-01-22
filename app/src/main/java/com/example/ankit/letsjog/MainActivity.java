package com.example.ankit.letsjog;

import android.content.Intent;
import android.os.Bundle;
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

        // Start the LocationService
        startService(new Intent(this,LocationService.class));


        Intent i = getIntent();

//        if(i.getStringExtra("fragment").equals("playlists")){
//            showPlaylistsFragment();
//        }
//        else if(i.getStringExtra("fragment").equals("songs")){
//            showSongsListFragment();
//        }

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
                finish();
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
            Log.i("fos","Activity not ye started");
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


    @Override
    public void onBackPressed() {
        if (mNavigationDrawerFragment.isDrawerOpen())
            mNavigationDrawerFragment.closeDrawer();
        else
            mNavigationDrawerFragment.openDrawer();
//            super.onBackPressed();
    }
}
