package com.ashwinkanchana.cansis.fragments;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EdgeEffect;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.RetryPolicy;
import com.android.volley.toolbox.JsonObjectRequest;
import com.ashwinkanchana.cansis.R;
import com.ashwinkanchana.cansis.adapters.AttendanceAdapter;
import com.ashwinkanchana.cansis.data.Attendance;
import com.ashwinkanchana.cansis.data.AttendanceAPI;
import com.ashwinkanchana.cansis.utils.App;
import com.ashwinkanchana.cansis.utils.VolleySingleton;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static android.content.Context.MODE_PRIVATE;
import static com.ashwinkanchana.cansis.utils.Constants.JSON_ARRAY_RESULT;
import static com.ashwinkanchana.cansis.utils.Constants.NETWORK_BROADCAST_ACTION;
import static com.ashwinkanchana.cansis.utils.Constants.NETWORK_BROADCAST_SOURCE;
import static com.ashwinkanchana.cansis.utils.Constants.NETWORK_BROADCAST_VALUE;
import static com.ashwinkanchana.cansis.utils.Constants.PACKAGE_NAME;
import static com.ashwinkanchana.cansis.utils.Constants.PREF_KEY_ATTD_CACHE;
import static com.ashwinkanchana.cansis.utils.Constants.REQUEST_QUEUE_TAG_ATTENDANCE;
import static com.ashwinkanchana.cansis.utils.Constants.STATE_CONNECTED;
import static com.ashwinkanchana.cansis.utils.Constants.STATE_CONTENT;
import static com.ashwinkanchana.cansis.utils.Constants.STATE_EMPTY;
import static com.ashwinkanchana.cansis.utils.Constants.STATE_LOADING;
import static com.ashwinkanchana.cansis.utils.Constants.STATE_NO_CONNECTION;
import static com.ashwinkanchana.cansis.utils.Constants.STATE_UNKNOWN_ERROR;

public class AttendanceFragment extends Fragment  {

    public static AttendanceFragment newInstance() {
        return new AttendanceFragment();
    }


    public AttendanceFragment() {
        // Required empty public constructor
    }

    private static final String ATTENDANCE_API_URL = "ATTENDANCE_API_ENDPOINT";
    private static final String TAG = AttendanceFragment.class.getSimpleName();
    private RecyclerView recyclerView;
    private RequestQueue requestQueue;
    private ArrayList<Attendance> attendanceList;
    private ArrayList<AttendanceAPI> attendanceAPIArrayList;
    private AttendanceAdapter attendanceAdapter;
    private ShimmerFrameLayout shimmerContainer;
    private SwipeRefreshLayout swipeContainer;
    private ImageView errorIllustration;
    private TextView errorDescription;
    private MaterialButton retryButton;
    private String username;
    private boolean loadComplete,isConnected,cacheData;
    private CoordinatorLayout coordinatorLayout;
    private Snackbar snackbar;
    private Disposable networkDisposable;




    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        loadComplete = false;
        cacheData = false;
        attendanceList = new ArrayList<Attendance>();
        attendanceAPIArrayList = new ArrayList<AttendanceAPI>();

        View view = inflater.inflate(R.layout.fragment_attendance, container, false);
        coordinatorLayout = view.findViewById(R.id.attendanceFragment);

        username  = App.getUsername();

        requestQueue = VolleySingleton.getInstance(getActivity()).getRequestQueue();

        recyclerView = view.findViewById(R.id.recycler_view);
        attendanceAdapter = new AttendanceAdapter(getActivity(),attendanceList);
        swipeContainer = view.findViewById(R.id.swipe_container);
        swipeContainer.setColorSchemeResources(R.color.black);
        shimmerContainer = view.findViewById(R.id.shimmer_view_container);
        errorIllustration = view.findViewById(R.id.error_illustration);
        errorDescription = view.findViewById(R.id.error_description);
        retryButton = view.findViewById(R.id.retry_button);
        retryButton.setOnClickListener(this::retryAttendance);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setAdapter(attendanceAdapter);
        swipeContainer.setOnRefreshListener(this::retryAttendance);
        setOverScrollColor();

