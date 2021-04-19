package com.ashwinkanchana.cansis.activities;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import com.android.volley.RequestQueue;
import com.ashwinkanchana.cansis.utils.App;
import com.ashwinkanchana.cansis.utils.VolleySingleton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import br.com.simplepass.loadingbutton.customViews.CircularProgressButton;
import com.ashwinkanchana.cansis.R;
import com.ashwinkanchana.cansis.fragments.ForgotPasswordDialog;

import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;


import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import static com.ashwinkanchana.cansis.utils.Constants.FS_ACCOUNT_UID;
import static com.ashwinkanchana.cansis.utils.Constants.FS_COLLECTION_STUDENT_DATA;
import static com.ashwinkanchana.cansis.utils.Constants.FS_COLLECTION_USER_PARENT;
import static com.ashwinkanchana.cansis.utils.Constants.FS_COLLECTION_USER_STUDENT;
import static com.ashwinkanchana.cansis.utils.Constants.FS_FCM_TOKEN;
import static com.ashwinkanchana.cansis.utils.Constants.FS_KEY_CREATED_AT;
import static com.ashwinkanchana.cansis.utils.Constants.FS_KEY_EMAIL;
import static com.ashwinkanchana.cansis.utils.Constants.FS_KEY_ENCODED_NAME;
import static com.ashwinkanchana.cansis.utils.Constants.FS_KEY_IMAGE;
import static com.ashwinkanchana.cansis.utils.Constants.FS_KEY_OTP_COUNT;
import static com.ashwinkanchana.cansis.utils.Constants.FS_KEY_PARENT_NAME;
import static com.ashwinkanchana.cansis.utils.Constants.FS_KEY_PARENT_PHONE;
import static com.ashwinkanchana.cansis.utils.Constants.FS_KEY_PHONE;
import static com.ashwinkanchana.cansis.utils.Constants.FS_KEY_SEMESTER_COUNT;
import static com.ashwinkanchana.cansis.utils.Constants.FS_KEY_STUDENT_FIRST_NAME;
import static com.ashwinkanchana.cansis.utils.Constants.FS_KEY_STUDENT_ID;
import static com.ashwinkanchana.cansis.utils.Constants.FS_KEY_STUDENT_NAME;
import static com.ashwinkanchana.cansis.utils.Constants.FS_KEY_STUDENT_PHONE;
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

public class LoginActivity extends AppCompatActivity  implements ForgotPasswordDialog.ExampleDialogListener{

