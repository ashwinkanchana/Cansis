package com.ashwinkanchana.cansis.utils;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.ashwinkanchana.cansis.R;
import com.ashwinkanchana.cansis.activities.LoginActivity;
import com.ashwinkanchana.cansis.activities.OtpActivity;
import com.github.sendgrid.SendGrid;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.util.Assert;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static com.ashwinkanchana.cansis.utils.Constants.CRED;
import static com.ashwinkanchana.cansis.utils.Constants.EMAIL_FROM;
import static com.ashwinkanchana.cansis.utils.Constants.EMAIL_FROM_NAME;
import static com.ashwinkanchana.cansis.utils.Constants.EMAIL_PLACEHOLDER_NAME;
import static com.ashwinkanchana.cansis.utils.Constants.EMAIL_PLACEHOLDER_OTP;
import static com.ashwinkanchana.cansis.utils.Constants.EMAIL_RESPONSE;
import static com.ashwinkanchana.cansis.utils.Constants.EMAIL_SUCCESS;
import static com.ashwinkanchana.cansis.utils.Constants.EMAIL_TEXT;
import static com.ashwinkanchana.cansis.utils.Constants.EMAIL_TITLE;
import static com.ashwinkanchana.cansis.utils.Constants.FS_COLLECTION_USER_STUDENT;
import static com.ashwinkanchana.cansis.utils.Constants.FS_KEY_OTP;
import static com.ashwinkanchana.cansis.utils.Constants.FS_KEY_OTP_COUNT;
import static com.ashwinkanchana.cansis.utils.Constants.HTML_TEMPLATE;
import static com.ashwinkanchana.cansis.utils.Constants.PACKAGE_NAME;

public class SendGridAsyncTask extends AsyncTask<String, String, String> {
    public static final String TAG = SendGridAsyncTask.class.getSimpleName();
    private static String otp;
    private String response;
    private JsonObject jsonObject;


    private WeakReference<OtpActivity> activityWeakReference;

    public SendGridAsyncTask (OtpActivity activity){
        activityWeakReference = new WeakReference<OtpActivity>(activity);
    }


    @Override
    protected String doInBackground(String... strings) {
        otp = generateOTP();
        String emailTo = strings[0];
        String phone = strings[1];
        String studentName = strings[2];
        FirebaseFirestore db;
        db= FirebaseFirestore.getInstance();
        DocumentReference userDocRef = db.collection(FS_COLLECTION_USER_STUDENT).document(phone);
        userDocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        long otpCount = (long)document.get(FS_KEY_OTP_COUNT);
                        Map<String, Object> data = new HashMap<>();
                        data.put(FS_KEY_OTP, otp);
                        data.put(FS_KEY_OTP_COUNT, otpCount+1);
                        userDocRef.set(data, SetOptions.merge());


                    } else {
                        Map<String, Object> data = new HashMap<>();
                        data.put(FS_KEY_OTP, otp);
                        data.put(FS_KEY_OTP_COUNT, 1);
                        userDocRef.set(data, SetOptions.merge());
                        userDocRef.set(data);
                    }
                } else {
                    publishProgress("{\"message\":\"Failure\"}");
                }
            }
        });
        response = sendOTP(emailTo,studentName);
        return response;

    }

    @Override
    protected void onProgressUpdate(String... strings) {
        super.onProgressUpdate(strings);
        OtpActivity activity = activityWeakReference.get();
        if (activity == null || activity.isFinishing()) {
            return;
        }
        jsonObject = new JsonParser().parse(strings[0]).getAsJsonObject();
        String response = jsonObject.get(EMAIL_RESPONSE).getAsString();
        Log.d(TAG, "onProgressUpdate: "+response);
        Intent intent = new Intent(activity,LoginActivity.class);
        Snackbar snackbar;
        RelativeLayout relativeLayout = activity.findViewById(R.id.otp_activity_layout);
        if (response.equals("error")) {
            snackbar = Snackbar.make(relativeLayout, R.string.limit_exceeded, Snackbar.LENGTH_LONG);
            goToPreviousActivity(snackbar,activity,intent);
        }else if(!response.equals(EMAIL_SUCCESS)) {
            snackbar = Snackbar.make(relativeLayout, R.string.somethin_went_wrg_try_again, Snackbar.LENGTH_LONG);
            goToPreviousActivity(snackbar,activity,intent);
        }

    }



    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);

        OtpActivity activity = activityWeakReference.get();
        if (activity == null || activity.isFinishing()) {
            return;
        }
        Log.d(TAG, "onPostExecute: "+s);
        jsonObject = new JsonParser().parse(s).getAsJsonObject();
        String response = jsonObject.get(EMAIL_RESPONSE).getAsString();
        Intent intent = new Intent(activity,LoginActivity.class);
        Snackbar snackbar;
        RelativeLayout relativeLayout = activity.findViewById(R.id.otp_activity_layout);
        if (response.equals("error")) {
            snackbar = Snackbar.make(relativeLayout, R.string.limit_exceeded, Snackbar.LENGTH_LONG);
            goToPreviousActivity(snackbar,activity,intent);
        }else if(!response.equals(EMAIL_SUCCESS)) {
            snackbar = Snackbar.make(relativeLayout, R.string.somethin_went_wrg_try_again, Snackbar.LENGTH_LONG);
            goToPreviousActivity(snackbar,activity,intent);
        }

    }

    private void goToPreviousActivity(Snackbar snackbar,OtpActivity activity,Intent intent){
        View snackBarView = snackbar.getView();
        snackBarView.setBackgroundColor(activity.getResources().getColor(R.color.colorRed));
        snackbar.setTextColor(activity.getResources().getColor(R.color.colorWhite));
        snackbar.show();
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                activity.startActivity(intent);
                activity.finish();
            }
        }, 2500);
    }

    private String sendOTP(String emailTo,String studentName){
        try {
            SendGrid sendGrid = new SendGrid(CRED, "SENDGRID_PASSWORD_GOES_HERE");
            sendGrid.addTo(emailTo);
            sendGrid.setFromName(EMAIL_FROM_NAME);
            sendGrid.setFrom(EMAIL_FROM);
            sendGrid.setSubject(EMAIL_TITLE);
            String html = HTML_TEMPLATE;
            html = html.replace(EMAIL_PLACEHOLDER_NAME,studentName);
            html = html.replace(EMAIL_PLACEHOLDER_OTP,otp);
            sendGrid.setText(EMAIL_TEXT);
            sendGrid.setHtml(html);
            String response = sendGrid.send();
            Log.d(TAG, "sendOTP: "+response);
            return response;
        } catch (Exception e){
           publishProgress("{\"message\":\"Failure\"}");
           e.printStackTrace();
        }
        return "{\"message\":\"Failure\"}";

    }
    private static String generateOTP() {
        String numbers = "123456789";
        Random random = new Random();
        char[] otp = new char[6];
        for (int i = 0; i < 6; i++) {
            otp[i] = numbers.charAt(random.nextInt(numbers.length()));
        }
        return new String(otp);
    }
}
