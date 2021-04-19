package com.ashwinkanchana.cansis.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.ashwinkanchana.cansis.fragments.BatteryOptimizationDialog;
import com.ashwinkanchana.cansis.fragments.FeedbackDialog;
import com.ashwinkanchana.cansis.utils.VolleySingleton;
import com.franmontiel.attributionpresenter.AttributionPresenter;
import com.franmontiel.attributionpresenter.entities.Attribution;
import com.franmontiel.attributionpresenter.entities.License;
import com.google.android.material.switchmaterial.SwitchMaterial;

import com.ashwinkanchana.cansis.R;
import com.ashwinkanchana.cansis.utils.App;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import java.util.Objects;

import static com.ashwinkanchana.cansis.utils.Constants.PREF_KEY_DARK_MODE;
import static com.ashwinkanchana.cansis.utils.Constants.RC_SIGNATURE_CLICK_ENABLED;
import static com.ashwinkanchana.cansis.utils.Constants.RC_SIGNATURE_CLICK_SPAN_START;
import static com.ashwinkanchana.cansis.utils.Constants.RC_SIGNATURE_CLICK_SPAN_STOP;
import static com.ashwinkanchana.cansis.utils.Constants.RC_SIGNATURE_TEXT;
import static com.ashwinkanchana.cansis.utils.Constants.RC_SIGNATURE_URL;
import static com.ashwinkanchana.cansis.utils.App.PACKAGE_NAME;
import static com.ashwinkanchana.cansis.utils.Constants.REQUEST_QUEUE_TAG_ATTENDANCE;
import static com.ashwinkanchana.cansis.utils.Constants.REQUEST_QUEUE_TAG_GALLERY;
import static com.ashwinkanchana.cansis.utils.Constants.REQUEST_QUEUE_TAG_NOTICE;
import static com.ashwinkanchana.cansis.utils.Constants.REQUEST_QUEUE_TAG_PROFILE;


