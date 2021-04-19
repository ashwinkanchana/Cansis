package com.ashwinkanchana.cansis.fragments;

import android.app.Dialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Objects;

import com.ashwinkanchana.cansis.R;

public class ForgotPasswordDialog extends DialogFragment {

    private EditText emailEditText;
    private TextInputLayout inputLayout;
    private EditText phoneEditText;
    private TextInputLayout phoneLayout;
    private ExampleDialogListener listener;
    private String email,phone;


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        LayoutInflater inflater = Objects.requireNonNull(getActivity()).getLayoutInflater();
        View view = inflater.inflate(R.layout.layout_forgot_password, null);
        inputLayout = view.findViewById(R.id.layout_input_forgot_password);
        phoneLayout = view.findViewById(R.id.phone_edittext_layout);
        emailEditText = view.findViewById(R.id.forgot_password_edittext);
        phoneEditText = view.findViewById(R.id.forgot_pass_phone);

        final AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setView(view)
                //.setTitle(R.string.forgot_password_heading)
                //.setMessage(R.string.enter_email)
                .setPositiveButton(R.string.confirm, null)
                .setNegativeButton(R.string.cancel, null)
                .create();

        dialog.setOnShowListener(dialogInterface -> {

            Button button = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            button.setOnClickListener(view1 -> {
                email = emailEditText.getText().toString().trim();
                phone = phoneEditText.getText().toString().trim();
                if(!isConnected()) {
                    Snackbar snackbar;
                    snackbar = Snackbar.make(inputLayout, R.string.no_connection_snackbar, Snackbar.LENGTH_SHORT);
                    View snackBarView = snackbar.getView();
                    snackBarView.setBackgroundColor(getResources().getColor(R.color.colorRed));
                    snackBarView.setTranslationY(-(convertDpToPixel(Objects.requireNonNull(getContext()))));
                    snackbar.show();

                } else if (!validateEmail()| !validatePhone()) {
                    //Do nothing
                }  else {

                    listener.forgotPassword(email,phone);
                    dialog.dismiss();

                }

            });
        });
        phoneEditText.requestFocus();
        return dialog;
    }





    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            listener = (ExampleDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() +
                    "must implement ForgotPasswordDialogListener");
        }
    }

    public interface ExampleDialogListener {
        void forgotPassword(String email,String phone);
    }

    private boolean validateEmail() {
        String email = emailEditText.getText().toString().trim();
        if (email.isEmpty()) {
            inputLayout.setError(getResources().getString(R.string.error_empty_field));
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            inputLayout.setError(getResources().getString(R.string.error_invalid_email));
            return false;
        } else {
            inputLayout.setError(null);
            return true;
        }
    }


    private boolean validatePhone() {
        if (phone.isEmpty()) {
            phoneLayout.setError(getResources().getString(R.string.error_empty_field));
            return false;
        } else if (phone.length() < 10) {
            phoneLayout.setError(getResources().getString(R.string.error_invalid_phone));
            return false;
        } else {
            phoneLayout.setError(null);
            return true;
        }

    }

    private boolean isConnected() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) Objects.requireNonNull(getActivity()).getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }


    private static float convertDpToPixel(Context context){
        return 36 * ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }

}




