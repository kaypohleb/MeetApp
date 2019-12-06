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

import java.util.ArrayList;
import java.util.List;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

public class PollAdapter extends RecyclerView.Adapter<PollAdapter.ViewHolder> {

    private List<Polls.Poll> mData;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private int checkedPosition = -1;
    private List<CardView> cardViews = new ArrayList<>();
    public PollAdapter(Context context, List<Polls.Poll> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
        for(Polls.Poll poll:mData){
            poll.setSelected(false);
        }
    }



    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.card_polldates, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Polls.Poll dateVote = mData.get(position);
        holder.dateTV.setText(DateConverter.dateTimeConvert(dateVote.getDate()));
        holder.voteTV.setText(dateVote.getTotal_vote());
        if (!cardViews.contains(holder.pollCV)) {
            cardViews.add(holder.pollCV);
        }


    }


    @Override
    public int getItemCount() {
        return mData.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView dateTV,voteTV;
        CardView pollCV;

        ViewHolder(View itemView) {
            super(itemView);
            dateTV = itemView.findViewById(R.id.eventDate);
            voteTV = itemView.findViewById(R.id.totalVotes);
            pollCV = itemView.findViewById(R.id.poll_cv);
            itemView.setOnClickListener(this);

        }


        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
            if(checkedPosition!=getAdapterPosition()){
                notifyItemChanged(checkedPosition);
                checkedPosition = getAdapterPosition();
                for(CardView cardView : cardViews){
                    cardView.setCardBackgroundColor(Color.WHITE);
                    TextView voteCV = cardView.findViewById(R.id.totalVotes);
                    voteCV.setTextColor(Color.BLACK);
                    TextView dateCV = cardView.findViewById(R.id.eventDate);
                    dateCV.setTextColor(Color.BLACK);
                }
                //The selected card is set to colorSelected
                pollCV.setCardBackgroundColor(Color.parseColor("#00574B"));
                dateTV.setTextColor(Color.WHITE);
                voteTV.setTextColor(Color.WHITE);
            }else{
                pollCV.setCardBackgroundColor(Color.WHITE);
                dateTV.setTextColor(Color.BLACK);
                voteTV.setTextColor(Color.BLACK);
                checkedPosition=-1;
            }
            Log.i("checked",String.valueOf(checkedPosition));
        }
    }


    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
    public Polls.Poll getSelected() {
        if (checkedPosition != -1) {
            return mData.get(checkedPosition);
        }
        return null;
    }

}