package com.example.d_wen.freerun;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class GroupActivity extends AppCompatActivity {

    final FirebaseDatabase database=FirebaseDatabase.getInstance();
    final DatabaseReference profileGetRef=database.getReference( "profiles" );
    final DatabaseReference groupGetRef=database.getReference( "profiles" );

    public static final String USER_ID = "USER_ID";
    public static final String SCORE = "SCORE";
    public static final String SCORE_KM = "SCORE_KM";


    private String userID;
    private String score;
    private String scoreKM;
    private String groupID;
    private String groupName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_group );

        Intent intent = getIntent();
        userID = intent.getExtras().getString(USER_ID);
        score = intent.getExtras().getString(SCORE);
        scoreKM = intent.getExtras().getString(SCORE_KM);

        getGroupInfo();
        getRunInformations();
    }

    private void getGroupInfo() {

        profileGetRef.child(userID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                groupID = dataSnapshot.child("groupID").getValue(String.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        groupGetRef.child(groupID).addValueEventListener( new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                groupName = dataSnapshot.child("groupName").getValue(String.class);

                TextView groupTitle = findViewById(R.id.groupTitleText);
                groupTitle.setText(groupName);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        groupGetRef.child(groupID).child("groupName").child("participants")
                .addValueEventListener( new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                groupName = dataSnapshot.child("groupName").getValue(String.class);

                TextView groupTitle = findViewById(R.id.groupTitleText);
                groupTitle.setText(groupName);

                int[] groupMembers = {R.id.member1NameText, R.id.member2NameText,};
                int[] memberScore = {R.id.member1ScoreText, R.id.member1ScoreText};
                int[] memberScoreKM = {R.id.member1ScoreKMText, R.id.member2ScoreKMText};

                int i = 0;
                for (final DataSnapshot participants : dataSnapshot.getChildren()) {
                    i = i+1;
                    String groupMemberDatabase = participants.getValue(String.class);
                    String memberScoreDatabase = participants.child("score").getValue(String.class);
                    String memberDistanceDatabase = participants.child("score_km").
                            getValue(String.class);

                    TextView gm = findViewById(groupMembers[i]);
                    TextView ms = findViewById(memberScore[i]);
                    TextView md = findViewById(memberScoreKM[i]);

                    gm.setText(groupMemberDatabase);
                    ms.setText(memberScoreDatabase);
                    md.setText(memberDistanceDatabase);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getRunInformations(){

        profileGetRef.child(userID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("username").getValue(String.class);

                TextView yourName = findViewById(R.id.yourNameText);
                yourName.setText(name);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        TextView yourScore = findViewById(R.id.yourScoreText);
        TextView yourScoreKM = findViewById(R.id.yourScoreKMText);

        yourScore.setText(score);
        yourScoreKM.setText(scoreKM);
    }

    public void clickOkButton (View view){
        Intent intent = new Intent ( GroupActivity.this, LeaderboardFragment.class);
        intent.putExtra(USER_ID,userID);
        startActivity(intent);
    }
}


