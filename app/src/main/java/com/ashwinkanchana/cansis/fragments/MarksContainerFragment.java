package com.ashwinkanchana.cansis.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.widget.ContentLoadingProgressBar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.ashwinkanchana.cansis.utils.App;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ashwinkanchana.cansis.R;
import com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static android.content.Context.MODE_PRIVATE;
import static com.ashwinkanchana.cansis.utils.Constants.NETWORK_BROADCAST_ACTION;
import static com.ashwinkanchana.cansis.utils.Constants.NETWORK_BROADCAST_SOURCE;
import static com.ashwinkanchana.cansis.utils.Constants.NETWORK_BROADCAST_VALUE;
import static com.ashwinkanchana.cansis.utils.Constants.PREF_KEY_CURRENT_SEM;
import static com.ashwinkanchana.cansis.utils.Constants.PREF_KEY_ENCODED_NAME;
import static com.ashwinkanchana.cansis.utils.Constants.PREF_KEY_STUDENT_ID;
import static com.ashwinkanchana.cansis.utils.Constants.PREF_KEY_USN;
import static com.ashwinkanchana.cansis.utils.Constants.STATE_CONNECTED;
import static com.ashwinkanchana.cansis.utils.Constants.STATE_LOADING;
import static com.ashwinkanchana.cansis.utils.Constants.STATE_NO_CONNECTION;
import static com.ashwinkanchana.cansis.utils.Constants.STATE_UNKNOWN_ERROR;
import static com.ashwinkanchana.cansis.utils.App.PACKAGE_NAME;


public class MarksContainerFragment extends Fragment {
    private static final String SEMESTER_URL = "SEMESTER_WEB_URL";
    private static final String TAG = MarksContainerFragment.class.getSimpleName();
    private String usn,name,studentID;
    private int semesterCount;
    private ContentLoadingProgressBar progressBar;
    private TextView errorDescription;
    private ImageView errorIllustration;
    private Button retryButton;
    private MarksSemScrape marksSemScrape;
    private SharedPreferences prefs;
    private boolean loadComplete,isConnected,cachedData;
    private CoordinatorLayout coordinatorLayout;
    private Disposable networkDisposable;


    public static MarksContainerFragment newInstance() {
        return new MarksContainerFragment();
    }


