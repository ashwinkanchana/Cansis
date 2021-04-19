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
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EdgeEffect;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.ashwinkanchana.cansis.utils.Constants;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipDrawable;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.ref.WeakReference;
import java.lang.reflect.Type;
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
import com.ashwinkanchana.cansis.adapters.MarksSubjectAdapter;
import com.ashwinkanchana.cansis.utils.App;
import com.ashwinkanchana.cansis.data.MarksSubject;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static android.content.Context.MODE_PRIVATE;
import static com.ashwinkanchana.cansis.activities.StudentActivity.bottomSelectedId;
import static com.ashwinkanchana.cansis.activities.StudentActivity.toast;
import static com.ashwinkanchana.cansis.utils.Constants.NETWORK_BROADCAST_ACTION;
import static com.ashwinkanchana.cansis.utils.Constants.NETWORK_BROADCAST_SOURCE;
import static com.ashwinkanchana.cansis.utils.Constants.NETWORK_BROADCAST_VALUE;
import static com.ashwinkanchana.cansis.utils.Constants.PREF_KEY_DEFAULT_SEMESTER;
import static com.ashwinkanchana.cansis.utils.Constants.PREF_KEY_MARKS_SUBS_CACHE;
import static com.ashwinkanchana.cansis.utils.Constants.PREF_KEY_MARKS_SUBS_CACHE_SEM;
import static com.ashwinkanchana.cansis.utils.Constants.SEMESTERS;
import static com.ashwinkanchana.cansis.utils.Constants.STATE_CHOOSE_SEM;
import static com.ashwinkanchana.cansis.utils.Constants.STATE_CONNECTED;
import static com.ashwinkanchana.cansis.utils.Constants.STATE_CONTENT;
import static com.ashwinkanchana.cansis.utils.Constants.STATE_EMPTY;
import static com.ashwinkanchana.cansis.utils.Constants.STATE_LOADING;
import static com.ashwinkanchana.cansis.utils.Constants.STATE_NO_CONNECTION;
import static com.ashwinkanchana.cansis.utils.Constants.STATE_UNKNOWN_ERROR;
import static com.ashwinkanchana.cansis.utils.App.PACKAGE_NAME;


public class MarksSubjectFragment extends Fragment  {


