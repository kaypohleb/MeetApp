package com.example.meetapp.utility;

import androidx.annotation.NonNull;

public class PollDetails{
    private String date_id;
    private String event_id;
    private String date;
    private String vote;
    private String total_vote;
    private Boolean selected;

    public Boolean getSelected() {
        return selected;
    }

    public void setSelected(Boolean selected) {
        this.selected = selected;
    }

    public String getEvent_id() {
        return event_id;
    }

    public String getDate() {
        return date;
    }

    public String getDate_id() {
        return date_id;
    }

    public String getTotal_vote() {
        return total_vote;
    }

    public String getVote() {
        return vote;
    }

    @NonNull
    @Override
    public String toString() {
            String result = "Date_id" + getDate_id() + ",Date" + getDate() + ",Vote" + getVote() + ",TotalVote" + getTotal_vote()+ ",Event" + getEvent_id();
            return result;

    }
}
