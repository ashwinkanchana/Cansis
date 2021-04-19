package com.ashwinkanchana.cansis.stepperform;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.ashwinkanchana.cansis.R;
import com.ashwinkanchana.cansis.activities.OtpActivity;
import com.ashwinkanchana.cansis.utils.SendGridAsyncTask;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.safetynet.SafetyNet;
import com.google.android.gms.safetynet.SafetyNetApi;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.chip.Chip;
import com.google.api.LogDescriptor;

import ernestoyaquello.com.verticalstepperform.Step;

import static com.ashwinkanchana.cansis.utils.Constants.RECAPTCHA_SITE_KEY;

public class FormCaptchaStep extends Step {
    private static final String TAG = FormCaptchaStep.class.getSimpleName();
    private boolean captchaCompleted;
    private Chip chip;
    private Context context;
    private String email;
    private String phone;
    private String studentFirstName;
    private LinearLayout progressLayout;
    private TextView errorText;



    public FormCaptchaStep(String title,
                           Context OtpActivity,
                           String email,
                           String phone,
                           String studentFirstName) {
        super(title);
        this.captchaCompleted = false;
        this.context = OtpActivity;
        this.email = email;
        this.phone = phone;
        this.studentFirstName = studentFirstName;

    }

    @Override
    protected void onStepOpened(boolean animated) {

    }
    @Override
    protected void onStepClosed(boolean animated) {

    }
    @Override
    protected void onStepMarkedAsCompleted(boolean animated) {

    }
    @Override
    protected void onStepMarkedAsUncompleted(boolean animated) {

    }


    @Override
    public Object getStepData() {
        return captchaCompleted;
    }

    @Override
    public String getStepDataAsHumanReadableString() {
        return null;
    }

    @Override
    public void restoreStepData(Object data) {

    }

    @Override
    protected IsDataValid isStepDataValid(Object stepData) {
        if((boolean) stepData)
            return new IsDataValid(true);
        else return new IsDataValid(false);
    }

    @Override
    protected View createStepContentLayout() {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View captchaStep = inflater.inflate(R.layout.step_captcha, null, false);
        chip = captchaStep.findViewById(R.id.captcha_chip);
        progressLayout = captchaStep.findViewById(R.id.progress_otp);
        chip.setCheckable(false);
        errorText = captchaStep.findViewById(R.id.error_text_captcha);
        errorText.setText(R.string.captcha_welcome);
        errorText.setTextColor(getContext().getResources().getColor(R.color.colorGrey));
        chip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCaptcha();
            }
        });
        return captchaStep;
    }




    public void showCaptcha(){


        SafetyNet.getClient(getContext()).verifyWithRecaptcha(RECAPTCHA_SITE_KEY)
                .addOnSuccessListener(recaptchaTokenResponse -> {
                    // successful.
                    String userResponseToken = recaptchaTokenResponse.getTokenResult();
                    if (!userResponseToken.isEmpty()) {

                        captchaCompleted = true;
                        chip.setCheckable(true);
                        chip.setChecked(true);
                        chip.setEnabled(false);
                        chip.setText("Done");

                        errorText.setText("Captcha completed");
                        errorText.setTextColor(getContext().getResources().getColor(R.color.colorGreen));

                        progressLayout.setVisibility(View.VISIBLE);

                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.post(() -> {
                            sendMailUsingSendGrid(email, phone, studentFirstName, context);
                            getFormView().markOpenStepAsCompleted(true);
                            getFormView().goToNextStep(true);
                        });

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (e instanceof ApiException) {
                            // An error occurred when communicating with the
                            // reCAPTCHA service. Refer to the status code to
                            // handle the error appropriately.
                            ApiException apiException = (ApiException) e;
                            int statusCode = apiException.getStatusCode();
                            Log.e(TAG, "Error: " + CommonStatusCodes
                                    .getStatusCodeString(statusCode));

                            errorText.setText("Captcha incomplete - "+e.getMessage());
                            errorText.setTextColor(getContext().getResources().getColor(R.color.colorRed));
                        } else {
                            // A different, unknown type of error occurred.
                            Log.e(TAG, "Error: " + e.getMessage());

                            errorText.setText("Captcha incomplete - "+e.getMessage());
                            errorText.setTextColor(getContext().getResources().getColor(R.color.colorRed));
                        }
                    }
                });
    }


    private void sendMailUsingSendGrid(String emailTo,String phone,String studentName,Context context){
        SendGridAsyncTask email = new SendGridAsyncTask((OtpActivity) context);
        try{

            email.execute(emailTo, phone, studentName).get();

        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
