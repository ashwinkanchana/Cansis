package com.ashwinkanchana.cansis.fragments;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.NetworkInfo;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EdgeEffect;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.ashwinkanchana.cansis.utils.VolleySingleton;
import com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import com.ashwinkanchana.cansis.R;
import com.ashwinkanchana.cansis.utils.DividerItemDecoration;
import com.ashwinkanchana.cansis.data.Gallery;
import com.ashwinkanchana.cansis.adapters.GalleryAdapter;

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
import static com.ashwinkanchana.cansis.utils.Constants.PREF_KEY_GALLERY_CACHE;
import static com.ashwinkanchana.cansis.utils.Constants.REQUEST_QUEUE_TAG_GALLERY;
import static com.ashwinkanchana.cansis.utils.Constants.STATE_CONNECTED;
import static com.ashwinkanchana.cansis.utils.Constants.STATE_CONTENT;
import static com.ashwinkanchana.cansis.utils.Constants.STATE_EMPTY;
import static com.ashwinkanchana.cansis.utils.Constants.STATE_LOADING;
import static com.ashwinkanchana.cansis.utils.Constants.STATE_NO_CONNECTION;
import static com.ashwinkanchana.cansis.utils.Constants.STATE_UNKNOWN_ERROR;


public class GalleryFragment extends Fragment {

    public static GalleryFragment newInstance() {
        return new GalleryFragment();
    }



    public GalleryFragment() {
    }


    private static final String URL = "PHOTOS_ENDPOINT";
    private static final String TAG = GalleryFragment.class.getSimpleName();
    private RequestQueue requestQueue;
    private RecyclerView recyclerView;
    private ArrayList<Gallery> galleryList;
    private GalleryAdapter galleryAdapter;
    private SwipeRefreshLayout swipeContainer;
    private ImageView errorIllustration;
    private TextView errorDescription;
    private MaterialButton retryButton;
    private Snackbar snackbar;
    private boolean loadComplete,isConnected,cachedData;
    private CoordinatorLayout coordinatorLayout;
    private Disposable networkDisposable;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        loadComplete = false;
        View view = inflater.inflate(R.layout.fragment_gallery, container, false);
        coordinatorLayout = view.findViewById(R.id.galleryFragment);


        galleryList = new ArrayList<Gallery>();
        galleryAdapter = new GalleryAdapter(getActivity(), galleryList);
        swipeContainer =  view.findViewById(R.id.swipe_container);
        swipeContainer.setColorSchemeResources(R.color.black);

        requestQueue = VolleySingleton.getInstance(getActivity()).getRequestQueue();

