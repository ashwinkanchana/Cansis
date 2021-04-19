package com.ashwinkanchana.cansis.fragments;

import android.animation.Animator;
import android.app.Dialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.airbnb.lottie.LottieAnimationView;
import com.ashwinkanchana.cansis.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ServerTimestamp;

import java.security.PrivateKey;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static android.content.Context.INPUT_METHOD_SERVICE;
import static com.ashwinkanchana.cansis.utils.Constants.FS_COLLECTION_FEEDBACK;
import static com.ashwinkanchana.cansis.utils.Constants.FS_KEY_FEEDBACK;
import static com.ashwinkanchana.cansis.utils.Constants.FS_KEY_RATING;
import static com.ashwinkanchana.cansis.utils.Constants.FS_KEY_TIMESTAMP;

public class FeedbackDialog extends DialogFragment {

    private RatingBar ratingBar;
    private EditText feedbackEditText;
    private String feedBack;
    private RelativeLayout layout;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private float ratingValue;
    private TextView title,message,info;
    private TextInputLayout review;
    private LottieAnimationView animationView;
    private ProgressBar progressBar;


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        LayoutInflater inflater = Objects.requireNonNull(getActivity()).getLayoutInflater();
        View view = inflater.inflate(R.layout.layout_feedback, null);
        feedbackEditText = view.findViewById(R.id.feedback_edittext);
        review = view.findViewById(R.id.feedback_edit_text_layout);
        ratingBar = view.findViewById(R.id.rating);
        layout = view.findViewById(R.id.feedback_view);
        animationView = view.findViewById(R.id.done);
        animationView.setVisibility(View.GONE);
        title = view.findViewById(R.id.title);
        message = view.findViewById(R.id.message);
        info = view.findViewById(R.id.info_feedback);
        progressBar = view.findViewById(R.id.progress_feedback);
        ratingValue = 0;

        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                if(rating<1.0f)
                    ratingBar.setRating(1.0f);

               ratingValue = rating;
            }
        });

        final AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setView(view)
                .setPositiveButton(R.string.feedback_send, null)
                .create();

        dialog.setOnShowListener(dialogInterface -> {

            Button button = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            button.setOnClickListener(view1 -> {
                feedBack = feedbackEditText.getText().toString().trim();
                if(!isConnected()) {
                    Snackbar snackbar;
                    snackbar = Snackbar.make(layout, R.string.no_connection_snackbar, Snackbar.LENGTH_SHORT);
                    snackbar.setTextColor(getResources().getColor(R.color.colorWhite));
                    View snackBarView = snackbar.getView();
                    snackBarView.setBackgroundColor(getResources().getColor(R.color.colorRed));
                    snackBarView.setTranslationY(-(convertDpToPixel( Objects.requireNonNull(getContext()))));
                    snackbar.show();

                }else if(ratingValue==0 && feedBack.length()<=0){
                    Snackbar snackbar;
                    snackbar = Snackbar.make(layout, "Rating or review required", Snackbar.LENGTH_SHORT);
                    View snackBarView = snackbar.getView();
                    snackBarView.setTranslationY(-(convertDpToPixel( Objects.requireNonNull(getContext()))));
                    snackbar.show();

                }else{
                    try {
                        InputMethodManager inputMethodManager2 = (InputMethodManager) getActivity().getSystemService(INPUT_METHOD_SERVICE);
                        Objects.requireNonNull(inputMethodManager2).hideSoftInputFromWindow(Objects.requireNonNull(getActivity().getCurrentFocus()).getWindowToken(), 0);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    sendFeedback();
                }

            });
        });
        feedbackEditText.requestFocus();

        return dialog;
    }


    private boolean isConnected() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) Objects.requireNonNull(getActivity()).getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void sendFeedback(){
        progressBar.setVisibility(View.VISIBLE);
        DocumentReference ref = db.collection(FS_COLLECTION_FEEDBACK).document(String.valueOf(Timestamp.now().getSeconds()));
        Map<String, Object> feedback = new HashMap<>();
        feedback.put(FS_KEY_FEEDBACK, feedBack);
        feedback.put(FS_KEY_RATING,ratingValue);
        feedback.put(FS_KEY_TIMESTAMP, Timestamp.now());
        ref.set(feedback)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("TAG", "DocumentSnapshot successfully written!");
                        message.setVisibility(View.GONE);
                        review.setVisibility(View.GONE);
                        ratingBar.setVisibility(View.GONE);
                        info.setVisibility(View.GONE);
                        title.setVisibility(View.GONE);
                        progressBar.setVisibility(View.GONE);
                        Snackbar.make(layout,"Thank you for your feedback",Snackbar.LENGTH_LONG).show();
                        animationView.setVisibility(View.VISIBLE);
                        animationView.setRepeatCount(0);
                        animationView.addAnimatorListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animator animation) {
                                dismiss();
                            }

                            @Override
                            public void onAnimationCancel(Animator animation) {

                            }

                            @Override
                            public void onAnimationRepeat(Animator animation) {

                            }
                        });

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("TAG", "Error writing document", e);
                        Snackbar snackbar;
                        snackbar = Snackbar.make(layout, "Couldn't submit feedback", Snackbar.LENGTH_SHORT);
                        View snackBarView = snackbar.getView();
                        snackBarView.setTranslationY(-(convertDpToPixel( Objects.requireNonNull(getContext()))));
                        snackbar.show();

                    }
                });
    }


    private static float convertDpToPixel(Context context){
        return 36 * ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }
}