    public static MarksSubjectFragment newInstance(int currentSemester,String studentName,String usn,String studentID) {
        MarksSubjectFragment marksSubjectFragment = new MarksSubjectFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SEMESTER,currentSemester);
        args.putString(ARG_STUDENT_NAME,studentName);
        args.putString(ARG_STUDENT_ID,studentID);
        args.putString(ARG_STUDENT_USN,usn);
        marksSubjectFragment.setArguments(args);
        return marksSubjectFragment;
    }


    public MarksSubjectFragment() {
        // Required empty public constructor
    }



    private static final String MARKS_SUBJECT_URL = "SUBJECT_MARKS_URL";
    private static final String TAG = MarksSubjectFragment.class.getSimpleName();
    private static final String ARG_SEMESTER = "argSemester";
    private static final String ARG_STUDENT_ID = "argStudentID";
    private static final String ARG_STUDENT_NAME = "argStudentName";
    private static final String ARG_STUDENT_USN = "argStudentUsn";

    private static MarksSubjectScrape marksSubjectScrape;
    private RecyclerView recyclerView;
    private List<MarksSubject> marksList;
    private MarksSubjectAdapter marksSubjectAdapter;
    private ShimmerFrameLayout shimmerContainer;
    private SwipeRefreshLayout swipeContainer;
    private ImageView errorIllustration;
    private LottieAnimationView animationView;
    private TextView errorDescription;
    private MaterialButton retryButton;
    private ExtendedFloatingActionButton eFab;
    private SharedPreferences prefs;
    private BottomSheetBehavior mBottomSheetBehavior;
    private ChipGroup chipGroup;
    private int defaultSemester,currentSemester,currentState,cacheSem;
    private String selectedSemester,studentID,studentName,usn,romanSem;
    private boolean loadComplete,isConnected,cacheData,firstLoad;
    private CoordinatorLayout coordinatorLayout;
    private Snackbar snackbar;
    private Disposable networkDisposable;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = Objects.requireNonNull(getActivity()).getSharedPreferences(PACKAGE_NAME, MODE_PRIVATE);
        defaultSemester = prefs.getInt(PREF_KEY_DEFAULT_SEMESTER,-1);
        cacheSem = prefs.getInt(PREF_KEY_MARKS_SUBS_CACHE_SEM,-1);
    }




    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_marks_subject, container, false);
        if(getArguments() != null){
            currentSemester = getArguments().getInt(ARG_SEMESTER);
            studentName = getArguments().getString(ARG_STUDENT_NAME);
            usn = getArguments().getString(ARG_STUDENT_USN);
            studentID = getArguments().getString(ARG_STUDENT_ID);
            romanSem = SEMESTERS[currentSemester];
        }

        loadComplete = false;
        cacheData = false;
        firstLoad = true;

        isConnected = App.getInstance().isConnected();

        coordinatorLayout = view.findViewById(R.id.marksSubjectFragment);

        recyclerView = view.findViewById(R.id.recycler_view);
        marksList = new ArrayList<MarksSubject>();
        marksSubjectAdapter = new MarksSubjectAdapter(getActivity(), marksList);

        View bottomSheet = view.findViewById(R.id.bottom_sheet);
        mBottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        mBottomSheetBehavior.setHideable(true);
        mBottomSheetBehavior.setSkipCollapsed(true);
        eFab = view.findViewById(R.id.marks_sem_fab);
        TextView bottomSheetHeading = view.findViewById(R.id.bottom_sheet_heading);
        chipGroup = view.findViewById(R.id.chip_group);
        addChips();
        mBottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_HIDDEN:
                        if (defaultSemester == -1)
                            mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                        break;
                    case BottomSheetBehavior.STATE_EXPANDED:
                    case BottomSheetBehavior.STATE_COLLAPSED:
                    case BottomSheetBehavior.STATE_SETTLING:
                    case BottomSheetBehavior.STATE_DRAGGING:
                    case BottomSheetBehavior.STATE_HALF_EXPANDED:
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                //Do nothing
            }
        });
        eFab.setOnClickListener(this::onFabClicked);

        swipeContainer = view.findViewById(R.id.swipe_container);
        swipeContainer.setColorSchemeResources(R.color.black);
        shimmerContainer = view.findViewById(R.id.shimmer_view_container);

        errorIllustration = view.findViewById(R.id.error_illustration);
        animationView = view.findViewById(R.id.choose_sem_animation);
        errorDescription = view.findViewById(R.id.error_description);
        retryButton = view.findViewById(R.id.retry_button);

        chipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            switch (checkedId){
                case -1: selectedSemester = getResources().getString(R.string.none);
                    marksSubjectScrape.cancel(true);
                    bottomSheetHeading.setText(R.string.none);
                    defaultSemester =-1;
                    eFab.setText(R.string.none);
                    setState(STATE_CHOOSE_SEM);
                    break;
                case R.id.chip_1:bottomSheetHeading.setText(R.string.sem1);
                    defaultSemester =1;
                    eFab.setText(R.string.roman_1);
                    selectedSemester = getResources().getString(R.string.sem1);
                    break;
                case R.id.chip_2:bottomSheetHeading.setText(R.string.sem2);
                    defaultSemester =2;
                    eFab.setText(R.string.roman_2);
                    selectedSemester = getResources().getString(R.string.sem2);
                    break;
                case R.id.chip_3:bottomSheetHeading.setText(R.string.sem3);
                    defaultSemester =3;
                    eFab.setText(R.string.roman_3);
                    selectedSemester = getResources().getString(R.string.sem3);
                    break;
                case R.id.chip_4:bottomSheetHeading.setText(R.string.sem4);
                    defaultSemester =4;
                    eFab.setText(R.string.roman_4);
                    selectedSemester = getResources().getString(R.string.sem4);
                    break;
                case R.id.chip_5:bottomSheetHeading.setText(R.string.sem5);
                    defaultSemester =5;
                    eFab.setText(R.string.roman_5);
                    selectedSemester = getResources().getString(R.string.sem5);
                    break;
                case R.id.chip_6:bottomSheetHeading.setText(R.string.sem6);
                    defaultSemester =6;
                    eFab.setText(R.string.roman_6);
                    selectedSemester = getResources().getString(R.string.sem6);
                    break;
                case R.id.chip_7:bottomSheetHeading.setText(R.string.sem7);
                    defaultSemester =7;
                    eFab.setText(R.string.roman_7);
                    selectedSemester = getResources().getString(R.string.sem7);
                    break;
                case R.id.chip_8:bottomSheetHeading.setText(R.string.sem8);
                    defaultSemester =8;
                    eFab.setText(R.string.roman_8);
                    selectedSemester = getResources().getString(R.string.sem8);
                    break;

            }
            if(defaultSemester !=-1) {
                final Handler handler = new Handler();
                handler.postDelayed(this::collapseBottomSheet, 300);
                prefs.edit().putInt(PREF_KEY_DEFAULT_SEMESTER, defaultSemester).apply();
                fetchMarksSubject();

            }else setState(STATE_CHOOSE_SEM);


        });
        retryButton.setOnClickListener(this::retryMarksSubject);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setAdapter(marksSubjectAdapter);
        setOverScrollColor();

        marksSubjectAdapter.setOnItemClickListener(this::showMarks);

        swipeContainer.setOnRefreshListener(this::retryMarksSubject);


        if(defaultSemester != -1 && cacheSem == defaultSemester)
            cacheData = showCachedData();

        chooseDefaultSem();





        return view;
    }



    private void fetchMarksSubject(){
        if(defaultSemester == -1) {
            setState(STATE_CHOOSE_SEM);
            return;
        }

        if(marksSubjectScrape!=null && marksSubjectScrape.getStatus() == AsyncTask.Status.RUNNING) {
            marksSubjectScrape.cancel(true);
            marksSubjectScrape = new MarksSubjectScrape(this);
            marksSubjectScrape.execute(studentID,studentName,usn,SEMESTERS[defaultSemester]);
        }else if(marksSubjectScrape!=null &&marksSubjectScrape.getStatus() == AsyncTask.Status.PENDING) {
            marksSubjectScrape.cancel(true);
            marksSubjectScrape = new MarksSubjectScrape(this);
            marksSubjectScrape.execute(studentID,studentName,usn,SEMESTERS[defaultSemester]);
        }else{
            marksSubjectScrape = new MarksSubjectScrape(this);
            marksSubjectScrape.execute(studentID,studentName,usn,SEMESTERS[defaultSemester]);
        }

    }

    private void showMarks(int position,int color) {
        if(isConnected) {
            FragmentManager fragmentManager2 = getFragmentManager();
            FragmentTransaction fragmentTransaction2 = fragmentManager2.beginTransaction();
            fragmentTransaction2.addToBackStack(null);
            fragmentTransaction2.add(R.id.marksContainer, MarksFragment
                            .newInstance(Integer.parseInt(marksList.get(position).getSubjectNo())
                                    , marksList.get(position).getSubjectName()
                                    , selectedSemester
                                    , studentID
                                    , color),
                    "MarksFragment");
            fragmentTransaction2.commit();
        }
        else {
            showConnectionSnackBar();
        }
    }

    private void setOverScrollColor(){

        recyclerView.setEdgeEffectFactory(new RecyclerView.EdgeEffectFactory() {
            @NonNull
            @Override protected EdgeEffect createEdgeEffect(@NonNull RecyclerView view, int direction)
            { EdgeEffect edge = new EdgeEffect(view.getContext());
                edge.setColor(ContextCompat.getColor(view.getContext(), R.color.colorGrey2)); return edge; } });

    }


    private void retryMarksSubject(){
        if(isConnected) {
            fetchMarksSubject();
        }
        else {
            swipeContainer.setRefreshing(false);
            showConnectionSnackBar();
        }
    }
    private void retryMarksSubject(View view){
        retryMarksSubject();

    }

    private void chooseDefaultSem() {
        if(defaultSemester !=-1){
            mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            switch (defaultSemester){
                case 1:chipGroup.check(R.id.chip_1);
                    break;
                case 2:chipGroup.check(R.id.chip_2);
                    break;
                case 3:chipGroup.check(R.id.chip_3);
                    break;
                case 4:chipGroup.check(R.id.chip_4);
                    break;
                case 5:chipGroup.check(R.id.chip_5);
                    break;
                case 6:chipGroup.check(R.id.chip_6);
                    break;
                case 7:chipGroup.check(R.id.chip_7);
                    break;
                case 8:chipGroup.check(R.id.chip_8);
                    break;

            }
        } else {
            setState(STATE_CHOOSE_SEM);
            mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        }
    }


    private boolean showCachedData() {
        try {
            SharedPreferences prefs = Objects.requireNonNull(getActivity()).getSharedPreferences(Constants.PACKAGE_NAME,MODE_PRIVATE);
            Gson gson = new Gson();
            String json = prefs.getString(PREF_KEY_MARKS_SUBS_CACHE,null);
            Type type = new TypeToken<ArrayList<MarksSubject>>() {}.getType();
            ArrayList marksSubjectCache;
            marksSubjectCache = gson.fromJson(json,type);


            if(marksSubjectCache==null){
                marksSubjectCache = new ArrayList<MarksSubject>();
                if(!isConnected) {
                    setState(STATE_NO_CONNECTION);
                }
                else {
                    setState(STATE_UNKNOWN_ERROR);
                }
                return false;
            }else {
                marksList.clear();
                marksList.addAll(marksSubjectCache);
                setState(STATE_CONTENT);
                return true;
            }
        }catch (NullPointerException e){
            e.printStackTrace();
            return false;
        }
    }


    private void storeData(ArrayList<MarksSubject> marksSubjects) {
        try {
            SharedPreferences prefs = Objects.requireNonNull(getActivity()).getSharedPreferences(Constants.PACKAGE_NAME,MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            Gson gson = new Gson();
            String json = gson.toJson(marksSubjects);
            editor.putString(PREF_KEY_MARKS_SUBS_CACHE,json);
            editor.putInt(PREF_KEY_MARKS_SUBS_CACHE_SEM,defaultSemester);
            editor.apply();
        }catch (Exception e){
            e.printStackTrace();
        }

    }




    private void onFabClicked(View view) {
        if(mBottomSheetBehavior.getState()==BottomSheetBehavior.STATE_HIDDEN){
            mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        }
        else if (mBottomSheetBehavior.getState()==BottomSheetBehavior.STATE_EXPANDED) {
            mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        }

    }

    private void collapseBottomSheet(){
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
    }

    private void addChips(){
        String[] sems = getActivity().getResources().getStringArray(R.array.sem_array);
        for(int i = currentSemester-1; i>=0; i--) {
            Chip chip = (Chip) new Chip(Objects.requireNonNull(getActivity()));
            chip.setChipDrawable(ChipDrawable.createFromResource(getActivity(), R.xml.item_sem_chip));
            chip.setTextAppearance(R.style.chipTextAppearance);
            chip.setText(sems[i]);
            chip.setId(R.id.chip_1+i);
            chipGroup.addView(chip);
        }

    }

    private void setState(int state){
        currentState = state;
        switch (state){
            case STATE_EMPTY:
                recyclerView.setVisibility(View.GONE);
                shimmerContainer.stopShimmer();
                shimmerContainer.setVisibility(View.GONE);
                errorIllustration.setImageResource(R.drawable.illustration_empty);
                errorDescription.setText(R.string.nothing_here);
                errorIllustration.setVisibility(View.VISIBLE);
                animationView.setVisibility(View.GONE);
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
                animationView.setVisibility(View.GONE);
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
                animationView.setVisibility(View.GONE);
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
                animationView.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                marksSubjectAdapter.notifyDataSetChanged();
                swipeContainer.setRefreshing(false);
                swipeContainer.setEnabled(true);
                break;


            case STATE_LOADING:
                marksList.clear();
                marksSubjectAdapter.randomColor();
                marksSubjectAdapter.notifyDataSetChanged();
                errorIllustration.setVisibility(View.GONE);
                errorDescription.setVisibility(View.GONE);
                animationView.setVisibility(View.GONE);
                retryButton.setVisibility(View.GONE);
                shimmerContainer.setVisibility(View.VISIBLE);
                shimmerContainer.startShimmer();
                swipeContainer.setEnabled(false);
                break;

            case STATE_CHOOSE_SEM:
                recyclerView.setVisibility(View.INVISIBLE);
                shimmerContainer.stopShimmer();
                shimmerContainer.setVisibility(View.INVISIBLE);
                errorDescription.setText(R.string.choose_sem);
                errorIllustration.setVisibility(View.GONE);
                errorDescription.setVisibility(View.VISIBLE);
                animationView.setVisibility(View.VISIBLE);
                retryButton.setVisibility(View.INVISIBLE);
                swipeContainer.setRefreshing(false);
                swipeContainer.setEnabled(false);
                break;

        }
    }




    private void loadMarksSubject(ArrayList<MarksSubject> marksSubjects){
        marksList.clear();
        marksList.addAll(marksSubjects);
        setState(STATE_CONTENT);

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
            isConnected = true;
            if(!loadComplete)
                fetchMarksSubject();

        }
        else{
            isConnected = false;
            if(!loadComplete){

                if(cacheSem == defaultSemester && cacheSem != -1) {
                    showCachedData();
                }
                else setState(STATE_NO_CONNECTION);
            }


            showConnectionSnackBar();
        }
    }

     private void showConnectionSnackBar(){
         swipeContainer.setRefreshing(false);
         if(bottomSelectedId==R.id.navMarks) {
             snackbar = Snackbar.make(coordinatorLayout, R.string.no_connection_snackbar, Snackbar.LENGTH_SHORT);
             View snackBarView = snackbar.getView();
             toast = new Toast(getActivity());
             //toast.setGravity(Gravity.TOP|Gravity.FILL_HORIZONTAL, 0, 180);
             toast.setGravity(Gravity.TOP, 0, 180);
             toast.setDuration(Toast.LENGTH_SHORT);
             toast.setView(snackBarView);
             toast.show();
         }
     }

    private static class MarksSubjectScrape extends AsyncTask<String, Void, ArrayList<MarksSubject>> {
        private WeakReference<MarksSubjectFragment> fragmentWeakReference;
        private ArrayList<MarksSubject> marksSubjectList;
        MarksSubjectScrape(MarksSubjectFragment fragment){
            fragmentWeakReference = new WeakReference<MarksSubjectFragment>(fragment);

        }


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            marksSubjectList = new ArrayList<>();
            MarksSubjectFragment fragment = fragmentWeakReference.get();
            if(fragment == null || fragment.isRemoving()){
                return;
            }

            if(fragment.isConnected)
                fragment.setState(STATE_LOADING);

        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
            MarksSubjectFragment fragment = fragmentWeakReference.get();
            if(fragment == null || fragment.isRemoving()){
                return;
            }
            fragment.swipeContainer.setRefreshing(false);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            MarksSubjectFragment fragment = fragmentWeakReference.get();
            if(fragment == null || fragment.isRemoving()){
                return;
            }
            fragment.swipeContainer.setRefreshing(false);
        }

        @Override
        protected ArrayList<MarksSubject> doInBackground(String... strings) {

            fetchMarksSubjects(strings[0],strings[1],strings[2],strings[3]);
            return marksSubjectList;
        }

        @Override
        protected void onPostExecute(ArrayList<MarksSubject> marksSubjectList) {
            super.onPostExecute(marksSubjectList);
            MarksSubjectFragment fragment = fragmentWeakReference.get();
            if(fragment == null || fragment.isRemoving()){
                return;
            }
            fragment.swipeContainer.setRefreshing(false);

            if(!fragment.isConnected) {

                if(fragment.cacheSem== fragment.defaultSemester
                        && fragment.cacheSem != -1) {
                    fragment.showCachedData();
                }
                else fragment.setState(STATE_NO_CONNECTION);
                //fragment.showConnectionSnackBar();
            }
            else if (marksSubjectList.isEmpty())
                fragment.setState(STATE_UNKNOWN_ERROR);
            else {
                fragment.loadComplete = true;
                fragment.cacheData = false;
                fragment.storeData(marksSubjectList);
                fragment.loadMarksSubject(marksSubjectList);
                }

        }





        private void fetchMarksSubjects(String studentID,String studentName,String usn,String sem){
            try {

                java.net.URL url = new URL(MARKS_SUBJECT_URL);
                Map<String,Object> params = new LinkedHashMap<>();
                params.put("std", studentID);
                params.put("sname",studentName);
                params.put("susn", usn);
                params.put("semk", sem);


                StringBuilder postData = new StringBuilder();
                for (Map.Entry<String,Object> param : params.entrySet()) {
                    if (postData.length() != 0) postData.append('&');
                    postData.append(URLEncoder.encode(param.getKey(), "UTF-8"/*String.valueOf(StandardCharsets.UTF_8)*/));
                    postData.append('=');
                    postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"/*String.valueOf(StandardCharsets.UTF_8)*/));
                }
                Log.d(TAG, "fetchMarksSubjects: "+postData.toString());
                byte[] postDataBytes = postData.toString().getBytes("UTF-8"/*String.valueOf(StandardCharsets.UTF_8)*/);

                HttpURLConnection conn = (HttpURLConnection)url.openConnection();
                conn.setRequestMethod("POST");

                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
                conn.setDoOutput(true);
                conn.getOutputStream().write(postDataBytes);

                Log.d(TAG, "fetchMarksSubjects: htt conn "+conn.getResponseCode());
                Reader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"/*String.valueOf(StandardCharsets.UTF_8)*/));
                StringBuilder str = new StringBuilder();


                for (int c; (c = in.read()) >= 0;) {
                    str.append((char)c);
                }
                String res = str.toString();

                //regex to get lis tof subjects.
                Pattern p = Pattern.compile("\\<tr><td align=\"center\">(.*?)\\</a></td>");
                Matcher m = p.matcher(res);
                String[] arrOfStr;
                while(m.find()) {
                    arrOfStr = Objects.requireNonNull(m.group(1)).split("</td><td>", 2);
                    MarksSubject subject = new MarksSubject(arrOfStr[0], arrOfStr[1]);
                    marksSubjectList.add(subject);
                }
                publishProgress();
            }catch (Exception e){
                e.printStackTrace();
                publishProgress();
            }
        }

    }



}