        recyclerView = view.findViewById(R.id.recycler_view);
        RecyclerView.LayoutManager mLayoutManager =
                new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration
                (Objects.requireNonNull(getActivity()),
                        LinearLayoutManager.VERTICAL,
                        0));
        recyclerView.setAdapter(galleryAdapter);

        errorIllustration = (ImageView)view.findViewById(R.id.error_illustration);
        errorDescription = view.findViewById(R.id.error_description);
        retryButton = view.findViewById(R.id.retry_button);

        retryButton.setOnClickListener(this::retryGallery);
        setOverScrollColor();

        swipeContainer.setOnRefreshListener(this::retryGallery);

        //fetchGallery();
        cachedData = showCachedData();
        return view;
    }

    private void fetchGallery() {
        if(cachedData)
            swipeContainer.setRefreshing(true);
        else
            setState(STATE_LOADING);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, URL, null, response -> {
                    JSONArray jsonArray = response.optJSONArray(JSON_ARRAY_RESULT);


                    List<Gallery> galleries = new Gson().fromJson(jsonArray.toString(), new TypeToken<List<Gallery>>() {
                    }.getType());

                    galleryList.clear();
                    galleryList.addAll(galleries);

                    storeData(galleryList);

                    if(galleryList.size()==0)
                        setState(STATE_EMPTY);
                    else {
                        setState(STATE_CONTENT);
                        loadComplete  = true;
                    }

                    }, error -> {
                    Log.e(TAG, "Error: " + error.getMessage());
                    showFetchFailedToast();
                    showCachedData();
                });
        jsonObjectRequest.setTag(REQUEST_QUEUE_TAG_GALLERY);
        requestQueue.add(jsonObjectRequest);


    }


    private void setOverScrollColor(){
        recyclerView.setEdgeEffectFactory(new RecyclerView.EdgeEffectFactory() {
            @NonNull
            @Override protected EdgeEffect createEdgeEffect(@NonNull RecyclerView view, int direction)
            { EdgeEffect edge = new EdgeEffect(view.getContext());
                edge.setColor(ContextCompat.getColor(view.getContext(), R.color.colorGrey2)); return edge; } });
    }

    private void retryGallery(){
        if(isConnected) {
            fetchGallery();
        }
        else {
            swipeContainer.setRefreshing(false);
            showConnectionSnackBar();
        }
    }
    private void retryGallery(View view){
        retryGallery();
    }



    private void setState(int state){
        switch (state){
            case STATE_EMPTY:
                recyclerView.setVisibility(View.GONE);
                errorIllustration.setImageResource(R.drawable.illustration_empty);
                errorDescription.setText(R.string.nothing_here);
                errorIllustration.setVisibility(View.VISIBLE);
                errorDescription.setVisibility(View.VISIBLE);
                retryButton.setVisibility(View.GONE);
                swipeContainer.setEnabled(true);
                swipeContainer.setRefreshing(false);
                break;

            case STATE_UNKNOWN_ERROR:
                recyclerView.setVisibility(View.GONE);
                errorIllustration.setImageResource(R.drawable.illustration_error_unknown);
                errorDescription.setText(R.string.something_went_wrg);
                errorIllustration.setVisibility(View.VISIBLE);
                errorDescription.setVisibility(View.VISIBLE);
                retryButton.setVisibility(View.VISIBLE);
                swipeContainer.setRefreshing(false);
                swipeContainer.setEnabled(false);
                break;
            case STATE_NO_CONNECTION:
                recyclerView.setVisibility(View.GONE);
                errorIllustration.setImageResource(R.drawable.illustration_no_connection);
                errorDescription.setText(R.string.no_connection_snackbar);
                errorIllustration.setVisibility(View.VISIBLE);
                errorDescription.setVisibility(View.VISIBLE);
                retryButton.setVisibility(View.VISIBLE);
                swipeContainer.setRefreshing(false);
                swipeContainer.setEnabled(false);
                showConnectionSnackBar();
                break;
            case STATE_CONTENT:
                swipeContainer.setRefreshing(false);
                errorIllustration.setVisibility(View.GONE);
                errorDescription.setVisibility(View.GONE);
                retryButton.setVisibility(View.GONE);
                galleryAdapter.notifyDataSetChanged();
                recyclerView.setVisibility(View.VISIBLE);
                swipeContainer.setEnabled(true);
                break;


            case STATE_LOADING:
                galleryList.clear();
                swipeContainer.setRefreshing(true);
                galleryAdapter.notifyDataSetChanged();
                errorIllustration.setVisibility(View.GONE);
                errorDescription.setVisibility(View.GONE);
                retryButton.setVisibility(View.GONE);
                swipeContainer.setEnabled(true);
                break;

        }
    }


    private void showFetchFailedToast()  {
        Snackbar.make(coordinatorLayout,"Couldn't fetch photos",Snackbar.LENGTH_SHORT).show();
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
                    retryGallery(v);
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
            String json = prefs.getString(PREF_KEY_GALLERY_CACHE,null);
            Type type = new TypeToken<ArrayList<Gallery>>() {}.getType();
            ArrayList galleryCacheList;
            galleryCacheList = gson.fromJson(json,type);


            if(galleryCacheList==null){
                galleryCacheList = new ArrayList<Gallery>();
                if(!isConnected) {
                    setState(STATE_NO_CONNECTION);
                }
                else {
                    setState(STATE_UNKNOWN_ERROR);
                }
                return false;
            }else {
                galleryList.clear();
                galleryList.addAll(galleryCacheList);
                setState(STATE_CONTENT);
                return true;
            }
        }catch (NullPointerException e){
            e.printStackTrace();
            return false;
        }

    }


    private void storeData(ArrayList<Gallery> galleryList) {
        try {
            SharedPreferences prefs = Objects.requireNonNull(getActivity()).getSharedPreferences(PACKAGE_NAME,MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            Gson gson = new Gson();
            String json = gson.toJson(galleryList);
            editor.putString(PREF_KEY_GALLERY_CACHE,json);
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
            if(snackbar !=null){
                snackbar.dismiss();
            }
            isConnected = true;
            //swipeContainer.setEnabled(true);
            if(!loadComplete)
                fetchGallery();
        }else{
            isConnected = false;
            showConnectionSnackBar();
        }
    }
}
