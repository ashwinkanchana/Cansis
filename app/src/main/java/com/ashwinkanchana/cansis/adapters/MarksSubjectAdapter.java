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
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Random;

import com.ashwinkanchana.cansis.R;
import com.ashwinkanchana.cansis.data.MarksSubject;
import com.ashwinkanchana.cansis.utils.App;

public class MarksSubjectAdapter extends RecyclerView.Adapter<MarksSubjectAdapter.MyViewHolder> {

    private List<MarksSubject> marksSubjectList;
    private OnItemClickListener mListener;
    private Context context;
    private int color;
    private TypedArray colorList;



    public interface OnItemClickListener {
        void onItemClick(int position,int color);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }



    public MarksSubjectAdapter(Context marksContext, List<MarksSubject> marksList) {
        context = marksContext;
        this.marksSubjectList = marksList;
        colorList = context.getResources().obtainTypedArray(R.array.custom);
        randomColor();
    }



    public final class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView s_m_sub,s_m_no;
        private RelativeLayout marks_thumbnail;
        private View mView;



        public MyViewHolder(View view,OnItemClickListener listener) {
            super(view);
            mView = view;
            marks_thumbnail = view.findViewById(R.id.marks_thumbnail);
            s_m_sub = view.findViewById(R.id.s_m_sub);
            s_m_no = view.findViewById(R.id.s_m_no);



            view.setOnClickListener(v -> {
                if(listener != null) {

                    int position = getAdapterPosition();
                    if(position != RecyclerView.NO_POSITION){
                        listener.onItemClick(position,color);
                    }
                }
            });

        }
    }








    @NonNull
    @Override
    public MarksSubjectAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_marks_subject, parent, false);


        return new MarksSubjectAdapter.MyViewHolder(itemView,mListener);
    }


    @Override
    public void onBindViewHolder(@NonNull MarksSubjectAdapter.MyViewHolder holder, final int position) {
        final MarksSubject marksSubject = marksSubjectList.get(position);


        holder.s_m_no.setText(marksSubject.getSubjectNo());
        holder.s_m_sub.setText(marksSubject.getSubjectName());
        try {

            holder.marks_thumbnail.setBackgroundTintList(ColorStateList.valueOf(colorList.getColor(color, Color.BLUE)));
        }catch (Exception e){
            e.printStackTrace();
        }




        Animation animation = AnimationUtils.loadAnimation(context, R.anim.anim_recycler_item_show);
        holder.mView.startAnimation(animation);

        AlphaAnimation aa1 = new AlphaAnimation(1.0f, 0.1f);
        aa1.setDuration(400);
        holder.marks_thumbnail.startAnimation(aa1);

        AlphaAnimation aa = new AlphaAnimation(0.1f, 1.0f);
        aa.setDuration(400);


        holder.marks_thumbnail.startAnimation(aa);


    }



    public void randomColor(){
        Random random = new Random();
        int maxIndex = 9;
        color = random.nextInt(maxIndex);

    }

    @Override
    public int getItemCount() {
        return marksSubjectList.size();
    }



}
