package com.example.d_wen.freerun;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity implements MyProfileFragment.
        OnFragmentInteractionListener, LeaderboardFragment.OnFragmentInteractionListener,
        RunPreparationFragment.OnFragmentInteractionListener,
        HistoryFragment.OnFragmentInteractionListener{

    private ActionBar toolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar=getSupportActionBar();

        toolbar.setTitle("Run");
        loadFragment(new RunPreparationFragment());

        BottomNavigationView navigation = findViewById(R.id.pageTabStrip);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            =new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Fragment fragment;
            switch (item.getItemId()) {
                case R.id.navigation_run:
                    toolbar.setTitle("Run");
                    fragment=new RunPreparationFragment();
                    loadFragment(fragment);
                    return true;
                case R.id.navigation_profile:
                    toolbar.setTitle("Profile");
                    fragment=new MyProfileFragment();
                    loadFragment(fragment);
                    return true;
                case R.id.navigation_history:
                    toolbar.setTitle("History");
                    fragment=new HistoryFragment();
                    loadFragment(fragment);
                    return true;
                case R.id.navigation_leaderboard:
                    toolbar.setTitle("Leaderboard");
                    fragment=new LeaderboardFragment();
                    loadFragment(fragment);
                    return true;
            }

            return false;
        }
    };

    private void loadFragment(Fragment fragment) {
        // load fragment
        FragmentTransaction transaction=getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }


    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
