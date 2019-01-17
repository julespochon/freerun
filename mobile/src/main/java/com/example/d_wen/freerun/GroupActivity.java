package com.example.d_wen.freerun;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class GroupActivity extends AppCompatActivity {

    final FirebaseDatabase database=FirebaseDatabase.getInstance();
    final DatabaseReference profileGetRef=database.getReference( "profiles" );
    final DatabaseReference groupGetRef=database.getReference( "Groupes" );

    public static final String USER_ID = "USER_ID";
    public static final String SCORE = "SCORE";
    public static final String SCORE_KM = "SCORE_KM";

    private String userID;
    private String score;
    private String scoreKM;
    private String groupName;
    private String userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_group );

        Intent intent = getIntent();
        userID = intent.getExtras().getString(USER_ID);
        score = intent.getExtras().getString(SCORE);
        scoreKM = intent.getExtras().getString(SCORE_KM);

        readGroupName();
        getRunInformations();
    }

    private void readGroupName(){
        profileGetRef.child(userID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                groupName = dataSnapshot.child("groupe").getValue(String.class);
                userName = dataSnapshot.child("username").getValue(String.class);
                getGroupInfo();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getGroupInfo() {

       TextView groupTitle = findViewById(R.id.groupTitleText);
       groupTitle.setText(groupName);

        groupGetRef.child(groupName).child("Participants").
                addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                ArrayList<TextView> groupMembers = new ArrayList<>();
                ArrayList<TextView> memberScore = new ArrayList<>();
                ArrayList<TextView> memberScoreKM = new ArrayList<>();

                groupMembers.add( (TextView) findViewById(R.id.member1NameText) );
                groupMembers.add( (TextView) findViewById(R.id.member2NameText) );
                groupMembers.add( (TextView) findViewById(R.id.member3NameText) );
                groupMembers.add( (TextView) findViewById(R.id.member4NameText) );
                groupMembers.add( (TextView) findViewById(R.id.member5NameText) );
                groupMembers.add( (TextView) findViewById(R.id.member6NameText) );
                groupMembers.add( (TextView) findViewById(R.id.member7NameText) );
                groupMembers.add( (TextView) findViewById(R.id.member8NameText) );
                groupMembers.add( (TextView) findViewById(R.id.member9NameText) );
                groupMembers.add( (TextView) findViewById(R.id.member10NameText) );

                memberScore.add( (TextView) findViewById(R.id.member1ScoreText));
                memberScore.add( (TextView) findViewById(R.id.member2ScoreText));
                memberScore.add( (TextView) findViewById(R.id.member3ScoreText));
                memberScore.add( (TextView) findViewById(R.id.member4ScoreText));
                memberScore.add( (TextView) findViewById(R.id.member5ScoreText));
                memberScore.add( (TextView) findViewById(R.id.member6ScoreText));
                memberScore.add( (TextView) findViewById(R.id.member7ScoreText));
                memberScore.add( (TextView) findViewById(R.id.member8ScoreText));
                memberScore.add( (TextView) findViewById(R.id.member9ScoreText));
                memberScore.add( (TextView) findViewById(R.id.member10ScoreText));

                memberScoreKM.add((TextView) findViewById(R.id.member1ScoreKMText));
                memberScoreKM.add((TextView) findViewById(R.id.member2ScoreKMText));
                memberScoreKM.add((TextView) findViewById(R.id.member3ScoreKMText));
                memberScoreKM.add((TextView) findViewById(R.id.member4ScoreKMText));
                memberScoreKM.add((TextView) findViewById(R.id.member5ScoreKMText));
                memberScoreKM.add((TextView) findViewById(R.id.member6ScoreKMText));
                memberScoreKM.add((TextView) findViewById(R.id.member7ScoreKMText));
                memberScoreKM.add((TextView) findViewById(R.id.member8ScoreKMText));
                memberScoreKM.add((TextView) findViewById(R.id.member9ScoreKMText));
                memberScoreKM.add((TextView) findViewById(R.id.member10ScoreKMText));

                int i = 0;

                for (DataSnapshot childLoc : dataSnapshot.getChildren()){

                    String groupMemberDatabase = childLoc.getKey();
                    String memberScoreDatabase = childLoc.child("score_in _tot")
                            .getValue(String.class);
                    String memberDistanceDatabase = childLoc.child("score_each_km")
                            .getValue(String.class);

                    TextView gm = groupMembers.get( i );
                    TextView ms = memberScore.get( i );
                    TextView md = memberScoreKM.get( i );

                    gm.setText(groupMemberDatabase);
                    ms.setText(memberScoreDatabase);
                    md.setText(memberDistanceDatabase);

                    i = i+1;
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
        groupGetRef.child(groupName).child("Participants").child(userName).child("score_each_km").setValue(scoreKM);
        groupGetRef.child(groupName).child("Participants").child(userName).child("score_in _tot").setValue(score);
        Intent intent = new Intent ( GroupActivity.this, MainActivity.class);
        intent.putExtra(USER_ID,userID);
        startActivity(intent);
    }
}