public class AboutActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String COLLEGE_WEBSITE = "http://www.canaraengineering.in/";
    public static final String TAG = AboutActivity.class.getSimpleName();
    private FirebaseRemoteConfig remoteConfig;
    private SwitchMaterial switchMaterial;
    private TextView signatureTextView;
    private String signatureText;
    private String signatureTextUrl;
    private boolean signatureClickEnabled;
    private RequestQueue requestQueue;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if(App.getInstance().isNightModeEnabled()) {
            setTheme(R.style.DarkTheme);}
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.toolbar_color));
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        signatureTextView = findViewById(R.id.signature_text);

        requestQueue = VolleySingleton.getInstance(this).getRequestQueue();

        remoteConfig = FirebaseRemoteConfig.getInstance();
        signatureText = remoteConfig.getString(RC_SIGNATURE_TEXT);
        signatureTextUrl = remoteConfig.getString(RC_SIGNATURE_URL);
        signatureClickEnabled = remoteConfig.getBoolean(RC_SIGNATURE_CLICK_ENABLED);
        viewLatestValues();


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        switchMaterial = findViewById(R.id.theme_switch);
        SharedPreferences prefs = getSharedPreferences(PACKAGE_NAME,MODE_PRIVATE);
        final Handler handler = new Handler();
        if(prefs.getBoolean(PREF_KEY_DARK_MODE,false))
            switchMaterial.setChecked(true);

        switchMaterial.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Toast.makeText(this,"Restarting app",Toast.LENGTH_LONG).show();
            switchMaterial.setEnabled(false);

            if(isChecked){
                prefs.edit().putBoolean(PREF_KEY_DARK_MODE,true).apply();
                handler.postDelayed(this:: restartApp, 250);
            }else{
                prefs.edit().putBoolean(PREF_KEY_DARK_MODE,false).apply();
                handler.postDelayed(this:: restartApp, 250);
            }

        });


        initView();

    }


    private void restartApp() {
        Intent mStartActivity = new Intent(this, SplashActivity.class);
        overridePendingTransition(-1, R.anim.activity_exit_alpha);
        int mPendingIntentId = 123456;
        requestQueue.cancelAll(REQUEST_QUEUE_TAG_NOTICE);
        requestQueue.cancelAll(REQUEST_QUEUE_TAG_ATTENDANCE);
        requestQueue.cancelAll(REQUEST_QUEUE_TAG_GALLERY);
        requestQueue.cancelAll(REQUEST_QUEUE_TAG_PROFILE);
        PendingIntent mPendingIntent = PendingIntent.getActivity(this, mPendingIntentId,    mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager mgr = (AlarmManager)this.getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis()+100, mPendingIntent);
        System.exit(0);
        startActivity(mStartActivity);
    }



    public void initView() {
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.anim_about_card_show);
        LinearLayout about = findViewById(R.id.about_layout);
        about.startAnimation(animation);

        TextView feedback_option = findViewById(R.id.feedback);
        TextView website = findViewById(R.id.website);
        TextView open_source_licenses = findViewById(R.id.open_source_licenses);
        TextView battery_optimization = findViewById(R.id.battery_optimization);
        switchMaterial.setText(R.string.dark_mode);

        feedback_option.setOnClickListener(this);
        website.setOnClickListener(this);
        open_source_licenses.setOnClickListener(this);
        battery_optimization.setOnClickListener(this);


        AlphaAnimation alphaAnimation = new AlphaAnimation(0.0f, 1.0f);
        alphaAnimation.setDuration(300);
        alphaAnimation.setStartOffset(600);

        TextView about_version = findViewById(R.id.about_version);
        String version = getResources().getString(R.string.v)+ App.getVersionName(this);
        about_version.setText(version);
        about_version.startAnimation(alphaAnimation);
    }

    @Override
    public void onClick(View view) {


        switch (view.getId()) {
            case R.id.feedback:
                showFeedbackDialog();
                break;


            case R.id.open_source_licenses:
                attributionDialogue();
                break;

            case R.id.website:
                Intent intent = new Intent();
                intent.setData(Uri.parse(COLLEGE_WEBSITE));
                intent.setAction(Intent.ACTION_VIEW);
                startActivity(intent);
                break;

            case R.id.battery_optimization:
                showBatteryOptimizationDialog();
                break;

        }



}

    public void attributionDialogue(){


        AttributionPresenter attributionPresenter = new AttributionPresenter.Builder(this)
                .addAttributions(
                        new Attribution.Builder("Shimmer-Android")
                                .addCopyrightNotice("Copyright (c) Facebook, Inc.")
                                .addLicense(License.BSD_3)
                                .setWebsite("https://github.com/facebook/shimmer-android/blob/master/LICENSE")
                                .build()
                )
                .addAttributions(
                        new Attribution.Builder("LoadingButtonAndroid")
                                .addCopyrightNotice("Copyright (c) 2016 leandroBorgesFerreira")
                                .addLicense(License.MIT)
                                .setWebsite("https://github.com/leandroBorgesFerreira/LoadingButtonAndroid/blob/master/LICENSE")
                                .build()
                )
                .addAttributions(
                        new Attribution.Builder("IntentAnimation")
                                .addCopyrightNotice("Copyright (c) 2018 Elnur")
                                .addLicense(License.MIT)
                                .setWebsite("https://github.com/hajiyevelnur92/intentanimation/blob/master/LICENSE")
                                .build()
                )
                .addAttributions(
                        new Attribution.Builder("Commons-Text")
                                .addCopyrightNotice("Copyright (c) The Apache Software Foundation")
                                .addLicense(License.APACHE)
                                .setWebsite("https://github.com/apache/commons-text/blob/master/LICENSE.txt")
                                .build()
                )
                .addAttributions(
                        new Attribution.Builder("CircleImageView")
                                .addCopyrightNotice("Copyright 2014 - 2020 Henning Dodenhof")
                                .addLicense(License.APACHE)
                                .setWebsite("https://github.com/hdodenhof/CircleImageView/blob/master/LICENSE.txt")
                                .build()
                )
                .addAttributions(
                        new Attribution.Builder("Volley")
                                .addCopyrightNotice("Copyright (c) Google, Inc.")
                                .addLicense(License.APACHE)
                                .setWebsite("https://github.com/google/volley/blob/master/LICENSE")
                                .build()
                )
                .addAttributions(
                        new Attribution.Builder("Gson")
                                .addCopyrightNotice("Copyright 2008-2011 Google, Inc.")
                                .addLicense(License.APACHE)
                                .setWebsite("https://github.com/google/gson/blob/master/gson/LICENSE")
                                .build())
                .addAttributions(
                        new Attribution.Builder("Lottie-Android")
                                .addCopyrightNotice("Copyright 2018 Airbnb, Inc.")
                                .addLicense(License.APACHE)
                                .setWebsite("https://github.com/airbnb/lottie-android/blob/master/LICENSE")
                                .build())
                .addAttributions(
                        new Attribution.Builder("VerticalStepperForm")
                                .addCopyrightNotice("Copyright 2018 Julio Ernesto Rodríguez Cabañas")
                                .addLicense(License.APACHE)
                                .setWebsite("https://github.com/ernestoyaquello/VerticalStepperForm/blob/master/LICENSE.txt")
                                .build()
                )
                .addAttributions(
                        new Attribution.Builder("Android-OTPview-pinview")
                                .addCopyrightNotice("Copyright (c) 2018 Mukesh Solanki")
                                .addLicense(License.MIT)
                                .setWebsite("https://github.com/mukeshsolanki/android-otpview-pinview/blob/master/LICENSE")
                                .build()
                )
                .addAttributions(
                        new Attribution.Builder("ZoomInImageView")
                                .addCopyrightNotice("Copyright 2018 Zolad")
                                .addLicense(License.APACHE)
                                .setWebsite("https://github.com/zolad/ZoomInImageView")
                                .build()
                )
                .addAttributions(
                        new Attribution.Builder("Glide")
                                .addCopyrightNotice("Copyright 2014 Google, Inc.")
                                .addLicense(License.APACHE)
                                .setWebsite("https://github.com/bumptech/glide/blob/master/LICENSE")
                                .build()
                )
                .addAttributions(
                        new Attribution.Builder("Material-Design-Icons")
                                .addCopyrightNotice("Copyright (c) Google, Inc.")
                                .addLicense(License.APACHE)
                                .setWebsite("https://github.com/google/material-design-icons/blob/master/LICENSE")
                                .build())
                .addAttributions(
                        new Attribution.Builder("Sendgrid-java")
                                .addCopyrightNotice("Copyright (C) 2020, Twilio SendGrid, Inc.")
                                .addLicense(License.MIT)
                                .setWebsite("https://github.com/sendgrid/sendgrid-java/blob/master/LICENSE.md")
                                .build())
                .addAttributions(
                        new Attribution.Builder("")
                                .addCopyrightNotice("This application uses Open Source components. You can find the source code of these projects along with license information here. We acknowledge and are grateful to these developers for their contributions to open source.\uD83D\uDC99").build())

                .build();
        attributionPresenter.showDialog(getResources().getString(R.string.license));

    }

    private void clickableText(){
        int spanStart = Integer.parseInt(remoteConfig.getString(RC_SIGNATURE_CLICK_SPAN_START));
        int spanStop = Integer.parseInt(remoteConfig.getString(RC_SIGNATURE_CLICK_SPAN_STOP));
        SpannableString ss = new SpannableString(signatureText);
        ClickableSpan developerName = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                Intent intent = new Intent();
                intent.setData(Uri.parse(signatureTextUrl));
                intent.setAction(Intent.ACTION_VIEW);
                startActivity(intent);
                widget.invalidate();
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(true);
                if (signatureTextView.isPressed()) {
                    ds.setColor(getResources().getColor(R.color.colorBlue));
                } else {
                    ds.setColor(getResources().getColor(R.color.noticeHeaderText));
                }
                signatureTextView.invalidate();
                signatureTextView.setHighlightColor(Color.TRANSPARENT);
            }
        };
        ss.setSpan(developerName, spanStart , spanStop, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        signatureTextView.setText(ss);
        signatureTextView.setMovementMethod(LinkMovementMethod.getInstance());
    }

    public void showFeedbackDialog(){
        FeedbackDialog feedbackDialog = new FeedbackDialog();
        feedbackDialog.show(getSupportFragmentManager(), "feedback");
    }

    public void showBatteryOptimizationDialog(){
        BatteryOptimizationDialog batteryOptimizationDialog = new BatteryOptimizationDialog();
        batteryOptimizationDialog.show(getSupportFragmentManager(), "battery_optimization");
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void viewLatestValues(){
        signatureTextView.setText(signatureText);
        if(signatureClickEnabled){
            clickableText();
        }
    }
}
