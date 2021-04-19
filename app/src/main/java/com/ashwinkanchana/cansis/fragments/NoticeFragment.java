package com.ashwinkanchana.cansis.fragments;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
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
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.ashwinkanchana.cansis.R;
import com.ashwinkanchana.cansis.adapters.NoticeAdapter;
import com.ashwinkanchana.cansis.data.Notice;
import com.ashwinkanchana.cansis.utils.App;
import com.ashwinkanchana.cansis.utils.VolleySingleton;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
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
import static com.ashwinkanchana.cansis.utils.Constants.PREF_KEY_NOTICE_CACHE;
import static com.ashwinkanchana.cansis.utils.Constants.REQUEST_QUEUE_TAG_NOTICE;
import static com.ashwinkanchana.cansis.utils.Constants.STATE_CONNECTED;
import static com.ashwinkanchana.cansis.utils.Constants.STATE_CONTENT;
import static com.ashwinkanchana.cansis.utils.Constants.STATE_EMPTY;
import static com.ashwinkanchana.cansis.utils.Constants.STATE_LOADING;
import static com.ashwinkanchana.cansis.utils.Constants.STATE_NO_CONNECTION;
import static com.ashwinkanchana.cansis.utils.Constants.STATE_UNKNOWN_ERROR;

public class NoticeFragment extends Fragment {

    public static NoticeFragment newInstance() {
        return new NoticeFragment();
    }

    public NoticeFragment() {
    }

