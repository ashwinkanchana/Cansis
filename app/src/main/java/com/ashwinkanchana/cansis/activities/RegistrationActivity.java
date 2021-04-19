package com.ashwinkanchana.cansis.activities;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import br.com.simplepass.loadingbutton.customViews.CircularProgressButton;
import com.ashwinkanchana.cansis.R;

import static com.ashwinkanchana.cansis.utils.Constants.STR_USER_PARENT;
import static com.ashwinkanchana.cansis.utils.Constants.STR_USER_STUDENT;

public class RegistrationActivity extends AppCompatActivity {
    private Spinner spinner;
    private TextInputLayout textInputName;
    private TextInputLayout textInputEmail;
    private TextInputLayout textInputPhone;
    private TextInputLayout textInputPassword;
    private CircularProgressButton circularProgressButton;
    private String name;
    private String email;
    private String password;
    private String phone;


    private static final String PARENT_SIGN_UP_URL = "PARENT_REGISTER_ENDPOINT_GOES_HERE";
    private static final String STUDENT_SIGN_UP_URL = "STUDENT_REGISTER_ENDPOINT_GOES_HERE";
    private String SIGN_UP_URL;
    private static final String SIGN_UP_SUCCESS = "1";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        spinner = findViewById(R.id.spinner);
        textInputName = findViewById(R.id.text_input_name);
        textInputEmail = findViewById(R.id.text_input_email);
        textInputPhone = findViewById(R.id.text_input_phone);
        textInputPassword = findViewById(R.id.text_input_password);
        EditText passwordEditText = findViewById(R.id.password_edit_text);
        circularProgressButton = findViewById(R.id.sign_up_button);
        circularProgressButton.saveInitialState();

        spinner.setSelection(0);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String user = spinner.getSelectedItem().toString();
                if(user.equals(STR_USER_PARENT)){
                    SIGN_UP_URL = PARENT_SIGN_UP_URL;
                }
                else if(user.equals(STR_USER_STUDENT)){
                    SIGN_UP_URL = STUDENT_SIGN_UP_URL;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Do nothing
            }

        });





        circularProgressButton.setOnClickListener(v -> signUp());

        passwordEditText.setOnKeyListener((view, keyCode, event) -> {
            if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                    (keyCode == KeyEvent.KEYCODE_ENTER)) {
                signUp();
                return true;
            }
            return false;
        });
        findViewById(R.id.text_input_name).requestFocus();
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }


    private boolean validateName() {
        if (name.isEmpty()) {
            textInputName.setError(getResources().getString(R.string.error_empty_field));
            return false;
        } else {
            textInputName.setError(null);
            return true;
        }
    }

    private boolean validateEmail() {
        if (email.isEmpty()) {
            textInputEmail.setError(getResources().getString(R.string.error_empty_field));
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            textInputEmail.setError(getResources().getString(R.string.error_invalid_email));
            return false;
        } else {
            textInputEmail.setError(null);
            return true;
        }
    }

    private boolean validatePhone() {

        if (phone.isEmpty()) {
            textInputPhone.setError(getResources().getString(R.string.error_empty_field));
            return false;
        } else if (phone.length() < 10) {
            textInputPhone.setError(getResources().getString(R.string.error_invalid_phone));
            return false;
        } else {
            textInputPhone.setError(null);
            return true;
        }

    }

    private boolean validatePassword() {

        if (password.isEmpty()) {
            textInputPassword.setError(getResources().getString(R.string.error_empty_field));
            return false;
        } else if (password.length() < 4) {
            textInputPassword.setError(getResources().getString(R.string.error_short_password));
            return false;
        } else {
            textInputPassword.setError(null);
            return true;
        }

    }


    public void signUp() {

        name = Objects.requireNonNull(textInputName.getEditText()).getText().toString().trim();
        email = Objects.requireNonNull(textInputEmail.getEditText()).getText().toString().trim();
        password = Objects.requireNonNull(textInputPassword.getEditText()).getText().toString().trim();
        phone = Objects.requireNonNull(textInputPhone.getEditText()).getText().toString().trim();


        if(!isConnected()){
            Snackbar snackbar;
            snackbar = Snackbar.make(findViewById(R.id.sign_up_layout), R.string.no_connection_snackbar, Snackbar.LENGTH_LONG);
            View snackBarView = snackbar.getView();
            snackBarView.setBackgroundColor(getResources().getColor(R.color.colorRed));
            snackbar.show();
            } else if (!validateName() | !validateEmail() | !validatePhone() | !validatePassword()) {
            return;
        }else
            circularProgressButton.startAnimation();
            sendSignUpRequest();

    }

    public void sendSignUpRequest() {
        spinner.setClickable(false);
        spinner.setEnabled(false);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, SIGN_UP_URL,
                response -> {
                    JsonObject jsonObject = new JsonParser().parse(response).getAsJsonObject();
                    String responseCode = String.valueOf(jsonObject.get("success"));
                    String responseMessage = String.valueOf(jsonObject.get("message"));


                    spinner.setClickable(true);
                    spinner.setEnabled(true);
                    if (responseCode.equals(SIGN_UP_SUCCESS)) {
                        onSignUpSuccess();
                    } else if (responseMessage.contains("I'm sorry, This is not registered mobile")) {
                        circularProgressButton.startMorphRevertAnimation();
                        Snackbar.make(findViewById(R.id.sign_up_layout), R.string.details_not_found, Snackbar.LENGTH_LONG).show();
                    } else if (responseMessage.contains("I'm sorry, this username is already in use")) {
                        circularProgressButton.startMorphRevertAnimation();
                        Snackbar.make(findViewById(R.id.sign_up_layout), R.string.account_exists, Snackbar.LENGTH_LONG).show();
                    } else {
                        circularProgressButton.startMorphRevertAnimation();
                        Snackbar.make(findViewById(R.id.sign_up_layout), responseMessage, Snackbar.LENGTH_LONG).show();
                    }

                },
                error ->{
                    spinner.setClickable(true);
                    spinner.setEnabled(true);
                        circularProgressButton.startMorphRevertAnimation();
                        Snackbar.make(findViewById(R.id.sign_up_layout), R.string.unknown_error_snackbar, Snackbar.LENGTH_LONG).show();}) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("username", phone);
                params.put("password", password);
                params.put("email", email);
                params.put("fname", name);
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }


    private boolean isConnected() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public void onSignUpSuccess() {
        Intent intent = new Intent(this, LoginActivity.class);
        Toast.makeText(this, R.string.sign_up_success, Toast.LENGTH_SHORT).show();
        startActivity(intent);
        finish();


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        circularProgressButton.dispose();
    }


    @Override public void onBackPressed ()
    {

        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

}
