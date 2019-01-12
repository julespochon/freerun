package com.example.d_wen.freerun;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class RunActivity extends AppCompatActivity {
    public static final String RECEIVE_HEART_RATE = "RECEIVE_HEART_RATE";
    public static final String HEART_RATE = "HEART_RATE";

    private HeartRateBroadcastReceiver heartRateBroadcastReceiver;
    private ArrayList<Integer> HRDataArrayList = new ArrayList<>();

    private DatabaseReference recordingRef;

    String text;
    EditText et;
    TextToSpeech tts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_run);

        Intent intentFromPrep = getIntent();
        String userID = intentFromPrep.getStringExtra(MyProfileFragment.USER_ID);
        String recID = intentFromPrep.getStringExtra(RunPreparationFragment.RECORDIND_ID);

        // Get recording information from Firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference profileGetRef = database.getReference("profiles");
        recordingRef = profileGetRef.child(userID).child("recordings").child(recID);

        recordingRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String switchWatch = dataSnapshot.child("use_watch").getValue().toString();
                String switchBelt = dataSnapshot.child("use_belt").getValue().toString();
                String switchVocalCoach = dataSnapshot.child("use_vocal_coach").getValue().toString();

                TextView exerciseDatetime = findViewById(R.id.exerciseDateTimeLive);
                Long datetime = Long.parseLong(dataSnapshot.child("datetime").getValue().toString());
                SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy" +
                        " hh:mm:ss", Locale.getDefault());
                exerciseDatetime.setText(formatter.format(new Date(datetime)));

                String aimedPace = dataSnapshot.child("aimed_pace").getValue().toString();
                String plannedDistance = dataSnapshot.child("planned_distance").getValue().toString();

                TextView HeartRateWatch = findViewById(R.id.heartRateLive);
                HeartRateWatch.setText(switchWatch);
                // TODO: use switchBelt and others
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        et=(EditText)findViewById(R.id.editText1);
        tts=new TextToSpeech(RunActivity.this, new TextToSpeech.OnInitListener() {

            @Override
            public void onInit(int status) {
                // TODO Auto-generated method stub
                if(status == TextToSpeech.SUCCESS){
                    int result=tts.setLanguage(Locale.US);
                    if(result==TextToSpeech.LANG_MISSING_DATA ||
                            result==TextToSpeech.LANG_NOT_SUPPORTED){
                        Log.e("error", "This Language is not supported");
                    }
                    else{
                        ConvertTextToSpeech();
                    }
                }
                else
                    Log.e("error", "Initilization Failed!");
            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();
        heartRateBroadcastReceiver = new HeartRateBroadcastReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(heartRateBroadcastReceiver,
                new IntentFilter(RECEIVE_HEART_RATE));
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        LocalBroadcastManager.getInstance(this).unregisterReceiver(heartRateBroadcastReceiver);
    }

    public void onClick(View v) {

        ConvertTextToSpeech();

    }

    private void ConvertTextToSpeech() {
        // TODO Auto-generated method stub
        text = et.getText().toString();
        if(text==null||"".equals(text))
        {
            text = "Content not available";
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
        }else
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
    }

    public void stopRecordingOnWear(View view) {

        Intent intentStopRec = new Intent(RunActivity.this, WearService.class);
        intentStopRec.setAction(WearService.ACTION_SEND.STOPACTIVITY.name());
        intentStopRec.putExtra(WearService.ACTIVITY_TO_STOP, BuildConfig
                .W_runningrecordingactivity);
        startService(intentStopRec);

        recordingRef.child("HR_watch").setValue(HRDataArrayList).addOnSuccessListener
                (new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(getApplicationContext(), "Saved HR data successfully",
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private class HeartRateBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            // Show HR in a TextView
            int heartRateWatch = intent.getIntExtra(HEART_RATE, -1);
            TextView hrTextView = findViewById(R.id.heartRateLive);
            hrTextView.setText(String.valueOf(heartRateWatch));

            // TODO: Plot HR data

            // Add HR value to HR ArrayList
            HRDataArrayList.add(heartRateWatch);
        }
    }
}


