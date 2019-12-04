package com.example.meetapp.utility;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.meetapp.R;

import org.w3c.dom.Text;

import java.util.List;

import androidx.recyclerview.widget.RecyclerView;

public class TimingAdapter extends RecyclerView.Adapter<TimingAdapter.ViewHolder> {

    private List<Timing> mData;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;

    // data is passed into the constructor
    public TimingAdapter(Context context, List<Timing> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.dialog_card_timing, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Timing startEnd = mData.get(position);
        holder.startTv.setText(startEnd.getStart());
        holder.endTV.setText(startEnd.getEnd());
        holder.check(startEnd.isChecked());

    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mData.size();
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView startTv;
        TextView endTV;

        ViewHolder(View itemView) {
            super(itemView);
            startTv = itemView.findViewById(R.id.details_start);
            endTV = itemView.findViewById(R.id.details_end);
            itemView.setOnClickListener(this);
        }
        public void check(boolean b){
            if(b) {
                startTv.setBackgroundColor(Color.parseColor("#00574B"));
                startTv.setTextColor(Color.WHITE);
                endTV.setBackgroundColor(Color.parseColor("#00574B"));
                endTV.setTextColor(Color.WHITE);
            }else{
                startTv.setBackgroundColor(Color.WHITE);
                startTv.setTextColor(Color.BLACK);
                endTV.setBackgroundColor(Color.WHITE);
                endTV.setTextColor(Color.BLACK);
            }
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
            Timing selected = mData.get(getAdapterPosition());
            selected.setChecked();
            SelectedTiming.addSelected(selected.getStart(),selected.getEnd());
            notifyDataSetChanged();
        }
    }

    // convenience method for getting data at click position
    Timing getItem(int id) {
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