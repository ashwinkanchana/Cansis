package com.ashwinkanchana.cansis.adapters;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ashwinkanchana.cansis.R;
import com.ashwinkanchana.cansis.data.Marks;


import java.util.List;

public class MarksAdapter extends RecyclerView.Adapter<MarksAdapter.MyViewHolder> {
    private Context mContext;
    private List<Marks> marksList;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView name, value;

        public MyViewHolder(View view) {
            super(view);
            name = view.findViewById(R.id.ia_name);

            value = view.findViewById(R.id.ia_value);

        }
    }


    public MarksAdapter(Context mContext, List<Marks> marksList) {
        this.mContext = mContext;
        this.marksList = marksList;
    }


    @Override
    public MarksAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_marks, parent, false);

        return new MarksAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        final Marks marks = marksList.get(position);
        holder.name.setText(marks.getName());

        holder.value.setText(marks.getValue());


    }


    @Override
    public int getItemCount() {
        return marksList.size();
    }
}