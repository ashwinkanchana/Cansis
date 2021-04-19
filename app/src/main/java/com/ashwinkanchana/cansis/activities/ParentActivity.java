package com.ashwinkanchana.cansis.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.android.volley.RequestQueue;
import com.ashwinkanchana.cansis.fragments.AttendanceFragment;
import com.ashwinkanchana.cansis.fragments.MarksContainerFragment;
import com.ashwinkanchana.cansis.fragments.NoticeFragment;
import com.ashwinkanchana.cansis.utils.App;
import com.ashwinkanchana.cansis.data.Profile;

import maes.tech.intentanim.CustomIntent;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.ashwinkanchana.cansis.utils.VolleySingleton;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.navigation.NavigationView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.google.gson.Gson;

import com.google.gson.reflect.TypeToken;


import com.ashwinkanchana.cansis.R;


import org.json.JSONArray;

import java.util.List;

import static com.ashwinkanchana.cansis.activities.StudentActivity.toast;
import static com.ashwinkanchana.cansis.utils.Constants.ANIM_TYPE_LEFT_TO_RIGHT;
import static com.ashwinkanchana.cansis.utils.Constants.FCM_TOPIC_ANNOUNCEMENTS;
import static com.ashwinkanchana.cansis.utils.Constants.FCM_TOPIC_PARENTS;
import static com.ashwinkanchana.cansis.utils.Constants.FCM_TOPIC_STUDENTS;
import static com.ashwinkanchana.cansis.utils.Constants.JSON_ARRAY_RESULT;
import static com.ashwinkanchana.cansis.utils.Constants.PREF_KEY_ATTD_CACHE;
import static com.ashwinkanchana.cansis.utils.Constants.PREF_KEY_BRANCH_SHORT;
import static com.ashwinkanchana.cansis.utils.Constants.PREF_KEY_CURRENT_SEM;
import static com.ashwinkanchana.cansis.utils.Constants.PREF_KEY_DARK_MODE;
import static com.ashwinkanchana.cansis.utils.Constants.PREF_KEY_DEFAULT_SEMESTER;
import static com.ashwinkanchana.cansis.utils.Constants.PREF_KEY_ENCODED_NAME;
import static com.ashwinkanchana.cansis.utils.Constants.PREF_KEY_GALLERY_CACHE;
import static com.ashwinkanchana.cansis.utils.Constants.PREF_KEY_IS_FIRST_TIME;
import static com.ashwinkanchana.cansis.utils.Constants.PREF_KEY_IS_LOGGED_IN;
import static com.ashwinkanchana.cansis.utils.Constants.PREF_KEY_MARKS_SUBS_CACHE;
import static com.ashwinkanchana.cansis.utils.Constants.PREF_KEY_MARKS_SUBS_CACHE_SEM;
import static com.ashwinkanchana.cansis.utils.Constants.PREF_KEY_NOTICE_CACHE;
import static com.ashwinkanchana.cansis.utils.Constants.PREF_KEY_PROFILE_CACHE;
import static com.ashwinkanchana.cansis.utils.Constants.PREF_KEY_PROFILE_PIC_URL;
import static com.ashwinkanchana.cansis.utils.Constants.PREF_KEY_STUDENT_ID;
import static com.ashwinkanchana.cansis.utils.Constants.PREF_KEY_STUDENT_NAME;
import static com.ashwinkanchana.cansis.utils.Constants.PREF_KEY_USERNAME;
import static com.ashwinkanchana.cansis.utils.Constants.PREF_KEY_USER_TYPE;
import static com.ashwinkanchana.cansis.utils.Constants.PREF_KEY_USN;
import static com.ashwinkanchana.cansis.utils.Constants.RC_APP_LATEST_VERSION;
import static com.ashwinkanchana.cansis.utils.Constants.RC_APP_UPDATE_REQUIRED;
import static com.ashwinkanchana.cansis.utils.Constants.RC_DISABLED_DESCRIPTION;
import static com.ashwinkanchana.cansis.utils.Constants.RC_DISABLED_TITLE;
import static com.ashwinkanchana.cansis.utils.Constants.RC_ERROR_TYPE;
import static com.ashwinkanchana.cansis.utils.Constants.RC_ERROR_TYPE_DISABLED;
import static com.ashwinkanchana.cansis.utils.Constants.RC_ERROR_TYPE_UPDATE;
import static com.ashwinkanchana.cansis.utils.Constants.RC_IS_APP_DISABLED;
import static com.ashwinkanchana.cansis.utils.Constants.RC_UPDATE_DESC;
import static com.ashwinkanchana.cansis.utils.Constants.RC_UPDATE_TITLE;
import static com.ashwinkanchana.cansis.utils.Constants.RC_UPDATE_URL;
import static com.ashwinkanchana.cansis.utils.App.PACKAGE_NAME;
import static com.ashwinkanchana.cansis.utils.Constants.REQUEST_QUEUE_TAG_ATTENDANCE;
import static com.ashwinkanchana.cansis.utils.Constants.REQUEST_QUEUE_TAG_GALLERY;
import static com.ashwinkanchana.cansis.utils.Constants.REQUEST_QUEUE_TAG_NOTICE;
import static com.ashwinkanchana.cansis.utils.Constants.REQUEST_QUEUE_TAG_PROFILE;

