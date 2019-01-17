package com.example.d_wen.freerun;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.LocalBroadcastManager;
import android.support.wearable.activity.WearableActivity;
import android.widget.TextView;

public class RunningRecordingActivity extends WearableActivity implements
        SensorEventListener{

    public static final String STOP_ACTIVITY = "STOP_ACTIVITY";
    private TextView mTextView;
    private ConstraintLayout mLayout;

    private long MillisecondTime, TimeSaved, StartTime, TimeBuff, UpdateTime = 0L;
    private int Seconds, Minutes, MilliSeconds;
    private Handler handler;
    private TextView textViewChrono;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_running_record);

        mTextView = (TextView) findViewById(R.id.text);

        StartTime = SystemClock.uptimeMillis();
        handler = new Handler();
        handler.postDelayed(runnable, 0);
        textViewChrono = findViewById(R.id.timeLive);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                checkSelfPermission("android" + ""
                        + ".permission.BODY_SENSORS") == PackageManager.PERMISSION_DENIED) {
            requestPermissions(new String[]{"android.permission" + ".BODY_SENSORS"}, 0);
        }

        final SensorManager sensorManager = (SensorManager) getSystemService
                (MainActivity.SENSOR_SERVICE);
        Sensor heartRate_sensor = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE); // also possible: step counter!
        sensorManager.registerListener(this, heartRate_sensor,
                SensorManager.SENSOR_DELAY_UI);

        LocalBroadcastManager.getInstance(this).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                sensorManager.unregisterListener(RunningRecordingActivity.this);
                finish();
            }
        }, new IntentFilter(STOP_ACTIVITY));

        mLayout = findViewById(R.id.containerRecording);
        // Enables Always-on
        setAmbientEnabled();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        TextView textViewHR = findViewById(R.id.hearRateLive);
        int heartRate = (int) event.values[0];
        if (textViewHR != null)
            textViewHR.setText(String.valueOf(event.values[0]));

        Intent intent = new Intent(RunningRecordingActivity.this, WearService.class);
        intent.setAction(WearService.ACTION_SEND.HEART_RATE.name());
        intent.putExtra(WearService.HEART_RATE, heartRate);
        startService(intent);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public Runnable runnable = new Runnable() {

        public void run() {

            MillisecondTime = SystemClock.uptimeMillis() - StartTime;

            UpdateTime = TimeBuff + MillisecondTime;

            Seconds = (int) (UpdateTime / 1000);

            Minutes = Seconds / 60;

            Seconds = Seconds % 60;

            MilliSeconds = (int) (UpdateTime/100 % 10);


            textViewChrono.setText("" + Minutes + ":"
                    + String.format("%02d", Seconds) + ":"
                    + String.format("%d", MilliSeconds));

            handler.postDelayed(this, 0);
        }

    };
}
