package com.ashwinkanchana.cansis.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;


import androidx.appcompat.app.AppCompatActivity;



import com.ashwinkanchana.cansis.R;

import static com.ashwinkanchana.cansis.utils.Constants.PREF_KEY_IS_LOGGED_IN;
import static com.ashwinkanchana.cansis.utils.Constants.PREF_KEY_USER_TYPE;
import static com.ashwinkanchana.cansis.utils.Constants.USER_NONE;
import static com.ashwinkanchana.cansis.utils.Constants.USER_PARENT;
import static com.ashwinkanchana.cansis.utils.Constants.USER_STUDENT;
import static com.ashwinkanchana.cansis.utils.App.PACKAGE_NAME;

public class SplashActivity extends AppCompatActivity {

    private boolean isLoggedIn;
    private int user;



    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        SharedPreferences prefs = this.getSharedPreferences(PACKAGE_NAME, Context.MODE_PRIVATE);
        isLoggedIn = prefs.getBoolean(PREF_KEY_IS_LOGGED_IN, false);
        user = prefs.getInt(PREF_KEY_USER_TYPE,USER_NONE);

    }

    @Override
    protected void onStart() {
        super.onStart();
        run();
    }

    public void run() {
        if(isLoggedIn) {
            if (user == USER_STUDENT) {
                Intent intentStudent = new Intent(SplashActivity.this, StudentActivity.class);
                startActivity(intentStudent);
                overridePendingTransition(-1, R.anim.activity_exit_alpha);
                finish();
            } else if (user == USER_PARENT) {
                Intent intentParent = new Intent(SplashActivity.this, ParentActivity.class);
                startActivity(intentParent);
                overridePendingTransition(-1, R.anim.activity_exit_alpha);
                finish();

            }
        }
        else {
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
            overridePendingTransition(-1, R.anim.activity_exit_alpha);
            finish();

        }
    }






}