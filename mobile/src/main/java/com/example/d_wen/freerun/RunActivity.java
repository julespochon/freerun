package com.example.d_wen.freerun;

import android.Manifest;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.SphericalUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.PriorityQueue;

public class RunActivity extends AppCompatActivity implements LocationListener, OnMapReadyCallback {
    public static final String RECEIVE_HEART_RATE = "RECEIVE_HEART_RATE";
    public static final String HEART_RATE = "HEART_RATE";
    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    public static final String RUN_FOR_GROUP = "RUN_FOR_GROUP";
    private static final Double ONE_KM = 1000.0;

    private HeartRateBroadcastReceiver heartRateBroadcastReceiver;
    private ArrayList<Integer> hrWatchArrayList = new ArrayList<>();
    private ArrayList<Integer> hrBeltArrayList = new ArrayList<>();
    private ArrayList<Integer> hrOnLocList = new ArrayList<>();
    private ArrayList<Integer> hrWatchArrayListSaved = new ArrayList<>();
    private ArrayList<Integer> hrBeltArrayListSaved = new ArrayList<>();
    private ArrayList<Integer> hrOnLocListSaved = new ArrayList<>();

    private final String TAG = this.getClass().getSimpleName();
    private static final int TWO_MINUTES = 1000 * 60 * 2;

    private DatabaseReference recordingRef;
    private DatabaseReference profileRef;
    private String userID;
    private double totScore;
    private GoogleMap mMap;

    private TextView latituteField;
    private TextView longitudeField;
    private TextView speedField;
    private TextView distanceField;
    private LocationManager locationManager;
    private ArrayList<LatLng> latLngsPath = new ArrayList<>();
    private ArrayList<LatLng> latLngsPathSaved = new ArrayList<>();
    private Double totalDistance;
    private Location lastKnownLocation;
    private int totalKM = 0;

    private BluetoothLeService mBluetoothLeService;
    private String mDeviceAddress;
    private String mDeviceName;
    private boolean mConnected = false;

    private boolean ttsEnable = false;
    private String text;
    private EditText et;
    private TextToSpeech tts;

    private boolean RunForGroup;