        //fetchAttendance(username);

        cacheData = showCachedData();

        return view;
    }


    private void fetchAttendance(String username) {
        if(cacheData)
            swipeContainer.setRefreshing(true);
        else
            setState(STATE_LOADING);


        String URL = ATTENDANCE_API_URL+username;
        //String URL = ATTENDANCE_API_URL;
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, URL, null, response -> {
                    JSONArray jsonArray = response.optJSONArray(JSON_ARRAY_RESULT);
                    try{
                        if(jsonArray != null) {
                            List<AttendanceAPI> attendances = new Gson().fromJson(jsonArray.toString(), new TypeToken<List<AttendanceAPI>>() {
                            }.getType());

                            attendanceAPIArrayList.clear();
                            attendanceAPIArrayList.addAll(attendances);


                            if (attendances.size() == 0)
                                setState(STATE_EMPTY);
                            else {
                                String latestSem = attendanceAPIArrayList.get(0).getS_a_name();
                                attendanceList.clear();
                                for (int i = 0; i < attendanceAPIArrayList.size(); i++) {
                                    AttendanceAPI attendanceAPI = attendanceAPIArrayList.get(i);
                                    if (attendanceAPI.getS_a_name().equals(latestSem)) {
                                        attendanceList.add(new Attendance(String.valueOf(i + 1),
                                                attendanceAPI.getS_a_sub(),
                                                attendanceAPI.getS_a_values(), i));
                                    }
                                }
                                attendanceAdapter.notifyDataSetChanged();
                            }

                            storeData(attendanceList);
                            loadComplete = true;
                            setState(STATE_CONTENT);
                        }else{
                            setState(STATE_EMPTY);
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                        showFetchFailedToast();
                        showCachedData();
                    }


                }, error -> {
                    Log.e(TAG, "Error: " + error.getMessage());
                    showFetchFailedToast();
                    showCachedData();
                });

        int socketTimeout = 300000;//5 minutes timeout
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        jsonObjectRequest.setRetryPolicy(policy);
        jsonObjectRequest.setTag(REQUEST_QUEUE_TAG_ATTENDANCE);
        requestQueue.add(jsonObjectRequest);

    }

    private void setOverScrollColor(){
        recyclerView.setEdgeEffectFactory(new RecyclerView.EdgeEffectFactory() {
            @NonNull
            @Override protected EdgeEffect createEdgeEffect(@NonNull RecyclerView view, int direction)
            { EdgeEffect edge = new EdgeEffect(view.getContext());
                edge.setColor(ContextCompat.getColor(view.getContext(), R.color.colorGrey2)); return edge; } });

    }


    private void retryAttendance(){
        if(isConnected) {
            fetchAttendance(username);
        }
        else {
            swipeContainer.setRefreshing(false);
            showConnectionSnackBar();
        }
    }
    private void retryAttendance(View view){
        retryAttendance();

    }


    private void setState(int state){
        switch (state){
            case STATE_EMPTY:
                recyclerView.setVisibility(View.GONE);
                shimmerContainer.stopShimmer();
                shimmerContainer.setVisibility(View.GONE);
                errorIllustration.setImageResource(R.drawable.illustration_empty);
                errorDescription.setText(R.string.nothing_here);
                errorIllustration.setVisibility(View.VISIBLE);
                errorDescription.setVisibility(View.VISIBLE);
                retryButton.setVisibility(View.GONE);
                swipeContainer.setEnabled(true);
                swipeContainer.setRefreshing(false);
                break;

            case STATE_UNKNOWN_ERROR:
                shimmerContainer.stopShimmer();
                shimmerContainer.setVisibility(View.GONE);
                recyclerView.setVisibility(View.GONE);
                errorIllustration.setImageResource(R.drawable.illustration_error_unknown);
                errorDescription.setText(R.string.something_went_wrg);
                errorIllustration.setVisibility(View.VISIBLE);
                errorDescription.setVisibility(View.VISIBLE);
                retryButton.setVisibility(View.VISIBLE);
                swipeContainer.setEnabled(false);
                break;
            case STATE_NO_CONNECTION:
                shimmerContainer.stopShimmer();
                shimmerContainer.setVisibility(View.GONE);
                recyclerView.setVisibility(View.GONE);
                errorIllustration.setImageResource(R.drawable.illustration_no_connection);
                errorDescription.setText(R.string.no_connection_snackbar);
                errorIllustration.setVisibility(View.VISIBLE);
                errorDescription.setVisibility(View.VISIBLE);
                retryButton.setVisibility(View.VISIBLE);
                swipeContainer.setEnabled(false);
                break;
            case STATE_CONTENT:
                shimmerContainer.stopShimmer();
                shimmerContainer.setVisibility(View.GONE);
                errorIllustration.setVisibility(View.GONE);
                errorDescription.setVisibility(View.GONE);
                retryButton.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                attendanceAdapter.notifyDataSetChanged();
                swipeContainer.setRefreshing(false);
                swipeContainer.setEnabled(true);
                break;


            case STATE_LOADING:
                attendanceList.clear();
                attendanceAdapter.randomColor();
                attendanceAdapter.notifyDataSetChanged();
                errorIllustration.setVisibility(View.GONE);
                errorDescription.setVisibility(View.GONE);
                retryButton.setVisibility(View.GONE);
                shimmerContainer.setVisibility(View.VISIBLE);
                shimmerContainer.startShimmer();
                swipeContainer.setEnabled(false);
                break;




        }
    }

    private void showFetchFailedToast()  {
        Snackbar.make(coordinatorLayout,"Couldn't fetch attendance",Snackbar.LENGTH_SHORT).show();
    }

    private void showConnectionSnackBar(){
        swipeContainer.setRefreshing(false);
        snackbar = Snackbar.make(coordinatorLayout, R.string.no_connection_snackbar, Snackbar.LENGTH_LONG);
        View snackBarView = snackbar.getView();
        snackBarView.setElevation(30);
        if(!loadComplete) {
            snackbar.setDuration(Snackbar.LENGTH_INDEFINITE);
            snackbar.setAction(R.string.retry, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    retryAttendance();
                }
            });
        }
        snackbar.show();

    }





    private boolean showCachedData() {
        if(!isConnected)
            showConnectionSnackBar();

        try {
            SharedPreferences prefs = Objects.requireNonNull(getActivity()).getSharedPreferences(PACKAGE_NAME,MODE_PRIVATE);
            Gson gson = new Gson();
            String json = prefs.getString(PREF_KEY_ATTD_CACHE,null);
            Type type = new TypeToken<ArrayList<Attendance>>() {}.getType();
            ArrayList attendanceCacheList;
            attendanceCacheList = gson.fromJson(json,type);


            if(attendanceCacheList==null){
                Log.d(TAG, "showCachedData: attendanceCacheList==null)");
                attendanceCacheList = new ArrayList<Attendance>();
                if(!isConnected) {
                    setState(STATE_NO_CONNECTION);
                }
                else {
                    setState(STATE_UNKNOWN_ERROR);
                }
                return false;
            }else {
                attendanceList.clear();
                attendanceList.addAll(attendanceCacheList);
                setState(STATE_CONTENT);
                return true;
            }
        }catch (NullPointerException e){
            e.printStackTrace();
            return false;
        }
    }

    private void storeData(ArrayList<Attendance> attendanceList) {
        SharedPreferences prefs = Objects.requireNonNull(getActivity()).getSharedPreferences(PACKAGE_NAME,MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(attendanceList);
        editor.putString(PREF_KEY_ATTD_CACHE,json);
        editor.apply();
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
            if(snackbar != null)
                snackbar.dismiss();
            isConnected = true;
            //swipeContainer.setEnabled(true);
            if(!loadComplete)
                fetchAttendance(username);
        }
        else{
            isConnected = false;
            showConnectionSnackBar();
        }
    }
}


