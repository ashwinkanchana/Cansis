package com.ashwinkanchana.cansis.utils;

import android.annotation.SuppressLint;
import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Base64;


import androidx.appcompat.app.AppCompatDelegate;

import com.android.volley.Request;
import com.android.volley.toolbox.Volley;
import com.ashwinkanchana.cansis.R;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import static com.ashwinkanchana.cansis.utils.Constants.NOTIFICATION_CHANNEL_ANNOUNCEMENTS;
import static com.ashwinkanchana.cansis.utils.Constants.NOTIFICATION_CHANNEL_GENERAL;
import static com.ashwinkanchana.cansis.utils.Constants.NOTIFICATION_CHANNEL_OTHERS;
import static com.ashwinkanchana.cansis.utils.Constants.PREF_KEY_DARK_MODE;
import static com.ashwinkanchana.cansis.utils.Constants.PREF_KEY_DEFAULT_SEMESTER;
import static com.ashwinkanchana.cansis.utils.Constants.PREF_KEY_USERNAME;
import static com.ashwinkanchana.cansis.utils.Constants.PREF_KEY_USER_TYPE;


public class App extends Application {
    public static final String TAG = App.class.getSimpleName();
    public static final String URL = "URL";
    public static final String PACKAGE_NAME = "com.ashwinkanchana.cansis";
    private static final String ALGORITHM = "Blowfish";
    private static final String MODE = "Blowfish/CBC/PKCS5Padding";
    private static final String IV = "SECRET_GOES_HERE";
    private static final String KEY= "SECRET_GOES_HERE";
    public static String username;
    private static String hash;



    private boolean isNightModeEnabled = false;
    private int defaultSemester;
    private int userType;
    private static App mInstance;

    public App(){
    }


    @Override
    public void onCreate() {
        mInstance = this;
        initUsername(this);
        SharedPreferences prefs =  getSharedPreferences(PACKAGE_NAME,MODE_PRIVATE);
        this.isNightModeEnabled = prefs.getBoolean(PREF_KEY_DARK_MODE, false);
        this.defaultSemester = prefs.getInt(PREF_KEY_DEFAULT_SEMESTER,0);
        this.userType = prefs.getInt(PREF_KEY_USER_TYPE,0);
        if(isNightModeEnabled){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        }
        super.onCreate();
        createNotificationChannel();




    }


    public static void changeTheme(Context context,boolean darkMode){
        if(darkMode){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            context.setTheme(R.style.DarkTheme);
        }else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            context.setTheme(R.style.AppTheme);

        }
    }

    public boolean isNightModeEnabled() {
        return isNightModeEnabled;
    }

    public static void initUsername(Context context){
        try {
            if(loadHash(context)!=null)
                username = decryptUsername(loadHash(context));
        } catch (NoSuchPaddingException | InvalidAlgorithmParameterException | NoSuchAlgorithmException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
            e.printStackTrace();
        }
    }

    public static String getUsername(){
        return username;
    }

    public static String loadHash(Context context){
        SharedPreferences prefs =  context.getSharedPreferences(PACKAGE_NAME,MODE_PRIVATE);
        hash = prefs.getString(PREF_KEY_USERNAME, null);
        return hash;
    }

    private void createNotificationChannel() {
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            @SuppressLint("WrongConstant") NotificationChannel channel1 = new NotificationChannel(
                    NOTIFICATION_CHANNEL_ANNOUNCEMENTS,
                    NOTIFICATION_CHANNEL_ANNOUNCEMENTS,
                    NotificationManager.IMPORTANCE_MAX

            );
            channel1.setDescription("College circulars notification");

            @SuppressLint("WrongConstant") NotificationChannel channel2 = new NotificationChannel(
                    NOTIFICATION_CHANNEL_GENERAL,
                    NOTIFICATION_CHANNEL_GENERAL,
                    NotificationManager.IMPORTANCE_MAX

            );
            channel2.setDescription("General notifications");
            @SuppressLint("WrongConstant") NotificationChannel channel3 = new NotificationChannel(
                    NOTIFICATION_CHANNEL_OTHERS,
                    NOTIFICATION_CHANNEL_OTHERS,
                    NotificationManager.IMPORTANCE_MAX

            );
            channel2.setDescription("Others");

            NotificationManager manager = getSystemService(NotificationManager.class);

            manager.createNotificationChannel(channel1);
            manager.createNotificationChannel(channel2);
            manager.createNotificationChannel(channel3);
        }
    }

    public static synchronized App getInstance() {
        return mInstance;
    }





    public boolean isConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager)(this.getSystemService(Context.CONNECTIVITY_SERVICE));
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
       return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static String getVersionName(Context context) {
        try {
            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            String version = info.versionName;
            version = version.replaceAll("[a-zA-Z]|-","");
            return version;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static  String encryptUsername(String value) throws
                        NoSuchPaddingException,
                        NoSuchAlgorithmException,
                        InvalidAlgorithmParameterException,
                        InvalidKeyException,
                        BadPaddingException,
                        IllegalBlockSizeException {
        SecretKeySpec secretKeySpec = new SecretKeySpec(KEY.getBytes(), ALGORITHM);
        Cipher cipher = Cipher.getInstance(MODE);
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, new IvParameterSpec(IV.getBytes()));
        byte[] values = cipher.doFinal(value.getBytes());
        return Base64.encodeToString(values, Base64.DEFAULT);

    }

    public static  String decryptUsername(String value) throws
                        NoSuchPaddingException,
                        NoSuchAlgorithmException,
                        InvalidAlgorithmParameterException,
                        InvalidKeyException,
                        BadPaddingException,
                        IllegalBlockSizeException {
        byte[] values = Base64.decode(value, Base64.DEFAULT);
        SecretKeySpec secretKeySpec = new SecretKeySpec(KEY.getBytes(), ALGORITHM);
        Cipher cipher = Cipher.getInstance(MODE);
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, new IvParameterSpec(IV.getBytes()));
        return new String(cipher.doFinal(values));
    }






}
