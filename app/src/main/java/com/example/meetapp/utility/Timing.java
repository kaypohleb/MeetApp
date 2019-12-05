package com.example.meetapp.utility;

public class Timing{
    private String[] fromTo;
    private boolean isChecked;

    Timing(String[] s){
        fromTo = s;
        isChecked = false;
    }
    public String[] getFromTo() {
        return fromTo;
    }
    public String getStart(){
        return fromTo[0];
    }
    public String getEnd(){
        return fromTo[1];
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked() {
        isChecked = !isChecked;
    }
}