    private static final String PARENT_LOGIN_URL = "PARENT_LOGIN_URL_HERE";
    private static final String STUDENT_LOGIN_URL = "STUDENT_LOGIN_URL_HERE";
    public static final String TAG = "loginActivity";
    private RequestQueue requestQueue;
    private static final String LOGIN_SUCCESS = "1";
    private int USER_TYPE;
    private String LOGIN_URL;
    private MaterialButtonToggleGroup toggleGroup;
    private MaterialButton parentBtn,studentBtn;
    private TextView loginTextView;
    private TextInputLayout textInputUsername;
    private TextInputLayout textInputPassword;
    private EditText usernameEditText;
    private EditText passwordEditText;
    private CircularProgressButton circularProgressButton;
    private Button forgotPasswordButton;
    private Button signUpButton;
    private LinearLayout loginLayout;
    private String username;
    private String password;
    private String fcmToken;
    private String studentPhone;
    private String usn;
    private String encodedName;
    private long semesterCount;
    private SharedPreferences prefs;
    private FirebaseFirestore db;
    private DocumentReference userDocReference;
    private DocumentReference dataDocReference;
    private ProgressDialog progressDialog;
    private String registeredEmail,studentName,profilePicUrl,studentID,studentFullname,parentFullname;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(AppCompatDelegate.getDefaultNightMode()==AppCompatDelegate.MODE_NIGHT_YES) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            setTheme(R.style.AppTheme);
        }
        setContentView(R.layout.activity_login);
        db = FirebaseFirestore.getInstance();
        requestQueue = VolleySingleton.getInstance(this).getRequestQueue();
        loginLayout = findViewById(R.id.login_layout);
        toggleGroup = findViewById(R.id.toggle_button_group);
        loginTextView =  findViewById(R.id.loginTextView);
        textInputUsername = findViewById(R.id.text_input_username);
        textInputPassword = findViewById(R.id.text_input_password);
        usernameEditText = findViewById(R.id.username_edittext);
        passwordEditText = findViewById(R.id.password_edit_text);
        circularProgressButton = findViewById(R.id.login_button);
        forgotPasswordButton = findViewById(R.id.forgot_password);
        signUpButton = findViewById(R.id.sign_up_button);
        circularProgressButton.saveInitialState();
        student();
        parentBtn = findViewById(R.id.parentButton);
        studentBtn = findViewById(R.id.studentButton);

        toggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (group.getCheckedButtonId() == R.id.parentButton && isChecked) {
                parents();
            }
            else if (group.getCheckedButtonId() == R.id.studentButton && isChecked) {
                student();
            }


            if(group.getCheckedButtonId()==-1){
                group.check(R.id.studentButton);
            }
        });
        prefs = this.getSharedPreferences(PACKAGE_NAME,Context.MODE_PRIVATE);


        findViewById(R.id.text_input_username).requestFocus();
        circularProgressButton.setOnClickListener(v -> login( ));
        passwordEditText.setOnKeyListener((view, keyCode, event) -> {
            if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                    (keyCode == KeyEvent.KEYCODE_ENTER)) {
                login();
                return true;
            }
            return false;
        });


        forgotPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showForgotPasswordDialogue();
            }
        });
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signUp();
            }
        });
        usernameEditText.requestFocus();
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    public void login() {
        username = usernameEditText.getText().toString().trim();
        password = passwordEditText.getText().toString().trim();
        Log.d(TAG, "login: "+username);

        if (!isConnected()) {
            textInputUsername.setError(null);
            textInputPassword.setError(null);
            Snackbar snackbar;
            snackbar = Snackbar.make(loginLayout, R.string.no_connection_snackbar, Snackbar.LENGTH_LONG);
            View snackBarView = snackbar.getView();
            snackBarView.setBackgroundColor(getResources().getColor(R.color.colorRed));
            snackbar.show();
        }

        else if (!validatePhone() | !validatePassword()) {
                    return;
        }

        else {
            circularProgressButton.startAnimation();
            sendLoginRequest();
        }
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(textInputPassword.getWindowToken(), 0);

    }

    public void parents(){

        Animation animation = AnimationUtils.loadAnimation(this, R.anim.anim_about_card_show);
        loginTextView.startAnimation(animation);

        clearEditText();
        loginTextView.setText(R.string.parent_textview);
        LOGIN_URL = PARENT_LOGIN_URL;
        usernameEditText.setInputType(InputType.TYPE_CLASS_PHONE);
        textInputUsername.setHint(getResources().getString(R.string.phone));
        usernameEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});
        forgotPasswordButton.setVisibility(View.VISIBLE);
        USER_TYPE = USER_PARENT;

    }

    public void student(){

        Animation animation = AnimationUtils.loadAnimation(this, R.anim.anim_about_card_show);
        loginTextView.startAnimation(animation);


        clearEditText();
        loginTextView.setText(R.string.student_textview);
        LOGIN_URL = STUDENT_LOGIN_URL;
        usernameEditText.setInputType(InputType.TYPE_CLASS_PHONE);
        textInputUsername.setHint(getResources().getString(R.string.phone));
        usernameEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});
        forgotPasswordButton.setVisibility(View.VISIBLE);
        USER_TYPE = USER_STUDENT;
    }
    private void disableButtons(){
        forgotPasswordButton.setEnabled(false);
        signUpButton.setEnabled(false);
        parentBtn.setEnabled(false);
        studentBtn.setEnabled(false);
        usernameEditText.setEnabled(false);
        passwordEditText.setEnabled(false);
    }
    private void enableButtons(){
        parentBtn.setEnabled(true);
        studentBtn.setEnabled(true);
        signUpButton.setEnabled(true);
        forgotPasswordButton.setEnabled(true);
        usernameEditText.setEnabled(true);
        passwordEditText.setEnabled(true);
    }

    public void sendLoginRequest() {
         disableButtons();
         StringRequest stringRequest = new StringRequest(Request.Method.POST, LOGIN_URL,
                 response -> {
            JsonObject jsonObject = new JsonParser().parse(response).getAsJsonObject();
            String responseCode = String.valueOf(jsonObject.get("success"));
            String responseMessage = String.valueOf(jsonObject.get("message"));
            if (responseCode.equals(LOGIN_SUCCESS)) {
                onLoginSuccess();
            }
            else {
                resetLoadingButton();
                enableButtons();
                Snackbar.make(loginLayout, (responseMessage.substring(1, responseMessage.length() - 1)), Snackbar.LENGTH_LONG).show();
            }},
                 error -> {
                     parentBtn.setEnabled(true);
                     studentBtn.setEnabled(true);
                     signUpButton.setEnabled(true);
                     forgotPasswordButton.setEnabled(true);
                     usernameEditText.setEnabled(true);
                     passwordEditText.setEnabled(true);
            circularProgressButton.startMorphRevertAnimation();



            if(isConnected()){
                enableButtons();
                resetLoadingButton();
                Snackbar.make(loginLayout, R.string.unknown_error_snackbar, Snackbar.LENGTH_LONG).show();
            }
})

        {
            @Override
            protected Map<String, String> getParams()
            { Map<String, String> params = new HashMap<String, String>();
                params.put("username",username);
                params.put("password",password);
                return params;
            }

        };
       requestQueue.add(stringRequest);
    }

    @Override
    protected void onStart() {
        super.onStart();
        getFcmToken();
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            InputMethodManager inputMethodManager2 = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager2.hideSoftInputFromWindow(Objects.requireNonNull(getCurrentFocus()).getWindowToken(), 0);
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        circularProgressButton.dispose();
    }

    private boolean isConnected() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public void clearEditText(){
        textInputUsername.setError(null);
        textInputPassword.setError(null);
    }

    private boolean validatePhone() {
        if (username.isEmpty()) {
            textInputUsername.setError(getResources().getString(R.string.error_empty_field));
            return false;
        } else if (username.length() < 10 && USER_TYPE == 3) {
            textInputUsername.setError(null);
            return true;
        } else if (username.length() < 10) {
            textInputUsername.setError(getResources().getString(R.string.error_invalid_phone));
            return false;
        } else {
            textInputUsername.setError(null);
            return true;
        }

    }

    private boolean validatePassword() {
        if (password.isEmpty()) {
            textInputPassword.setError(getResources().getString(R.string.error_empty_field));
            return false;
        } else {
            textInputPassword.setError(null);
            return true;
        }

    }

    public void showForgotPasswordDialogue(){
            ForgotPasswordDialog forgotPasswordDialog = new ForgotPasswordDialog();
            forgotPasswordDialog.show(getSupportFragmentManager(), "forgot_password");
    }

    public void resetLoadingButton() {
        circularProgressButton.startMorphRevertAnimation();

    }


    private void signUp(){
        Intent intent = new Intent(this, RegistrationActivity.class);
        startActivity(intent);
    }

    private void signInAnonymously(String displayName) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mAuth.signInAnonymously()
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            storeUser(username);
                        } else {
                            enableButtons();
                            resetLoadingButton();
                            Snackbar.make(loginLayout, R.string.try_again_snackbar, Snackbar.LENGTH_LONG).show();
                        }


                    }
                });

    }

    private void getFcmToken(){
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            return;
                        }
                        fcmToken = task.getResult().getToken();

                    }

                });
    }


    private void createTempUser(String email,String phone){
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mAuth.signInAnonymously()
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName("forgotPassword")
                                    .build();

                            verifyEmailPhone(email,phone);

                        } else {
                            progressDialog.dismiss();
                            Snackbar.make(loginLayout, R.string.unknown_error_snackbar, Snackbar.LENGTH_LONG).show();
                        }


                    }
                });
    }

    public void forgotPassword(String email,String phone) {
        createTempUser(email,phone);
    }

    private void verifyEmailPhone(String email,String phone){
    DocumentReference docRef = db.collection(FS_COLLECTION_STUDENT_DATA).document(phone);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
        @Override
        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    registeredEmail = (String)document.get(FS_KEY_EMAIL);
                    studentName = (String)document.get(FS_KEY_STUDENT_FIRST_NAME);

                    studentFullname = (String)document.get(FS_KEY_STUDENT_NAME);
                    parentFullname = (String)document.get(FS_KEY_PARENT_NAME);

                    if(registeredEmail != null && !registeredEmail.isEmpty()){
                        if(email.equals(registeredEmail)) {
                            progressDialog.dismiss();
                            Intent intent = new Intent(getApplicationContext(), OtpActivity.class);
                            intent.putExtra(FS_KEY_EMAIL, registeredEmail);
                            intent.putExtra(FS_KEY_PHONE, phone);
                            intent.putExtra(FS_KEY_STUDENT_FIRST_NAME, studentName);
                            intent.putExtra(FS_KEY_STUDENT_NAME, studentFullname);
                            intent.putExtra(FS_KEY_PARENT_NAME, parentFullname);

                            startActivity(intent);
                        }else {
                            progressDialog.dismiss();
                            Snackbar.make(loginLayout, R.string.email_mismatch_snackbar, Snackbar.LENGTH_LONG).show();
                        }
                    }else{
                        //no email
                        progressDialog.dismiss();
                        Snackbar.make(loginLayout, R.string.unknown_error_snackbar, Snackbar.LENGTH_LONG).show();
                    }
                } else {
                    //no document
                    progressDialog.dismiss();
                    Snackbar.make(loginLayout, R.string.details_not_found, Snackbar.LENGTH_LONG).show();
                }
            } else {
                //fetch failed
                Log.d(TAG, "get failed with ", task.getException());
                progressDialog.dismiss();
                Snackbar.make(loginLayout, R.string.unknown_error_snackbar, Snackbar.LENGTH_LONG).show();
            }
        }
    });
    }

    @Override public void onBackPressed () {
        finish();
    }

    public void onLoginSuccess() {
        signInAnonymously(username);
        //storeUser(username);
    }




    private void storeUser(String username) {
        if(USER_TYPE==USER_STUDENT){
            userDocReference = db.collection(FS_COLLECTION_USER_STUDENT)
                    .document(username);

        }else  if(USER_TYPE==USER_PARENT){
            userDocReference = db.collection(FS_COLLECTION_USER_PARENT)
                    .document(username);

        }
        userDocReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        encodedName = (String)document.get(FS_KEY_ENCODED_NAME);
                        if(document.get(FS_KEY_SEMESTER_COUNT)!=null)
                            semesterCount = (long)document.get(FS_KEY_SEMESTER_COUNT);

                        Map<String,Object> user = new HashMap<>();
                        if(USER_TYPE==USER_PARENT) {
                            studentPhone = (String) document.get(FS_KEY_STUDENT_PHONE);
                            user.put(FS_KEY_STUDENT_PHONE,studentPhone);

                        }

                        if(USER_TYPE==USER_STUDENT){
                            loginComplete(username);

                        }else  if(USER_TYPE==USER_PARENT){
                            loginComplete(studentPhone);

                        }

                        user.put(FS_FCM_TOKEN,fcmToken);
                        user.put(FS_KEY_PHONE,username);
                        if(FirebaseAuth.getInstance().getCurrentUser() != null){
                            user.put(FS_ACCOUNT_UID, FirebaseAuth.getInstance().getCurrentUser().getUid());
                        }
                        userDocReference.set(user,SetOptions.merge());
                    } else {
                        newUser(username);
                    }
                }
                else{
                    enableButtons();
                    resetLoadingButton();
                    Snackbar.make(loginLayout, R.string.try_again_snackbar, Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }


    private void newUser(String username){
        if(USER_TYPE==USER_STUDENT){
            createStudentUser(username);
        }else if(USER_TYPE==USER_PARENT){
              db.collection(FS_COLLECTION_STUDENT_DATA)
                    .whereEqualTo(FS_KEY_PARENT_PHONE, username)
                    .limit(1)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    if(String.valueOf(document.get(FS_KEY_PARENT_PHONE)).equals(username)) {
                                        studentPhone = (String) document.getId();
                                        createParentUser(studentPhone,username);
                                    }
                                }


                            } else {
                                enableButtons();
                                resetLoadingButton();
                                Snackbar.make(loginLayout,R.string.something_went_wrg,Snackbar.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
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
                        Snackbar.make(loginLayout,R.string.something_went_wrg,Snackbar.LENGTH_SHORT).show();
                        resetLoadingButton();
                        enableButtons();
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                    Snackbar.make(loginLayout,R.string.something_went_wrg,Snackbar.LENGTH_SHORT).show();
                    resetLoadingButton();
                    enableButtons();
                }
            }
        });

    }


    private void createStudentUser(String username){
        userDocReference = db.collection(FS_COLLECTION_USER_STUDENT)
                .document(username);

        Map<String,Object> user = new HashMap<>();
        user.put(FS_KEY_PHONE,username);
        user.put(FS_KEY_CREATED_AT,Timestamp.now());
        user.put(FS_FCM_TOKEN,fcmToken);
        if(FirebaseAuth.getInstance().getCurrentUser() != null){
            user.put(FS_ACCOUNT_UID, FirebaseAuth.getInstance().getCurrentUser().getUid());
        }
        user.put(FS_KEY_OTP_COUNT,0);
        userDocReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "DocumentSnapshot successfully updated!");
                loginComplete(username);
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error updating document", e);
                        Snackbar.make(loginLayout,R.string.something_went_wrg,Snackbar.LENGTH_SHORT).show();
                        enableButtons();
                        resetLoadingButton();
                    }
                });
    }

    private void createParentUser(String studentPhone,String username){
        userDocReference = db.collection(FS_COLLECTION_USER_PARENT)
                .document(username);
        Map<String,Object> user = new HashMap<>();
        user.put(FS_KEY_PHONE,username);
        user.put(FS_KEY_STUDENT_PHONE,studentPhone);
        user.put(FS_KEY_CREATED_AT,Timestamp.now());
        user.put(FS_FCM_TOKEN,fcmToken);
        if(FirebaseAuth.getInstance().getCurrentUser() != null){
            user.put(FS_ACCOUNT_UID, FirebaseAuth.getInstance().getCurrentUser().getUid());
        }
        user.put(FS_KEY_OTP_COUNT,0);
        userDocReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "DocumentSnapshot successfully updated!");
                loginComplete(studentPhone);
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error updating document", e);
                        Snackbar.make(loginLayout,R.string.something_went_wrg,Snackbar.LENGTH_SHORT).show();
                    }
                });

    }
}


