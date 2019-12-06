package com.example.meetapp.utility;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;

public class TimeSlot{
    private Calendar startTime;
    private Calendar endTime;
    private String durationStr;
    private int duration;
    private int score;
    private ArrayList<String> absentees;

    public TimeSlot(){
        score = 0;
        absentees = new ArrayList<String>();
    }
    public TimeSlot(Calendar startTime, Calendar endTime){
        score = 0;
        absentees = new ArrayList<String>();
        setStartTime(startTime);
        setEndTime(endTime);
        generateDurationFromStartAndEnd();
    }

    public TimeSlot(Calendar startTime){
        score = 0;
        absentees = new ArrayList<String>();
        this.startTime = startTime;
        Calendar endTime = (Calendar)startTime.clone();
        endTime.add(Calendar.MINUTE, +30);
//        System.out.println("New starttime: ");
//        System.out.println(convertCalendarToTime(startTime));
        this.endTime = endTime;
        generateDurationFromStartAndEnd();
    }

    // ------------------ duration methods --------------
    private void generateDurationFromStartAndEnd(){
        long hours = ChronoUnit.HOURS.between(startTime.toInstant(), endTime.toInstant());
        long mins = ChronoUnit.MINUTES.between(startTime.toInstant(), endTime.toInstant())% 60;
        String hourWord = "";
        String minWord = "";
        String r = "";
        if (hours != 0){
            if (hours > 1){
                hourWord = " hours";
            } else {
                hourWord = " hour";
            }
            r = hours + hourWord;
        }
        if (hours != 0 && mins != 0){
            r += " ";
        }
        if (mins != 0) {
            minWord = " mins";
            if (r != ""){
                r = r + mins + minWord;
            } else {
                r = mins + minWord;
            }
        }
        durationStr =  r;
        duration = (int)hours;
    }

    public String getDurationStr(){
        return durationStr;
    }
    public int getDuration() { return duration; }


    // ------------------ absentee methods --------------
    public void addAbsentee(String name, int absenteeScore){
        if(!absentees.contains(name)) {
            absentees.add(name);
            score = score - absenteeScore;
        }

    }

    public ArrayList<String> getAbsentees() {
        return absentees;
    }

    public String getAbsenteesString(){
        String r = "" + absentees.size() + " absentees: ";
        int x = 0;
        while (x < absentees.size()){
            r += absentees.get(x);
            if ( x < absentees.size()-1) {
                r += ", ";
            } else {
                r += ".";
            }
            x++;
        }
        return r;
    }

    // ----------- startTime methods ------------
    public Calendar getStartTime() {
        return startTime;
    }

    public String getStartTimeStr(){
        return convertCalendarToTime(startTime).replace(" ", "\n ");
    }

    public void setStartTime(Calendar startTime) {
        generateDurationFromStartAndEnd();
        this.startTime = startTime;
    }

    // ------------ endTime methods --------------
    public String getEndTimeStr(){
        return convertCalendarToTime(endTime).replace(" ", "\n ");
    }


    public Calendar getEndTime() { return endTime;
    }

    public void setEndTime(Calendar endTime) {
        this.endTime = endTime;
        generateDurationFromStartAndEnd();
    }

    // ------------- score methods -------------------
    public int getScore() {
        return score;
    }

    // ------------- misc  ---------------
    public String toString(){
        String r = getStartTimeStr() + "," + getEndTimeStr() + "," + getScore();
        return r;
    }

    // -------------- custom time and calendar methods --------------------
    public static boolean isBetween(Calendar start, Calendar target, Calendar end){
        if ( start.compareTo(target) <= 0 && end.compareTo(target)> 0){ return true; } else { return false; }
    }
    public static boolean isCalEqual(Calendar c1, Calendar c2){
        return c1.compareTo(c2) == 0;
    }
    private static boolean isLessThan(Calendar c1, Calendar c2){
        if (c1.compareTo(c2) < 0) { return true; } else { return false; }
    }

    public static Calendar convertTimeToCalendar(String time){
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Calendar cal = Calendar.getInstance();
        try {
            cal.setTime(formatter.parse(time));
        } catch (ParseException e){
            System.out.println("parse exception");
        }
        return cal;
    }

    public static String convertCalendarToTime(Calendar cal){
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String strDate = "";
        if (cal != null) {
            strDate = formatter.format(cal.getTime());
        }
        return strDate;
    }

    public static Calendar convertDateToCalendar(String date){
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();
        try {
            cal.setTime(formatter.parse(date));
        } catch (ParseException e){
            System.out.println("parse exception");
        }
        return cal;
    }
}