    private long MillisecondTime, TimeSaved, StartTime, TimeBuff, UpdateTime = 0L;
    private int Seconds, Minutes, MilliSeconds;
    private Handler handler;
    private TextView textViewChrono;

    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                registerHeartRateServices(mBluetoothLeService.getSupportedGattServices());
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                displayData(intent.getIntExtra(BluetoothLeService.EXTRA_DATA, 0));
            }
        }
    };



    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    private void registerHeartRateServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;
        String uuid = null;

        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {

            List<BluetoothGattCharacteristic> gattCharacteristics =
                    gattService.getCharacteristics();

            // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic :
                    gattCharacteristics) {
                uuid = gattCharacteristic.getUuid().toString();

                // Find heart rate measurement (0x2A37)
                if (SampleGattAttributes.lookup(uuid, "unknown")
                        .equals("Heart Rate Measurement")) {
                    mBluetoothLeService.setCharacteristicNotification(
                            gattCharacteristic, true);
                }
            }
        }
    }

    private void displayData(int intExtra) {
        TextView hrBelt = findViewById(R.id.heartRateBeltLive);
        hrBelt.setText(String.valueOf(intExtra));

        hrBeltArrayList.add(intExtra);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_run);

        if (savedInstanceState != null) {
            hrWatchArrayList = hrWatchArrayListSaved;
            hrBeltArrayList = hrBeltArrayListSaved;
            hrOnLocList = hrOnLocListSaved;
            latLngsPath = latLngsPathSaved;
            MillisecondTime = TimeSaved;
        }

        latituteField = (TextView) findViewById(R.id.latitudeLive);
        longitudeField = (TextView) findViewById(R.id.longitudeLive);
        speedField = (TextView) findViewById(R.id.speedLive);
        distanceField = (TextView) findViewById(R.id.distanceLive);

        StartTime = SystemClock.uptimeMillis();
        handler = new Handler();
        handler.postDelayed(runnable, 0);
        textViewChrono = findViewById(R.id.stopWatchLive);

        //BLE
        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                (checkSelfPermission("android" + ""
                        + ".permission.ACCESS_FINE_LOCATION") == PackageManager
                        .PERMISSION_DENIED ||
                        checkSelfPermission("android.permission" +
                                ".ACCESS_COARSE_LOCATION") ==
                                PackageManager.PERMISSION_DENIED ||
                        checkSelfPermission("android" + "" +
                                ".permission.INTERNET") == PackageManager
                                .PERMISSION_DENIED)) {
            requestPermissions(new String[]{"android.permission" +
                    ".ACCESS_FINE_LOCATION", "android"
                    + ".permission.ACCESS_COARSE_LOCATION", "android" +
                    ".permission.INTERNET"}, 0);
        }

        // Location
        // Acquire a reference to the system Location Manager
        locationManager = (LocationManager) this
                .getSystemService(Context.LOCATION_SERVICE);

        if (locationManager != null) {
            try {
                lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                locationManager.requestLocationUpdates(LocationManager
                        .NETWORK_PROVIDER, 400, 1, this);
                locationManager.requestLocationUpdates(LocationManager.
                        GPS_PROVIDER, 400, 1, this);
                Log.d(TAG, "Registered for location updates");
            } catch (Exception e) {
                Log.w(TAG, "Could not request location updates");
            }
        }
        if (lastKnownLocation != null) {
            Log.d(TAG, "Found last location");
            latituteField.setText(String.valueOf(lastKnownLocation.getLatitude()));
            longitudeField.setText(String.valueOf(lastKnownLocation.getLongitude()));
            onLocationChanged(lastKnownLocation);
        } else {
            Log.d(TAG, "Location wasn't found");
            latituteField.setText("-");
            longitudeField.setText("-");
            speedField.setText("-");
            distanceField.setText("-");
        }

        // Google map
        // Obtain the SupportMapFragment and get notified when the map is
        // ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.GoogleMap);
        mapFragment.getMapAsync(this);

        // Firebase
        Intent intentFromPrep = getIntent();
        userID = intentFromPrep.getStringExtra(MyProfileFragment.USER_ID);
        String recID = intentFromPrep.getStringExtra(RunPreparationFragment.RECORDIND_ID);
        RunForGroup = intentFromPrep.getExtras().getBoolean(RUN_FOR_GROUP);

        // Get recording information from Firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference profileGetRef = database.getReference("profiles");
        profileRef = profileGetRef.child(userID).getRef();
        recordingRef = profileGetRef.child(userID).child("recordings").child(recID);

        profileRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                totScore = dataSnapshot.child("total_score").getValue(Double.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        recordingRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String switchWatch = dataSnapshot.child("use_watch").getValue().toString();
                String switchBelt = dataSnapshot.child("use_belt").getValue().toString();
                ttsEnable = (boolean) dataSnapshot.child("use_vocal_coach").getValue();

                TextView exerciseDatetime = findViewById(R.id.exerciseDateTimeLive);
                Long datetime = Long.parseLong(dataSnapshot.child("datetime").getValue().toString());
                SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy" +
                        " hh:mm:ss", Locale.getDefault());
                exerciseDatetime.setText(formatter.format(new Date(datetime)));

                String aimedPace = dataSnapshot.child("aimed_pace").getValue().toString();
                String plannedDistance = dataSnapshot.child("planned_distance").getValue().toString();

                TextView HeartRateWatch = findViewById(R.id.heartRateWatchLive);
                HeartRateWatch.setText(switchWatch);
                TextView HeartRateBelt = findViewById(R.id.heartRateBeltLive);
                HeartRateBelt.setText(switchBelt);

                // TODO: use switchVocalCoach and others
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        tts = new TextToSpeech(RunActivity.this, new TextToSpeech.OnInitListener() {

            @Override
            public void onInit(int status) {
                // TODO Auto-generated method stub
                if (status == TextToSpeech.SUCCESS) {
                    int result = tts.setLanguage(Locale.US);
                    if (result == TextToSpeech.LANG_MISSING_DATA ||
                            result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("error", "This Language is not supported");
                    } else if (ttsEnable) {
                        text = ("Start running now.");
                        ConvertTextToSpeech();
                    }
                } else
                    Log.e("error", "Initilization Failed!");
            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();
        // HR from watch
        heartRateBroadcastReceiver = new HeartRateBroadcastReceiver();

        //BLE
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }

        // Location
        LocalBroadcastManager.getInstance(this).registerReceiver(heartRateBroadcastReceiver,
                new IntentFilter(RECEIVE_HEART_RATE));

        if (locationManager != null) {
            try {
                if (ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.
                        PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.
                        PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                locationManager.requestLocationUpdates(LocationManager
                        .NETWORK_PROVIDER, 400, 1, this);
                locationManager.requestLocationUpdates(LocationManager.
                        GPS_PROVIDER, 400, 1, this);
                Log.d(TAG, "Registered for location updates");
            } catch (Exception e) {
                Log.w(TAG, "Could not request location updates");
            }
        }
        Log.d(TAG, "Activity resumes");
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();

        // BLE
        unregisterReceiver(mGattUpdateReceiver);

        // TTS
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }

        // Watch
        LocalBroadcastManager.getInstance(this).unregisterReceiver(heartRateBroadcastReceiver);

        // Location
        Log.d(TAG, "Activity goes to pause");
        locationManager.removeUpdates(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBluetoothLeService != null) {
            unbindService(mServiceConnection);
            mBluetoothLeService = null;
        }
    }

    public void onClick(View v) {

        ConvertTextToSpeech();

    }

    private void ConvertTextToSpeech() {
        // TODO Auto-generated method stub
        // text = et.getText().toString();
        if (text == null || "".equals(text)) {
            text = "Content not available";
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
        } else
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
    }

    public void stopRecordingRun(View view) {

        Intent intentStopRec = new Intent(RunActivity.this, WearService.class);
        intentStopRec.setAction(WearService.ACTION_SEND.STOPACTIVITY.name());
        intentStopRec.putExtra(WearService.ACTIVITY_TO_STOP, BuildConfig
                .W_runningrecordingactivity);
        startService(intentStopRec);

        // TODO: save meaningful data
        double distance = SphericalUtil.computeLength(latLngsPath);
        recordingRef.child("distance").setValue(distance);
        recordingRef.child("duration").setValue(MillisecondTime);
        double score = (distance * distance/(MillisecondTime/100));
        recordingRef.child("score").setValue(score);
        profileRef.child("total_score").setValue(totScore+score);

        if (!hrWatchArrayList.isEmpty()) {
            recordingRef.child("Heartrate").setValue(hrWatchArrayList);
        }else{
            recordingRef.child("Heartrate").setValue(hrBeltArrayList);
        }
        recordingRef.child("Locations_list").setValue(latLngsPath).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(getApplicationContext(), "Saved GPS data successfully",
                        Toast.LENGTH_SHORT).show();
            }
        });
        recordingRef.child("Heartrate_LOC_list").setValue(hrOnLocList).addOnSuccessListener
                (new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getApplicationContext(), "Saved HR data successfully",
                                Toast.LENGTH_SHORT).show();
                    }
                });
        if (RunForGroup){
            Intent intentGroup = new Intent(this, GroupActivity.class);
            intentGroup.putExtra(GroupActivity.USER_ID, userID);
            intentGroup.putExtra(GroupActivity.SCORE, String.valueOf(score));
            intentGroup.putExtra(GroupActivity.SCORE_KM, String.valueOf(score/distance));
            startActivity(intentGroup);
        }else {
            finish();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "New location was found");

        if (isBetterLocation(location, lastKnownLocation)) {
            // record location
            Log.d(TAG, "Location was changed");
            double lat = location.getLatitude();
            double lng = location.getLongitude();
            latituteField.setText(String.valueOf(lat));
            longitudeField.setText(String.valueOf(lng));
            lastKnownLocation = location;

            // record distance and HR at that location
            latLngsPath.add(new LatLng(lat, lng));
            totalDistance = SphericalUtil.computeLength(latLngsPath);
            distanceField.setText(String.valueOf(Math.round(totalDistance / 10.0)/100.0) + " km");

//            if (ttsEnable) {
//                if (Math.floor(totalDistance/ONE_KM) > totalKM){
//                    newKMreached(totalDistance);
//                }
//            }

            if (!hrWatchArrayList.isEmpty()){
                hrOnLocList.add(hrWatchArrayList.get(hrWatchArrayList.size()-1)); // Priority on Watch
            }else if (!hrBeltArrayList.isEmpty()){
                hrOnLocList.add(hrBeltArrayList.get(hrBeltArrayList.size()-1));
            }

            // display speed
            if(location.hasSpeed()){
                speedField.setText(String.valueOf(location.getSpeed()) + " m/s");
            }else {
                speedField.setText("-");
            }

            //Update GoogleMaps location
            LatLng currentLocation = new LatLng(lat, lng);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation,15));
            mMap.addMarker(new MarkerOptions().position(currentLocation).title("Current Location"));

        }
    }

    private void newKMreached(Double totalDistance) {
        totalKM = (int) Math.round(totalDistance/ONE_KM);
        text = "You reached another kilometer";
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d(TAG, "Status was changed");
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d(TAG, "Provider was enabled");
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d(TAG, "Provider was disabled");
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (lastKnownLocation != null) {
            // Add a marker in the last known location and move the camera
            LatLng currentLocation = new LatLng
                    (lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
            Log.d(TAG, "Current location: " + currentLocation);

            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
            mMap.addMarker(new MarkerOptions().position(currentLocation).title("Current Location"));
        }
    }


    private class HeartRateBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            // Show HR in a TextView
            int heartRateWatch = intent.getIntExtra(HEART_RATE, -1);
            TextView hrTextView = findViewById(R.id.heartRateWatchLive);
            hrTextView.setText(String.valueOf(heartRateWatch));

            // TODO: Plot HR data

            // Add HR value to HR ArrayList
            hrWatchArrayList.add(heartRateWatch);
        }
    }

    /** Determines whether one Location reading is better than the current Location fix
     * @param newLocation  The new Location that you want to evaluate
     * @param currentBestLocation  The current Location fix, to which you want to compare the new one
     */
    protected boolean isBetterLocation(Location newLocation, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = newLocation.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (newLocation.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(newLocation.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }

    /** Checks whether two providers are the same */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
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

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        hrWatchArrayListSaved = hrWatchArrayList;
        hrBeltArrayListSaved = hrBeltArrayList;
        hrOnLocListSaved = hrOnLocList;

        latLngsPathSaved = latLngsPath;

        TimeSaved = MillisecondTime;
    }
}

