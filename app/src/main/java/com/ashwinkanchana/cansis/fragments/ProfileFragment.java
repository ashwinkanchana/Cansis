package com.ashwinkanchana.cansis.fragments;


import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.NetworkInfo;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.ashwinkanchana.cansis.activities.LoginActivity;
import com.ashwinkanchana.cansis.utils.Constants;
import com.ashwinkanchana.cansis.utils.VolleySingleton;
import com.bumptech.glide.Glide;
import com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import com.ashwinkanchana.cansis.R;
import com.ashwinkanchana.cansis.activities.AboutActivity;

import com.ashwinkanchana.cansis.utils.App;
import com.ashwinkanchana.cansis.data.Profile;

import org.json.JSONArray;


import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static android.content.Context.MODE_PRIVATE;
import static com.ashwinkanchana.cansis.utils.Constants.FCM_TOPIC_ANNOUNCEMENTS;
import static com.ashwinkanchana.cansis.utils.Constants.FCM_TOPIC_PARENTS;
import static com.ashwinkanchana.cansis.utils.Constants.FCM_TOPIC_STUDENTS;
import static com.ashwinkanchana.cansis.utils.Constants.JSON_ARRAY_RESULT;
import static com.ashwinkanchana.cansis.utils.Constants.NETWORK_BROADCAST_ACTION;
import static com.ashwinkanchana.cansis.utils.Constants.NETWORK_BROADCAST_SOURCE;
import static com.ashwinkanchana.cansis.utils.Constants.NETWORK_BROADCAST_VALUE;
import static com.ashwinkanchana.cansis.utils.Constants.PREF_KEY_ATTD_CACHE;
import static com.ashwinkanchana.cansis.utils.Constants.PREF_KEY_BRANCH_SHORT;
import static com.ashwinkanchana.cansis.utils.Constants.PREF_KEY_CURRENT_SEM;
import static com.ashwinkanchana.cansis.utils.Constants.PREF_KEY_DARK_MODE;
import static com.ashwinkanchana.cansis.utils.Constants.PREF_KEY_DEFAULT_SEMESTER;
import static com.ashwinkanchana.cansis.utils.Constants.PREF_KEY_ENCODED_NAME;
import static com.ashwinkanchana.cansis.utils.Constants.PREF_KEY_GALLERY_CACHE;
import static com.ashwinkanchana.cansis.utils.Constants.PREF_KEY_IS_FIRST_TIME;
import static com.ashwinkanchana.cansis.utils.Constants.PREF_KEY_IS_LOGGED_IN;
import static com.ashwinkanchana.cansis.utils.Constants.PREF_KEY_MARKS_SUBS_CACHE;
import static com.ashwinkanchana.cansis.utils.Constants.PREF_KEY_MARKS_SUBS_CACHE_SEM;
import static com.ashwinkanchana.cansis.utils.Constants.PREF_KEY_NOTICE_CACHE;
import static com.ashwinkanchana.cansis.utils.Constants.PREF_KEY_PROFILE_CACHE;
import static com.ashwinkanchana.cansis.utils.Constants.PREF_KEY_PROFILE_PIC_URL;
import static com.ashwinkanchana.cansis.utils.Constants.PREF_KEY_STUDENT_ID;
import static com.ashwinkanchana.cansis.utils.Constants.PREF_KEY_STUDENT_NAME;
import static com.ashwinkanchana.cansis.utils.Constants.PREF_KEY_USERNAME;
import static com.ashwinkanchana.cansis.utils.Constants.PREF_KEY_USER_TYPE;
import static com.ashwinkanchana.cansis.utils.Constants.PREF_KEY_USN;
import static com.ashwinkanchana.cansis.utils.Constants.REQUEST_QUEUE_TAG_ATTENDANCE;
import static com.ashwinkanchana.cansis.utils.Constants.REQUEST_QUEUE_TAG_GALLERY;
import static com.ashwinkanchana.cansis.utils.Constants.REQUEST_QUEUE_TAG_NOTICE;
import static com.ashwinkanchana.cansis.utils.Constants.REQUEST_QUEUE_TAG_PROFILE;
import static com.ashwinkanchana.cansis.utils.Constants.STATE_CONNECTED;
import static com.ashwinkanchana.cansis.utils.Constants.STATE_CONTENT;
import static com.ashwinkanchana.cansis.utils.Constants.STATE_EMPTY;
import static com.ashwinkanchana.cansis.utils.Constants.STATE_LOADING;
import static com.ashwinkanchana.cansis.utils.Constants.STATE_NO_CONNECTION;
import static com.ashwinkanchana.cansis.utils.Constants.STATE_UNKNOWN_ERROR;
import static com.ashwinkanchana.cansis.utils.App.PACKAGE_NAME;


