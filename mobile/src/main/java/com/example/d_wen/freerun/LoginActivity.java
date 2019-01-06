package com.example.d_wen.freerun;

import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {
    private static final int REGISTER_PROFILE = 1;
    private Profile userProfile = null;
    private String userID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }

    public void clickedLoginButton (View view){
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference profileRef = database.getReference("profiles");

        final TextView mTextView = findViewById(R.id.loginMessage);
        final String usernameInput = ((EditText) findViewById(R.id.username))
                .getText().toString();
        final String passwordInput = ((EditText) findViewById(R.id.password))
                .getText().toString();

        profileRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                profileRef.removeEventListener(this);
                for (final DataSnapshot user : dataSnapshot.getChildren()) {
                    String usernameDatabase=user.child("username").getValue
                            (String.class);
                    String passwordDatabase=user.child("password").getValue
                            (String.class);
                    if (usernameInput.equals(usernameDatabase)) {
                        if (passwordInput.equals(passwordDatabase)) {
                            userID=user.getKey();
                            Intent intent=new Intent(LoginActivity.this,
                                    MainActivity.class);
                            intent.putExtra("userProfile", userID);
                            startActivity(intent);
                            break;
                        } else {
                            mTextView.setText(R.string.wrong_password);
                            mTextView.setTextColor(Color.RED);
                            break;
                        }

                    } else {
                        mTextView.setText(R.string.not_registered);
                        mTextView.setTextColor(Color.RED);
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
        Intent intentMainActivity = new Intent(LoginActivity.this, MainActivityFrag.class);
        startActivity(intentMainActivity);
    }

    public void clickedRegisterButton (View view){
        Intent StartRegisterActivity = new Intent(this, RegisterActivity.class);
        startActivityForResult(StartRegisterActivity, REGISTER_PROFILE);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if ( requestCode == REGISTER_PROFILE && resultCode == RESULT_OK && data != null){
            userProfile = (Profile) data.getSerializableExtra
                    ("userProfile");
            if (userProfile != null) {
                TextView username=findViewById(R.id.username);
                username.setText(userProfile.username);
                TextView password=findViewById(R.id.password);
                password.setText(userProfile.password);
            }
        }
    }

}
