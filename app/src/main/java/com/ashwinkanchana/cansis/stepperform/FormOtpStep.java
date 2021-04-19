package com.ashwinkanchana.cansis.stepperform;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.ashwinkanchana.cansis.R;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.SetOptions;
import com.mukesh.OnOtpCompletionListener;
import com.mukesh.OtpView;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import ernestoyaquello.com.verticalstepperform.Step;

import static android.content.Context.INPUT_METHOD_SERVICE;
import static com.ashwinkanchana.cansis.activities.LoginActivity.TAG;
import static com.ashwinkanchana.cansis.utils.Constants.FS_ACCOUNT_UID;
import static com.ashwinkanchana.cansis.utils.Constants.FS_COLLECTION_USER_STUDENT;
import static com.ashwinkanchana.cansis.utils.Constants.FS_KEY_OTP;

public class FormOtpStep extends Step<String> implements OnOtpCompletionListener {
    private OtpView otpView;
    private String email;
    private String phone;
    private String inputOtp;
    private TextView errorText;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String requiredOTP;
    private RelativeLayout otpLayout;

    public FormOtpStep(String title,String email,String phone) {
        super(title);
        this.email = email;
        this.phone = phone;
        this.inputOtp = "0";
        getOTP();
    }

    @Override
    public String getStepData() {
       return Objects.requireNonNull(otpView.getText()).toString();
    }

    @Override
    public String getStepDataAsHumanReadableString() {
        return null;
    }

    @Override
    public void restoreStepData(String data) {

    }

    @Override
    protected IsDataValid isStepDataValid(String stepData) {
        if (stepData.length() < 6) {
            errorText.setText("");
            return new IsDataValid(false);
        } else {
            if(verifyOTP(stepData)) {
                errorText.setTextColor(getContext().getResources().getColor(R.color.colorGreen));
                errorText.setText("Successfully verified");
                try {
                    InputMethodManager inputMethodManager2 = (InputMethodManager) getContext().getSystemService(INPUT_METHOD_SERVICE);
                    if (inputMethodManager2 != null) {
                        inputMethodManager2.hideSoftInputFromWindow(getFormView().getWindowToken(), 0);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
                return new IsDataValid(true);
            }else{
                errorText.setTextColor(getContext().getResources().getColor(R.color.colorRed));
                errorText.setText("Invalid code");
                return new IsDataValid(false);
            }
        }
    }


    @Override
    protected View createStepContentLayout() {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View otpStep = inflater.inflate(R.layout.step_otp, null, false);
        otpView = otpStep.findViewById(R.id.otp_view);
        TextView emailTextView = otpStep.findViewById(R.id.email_text);
        errorText = otpStep.findViewById(R.id.error_text);
        otpLayout = otpStep.findViewById(R.id.otp_layout);
        otpLayout.setVisibility(View.INVISIBLE);
        emailTextView.setText(email);
        otpView.setOtpCompletionListener(this);
        otpView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                markAsCompletedOrUncompleted(true);
            }

            @Override
            public void afterTextChanged(Editable s) {
                verifyOTP(s.toString());

            }
        });
        otpView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                //getFormView().goToNextStep(true);
                return false;
            }
        });
        otpView.requestFocus();
        return otpStep;
    }

    @Override
    protected void onStepOpened(boolean animated) {

    }

    @Override
    protected void onStepClosed(boolean animated) {
        resetOTP();
    }

    @Override
    protected void onStepMarkedAsCompleted(boolean animated) {

    }

    @Override
    protected void onStepMarkedAsUncompleted(boolean animated) {

    }

    @Override
    public void onOtpCompleted(String otp) {
        if(isStepDataValid(otp).isValid()){
            inputOtp = otp;

        }

    }


    private boolean verifyOTP(String inputOtp){
        Log.d(TAG, "verifyOTP: "+requiredOTP);
        if(inputOtp.equals(requiredOTP)){
            getFormView().markOpenStepAsCompleted(true);
            //getFormView().goToNextStep(true);

            return true;
        }
        return false;
    }



    private void getOTP(){
        final DocumentReference docRef = db.collection(FS_COLLECTION_USER_STUDENT).document(phone);
        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    return;
                }

                if (snapshot != null && snapshot.exists()) {
                    Log.d(TAG, "Current data: " + snapshot.getData());
                    requiredOTP = (String)snapshot.get(FS_KEY_OTP);
                    otpLayout.setVisibility(View.VISIBLE);
                    Log.d(TAG, "onEvent: "+requiredOTP);
                } else {
                    Log.d(TAG, "Current data: null");
                    Snackbar.make(getContentLayout(),R.string.something_went_wrg,Snackbar.LENGTH_LONG).show();
                }
            }
        });

    }


    private void resetOTP(){
        Map<String, Object> data = new HashMap<>();
        if(FirebaseAuth.getInstance().getCurrentUser() != null){
            data.put(FS_ACCOUNT_UID, FirebaseAuth.getInstance().getCurrentUser().getUid());
        }
        data.put(FS_KEY_OTP, null);
        final DocumentReference docRef = db.collection(FS_COLLECTION_USER_STUDENT).document(phone);
        docRef.set(data, SetOptions.merge());
    }

}
