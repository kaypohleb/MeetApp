package com.example.meetapp.utility;


import androidx.annotation.NonNull;

public class IncomingDetails {
    private String event_name;
    private String user_id;
    private String event_id;
    private String date_from;
    private String date_to;
    private String description;
    private String duration;
    private String location;
    private String confirm_date;
    private String status;
    private String invite_id;
    private String interest;

    public IncomingDetails() {
    }

    public String getLocation() {
        return location;
    }

    public String getDuration() {
        return duration;
    }

    public String getDate_from() {
        return date_from;
    }

    public String getDate_to() {
        return date_to;
    }

    public String getEvent_id() {
        return event_id;
    }

    public String getDescription() {
        return description;
    }

    public String getEvent_name() {
        return event_name;
    }

    public String getConfirm_date() {
        return confirm_date;
    }

    public String getUser_id() {
        return user_id;
    }

    public String getInvite_id(){
        return invite_id;
    }

    public String getStatus() {
        return status;
    }

    public String getInterest() {
        return interest;
    }

    @NonNull
    @Override
    public String toString() {
        return super.toString();
    }
}