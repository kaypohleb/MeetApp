package com.example.meetapp.utility;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class DateConverter {
    public static String dateConvert(String s){
        String out = null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date date = sdf.parse(s);
            out = DateFormat.getDateInstance(DateFormat.MEDIUM).format(date).split(",")[0];

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return out;
    }
    public static String dateTimeConvert(String s){

        String out = null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date date = sdf.parse(s);
            out = DateFormat.getDateInstance(DateFormat.MEDIUM).format(date).split(",")[0]+ " " + DateFormat.getTimeInstance(DateFormat.SHORT).format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return out;
    }

    public static String dateTimewithoutSecondsConvert(String s){

        String out = null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd\n HH:mm");
        try {
            Date date = sdf.parse(s);
            out = DateFormat.getDateInstance(DateFormat.MEDIUM).format(date).split(",")[0]+ "\n" + DateFormat.getTimeInstance(DateFormat.SHORT).format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return out;
    }
}
