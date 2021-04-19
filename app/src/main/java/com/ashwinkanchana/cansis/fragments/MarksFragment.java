package com.ashwinkanchana.cansis.fragments;


import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;

import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;


import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.EdgeEffect;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.ashwinkanchana.cansis.utils.App;
import com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ashwinkanchana.cansis.R;
import com.ashwinkanchana.cansis.adapters.MarksAdapter;
import com.ashwinkanchana.cansis.data.Marks;
import com.google.android.material.snackbar.Snackbar;


import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static com.ashwinkanchana.cansis.activities.StudentActivity.toast;
import static com.ashwinkanchana.cansis.utils.Constants.NETWORK_BROADCAST_ACTION;
import static com.ashwinkanchana.cansis.utils.Constants.NETWORK_BROADCAST_SOURCE;
import static com.ashwinkanchana.cansis.utils.Constants.NETWORK_BROADCAST_VALUE;
import static com.ashwinkanchana.cansis.utils.Constants.STATE_CONNECTED;
import static com.ashwinkanchana.cansis.utils.Constants.STATE_CONTENT;
import static com.ashwinkanchana.cansis.utils.Constants.STATE_EMPTY;
import static com.ashwinkanchana.cansis.utils.Constants.STATE_LOADING;
import static com.ashwinkanchana.cansis.utils.Constants.STATE_NO_CONNECTION;
import static com.ashwinkanchana.cansis.utils.Constants.STATE_UNKNOWN_ERROR;



public class MarksFragment extends Fragment {


