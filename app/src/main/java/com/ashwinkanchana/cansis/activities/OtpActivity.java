package com.ashwinkanchana.cansis.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import com.ashwinkanchana.cansis.R;
import com.ashwinkanchana.cansis.stepperform.FormOtpStep;
import com.ashwinkanchana.cansis.utils.App;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import ernestoyaquello.com.verticalstepperform.VerticalStepperFormView;
import ernestoyaquello.com.verticalstepperform.listener.StepperFormListener;

import static com.ashwinkanchana.cansis.utils.Constants.FS_COLLECTION_STUDENT_DATA;
import static com.ashwinkanchana.cansis.utils.Constants.FS_KEY_EMAIL;
import static com.ashwinkanchana.cansis.utils.Constants.FS_KEY_ENCODED_NAME;
import static com.ashwinkanchana.cansis.utils.Constants.FS_KEY_IMAGE;
import static com.ashwinkanchana.cansis.utils.Constants.FS_KEY_PARENT_NAME;
import static com.ashwinkanchana.cansis.utils.Constants.FS_KEY_PHONE;
import static com.ashwinkanchana.cansis.utils.Constants.FS_KEY_SEMESTER_COUNT;
import static com.ashwinkanchana.cansis.utils.Constants.FS_KEY_STUDENT_FIRST_NAME;
import static com.ashwinkanchana.cansis.utils.Constants.FS_KEY_STUDENT_ID;
import static com.ashwinkanchana.cansis.utils.Constants.FS_KEY_STUDENT_NAME;
import static com.ashwinkanchana.cansis.utils.Constants.FS_KEY_USN;
import static com.ashwinkanchana.cansis.utils.Constants.PREF_KEY_BRANCH_SHORT;
import static com.ashwinkanchana.cansis.utils.Constants.PREF_KEY_DARK_MODE;
import static com.ashwinkanchana.cansis.utils.Constants.PREF_KEY_DEFAULT_SEMESTER;
import static com.ashwinkanchana.cansis.utils.Constants.PREF_KEY_ENCODED_NAME;
import static com.ashwinkanchana.cansis.utils.Constants.PREF_KEY_IS_FIRST_TIME;
import static com.ashwinkanchana.cansis.utils.Constants.PREF_KEY_IS_LOGGED_IN;
import static com.ashwinkanchana.cansis.utils.Constants.PREF_KEY_PROFILE_PIC_URL;
import static com.ashwinkanchana.cansis.utils.Constants.PREF_KEY_STUDENT_ID;
import static com.ashwinkanchana.cansis.utils.Constants.PREF_KEY_STUDENT_NAME;
import static com.ashwinkanchana.cansis.utils.Constants.PREF_KEY_USERNAME;
import static com.ashwinkanchana.cansis.utils.Constants.PREF_KEY_USER_TYPE;
import static com.ashwinkanchana.cansis.utils.Constants.PREF_KEY_USN;
import static com.ashwinkanchana.cansis.utils.Constants.USER_PARENT;
import static com.ashwinkanchana.cansis.utils.Constants.USER_STUDENT;
import static com.ashwinkanchana.cansis.utils.App.PACKAGE_NAME;

