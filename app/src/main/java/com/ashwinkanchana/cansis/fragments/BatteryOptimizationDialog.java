package com.ashwinkanchana.cansis.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.ashwinkanchana.cansis.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 */
public class BatteryOptimizationDialog extends DialogFragment {
    MaterialButton settingsIntentButton;
    View view;

    public BatteryOptimizationDialog() {
        // Required empty public constructor
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = Objects.requireNonNull(getActivity()).getLayoutInflater();
        view = inflater.inflate(R.layout.layout_battery_optimization, null);

        settingsIntentButton = view.findViewById(R.id.settings_intent_button);
        settingsIntentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openPowerSettings();
            }


        });
        final AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setView(view)
                .setPositiveButton(R.string.dismiss, null)
                .create();

        dialog.setOnShowListener(dialogInterface -> {

            Button button = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            button.setOnClickListener(view1 -> {
                dialog.dismiss();


            });
        });



        return dialog;
    }



    private void openPowerSettings() {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
        Snackbar snackbar = Snackbar.make(view, R.string.battery_optimization_toast, Snackbar.LENGTH_LONG);
        View snackBarView = snackbar.getView();
        Toast toast = new Toast(getActivity());
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(snackBarView);
        toast.show();
        this.startActivity(intent);



    }
}