public class ParentActivity extends AppCompatActivity {

    private static final String PROFILE_URL = "PROFILE_ENDPOINT_GOES_HERE";
    private ViewPager viewPager;
    private BottomNavigationView navigation;
    private Toolbar toolbar;
    private DrawerLayout drawer;
    private de.hdodenhof.circleimageview.CircleImageView profilePic;
    private TextView nameTextView;
    private TextView branch;
    private AppBarLayout appBarLayout;
    private SharedPreferences prefs;
    private FirebaseRemoteConfig remoteConfig;
    private RequestQueue requestQueue;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            StudentActivity.bottomSelectedId = item.getItemId();
            toolbar.setTitle(item.getTitle());
            if(toast!=null){
                toast.cancel();
            }
            switch (item.getItemId()) {
                case R.id.navNews:
                    toolbar.setTitle(R.string.app_name);
                    viewPager.setCurrentItem(0,false);
                    appBarLayout.setExpanded(true,true);
                    return true;
                case R.id.navAttendance:
                    toolbar.setTitle(navigation.getMenu().getItem(1).getTitle());
                    viewPager.setCurrentItem(1,false);
                    appBarLayout.setExpanded(true,true);
                    return true;
                case R.id.navMarks:
                    toolbar.setTitle(navigation.getMenu().getItem(2).getTitle());
                    viewPager.setCurrentItem(2,false);
                    appBarLayout.setExpanded(true,true);
                    return true;
            }
            return false;
        }
    };





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
        setContentView(R.layout.activity_parent);
        prefs = this.getSharedPreferences(PACKAGE_NAME,MODE_PRIVATE);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        appBarLayout = findViewById(R.id.app_bar);

        requestQueue = VolleySingleton.getInstance(this).getRequestQueue();

        viewPager = findViewById(R.id.view_pager);
        NavigationView navigationView = findViewById(R.id.nav_view);

        View headerView =  navigationView.inflateHeaderView(R.layout.nav_header);
        profilePic = headerView.findViewById(R.id.nav_image);
        nameTextView = headerView.findViewById(R.id.nav_name);
        branch = headerView.findViewById(R.id.nav_branch_text);

        drawer = findViewById(R.id.drawer_layout);

        ParentActivity.FragmentPageAdapter adapter = new ParentActivity.FragmentPageAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(adapter.getCount() - 1);
        navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        bindNavigationDrawer();
        subscribeFcmTopic();
        navigationView.getMenu().getItem(0).setChecked(true);

        boolean firstTime = prefs.getBoolean(PREF_KEY_IS_FIRST_TIME,true);



        profilePic.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
            startActivity(intent);
        });



        fetchProfile();

        remoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(900)
                .build();
        remoteConfig.setConfigSettingsAsync(configSettings);
        remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults);
        fetchRemoteConfig();

    }

    private void fetchRemoteConfig() {
        remoteConfig.fetchAndActivate()
                .addOnCompleteListener(this, new OnCompleteListener<Boolean>() {
                    @Override
                    public void onComplete(@NonNull Task<Boolean> task) {
                        if (task.isSuccessful()) {
                            boolean updated = task.getResult();
                            Log.d("TAG", "Config params updated: " + updated);

                            if(remoteConfig.getBoolean(RC_IS_APP_DISABLED)){
                                appDisabled();
                            } else if (remoteConfig.getBoolean(RC_APP_UPDATE_REQUIRED)) {
                                updateRequired();
                            }

                        }

                    }
                });

    }

    private void updateRequired(){

        String updateTitle = remoteConfig.getString(RC_UPDATE_TITLE);
        String updateDesc = remoteConfig.getString(RC_UPDATE_DESC);
        String updateURL = remoteConfig.getString(RC_UPDATE_URL);
        String latestVersion = remoteConfig.getString(RC_APP_LATEST_VERSION);
        String installedVersion = App.getVersionName(this);
        if (!TextUtils.equals(latestVersion, installedVersion)) {
            Intent intent = new Intent(this, ErrorActivity.class);
            intent.putExtra(RC_UPDATE_TITLE,updateTitle);
            intent.putExtra(RC_UPDATE_DESC,updateDesc);
            intent.putExtra(RC_UPDATE_URL,updateURL);
            intent.putExtra(RC_ERROR_TYPE,RC_ERROR_TYPE_UPDATE);
            startActivity(intent);
        }


    }


    private void appDisabled() {
        String killTitle = remoteConfig.getString(RC_DISABLED_TITLE);
        String killDescription = remoteConfig.getString(RC_DISABLED_DESCRIPTION);
        Intent intent = new Intent(this, ErrorActivity.class);
        intent.putExtra(RC_ERROR_TYPE,RC_ERROR_TYPE_DISABLED);
        intent.putExtra(RC_DISABLED_TITLE ,killTitle);
        intent.putExtra(RC_DISABLED_DESCRIPTION,killDescription);
        startActivity(intent);


    }





    private void bindNavigationDrawer() {



        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();


        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(item -> {
            // Handle navigation view item clicks here.
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                //Do Nothing
            } else if (id == R.id.nav_profile) {
                //toolbar.setTitle("Profile");
                final Handler handler = new Handler();
                handler.postDelayed(this::profileIntent, 290);

            }
            else if (id == R.id.nav_logout) {
                logout();
            }
            drawer.closeDrawer(GravityCompat.START);
            return true;
        });



    }

    private void profileIntent() {
        Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
        startActivity(intent);
        CustomIntent.customType(ParentActivity.this,ANIM_TYPE_LEFT_TO_RIGHT);
    }



    private static class FragmentPageAdapter extends FragmentPagerAdapter {


        private FragmentPageAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }



        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return NoticeFragment.newInstance();
                case 1:
                    return AttendanceFragment.newInstance();
                case 2:
                    return MarksContainerFragment.newInstance();
            }
            return null;
        }

        @Override
        public int getCount() {
            return 3;
        }
    }





    private void fetchProfile() {

        String username = App.getUsername();
        String encodedURL = PROFILE_URL+username;
        //String encodedURL  = PROFILE_URL;
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, encodedURL, null, response -> {
                    JSONArray jsonArray = response.optJSONArray(JSON_ARRAY_RESULT);

                    List<Profile> profile = new Gson().fromJson(jsonArray.toString(), new TypeToken<List<Profile>>() {
                    }.getType());
                    if(profile.size()!=0){
                        Profile prof = profile.get(0);

                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString(PREF_KEY_STUDENT_NAME,  prof.getName());
                        editor.putString(PREF_KEY_BRANCH_SHORT, prof.getBranch());
                        editor.putBoolean(PREF_KEY_IS_FIRST_TIME, false);
                        editor.apply();


                        branch.setText(prof.getBranch());
                        nameTextView.setText(prof.getName());
                        try{
                            Glide.with(this)
                                    .load(prof.getImage())
                                    .into(profilePic);
                        }catch (Exception e){
                            e.printStackTrace();
                        }


                    }}, error -> {
                    String studentBranch = prefs.getString(PREF_KEY_BRANCH_SHORT,"");
                    String studentName = prefs.getString(PREF_KEY_STUDENT_NAME,"");
                    branch.setText(studentBranch);
                    nameTextView.setText( studentName);
                });
        jsonObjectRequest.setTag(REQUEST_QUEUE_TAG_PROFILE);
        requestQueue.add(jsonObjectRequest);

    }


    @Override public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
        else if (getSupportFragmentManager().getBackStackEntryCount() > 0 && navigation.getSelectedItemId()!=R.id.navMarks) {
            navigation.setSelectedItemId(R.id.navMarks);
        }else {
            super.onBackPressed();

        }
    }




    public void logout(){

        DialogInterface.OnClickListener dialogClickListener = (DialogInterface dialog, int which) -> {
            switch (which){
                case DialogInterface.BUTTON_POSITIVE:
                    logoutConfirmed();
                    break;

            }
        };

      new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.logout_dialog_title)
                .setMessage(R.string.logout_dialog_description)
                .setPositiveButton(R.string.logout_positive_button, dialogClickListener)
                .setNegativeButton(R.string.logout_negative_button,null)
                .show();


    }



    public void logoutConfirmed(){

        requestQueue.cancelAll(REQUEST_QUEUE_TAG_NOTICE);
        requestQueue.cancelAll(REQUEST_QUEUE_TAG_ATTENDANCE);
        requestQueue.cancelAll(REQUEST_QUEUE_TAG_GALLERY);
        requestQueue.cancelAll(REQUEST_QUEUE_TAG_PROFILE);

        FirebaseMessaging.getInstance().unsubscribeFromTopic(FCM_TOPIC_ANNOUNCEMENTS);
        FirebaseMessaging.getInstance().unsubscribeFromTopic(FCM_TOPIC_PARENTS);
        FirebaseMessaging.getInstance().unsubscribeFromTopic(FCM_TOPIC_STUDENTS);
        FirebaseAuth.getInstance().signOut();

        Intent intent = new Intent (this, LoginActivity.class);

        SharedPreferences.Editor editor = prefs.edit();

        editor.putString(PREF_KEY_USERNAME,null);
        editor.putString(PREF_KEY_ENCODED_NAME,null);
        editor.putInt(PREF_KEY_CURRENT_SEM,-1);
        editor.putString(PREF_KEY_PROFILE_PIC_URL,null);
        editor.putString(PREF_KEY_STUDENT_NAME,null);
        editor.putString(PREF_KEY_BRANCH_SHORT,null);
        editor.putString(PREF_KEY_ATTD_CACHE,null);
        editor.putString(PREF_KEY_NOTICE_CACHE,null);
        editor.putString(PREF_KEY_PROFILE_CACHE,null);
        editor.putString(PREF_KEY_GALLERY_CACHE,null);
        editor.putString(PREF_KEY_MARKS_SUBS_CACHE,null);
        editor.putInt(PREF_KEY_MARKS_SUBS_CACHE_SEM,-1);
        editor.putString(PREF_KEY_STUDENT_ID,null);
        editor.putBoolean(PREF_KEY_IS_LOGGED_IN, false);
        editor.putBoolean(PREF_KEY_IS_FIRST_TIME, true);
        editor.putString(PREF_KEY_USN,null);
        editor.putBoolean(PREF_KEY_DARK_MODE,false);
        editor.putInt(PREF_KEY_USER_TYPE, 0);
        editor.putInt(PREF_KEY_DEFAULT_SEMESTER, -1);
        editor.apply();

        startActivity(intent);
        finish();
    }

    private void subscribeFcmTopic(){
        FirebaseMessaging.getInstance().subscribeToTopic(FCM_TOPIC_ANNOUNCEMENTS);
        FirebaseMessaging.getInstance().subscribeToTopic(FCM_TOPIC_PARENTS);
    }

}
