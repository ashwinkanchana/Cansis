package com.ashwinkanchana.cansis.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.ashwinkanchana.cansis.R;
import com.ashwinkanchana.cansis.activities.LoginActivity;
import com.ashwinkanchana.cansis.activities.ParentActivity;
import com.ashwinkanchana.cansis.activities.StudentActivity;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;


import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static android.content.ContentValues.TAG;
import static com.ashwinkanchana.cansis.utils.Constants.FS_COLLECTION_USER_PARENT;
import static com.ashwinkanchana.cansis.utils.Constants.FS_COLLECTION_USER_STUDENT;
import static com.ashwinkanchana.cansis.utils.Constants.FS_FCM_TOKEN;
import static com.ashwinkanchana.cansis.utils.Constants.NOTIFICATION_CHANNEL_ANNOUNCEMENTS;
import static com.ashwinkanchana.cansis.utils.Constants.PREF_KEY_USERNAME;
import static com.ashwinkanchana.cansis.utils.Constants.PREF_KEY_USER_TYPE;
import static com.ashwinkanchana.cansis.utils.Constants.USER_PARENT;
import static com.ashwinkanchana.cansis.utils.Constants.USER_STUDENT;
import static com.ashwinkanchana.cansis.utils.App.PACKAGE_NAME;


public class MessagingService extends FirebaseMessagingService {
    private int USER_TYPE;
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        SharedPreferences prefs = getSharedPreferences(PACKAGE_NAME,MODE_PRIVATE);
        USER_TYPE = prefs.getInt(PREF_KEY_USER_TYPE,USER_STUDENT);
        showNotification(remoteMessage);
    }

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
         FirebaseAuth  mAuth = FirebaseAuth.getInstance();
         FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            SharedPreferences prefs = getSharedPreferences(PACKAGE_NAME,MODE_PRIVATE);
            String  username = prefs.getString(PREF_KEY_USERNAME,"1111");
            USER_TYPE = prefs.getInt(PREF_KEY_USER_TYPE,USER_STUDENT);
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference userDocReference;
            if(USER_TYPE==USER_STUDENT){
                userDocReference = db.collection(FS_COLLECTION_USER_STUDENT)
                        .document(username);
            }else {
                userDocReference = db.collection(FS_COLLECTION_USER_PARENT)
                        .document(username);
            }
            Map<String,Object> userData = new HashMap<>();
            userData.put(FS_FCM_TOKEN,s);
            userDocReference.set(userData, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d(TAG,"FCM TOKEN updated!");
                }
            });
        }

    }

    public void showNotification(RemoteMessage remoteMessage){

        String title = remoteMessage.getData().get("title");
        String body = remoteMessage.getData().get("body");
        Intent intent;
        if(USER_TYPE==USER_STUDENT)
            intent = new Intent(this, StudentActivity.class);
        else if ((USER_TYPE==USER_PARENT))
            intent = new Intent(this, ParentActivity.class);
        else
            intent = new Intent(this, LoginActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                1,intent,0);
        Bitmap largeIcon = BitmapFactory.decodeResource(this.getResources(), R.drawable.cec_logo_transparent_small);
        Notification notification = new NotificationCompat.Builder(
                this,NOTIFICATION_CHANNEL_ANNOUNCEMENTS)
                .setSmallIcon(R.drawable.marks)
                .setContentTitle(title)
                .setContentText(body)
                .setLargeIcon(largeIcon)
                .setVibrate(new long[] { 1000, 500, 800})
                .setStyle(new NotificationCompat.BigTextStyle()
                        .setBigContentTitle(title)
                        .bigText(body)
                        .setSummaryText("Circular"))
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build();
        NotificationManagerCompat manager =  NotificationManagerCompat.from(this);
        Random random = new Random();
        manager.notify(random.nextInt(),notification);
    }





}