    private static final String SERVER_STATUS_CHECK_URL = "PING_URL";
    private static final String URL = "ANNOUNCEMENTS_URL";
    private static final String TAG = NoticeFragment.class.getSimpleName();
    private RecyclerView recyclerView;
    private ArrayList<Notice> noticeList;
    private NoticeAdapter noticeAdapter;
    private ShimmerFrameLayout mShimmerViewContainer;
    private SwipeRefreshLayout swipeContainer;
    private ImageView errorIllustration;
    private TextView errorDescription;
    private MaterialButton retryButton;
    private boolean loadComplete,isConnected,cachedData;
    private CoordinatorLayout coordinatorLayout;
    private Snackbar snackbar;
    private RequestQueue requestQueue;
    private Disposable networkDisposable;
    int statusCode;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                               Bundle savedInstanceState)  {
        loadComplete = false;
        View view = inflater.inflate(R.layout.fragment_notice, container, false);
        coordinatorLayout = view.findViewById(R.id.noticeFragment);


        noticeList = new ArrayList<>();
        noticeAdapter = new NoticeAdapter(getActivity(), noticeList);
        swipeContainer =  view.findViewById(R.id.swipe_container);
        swipeContainer.setColorSchemeResources(R.color.black);
        mShimmerViewContainer = view.findViewById(R.id.shimmer_view_container);

        requestQueue = VolleySingleton.getInstance(getActivity()).getRequestQueue();

        recyclerView = view.findViewById(R.id.recycler_view);
        RecyclerView.LayoutManager mLayoutManager =
                new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(noticeAdapter);

        errorIllustration = view.findViewById(R.id.error_illustration);
        errorDescription = view.findViewById(R.id.error_description);
        retryButton = view.findViewById(R.id.retry_button);

        retryButton.setOnClickListener(this::retryNotice);
        setOverScrollColor();

        swipeContainer.setOnRefreshListener(this::retryNotice);

        //fetchNotice();
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(!App.getInstance().isConnected())
                    showConnectionSnackBar();
            }
        }, 3000);


        cachedData = showCachedData();
        return view;

    }

    private void fetchNotice() {
       if(cachedData)
            swipeContainer.setRefreshing(true);
        else
            setState(STATE_LOADING);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, URL, null, response -> {
                    JSONArray jsonArray = response.optJSONArray(JSON_ARRAY_RESULT);

                    List<Notice> notices = new Gson().fromJson(jsonArray.toString(), new TypeToken<List<Notice>>() {
                    }.getType());

                    noticeList.clear();
                    noticeList.addAll(notices);

                    storeData(noticeList);

                    if(noticeList.size()==0)
                        setState(STATE_EMPTY);
                    else {
                        setState(STATE_CONTENT);
                        loadComplete  = true;
                    }

                    }, error -> {
                    Log.e(TAG, "Error: " + error.getMessage());
                    showFetchFailedToast();
                    showCachedData();
                    if(App.getInstance().isConnected()){
                        try {
                            Log.d(TAG, "fetchNotice: errcode"+error.networkResponse.statusCode);
                            //Toast.makeText(getActivity(), error.networkResponse.statusCode, Toast.LENGTH_SHORT).show();
                            if(error.networkResponse.statusCode>=400){
                                ServerErrorDialog serverErrorDialog = new ServerErrorDialog();
                                serverErrorDialog.show(Objects.requireNonNull(this.getFragmentManager()), "server_error");

                            }
                        }catch (Exception e){
                            Log.d(TAG, "fetchNotice: errcode exception"+e.getMessage());
                            e.printStackTrace();
                            if(App.getInstance().isConnected()){
                                ServerStatusChecker serverStatusChecker = new ServerStatusChecker(this);
                                serverStatusChecker.execute();
                            }
                        }
                    }
                });
        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, 2, 1.0f));
        jsonObjectRequest.setTag(REQUEST_QUEUE_TAG_NOTICE);
        requestQueue.add(jsonObjectRequest);

    }


    private void setOverScrollColor(){
        recyclerView.setEdgeEffectFactory(new RecyclerView.EdgeEffectFactory() {
            @NonNull
            @Override protected EdgeEffect createEdgeEffect(@NonNull RecyclerView view, int direction)
            { EdgeEffect edge = new EdgeEffect(view.getContext());
                edge.setColor(ContextCompat.getColor(view.getContext(), R.color.colorGrey2)); return edge; } });
    }


    private void retryNotice(){
        if(isConnected) {
            fetchNotice();
        }
        else {
            swipeContainer.setRefreshing(false);
            showConnectionSnackBar();
        }
    }
    private void retryNotice(View view){
        retryNotice();
    }


    private void setState(int state){
        switch (state){
            case STATE_EMPTY:
                recyclerView.setVisibility(View.GONE);
                mShimmerViewContainer.stopShimmer();
                mShimmerViewContainer.setVisibility(View.GONE);
                errorIllustration.setImageResource(R.drawable.illustration_empty);
                errorDescription.setText(R.string.nothing_here);
                errorIllustration.setVisibility(View.VISIBLE);
                errorDescription.setVisibility(View.VISIBLE);
                retryButton.setVisibility(View.GONE);
                swipeContainer.setEnabled(true);
                swipeContainer.setRefreshing(false);
                break;

            case STATE_UNKNOWN_ERROR:
                mShimmerViewContainer.stopShimmer();
                mShimmerViewContainer.setVisibility(View.GONE);
                recyclerView.setVisibility(View.GONE);
                errorIllustration.setImageResource(R.drawable.illustration_error_unknown);
                errorDescription.setText(R.string.something_went_wrg);
                errorIllustration.setVisibility(View.VISIBLE);
                errorDescription.setVisibility(View.VISIBLE);
                retryButton.setVisibility(View.VISIBLE);
                swipeContainer.setEnabled(false);
                break;
            case STATE_NO_CONNECTION:
                mShimmerViewContainer.stopShimmer();
                mShimmerViewContainer.setVisibility(View.GONE);
                recyclerView.setVisibility(View.GONE);
                errorIllustration.setImageResource(R.drawable.illustration_no_connection);
                errorDescription.setText(R.string.no_connection_snackbar);
                errorIllustration.setVisibility(View.VISIBLE);
                errorDescription.setVisibility(View.VISIBLE);
                retryButton.setVisibility(View.VISIBLE);
                swipeContainer.setEnabled(false);
                showConnectionSnackBar();
                break;
            case STATE_CONTENT:
                swipeContainer.setRefreshing(false);
                mShimmerViewContainer.stopShimmer();
                mShimmerViewContainer.setVisibility(View.GONE);
                errorIllustration.setVisibility(View.GONE);
                errorDescription.setVisibility(View.GONE);
                retryButton.setVisibility(View.GONE);
                noticeAdapter.notifyDataSetChanged();
                recyclerView.setVisibility(View.VISIBLE);
                swipeContainer.setEnabled(true);
                break;


            case STATE_LOADING:
                noticeList.clear();
                swipeContainer.setEnabled(false);
                noticeAdapter.notifyDataSetChanged();
                errorIllustration.setVisibility(View.GONE);
                errorDescription.setVisibility(View.GONE);
                retryButton.setVisibility(View.GONE);
                mShimmerViewContainer.setVisibility(View.VISIBLE);
                mShimmerViewContainer.startShimmer();
                break;

        }
    }


    private void showConnectionSnackBar(){
        swipeContainer.setRefreshing(false);
        snackbar = Snackbar.make(coordinatorLayout, R.string.no_connection_snackbar, Snackbar.LENGTH_INDEFINITE);
        View snackBarView = snackbar.getView();
        snackBarView.setElevation(30);
        if(!loadComplete) {
            snackbar.setDuration(Snackbar.LENGTH_INDEFINITE);
            snackbar.setAction(R.string.retry, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    retryNotice(v);
                }
            });
        }
        snackbar.show();

    }

    private void showFetchFailedToast()  {
       Snackbar.make(coordinatorLayout,"Couldn't fetch circulars",Snackbar.LENGTH_SHORT).show();
    }


    private boolean showCachedData() {
        if(!isConnected)
           showConnectionSnackBar();

        try {
            SharedPreferences prefs = Objects.requireNonNull(getActivity()).getSharedPreferences(PACKAGE_NAME,MODE_PRIVATE);
            Gson gson = new Gson();
            String json = prefs.getString(PREF_KEY_NOTICE_CACHE,null);
            Type type = new TypeToken<ArrayList<Notice>>() {}.getType();
            ArrayList noticeCacheList;
            noticeCacheList = gson.fromJson(json,type);


            if(noticeCacheList==null){
                noticeCacheList = new ArrayList<Notice>();
                if(!isConnected) {
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if(!App.getInstance().isConnected())
                                setState(STATE_NO_CONNECTION);
                        }
                    }, 1000);

                }
                else {
                    setState(STATE_UNKNOWN_ERROR);
                }
                return false;
            }else {
                noticeList.clear();
                noticeList.addAll(noticeCacheList);
                setState(STATE_CONTENT);
                return true;
            }
        }catch (NullPointerException e){
            e.printStackTrace();
            return false;
        }
    }


    private void storeData(ArrayList<Notice> noticeList) {
        try {
            SharedPreferences prefs = Objects.requireNonNull(getActivity()).getSharedPreferences(PACKAGE_NAME,MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            Gson gson = new Gson();
            String json = gson.toJson(noticeList);
            editor.putString(PREF_KEY_NOTICE_CACHE,json);
            editor.apply();
        }catch (Exception e){
            e.printStackTrace();
        }

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
                fetchNotice();
        }
        else{
            isConnected = false;
            showConnectionSnackBar();
        }
    }


    public class ServerStatusChecker extends AsyncTask<Void, Void, Integer> {

        private WeakReference<NoticeFragment> fragmentWeakReference;
        public ServerStatusChecker(NoticeFragment fragment){
            fragmentWeakReference = new WeakReference<NoticeFragment>(fragment);
        }

        @Override
        public Integer doInBackground(Void... voids) {
            return checkServerStatus();
        }
        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            NoticeFragment fragment = fragmentWeakReference.get();
            if(fragment == null || fragment.isRemoving()){
                return;
            }
             if(integer>=400 && isConnected && App.getInstance().isConnected()) {
                fragment.swipeContainer.setRefreshing(false);
                ServerErrorDialog serverErrorDialog = new ServerErrorDialog();
                serverErrorDialog.show(Objects.requireNonNull(fragment.getFragmentManager()), "server_error");
            }
        }

        private int checkServerStatus(){
            int responseCode = -1;
            try {
                HttpURLConnection connection = (HttpURLConnection) new URL(SERVER_STATUS_CHECK_URL).openConnection();
                connection.setConnectTimeout(8000);
                connection.setReadTimeout(8000);
                connection.setRequestMethod("HEAD");
                responseCode = connection.getResponseCode();
                return responseCode;
            } catch (IOException exception) {
                return -1;
            }
        }

    }
}
