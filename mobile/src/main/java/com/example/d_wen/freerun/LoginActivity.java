package com.example.d_wen.freerun;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class LoginActivity extends AppCompatActivity {

    private static final int REGISTER_PROFILE = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }

    public void clickedLoginButton (View view){

    }

    public void clickedRegisterButton (View view){
        Intent StartRegisterActivity = new Intent(this, RegisterActivity.class);
        startActivityForResult(StartRegisterActivity, REGISTER_PROFILE);

    }
}
