package com.ashwinkanchana.cansis.adapters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.widget.ContentLoadingProgressBar;
import androidx.recyclerview.widget.RecyclerView;


import java.util.List;
import java.util.Random;

import com.ashwinkanchana.cansis.R;
import com.ashwinkanchana.cansis.data.Attendance;
import com.ashwinkanchana.cansis.utils.App;


public class AttendanceAdapter extends RecyclerView.Adapter<AttendanceAdapter.MyViewHolder> {
    private  List<Attendance> attendanceList;
    private OnItemClickListener mListener;
    private Context context;
    private int color;
    private TypedArray colorList;


    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }



    public AttendanceAdapter(Context attdContext, List<Attendance> attendanceList) {
        context = attdContext;
        this.attendanceList = attendanceList;
        colorList = context.getResources().obtainTypedArray(R.array.custom);
        randomColor();
    }


    public final class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView name, value, id, retry;
        private RelativeLayout attd_thumbnail;
        private ContentLoadingProgressBar progress;
        private View mView;



        public MyViewHolder(View view, AttendanceAdapter.OnItemClickListener listener) {
            super(view);
            mView = view;
            attd_thumbnail = view.findViewById(R.id.attendance_thumbnail);
            id = view.findViewById(R.id.attd_id);
            name = view.findViewById(R.id.attd_name);
            value = view.findViewById(R.id.attd_value);


            view.setOnClickListener(v -> {
                if(listener != null) {

                    int position = getAdapterPosition();
                    if(position != RecyclerView.NO_POSITION){
                        listener.onItemClick(position);
                    }
                }
            });
        }


    }

    @NonNull
    @Override
    public AttendanceAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_attendance, parent, false);


        return new AttendanceAdapter.MyViewHolder(itemView,mListener);
    }




    @Override
    public void onBindViewHolder(@NonNull AttendanceAdapter.MyViewHolder holder,final int position) {
        final Attendance attendance = attendanceList.get(position);
        holder.id.setText(attendance.getId());
        holder.name.setText(attendance.getName());
        holder.value.setText(attendance.getValue());


        try {
            holder.attd_thumbnail.setBackgroundTintList(ColorStateList.valueOf(colorList.getColor(color, Color.BLUE)));
        }catch (Exception e){
            e.printStackTrace();
        }


        Animation animation = AnimationUtils.loadAnimation(context, R.anim.anim_recycler_item_show);
        holder.mView.startAnimation(animation);

        AlphaAnimation aa1 = new AlphaAnimation(1.0f, 0.1f);
        aa1.setDuration(400);
        holder.attd_thumbnail.startAnimation(aa1);

        AlphaAnimation aa = new AlphaAnimation(0.1f, 1.0f);
        aa.setDuration(400);


        holder.attd_thumbnail.startAnimation(aa);

    }


    public void  randomColor(){
        Random random = new Random();
        int maxIndex;
        maxIndex = 9;
        color = random.nextInt(maxIndex);
    }




    @Override
    public int getItemCount() {
        return attendanceList.size();
    }

}