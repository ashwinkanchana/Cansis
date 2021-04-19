package com.ashwinkanchana.cansis.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.ashwinkanchana.cansis.R;
import com.ashwinkanchana.cansis.utils.App;
import com.google.android.material.button.MaterialButton;

import static com.ashwinkanchana.cansis.utils.Constants.RC_DISABLED_DESCRIPTION;
import static com.ashwinkanchana.cansis.utils.Constants.RC_DISABLED_TITLE;
import static com.ashwinkanchana.cansis.utils.Constants.RC_ERROR_TYPE;
import static com.ashwinkanchana.cansis.utils.Constants.RC_UPDATE_DESC;
import static com.ashwinkanchana.cansis.utils.Constants.RC_UPDATE_TITLE;
import static com.ashwinkanchana.cansis.utils.Constants.RC_UPDATE_URL;

public class ErrorActivity extends AppCompatActivity {


    private TextView textViewTitle;
    private TextView textViewDescription;
    private MaterialButton button;


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
        setContentView(R.layout.activity_error);
        textViewTitle = findViewById(R.id.error_title);
        textViewDescription = findViewById(R.id.error_description);
        button = findViewById(R.id.button);

        int errorType = getIntent().getIntExtra(RC_ERROR_TYPE, 1);

        if(errorType==1){
            String updateTitle = getIntent().getStringExtra(RC_UPDATE_TITLE);
            String updateDescription = getIntent().getStringExtra(RC_UPDATE_DESC);
            String updateURL = getIntent().getStringExtra(RC_UPDATE_URL);

            textViewTitle.setText(updateTitle);
            textViewDescription.setText(updateDescription);
            button.setVisibility(View.VISIBLE);
            button.setText("Update");
            button.setOnClickListener(view -> {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(updateURL));
                startActivity(intent);
            });

        }
        else if(errorType==2) {
            String killTitle = getIntent().getStringExtra(RC_DISABLED_TITLE);
            String killDescription = getIntent().getStringExtra(RC_DISABLED_DESCRIPTION);
            textViewTitle.setText(killTitle);
            textViewDescription.setText(killDescription);
            button.setVisibility(View.VISIBLE);
            button.setOnClickListener(view -> {
                finishAffinity();
            });

        }

    }



    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity();
    }
}