    public static MarksFragment newInstance(int argId,String argSubName,String argSubSem,String argStudentID,int argColor) {
        MarksFragment marksFragment = new MarksFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_ID,argId);
        args.putString(ARG_SUB_NAME,argSubName);
        args.putString(ARG_SUB_SEM,argSubSem);
        args.putString(ARG_STUDENT_ID,argStudentID);
        args.putInt(ARG_COLOR,argColor);
        marksFragment.setArguments(args);
        return marksFragment;
    }

    public MarksFragment() {
        // Required empty public constructor
    }


    private static final String MARKS_URL = "MARKS_ENDPOINT";
    private static final String TAG = MarksFragment.class.getSimpleName();
    private static final String ARG_ID = "argId";
    private static final String ARG_SUB_NAME = "argSubName";
    private static final String ARG_SUB_SEM = "argSubSem";
    private static final String ARG_STUDENT_ID = "argStudentID";
    private static final String ARG_COLOR = "argColor";
    private String studentID;
    private String subName;
    private RecyclerView recyclerView;
    private LinearLayout content;
    private List<Marks> marksList;
    private MarksAdapter marksAdapter;
    private TextView errorDescription;
    private MaterialButton retryButton;
    private String subSem;
    private TextView maxMarksTextView,subNameTextView,semTextView;
    private SwipeRefreshLayout swipeContainer;
    private MarksScrape marksScrape;
    private int subId,color,currentState;
    private CoordinatorLayout coordinatorLayout;
    private Snackbar snackbar;
    private Disposable networkDisposable;



    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_marks, container, false);
        if(getArguments() != null){
            subId = getArguments().getInt(ARG_ID);
            studentID = getArguments().getString(ARG_STUDENT_ID);
            subName = getArguments().getString(ARG_SUB_NAME);
            subSem = getArguments().getString(ARG_SUB_SEM);
            color = getArguments().getInt(ARG_COLOR);


            MaterialToolbar toolbar = view.findViewById(R.id.toolbar);
            try {
                @SuppressLint("Recycle")
                TypedArray colors = Objects.requireNonNull(getActivity()).getResources().obtainTypedArray(R.array.custom);

                toolbar.setBackgroundTintList(ColorStateList.valueOf(colors.getColor(color,1)));
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        coordinatorLayout = view.findViewById(R.id.marksFragment);
        content = view.findViewById(R.id.content_marks);
        recyclerView = view.findViewById(R.id.recycler_view);
        marksList = new ArrayList<Marks>();
        marksAdapter = new MarksAdapter(getActivity(), marksList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setAdapter(marksAdapter);
        errorDescription = view.findViewById(R.id.error_description);
        retryButton = view.findViewById(R.id.retry_button);
        swipeContainer = view.findViewById(R.id.swipe_container);
        retryButton.setOnClickListener(this::retryMarks);
        subNameTextView = view.findViewById(R.id.s_m_name);
        semTextView = view.findViewById(R.id.s_m_sem);
        maxMarksTextView = view.findViewById(R.id.s_m_max);
        subNameTextView.setText(subName);
        semTextView.setText(subSem);
        ImageView backBtn = view.findViewById(R.id.back_nav);
        backBtn.setOnClickListener(this::onBackClick);

        setOverScrollColor();

        swipeContainer.setOnRefreshListener(this::fetchMarks);

        fetchMarks();
        return view;
    }

    private void onBackClick(View v) {
        Objects.requireNonNull(getActivity()).onBackPressed();
    }


    private void fetchMarks() {
        if(!App.getInstance().isConnected())
            setState(STATE_NO_CONNECTION);
        else {
            setState(STATE_LOADING);
            marksList.clear();
            if (marksScrape != null && marksScrape.getStatus() == AsyncTask.Status.RUNNING) {
                marksScrape.cancel(true);
                marksScrape = new MarksScrape(this);
                marksScrape.execute(studentID, subName);
            } else if (marksScrape != null && marksScrape.getStatus() == AsyncTask.Status.PENDING) {
                marksScrape.cancel(true);
                marksScrape = new MarksScrape(this);
                marksScrape.execute(studentID, subName);
            } else {
                marksScrape = new MarksScrape(this);
                marksScrape.execute(studentID, subName);
            }
        }
    }


    private void showData(ArrayList<Marks> marks){
        marksList.clear();
        marksList.addAll(marks);
        if(marksList.size()==0)
            setState(STATE_EMPTY);
        else {
            setState(STATE_CONTENT);
        }

    }

    private void setOverScrollColor(){
        recyclerView.setEdgeEffectFactory(new RecyclerView.EdgeEffectFactory() {
            @NonNull
            @Override protected EdgeEffect createEdgeEffect(@NonNull RecyclerView view, int direction)
            { EdgeEffect edge = new EdgeEffect(view.getContext());
                edge.setColor(ContextCompat.getColor(view.getContext(), R.color.colorGrey2)); return edge; } });
    }


    private void retryMarks(View view){
        if (App.getInstance().isConnected())
           fetchMarks();
    }
    private void retryMarks(){
        if (App.getInstance().isConnected())
            fetchMarks();
    }


    private void setState(int state){
        currentState = state;
        switch (state){
            case STATE_EMPTY:
                recyclerView.setVisibility(View.INVISIBLE);
                maxMarksTextView.setVisibility(View.INVISIBLE);
                errorDescription.setText(R.string.nothing_here);
                errorDescription.setVisibility(View.VISIBLE);
                retryButton.setVisibility(View.GONE);
                swipeContainer.setEnabled(true);
                swipeContainer.setRefreshing(false);
                break;

            case STATE_UNKNOWN_ERROR:
                recyclerView.setVisibility(View.INVISIBLE);
                maxMarksTextView.setVisibility(View.INVISIBLE);
                errorDescription.setText(R.string.something_went_wrg);
                errorDescription.setVisibility(View.VISIBLE);
                retryButton.setVisibility(View.VISIBLE);
                swipeContainer.setRefreshing(false);
                swipeContainer.setEnabled(false);
                break;
            case STATE_NO_CONNECTION:
                recyclerView.setVisibility(View.INVISIBLE);
                maxMarksTextView.setVisibility(View.INVISIBLE);
                errorDescription.setText(R.string.no_connection_snackbar);
                errorDescription.setVisibility(View.VISIBLE);
                retryButton.setVisibility(View.VISIBLE);
                swipeContainer.setRefreshing(false);
                swipeContainer.setEnabled(false);
                break;

            case STATE_CONTENT:

                swipeContainer.setRefreshing(false);
                errorDescription.setVisibility(View.GONE);
                retryButton.setVisibility(View.GONE);
                marksAdapter.notifyDataSetChanged();
                recyclerView.setVisibility(View.VISIBLE);
                maxMarksTextView.setVisibility(View.VISIBLE);
                swipeContainer.setEnabled(true);
                break;


            case STATE_LOADING:
                swipeContainer.setRefreshing(true);
                marksList.clear();
                marksAdapter.notifyDataSetChanged();
                recyclerView.setVisibility(View.INVISIBLE);
                maxMarksTextView.setVisibility(View.INVISIBLE);
                errorDescription.setVisibility(View.GONE);
                retryButton.setVisibility(View.GONE);
                swipeContainer.setEnabled(true);
                break;

        }
    }

    @Override
    public void onDestroy() {
        if (marksScrape != null && marksScrape.getStatus() == AsyncTask.Status.RUNNING) {
            marksScrape.cancel(true);
        } else if (marksScrape != null && marksScrape.getStatus() == AsyncTask.Status.PENDING) {
            marksScrape.cancel(true);
        }
        super.onDestroy();

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
            if(toast!=null){
                toast.cancel();
            }
            if(currentState == STATE_NO_CONNECTION||currentState ==STATE_UNKNOWN_ERROR)
                retryMarks();

        }
        else{
            setState(STATE_NO_CONNECTION);
        }
    }




    private static class MarksScrape extends AsyncTask<String, String,ArrayList> {
        private WeakReference<MarksFragment> fragmentWeakReference;
        MarksScrape(MarksFragment fragment){
            fragmentWeakReference = new WeakReference<MarksFragment>(fragment);
        }


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            MarksFragment fragment = fragmentWeakReference.get();
            if(fragment == null || fragment.isRemoving()){
                return;
            }
            fragment.setState(STATE_LOADING);
        }

        @Override
        protected ArrayList<Marks> doInBackground(String... strings) {
            ArrayList<Marks> marksArrayList = new ArrayList<>();
            List<String> res =  fetchMarks(strings[0],strings[1]);
            try {
                for(int i = 0; i<(res.size()/2); i++){
                    if(res.get(i+4).trim().equals("")){
                        marksArrayList.add(new Marks(res.get(i),"-"));
                    }else{
                        marksArrayList.add(new Marks(res.get(i),res.get(i+4)));
                    }


                }

                return marksArrayList;

            }catch (Exception e){
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onProgressUpdate(String... maxmarks) {
            super.onProgressUpdate(maxmarks);
            MarksFragment fragment = fragmentWeakReference.get();
            if(fragment == null || fragment.isRemoving()){
                return;
            }
            Log.d(TAG, "onProgressUpdate: "+maxmarks[0]);
            String maximum = "Maximum marks - "+ maxmarks[0];
            fragment.maxMarksTextView.setText(maximum);
        }

        @Override
        protected void onPostExecute(ArrayList marks) {
            super.onPostExecute(marks);
            MarksFragment fragment = fragmentWeakReference.get();
            if(fragment == null || fragment.isRemoving()){
                return;
            }
            else if(!App.getInstance().isConnected())
                fragment.setState(STATE_NO_CONNECTION);
            else if (marks==null) {
                fragment.setState(STATE_UNKNOWN_ERROR);
            }
            else {
                fragment.showData(marks);
            }
        }







        private List<String> fetchMarks(String studentID, String subName){
            try {

                URL url = new URL(MARKS_URL);
                Map<String,Object> params = new LinkedHashMap<>();
                params.put("stu", studentID);
                params.put("subject",subName);


                StringBuilder postData = new StringBuilder();
                for (Map.Entry<String,Object> param : params.entrySet()) {
                    if (postData.length() != 0) postData.append('&');
                    postData.append(URLEncoder.encode(param.getKey(), "UTF-8"/*String.valueOf(StandardCharsets.UTF_8)*/));
                    postData.append('=');
                    postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"/*String.valueOf(StandardCharsets.UTF_8)*/));
                }
                Log.d(TAG, "fetchMarks: "+postData.toString());
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

                List<String> resList = new ArrayList<>();

                String maxMarks = " ";
                String regexString = Pattern.quote("Maxmarks: ") + "(.*?)" + Pattern.quote("</caption>");
                Pattern pattern = Pattern.compile(regexString);
                Matcher matcher = pattern.matcher(res);
                while (matcher.find()) {
                    maxMarks = matcher.group(1);
                }
                if(maxMarks!= null){
                    publishProgress(maxMarks);
                }


                regexString = Pattern.quote("<td style='text-align:center;'>") + "(.*?)" + Pattern.quote("</td>");
                Pattern p = Pattern.compile(regexString);
                Matcher m = p.matcher(res);
                while(m.find()) {
                    resList.add(m.group(1));
                }
                return resList;
            }catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }

    }
}
