package com.example.d_wen.freerun;

import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "EmailPassword";
    public static final String USER_ID = "USER_ID";

    private static final int REGISTER_PROFILE = 1;
    private Profile userProfile = null;
    private String userID;
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mAuth = FirebaseAuth.getInstance();
    }

    public void clickedLoginButton (View view){
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference profileGetRef = database.getReference("profiles");

        final TextView mTextView = findViewById(R.id.loginMessage);
        final String email = ((EditText) findViewById(R.id.email))
                .getText().toString();
        final String password = ((EditText) findViewById(R.id.password))
                .getText().toString();

        signIn(email, password);
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
                TextView username=findViewById(R.id.email);
                username.setText(userProfile.email);
                TextView password=findViewById(R.id.password);
                password.setText(userProfile.password);
            }
        }
    }

    private void signIn(String email, String password) {
        Log.d(TAG, "signIn:" + email);

        // [START sign_in_with_email]
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");

                            FirebaseUser user = mAuth.getCurrentUser();
                            boolean emailVerified = user.isEmailVerified();
                            if (emailVerified){
                                userID=user.getUid();
                                Intent intent=new Intent(LoginActivity.this,
                                        MainActivity.class);
                                intent.putExtra(USER_ID, userID);
                                startActivity(intent);
                            }else{
                                Toast.makeText(LoginActivity.this, "Email not verified",
                                        Toast.LENGTH_SHORT).show();
                            }

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        // [END sign_in_with_email]
    }

}
