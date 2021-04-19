package com.ashwinkanchana.cansis.activities;


import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.ashwinkanchana.cansis.R;
import com.ashwinkanchana.cansis.fragments.ProfileFragment;
import com.ashwinkanchana.cansis.utils.App;

import maes.tech.intentanim.CustomIntent;
import android.os.Bundle;



import java.util.Objects;

import static com.ashwinkanchana.cansis.utils.Constants.ANIM_TYPE_RIGHT_TO_LEFT;


public class ProfileActivity extends AppCompatActivity {





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if(App.getInstance().isNightModeEnabled()) {
            setTheme(R.style.DarkTheme);}
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_profile);


        Toolbar toolbar =  findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);




        ProfileFragment profileFragment = new ProfileFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.profile_container,profileFragment).commit();


        }




    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        CustomIntent.customType(ProfileActivity.this,ANIM_TYPE_RIGHT_TO_LEFT);
    }

}
