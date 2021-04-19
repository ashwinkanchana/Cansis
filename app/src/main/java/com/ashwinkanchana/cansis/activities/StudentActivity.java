package com.ashwinkanchana.cansis.activities;


import androidx.annotation.NonNull;

import com.ashwinkanchana.cansis.utils.App;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;


import com.ashwinkanchana.cansis.fragments.AttendanceFragment;
import com.ashwinkanchana.cansis.fragments.MarksContainerFragment;
import com.ashwinkanchana.cansis.R;

import com.ashwinkanchana.cansis.fragments.GalleryFragment;
import com.ashwinkanchana.cansis.fragments.NoticeFragment;
import com.ashwinkanchana.cansis.fragments.ProfileFragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import androidx.fragment.app.Fragment;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.appcompat.app.AppCompatActivity;


import android.content.Intent;
import android.os.Build;
import android.os.Bundle;


import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;


import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;


import static com.ashwinkanchana.cansis.utils.Constants.FCM_TOPIC_ANNOUNCEMENTS;
import static com.ashwinkanchana.cansis.utils.Constants.FCM_TOPIC_STUDENTS;
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


public class StudentActivity extends AppCompatActivity  {

    private static final String TAG = StudentActivity.class.getSimpleName();
    private FirebaseRemoteConfig remoteConfig;


    private ViewPager viewPager;
    private BottomNavigationView navigation;
    private Toolbar toolbar;
    private AppBarLayout appBarLayout;
    private FragmentPageAdapter adapter;
    public static Toast toast;
    public static int bottomSelectedId;




    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            bottomSelectedId = item.getItemId();
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
                case R.id.navGallery:
                    toolbar.setTitle(navigation.getMenu().getItem(3).getTitle());
                    viewPager.setCurrentItem(3,false);
                    appBarLayout.setExpanded(true,true);
                    return true;
                case R.id.navProfile:
                    toolbar.setTitle(navigation.getMenu().getItem(4).getTitle());
                    viewPager.setCurrentItem(4,false);
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
        setContentView(R.layout.activity_student);
        appBarLayout = findViewById(R.id.app_bar);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        viewPager = findViewById(R.id.view_pager);
        adapter = new FragmentPageAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(adapter.getCount() - 1);
        navigation = findViewById(R.id.navigation);

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        subscribeFcmTopic();


        remoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(900)
                .build();
        remoteConfig.setConfigSettingsAsync(configSettings);
        remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults);
        fetchRemoteConfig();

        //navigation.setSelectedItemId(R.id.navMarks);
    }


    private void fetchRemoteConfig() {
        remoteConfig.fetchAndActivate()
                .addOnCompleteListener(this, new OnCompleteListener<Boolean>() {
                    @Override
                    public void onComplete(@NonNull Task<Boolean> task) {
                        if (task.isSuccessful()) {
                            boolean updated = task.getResult();
                            Log.d(TAG, "Config params updated: " + updated);
                            if(remoteConfig.getBoolean(RC_IS_APP_DISABLED)){
                                appDisabled();
                            }
                            else if (remoteConfig.getBoolean(RC_APP_UPDATE_REQUIRED)) {
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





    private static class FragmentPageAdapter extends FragmentPagerAdapter {

        public FragmentPageAdapter(FragmentManager fragmentManager) {
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
                    case 3:
                        return GalleryFragment.newInstance();
                    case 4:
                        return ProfileFragment.newInstance();

                }
                return null;
            }

            @Override
            public int getCount() {
                return 5;
            }

    }

    @Override
    public void onBackPressed () {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0 && navigation.getSelectedItemId() != R.id.navMarks) {
            navigation.setSelectedItemId(R.id.navMarks);
        } else {
            super.onBackPressed();
        }
    }

    private void subscribeFcmTopic(){
        FirebaseMessaging.getInstance().subscribeToTopic(FCM_TOPIC_ANNOUNCEMENTS);
        FirebaseMessaging.getInstance().subscribeToTopic(FCM_TOPIC_STUDENTS);
    }

}