public class OtpActivity extends AppCompatActivity implements StepperFormListener, DialogInterface.OnClickListener {
    private static final String TAG = OtpActivity.class.getSimpleName();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private VerticalStepperFormView verticalStepperForm;
    private SharedPreferences prefs;
    private com.ashwinkanchana.cansis.stepperform.FormCaptchaStep captchaStep;
    private com.ashwinkanchana.cansis.stepperform.FormOtpStep otpStep;
    private com.ashwinkanchana.cansis.stepperform.FormChooseUserStep chooseUserStep;
    private String email;
    private String phone;
    private String studentFirstName;
    private String studentName;
    private String parentName;
    private String studentID,usn,encodedName,profilePicUrl;
    private long semesterCount;
    private MaterialToolbar toolbar;
    private DocumentReference dataDocReference;
    private int USER_TYPE;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp);
        email = getIntent().getStringExtra(FS_KEY_EMAIL);
        phone = getIntent().getStringExtra(FS_KEY_PHONE);
        studentFirstName = getIntent().getStringExtra(FS_KEY_STUDENT_FIRST_NAME);
        studentName = getIntent().getStringExtra(FS_KEY_STUDENT_NAME);
        parentName = getIntent().getStringExtra(FS_KEY_PARENT_NAME);
        toolbar = findViewById(R.id.toolbar);
        prefs = this.getSharedPreferences(PACKAGE_NAME, Context.MODE_PRIVATE);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        String[] stepTitles = getResources().getStringArray(R.array.steps_titles);
        String[] stepSubTitles = getResources().getStringArray(R.array.steps_subtitles);

        captchaStep = new com.ashwinkanchana.cansis.stepperform.FormCaptchaStep(
                stepTitles[0],this,
                email,phone,studentFirstName);
        otpStep = new FormOtpStep(stepTitles[1],email,phone);

        chooseUserStep = new com.ashwinkanchana.cansis.stepperform.FormChooseUserStep(stepTitles[2],studentName,parentName);

        verticalStepperForm = findViewById(R.id.stepper_form);
        verticalStepperForm.setup( this, captchaStep, otpStep, chooseUserStep).init();



    }







    @Override
    public void onClick(DialogInterface dialog, int which) {

    }

    @Override
    public void onCompletedForm() {
        Snackbar.make(findViewById(R.id.otp_activity_layout),"Finishing up...",Snackbar.LENGTH_LONG).show();
        USER_TYPE = (Integer)chooseUserStep.getStepData();

        if(USER_TYPE == USER_STUDENT){
            loginComplete(phone);
        }else if(USER_TYPE == USER_PARENT){
            loginComplete(phone);
        }
    }

    @Override
    public void onCancelledForm() {

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }


    @Override
    public void onBackPressed() {
        showConformationDialog();
    }


    private void showConformationDialog(){
            DialogInterface.OnClickListener dialogClickListener = (DialogInterface dialog, int which) -> {
                if (which == DialogInterface.BUTTON_POSITIVE) {
                    Intent intent = new Intent(this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                }
            };
            new MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.form_discard_question)
                    .setMessage(R.string.form_info_will_be_lost)
                    .setPositiveButton(R.string.form_discard, dialogClickListener)
                    .setNegativeButton(R.string.form_discard_cancel,null)
                    .show();
    }


    private void loginComplete(String username){
        Log.d(TAG, "loginComplete: "+username);
        SharedPreferences.Editor editor = prefs.edit();
        dataDocReference = db.collection(FS_COLLECTION_STUDENT_DATA).document(username);
        dataDocReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        profilePicUrl = (String)document.get(FS_KEY_IMAGE);
                        studentName =(String)document.get(FS_KEY_STUDENT_NAME);
                        studentID = (String)document.get(FS_KEY_STUDENT_ID);
                        usn = (String)document.get(FS_KEY_USN);
                        encodedName = (String)document.get(FS_KEY_ENCODED_NAME);
                        if(document.get(FS_KEY_SEMESTER_COUNT)!=null)
                            semesterCount = (long)document.get(FS_KEY_SEMESTER_COUNT);
                        try {
                            editor.putString(PREF_KEY_USERNAME, App.encryptUsername(username));
                        } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidAlgorithmParameterException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
                            e.printStackTrace();
                        }
                        editor.putString(PREF_KEY_PROFILE_PIC_URL,profilePicUrl);
                        editor.putString(PREF_KEY_STUDENT_NAME,studentName);
                        editor.putString(PREF_KEY_BRANCH_SHORT,null);
                        editor.putString(PREF_KEY_USN,usn);
                        editor.putString(PREF_KEY_ENCODED_NAME,encodedName);
                        editor.putString(PREF_KEY_STUDENT_ID,studentID);
                        editor.putBoolean(PREF_KEY_IS_LOGGED_IN, true);
                        editor.putBoolean(PREF_KEY_IS_FIRST_TIME, true);
                        editor.putBoolean(PREF_KEY_DARK_MODE,false);
                        editor.putInt(PREF_KEY_USER_TYPE, USER_TYPE);
                        editor.putInt(PREF_KEY_DEFAULT_SEMESTER, -1).apply();

                        editor.apply();

                        App.initUsername(getApplicationContext());

                        if(USER_TYPE == USER_STUDENT) {
                            Intent intent_student = new Intent(getApplicationContext(), StudentActivity.class);
                            startActivity(intent_student);
                            finish();
                        }
                        else if(USER_TYPE == USER_PARENT) {
                            Intent intent_parent = new Intent(getApplicationContext(), ParentActivity.class);
                            startActivity(intent_parent);
                            finish();
                        }

                    } else {
                        Log.d(TAG, "No such document");
                        Snackbar.make(findViewById(R.id.otp_activity_layout),R.string.something_went_wrg,Snackbar.LENGTH_SHORT).show();
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                    Snackbar.make(findViewById(R.id.otp_activity_layout),R.string.something_went_wrg,Snackbar.LENGTH_SHORT).show();
                }
            }
        });

    }

}