    public MarksContainerFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_marks_container, container, false);
        prefs =  Objects.requireNonNull(getActivity()).getSharedPreferences(PACKAGE_NAME,MODE_PRIVATE);

        coordinatorLayout = view.findViewById(R.id.marksContainer);

        errorIllustration = (ImageView)view.findViewById(R.id.error_illustration);
        errorDescription = view.findViewById(R.id.error_description_attd);
        progressBar = view.findViewById(R.id.progress_horizontal_attd);
        retryButton = view.findViewById(R.id.retry_button_attd);
        retryButton.setOnClickListener(this::retry);
        cachedData = getCacheSem();
        return view;
    }

    private boolean getCacheSem(){
        semesterCount = prefs.getInt(PREF_KEY_CURRENT_SEM,-1);
        usn = prefs.getString(PREF_KEY_USN, null);
        name = prefs.getString(PREF_KEY_ENCODED_NAME, null);
        studentID = prefs.getString(PREF_KEY_STUDENT_ID,null);


        if(semesterCount>=1 && semesterCount<=8 && name != null){
            initRecyclerView(semesterCount, name);
            return true;
        }
        else
            return false;

    }

    private void sendSemesterRequest(String usn) {
        if(marksSemScrape!=null && marksSemScrape.getStatus() == AsyncTask.Status.RUNNING) {
            marksSemScrape.cancel(true);
            marksSemScrape = new MarksSemScrape(this);
            marksSemScrape.execute(usn);
        }else if(marksSemScrape!=null &&marksSemScrape.getStatus() == AsyncTask.Status.PENDING) {
            marksSemScrape.cancel(true);
            marksSemScrape = new MarksSemScrape(this);
            marksSemScrape.execute(usn);
        }else{
            marksSemScrape = new MarksSemScrape(this);
            marksSemScrape.execute(usn);
        }

    }

    private void retry(View view) {
        if(isConnected)
                sendSemesterRequest(usn);
        else {
            setState(STATE_NO_CONNECTION);
        }
    }




    private void setState(int state){
        switch (state){
            case STATE_UNKNOWN_ERROR:
                progressBar.setVisibility(View.GONE);
                errorIllustration.setImageResource(R.drawable.illustration_error_unknown);
                errorIllustration.setVisibility(View.VISIBLE);
                errorDescription.setText(R.string.something_went_wrg);
                errorDescription.setVisibility(View.VISIBLE);
                retryButton.setVisibility(View.VISIBLE);
                break;

            case STATE_NO_CONNECTION:
                errorIllustration.setImageResource(R.drawable.illustration_no_connection);
                errorIllustration.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                errorDescription.setText(R.string.no_connection_snackbar);
                errorDescription.setVisibility(View.VISIBLE);
                retryButton.setVisibility(View.VISIBLE);
                break;

            case STATE_LOADING:
                errorIllustration.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
                errorDescription.setVisibility(View.GONE);
                retryButton.setVisibility(View.GONE);
                break;

        }
    }

    private void storeData(ArrayList<String> sems){
        name=sems.get(0).replace(" ","+");
        prefs.edit().putInt(PREF_KEY_CURRENT_SEM,sems.size()-1).apply();
        prefs.edit().putString(PREF_KEY_ENCODED_NAME,name).apply();

        if(!cachedData)
            initRecyclerView(sems.size()-1,name);
    }

    private void initRecyclerView(int sems,String name){
        try {
            FragmentTransaction ft = Objects.requireNonNull(getActivity()).getSupportFragmentManager().beginTransaction();
            ft.add(R.id.marksContainer, MarksSubjectFragment.newInstance(sems,name,usn,studentID));
            //marksSemScrape.cancel(true);
            ft.commit();
        }catch (Exception e){e.printStackTrace();}

    }

    @Override
    public void onResume() {
        super.onResume();
        networkDisposable = ReactiveNetwork.observeNetworkConnectivity(Objects.requireNonNull(getActivity()))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(connectivity -> {
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
            progressBar.setVisibility(View.VISIBLE);
            isConnected = true;
            if(!loadComplete)
                sendSemesterRequest(usn);
            else getCacheSem();
        }
        else{

            isConnected = false;
            setState(STATE_NO_CONNECTION);
        }
    }






    private static class MarksSemScrape extends AsyncTask<String, Void,ArrayList> {
        private WeakReference<MarksContainerFragment> fragmentWeakReference;
        MarksSemScrape(MarksContainerFragment fragment){
            fragmentWeakReference = new WeakReference<MarksContainerFragment>(fragment);
        }


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            MarksContainerFragment fragment = fragmentWeakReference.get();
            if(fragment == null || fragment.isRemoving()){
                return;
            }
            fragment.setState(STATE_LOADING);
        }

        @Override
        protected ArrayList<String> doInBackground(String... strings) {
            return fetchSemester(strings[0]);
        }

        @Override
        protected void onPostExecute(ArrayList sems) {
            super.onPostExecute(sems);
            MarksContainerFragment fragment = fragmentWeakReference.get();
            if(fragment == null || fragment.isRemoving()){
                return;
            }
            else if(!App.getInstance().isConnected())
                fragment.setState(STATE_NO_CONNECTION);
            else if (sems==null || !((sems.size()-1)>=1&&(sems.size()-1)<=8))
                fragment.setState(STATE_UNKNOWN_ERROR);
            else {
                fragment.loadComplete = true;
                fragment.storeData(sems);
               }
        }



        private ArrayList<String> fetchSemester(String usn){
            try {

                URL url = new URL(SEMESTER_URL);
                Map<String,Object> params = new LinkedHashMap<>();
                params.put("susn", usn);


                StringBuilder postData = new StringBuilder();
                for (Map.Entry<String,Object> param : params.entrySet()) {
                    if (postData.length() != 0) postData.append('&');
                    postData.append(URLEncoder.encode(param.getKey(), "UTF-8"/*String.valueOf(StandardCharsets.UTF_8)*/));
                    postData.append('=');
                    postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"/*String.valueOf(StandardCharsets.UTF_8)*/));
                }
                byte[] postDataBytes = postData.toString().getBytes("UTF-8"/*String.valueOf(StandardCharsets.UTF_8)*/);

                HttpURLConnection conn = (HttpURLConnection)url.openConnection();
                conn.setRequestMethod("POST");

                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
                conn.setDoOutput(true);
                conn.getOutputStream().write(postDataBytes);

                Reader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"/*String.valueOf(StandardCharsets.UTF_8)*/));
                StringBuilder str = new StringBuilder();


                for (int c; (c = in.read()) >= 0;) {
                    str.append((char)c);
                }
                String res = str.toString();

                ArrayList<String> sems = new ArrayList<>();
                String studentName = " ";
                String regexString = Pattern.quote("name=\"sname\" value=\"") + "(.*?)" + Pattern.quote("\"/>");
                Pattern pattern = Pattern.compile(regexString);
                Matcher matcher = pattern.matcher(res);
                while (matcher.find()) {
                    studentName = matcher.group(1);
                }



                Pattern p = Pattern.compile("\\Q<option value=\"\\E(.*?)\\Q</option>\\E");
                Matcher m = p.matcher(res);
                String[] arrOfStr;

                while(m.find()) {
                    arrOfStr = Objects.requireNonNull(m.group(1)).split("\">", 2);
                    sems.add(arrOfStr[1]);
                }
                    sems.set(0,studentName);
                    return sems;
            }catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }

    }


}
