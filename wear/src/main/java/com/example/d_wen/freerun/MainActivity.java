package com.example.d_wen.freerun;

import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.wearable.activity.WearableActivity;
import android.widget.TextView;

public class MainActivity extends WearableActivity {

    private final String TAG = this.getClass().getSimpleName();

    public static final String ACTION_RECEIVE_PROFILE_INFO = "RECEIVE_PROFILE_INFO";
    public static final String PROFILE_IMAGE = "PROFILE_IMAGE";
    public static final String PROFILE_USERNAME = "PROFILE_USERNAME";

    private TextView mTextView;
    private ConstraintLayout mLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextView=(TextView) findViewById(R.id.wearTextView);
        mTextView.setText("Welcome!");

        mLayout = findViewById(R.id.container);

        // Enables Always-on
        setAmbientEnabled();
    }

    @Override
    public void onEnterAmbient(Bundle ambientDetails) {
        super.onEnterAmbient(ambientDetails);
        updateDisplay();
    }

    @Override
    public void onExitAmbient() {
        super.onExitAmbient();
        updateDisplay();
    }

    private void updateDisplay() {
        if (isAmbient()) {
            mLayout.setBackgroundColor(getResources().getColor(
                    android.R.color.black, getTheme()));
        } else {
            mLayout.setBackgroundColor(getResources().getColor(
                    android.R.color.black, getTheme()));
        }
    }
}
