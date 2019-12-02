package com.example.meetapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.meetapp.utility.TimeSlot;

import java.util.List;

import androidx.recyclerview.widget.RecyclerView;

public class suggestedTimesRecyclerViewAdapter extends RecyclerView.Adapter<suggestedTimesRecyclerViewAdapter.ViewHolder> {

    private List<TimeSlot> mData;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;

    // data is passed into the constructor
    suggestedTimesRecyclerViewAdapter(Context context, List<TimeSlot> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.suggested_times_recycler_view_row, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String startTime = mData.get(position).getStartTimeStr();
        String endTime = mData.get(position).getEndTimeStr();
        String duration = mData.get(position).getDurationStr();
        String absentees = mData.get(position).getAbsenteesString();

        holder.freeTimeAbsenteesTextView.setText(absentees);
        holder.freeTimeStartTextView.setText(startTime);
        holder.freeTimeEndTextView.setText(endTime);
        holder.freeTimeDurationTextView.setText(duration);
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mData.size();
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView freeTimeStartTextView;
        TextView freeTimeEndTextView;
        TextView freeTimeDurationTextView;
        TextView freeTimeAbsenteesTextView;

        ViewHolder(View itemView) {
            super(itemView);
            freeTimeStartTextView = itemView.findViewById(R.id.rvrow_free_time_start);
            freeTimeEndTextView = itemView.findViewById(R.id.rvrow_free_time_end);
            freeTimeDurationTextView = itemView.findViewById(R.id.rvrow_free_time_duration);
            freeTimeAbsenteesTextView = itemView.findViewById(R.id.rvrow_absentees);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    // convenience method for getting data at click position
    TimeSlot getItem(int id) {
        return mData.get(id);
    }

    // allows clicks events to be caught
    void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}