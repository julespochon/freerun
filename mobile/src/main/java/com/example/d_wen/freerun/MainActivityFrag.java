package com.example.d_wen.freerun;

import android.net.Uri;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivityFrag extends AppCompatActivity implements MyProfileFragment.
        OnFragmentInteractionListener, RunPreparationFragment.OnFragmentInteractionListener{
    private SectionsStatePagerAdapter mSectionStatePagerAdapter;
    private MyProfileFragment myProfileFragment;
    private RunPreparationFragment runPreparationFragment;

    // TODO: faire une app cool

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_frag);

        mSectionStatePagerAdapter = new SectionsStatePagerAdapter(getSupportFragmentManager());

        myProfileFragment = new MyProfileFragment();
        runPreparationFragment = new RunPreparationFragment();

        ViewPager mViewPager = findViewById(R.id.mainViewPager);
        setUpViewPager(mViewPager);

        // Set NewRecordingFragment as default tab once started the activity
        mViewPager.setCurrentItem(mSectionStatePagerAdapter
                .getPositionByTitle("New Recording"));
    }

    private void setUpViewPager(ViewPager mViewPager) {
        mSectionStatePagerAdapter.addFragment(runPreparationFragment, "RUN");
        mSectionStatePagerAdapter.addFragment(myProfileFragment, "PROFILE");
        mViewPager.setAdapter(mSectionStatePagerAdapter);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
