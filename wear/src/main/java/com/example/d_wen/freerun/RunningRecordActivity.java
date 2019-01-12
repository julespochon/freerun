package com.example.d_wen.freerun;

import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.widget.TextView;

public class RunningRecordActivity extends WearableActivity implements
        SensorEventListener{

    private TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_running_record);

        mTextView = (TextView) findViewById(R.id.text);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                checkSelfPermission("android" + ""
                        + ".permission.BODY_SENSORS") == PackageManager
                        .PERMISSION_DENIED) {
            requestPermissions(new String[]{"android.permission" +
                    ".BODY_SENSORS"}, 0);
        }

        final SensorManager sensorManager = (SensorManager) getSystemService
                (MainActivity.SENSOR_SERVICE);
        Sensor heartRate_sensor = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE); // also possible: step counter!
        sensorManager.registerListener(this, heartRate_sensor, SensorManager.SENSOR_DELAY_UI);


        // Enables Always-on
        setAmbientEnabled();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