public class ProfileFragment extends Fragment {


    public static ProfileFragment newInstance() {
        return new ProfileFragment();
    }

    public ProfileFragment() {
        // Required empty public constructor
    }

    private static final String URL = "PROFILE_URL";
    private static final String PROFILE_ACTIVITY_TAG = "ProfileActivity";
    private RequestQueue requestQueue;
    private TextView name;
    private TextView branch;
    private TextView father;
    private TextView contact;
    private TextView address;
    private SwipeRefreshLayout swipeContainer;
    private LinearLayout profileLayout;
    private SharedPreferences prefs;
    private de.hdodenhof.circleimageview.CircleImageView profilePic;
    private String username;
    private ImageView errorIllustration;
    private TextView errorDescription;
    private MaterialButton retryButton;
    private Snackbar snackbar;
    private CoordinatorLayout coordinatorLayout;
    private boolean isConnected,loadComplete,cachedData;
    private ArrayList<Profile> profiles;
    private Profile prof;
    private Disposable networkDisposable;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = Objects.requireNonNull(getActivity()).getSharedPreferences(PACKAGE_NAME, MODE_PRIVATE);
        username = App.getUsername();
    }

    @SuppressLint("CheckResult")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        loadComplete = false;
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        coordinatorLayout = view.findViewById(R.id.profileFragment);

        profiles = new ArrayList<Profile>();

        requestQueue = VolleySingleton.getInstance(getActivity()).getRequestQueue();

        profileLayout = view.findViewById(R.id.profile_layout);
        swipeContainer =  view.findViewById(R.id.swipe_container);
        swipeContainer.setColorSchemeResources(R.color.black);
        name = view.findViewById(R.id.s_name);
        branch = view.findViewById(R.id.s_lass);
        father = view.findViewById(R.id.s_father);
        contact = view.findViewById(R.id.s_contactno);
        address = view.findViewById(R.id.s_address);
        profilePic = view.findViewById(R.id.s_image);

        errorIllustration = view.findViewById(R.id.error_illustration);
        errorDescription = view.findViewById(R.id.error_description);
        retryButton = view.findViewById(R.id.retry_button);

        retryButton.setOnClickListener(this::retryProfile);

        ExtendedFloatingActionButton logoutFAB = view.findViewById(R.id.logout_efab);
        logoutFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });

        swipeContainer.setOnRefreshListener(this::retryProfile);

        setHasOptionsMenu(true);

        //fetchProfile();
        cachedData = showCachedData();
        return view;
    }



    private void fetchProfile() {
        if(cachedData)
            swipeContainer.setRefreshing(true);
        else
            setState(STATE_LOADING);

        try {
            String encodedURL = URL+username;
            //String encodedURL = URL;
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                    (Request.Method.GET, encodedURL, null, response -> {
                        JSONArray jsonArray = response.optJSONArray(JSON_ARRAY_RESULT);

                        List<Profile> profile = new Gson().fromJson(jsonArray.toString(), new TypeToken<ArrayList<Profile>>() {
                        }.getType());

                        profiles.clear();
                        profiles.addAll(profile);

                        storeStudentPrimaryInfo();
                        storeData(profiles);

                        if(profiles.size()==0)
                            setState(STATE_EMPTY);
                        else {
                            animate();
                            setState(STATE_CONTENT);
                            loadComplete  = true;
                        }
                    }, error ->{
                        showFetchFailedToast();
                        showCachedData();
                    });
            jsonObjectRequest.setTag(REQUEST_QUEUE_TAG_PROFILE);
            requestQueue.add(jsonObjectRequest);
        }catch (Exception e){
            e.printStackTrace();
        }

    }


    private void retryProfile(){
        if(isConnected) {
            fetchProfile();
        }
        else {
            swipeContainer.setRefreshing(false);
            showConnectionSnackBar();
        }
    }
    private void retryProfile(View view){
        retryProfile();
    }

    private void logout() {
        DialogInterface.OnClickListener dialogClickListener = (DialogInterface dialog, int which) -> {
            if (which == DialogInterface.BUTTON_POSITIVE) {
                logoutConfirmed();
            }
        };

        new MaterialAlertDialogBuilder(Objects.requireNonNull(getActivity()))
                .setTitle(R.string.logout_dialog_title)
                .setMessage(R.string.logout_dialog_description)
                .setPositiveButton(R.string.logout_positive_button, dialogClickListener)
                .setNegativeButton(R.string.logout_negative_button,null)
                .show();
    }


    private void logoutConfirmed(){
        FirebaseMessaging.getInstance().unsubscribeFromTopic(FCM_TOPIC_STUDENTS);
        FirebaseMessaging.getInstance().unsubscribeFromTopic(FCM_TOPIC_ANNOUNCEMENTS);
        FirebaseMessaging.getInstance().unsubscribeFromTopic(FCM_TOPIC_PARENTS);
        FirebaseAuth.getInstance().signOut();

        SharedPreferences.Editor editor = prefs.edit();

        editor.putString(PREF_KEY_USERNAME,null);
        editor.putString(PREF_KEY_ENCODED_NAME,null);
        editor.putInt(PREF_KEY_CURRENT_SEM,-1);
        editor.putString(PREF_KEY_PROFILE_PIC_URL,null);
        editor.putString(PREF_KEY_STUDENT_NAME,null);
        editor.putString(PREF_KEY_BRANCH_SHORT,null);
        editor.putString(PREF_KEY_ATTD_CACHE,null);
        editor.putString(PREF_KEY_NOTICE_CACHE,null);
        editor.putString(PREF_KEY_PROFILE_CACHE,null);
        editor.putString(PREF_KEY_GALLERY_CACHE,null);
        editor.putString(PREF_KEY_MARKS_SUBS_CACHE,null);
        editor.putInt(PREF_KEY_MARKS_SUBS_CACHE_SEM,-1);
        editor.putString(PREF_KEY_STUDENT_ID,null);
        editor.putBoolean(PREF_KEY_IS_LOGGED_IN, false);
        editor.putBoolean(PREF_KEY_IS_FIRST_TIME, true);
        editor.putString(PREF_KEY_USN,null);
        editor.putBoolean(PREF_KEY_DARK_MODE,false);
        editor.putInt(PREF_KEY_USER_TYPE, 0);
        editor.putInt(PREF_KEY_DEFAULT_SEMESTER, -1);
        editor.apply();
        requestQueue.cancelAll(REQUEST_QUEUE_TAG_NOTICE);
        requestQueue.cancelAll(REQUEST_QUEUE_TAG_ATTENDANCE);
        requestQueue.cancelAll(REQUEST_QUEUE_TAG_GALLERY);
        requestQueue.cancelAll(REQUEST_QUEUE_TAG_PROFILE);
        Intent intent = new Intent (getActivity(), LoginActivity.class);
        startActivity(intent);

        Objects.requireNonNull(getActivity()).finishAffinity();
    }

    private void storeStudentPrimaryInfo(){
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PREF_KEY_STUDENT_NAME,profiles.get(0).getName());
        editor.putString(PREF_KEY_BRANCH_SHORT,profiles.get(0).getBranch());
        editor.putString(PREF_KEY_PROFILE_PIC_URL,profiles.get(0).getImage());
        editor.putBoolean(PREF_KEY_IS_FIRST_TIME, false);
        editor.apply();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.about_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.about) {
            Intent intent = new Intent(getActivity(), AboutActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    private void setState(int state){
        switch (state){

            case STATE_UNKNOWN_ERROR:
                profileLayout.setVisibility(View.INVISIBLE);
                errorIllustration.setImageResource(R.drawable.illustration_error_unknown);
                errorDescription.setText(R.string.something_went_wrg);
                errorIllustration.setVisibility(View.VISIBLE);
                errorDescription.setVisibility(View.VISIBLE);
                retryButton.setVisibility(View.VISIBLE);
                swipeContainer.setRefreshing(false);
                swipeContainer.setEnabled(false);
                break;
            case STATE_NO_CONNECTION:
                profileLayout.setVisibility(View.INVISIBLE);
                errorIllustration.setImageResource(R.drawable.illustration_no_connection);
                errorDescription.setText(R.string.no_connection_snackbar);
                errorIllustration.setVisibility(View.VISIBLE);
                errorDescription.setVisibility(View.VISIBLE);
                retryButton.setVisibility(View.VISIBLE);
                swipeContainer.setRefreshing(false);
                swipeContainer.setEnabled(false);
                break;
            case STATE_CONTENT:
                prof = profiles.get(0);
                name.setText(prof.getName());
                branch.setText(prof.getBranch());
                contact.setText(prof.getContact());
                father.setText(prof.getFather());
                address.setText(prof.getAddress());
                if (isAdded()) {
                    Glide.with(Objects.requireNonNull(getActivity()))
                            .load(prof.getImage())
                            .into(profilePic);
                }
                profileLayout.setVisibility(View.VISIBLE);
                errorIllustration.setVisibility(View.GONE);
                errorDescription.setVisibility(View.GONE);
                retryButton.setVisibility(View.GONE);
                swipeContainer.setRefreshing(false);
                swipeContainer.setEnabled(true);
                break;


            case STATE_LOADING:
                errorIllustration.setVisibility(View.GONE);
                errorDescription.setVisibility(View.GONE);
                retryButton.setVisibility(View.GONE);
                profileLayout.setVisibility(View.INVISIBLE);
                swipeContainer.setRefreshing(true);
                swipeContainer.setEnabled(true);
                break;

        }
    }

    private void showFetchFailedToast()  {
        Snackbar.make(coordinatorLayout,"Couldn't fetch profile",Snackbar.LENGTH_SHORT).show();
    }

    private void showConnectionSnackBar(){
        swipeContainer.setRefreshing(false);
        snackbar = Snackbar.make(coordinatorLayout, R.string.no_connection_snackbar, Snackbar.LENGTH_SHORT);
        View snackBarView = snackbar.getView();
        snackBarView.setElevation(30);
        if(!loadComplete) {
            snackbar.setDuration(Snackbar.LENGTH_SHORT);
            snackbar.setAction(R.string.retry, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    retryProfile(v);
                }
            });
        }
        snackbar.show();

    }
    private boolean showCachedData() {
        try {
            SharedPreferences prefs = Objects.requireNonNull(getActivity()).getSharedPreferences(Constants.PACKAGE_NAME,MODE_PRIVATE);
            Gson gson = new Gson();
            String json = prefs.getString(PREF_KEY_PROFILE_CACHE,null);
            Type type = new TypeToken<ArrayList<Profile>>() {}.getType();
            ArrayList profileCache;
            profileCache = gson.fromJson(json,type);


            if(profileCache==null){
                profileCache = new ArrayList<Profile>();
                if(!isConnected) {
                    setState(STATE_NO_CONNECTION);
                }
                else {
                    setState(STATE_UNKNOWN_ERROR);
                }

                return false;
            }else {
                profiles.clear();
                profiles.addAll(profileCache);
                setState(STATE_CONTENT);
                return true;
            }
        }catch (NullPointerException e){
            e.printStackTrace();
            return false;
        }
    }


    private void storeData(ArrayList<Profile> profile) {
        SharedPreferences prefs = Objects.requireNonNull(getActivity()).getSharedPreferences(Constants.PACKAGE_NAME,MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(profile);
        Log.d("TAG", "storeData: "+json);
        editor.putString(PREF_KEY_PROFILE_CACHE,json);
        editor.apply();
    }


    private void animate(){
        Animation animation = AnimationUtils.loadAnimation(getActivity(), R.anim.anim_about_card_show);
        profileLayout.startAnimation(animation);
    }


    @Override
    public void onResume() {
        super.onResume();
        networkDisposable = ReactiveNetwork.observeNetworkConnectivity(Objects.requireNonNull(getActivity()))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(connectivity -> {
                    Log.d("ProfileFragment", connectivity.toString());
                    final NetworkInfo.State state = connectivity.state();
                    networkState(String.valueOf(state));
                });
    }

    @Override public void onPause() {
        super.onPause();
        safelyDispose(networkDisposable);
    }

    private void safelyDispose(Disposable... disposables) {
        for (Disposable subscription : disposables) {
            if (subscription != null && !subscription.isDisposed()) {
                subscription.dispose();
            }
        }
    }

    private void networkState(String state){
        if(state.equals(STATE_CONNECTED) ){
            if(snackbar !=null){
                snackbar.dismiss();
            }
            isConnected = true;
            //swipeContainer.setEnabled(true);
            if(!loadComplete)
                fetchProfile();
        }else{
            isConnected = false;
            showConnectionSnackBar();
        }
    }

}
