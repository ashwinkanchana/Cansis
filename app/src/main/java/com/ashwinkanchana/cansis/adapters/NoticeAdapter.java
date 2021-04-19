package com.ashwinkanchana.cansis.adapters;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ashwinkanchana.cansis.R;
import com.ashwinkanchana.cansis.data.Notice;

import java.util.List;

public class NoticeAdapter extends RecyclerView.Adapter<NoticeAdapter.MyViewHolder> {
    private Context noticeContext;
    private List<Notice> noticeList;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView title, contents, datestamp;

        public MyViewHolder(View view) {
            super(view);
            title = view.findViewById(R.id.title);

            contents = view.findViewById(R.id.contents);


            datestamp = view.findViewById(R.id.datestamp);

        }
    }


    public NoticeAdapter(Context noticeContext, List<Notice> noticeList) {
        this.noticeContext = noticeContext;
        this.noticeList = noticeList;
    }


    @Override
    public NoticeAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notice, parent, false);

        return new NoticeAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(NoticeAdapter.MyViewHolder holder, final int position) {
        final Notice notice = noticeList.get(position);
        holder.title.setText(notice.getTitle());

        holder.contents.setText(notice.getContents());

        holder.datestamp.setText(notice.getDatestamp());


    }

    @Override
    public int getItemCount() {
        return noticeList.size();
    }
}

