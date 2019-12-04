package com.example.meetapp.utility;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

public class AvailableTimes {
    public static ArrayList<String> dates;
    public static HashMap<String,ArrayList<String[]>> timingMap;
    public static ArrayList<String[]> timingsSelected;
    public AvailableTimes(ArrayList<String> list){
        dates = new ArrayList<>();
        timingsSelected =  new ArrayList<>();
        timingMap = new HashMap<>();
        for(String startEnd:list){
            String[] fromTo = startEnd.split(",");
            String[] dateTime = fromTo[0].split(" ");
            if(!dates.contains(dateTime[0])){
                dates.add(dateTime[0]);
                timingMap.put(dateTime[0],new ArrayList<String[]>());
            }
            timingMap.get(dateTime[0]).add(fromTo);
        }
    }

    public static ArrayList<Timing> getTimings(String s){
        ArrayList<String[]> output = timingMap.get(s);
        ArrayList<Timing> timings = new ArrayList<>();
        for(String[] strings: output){
            timings.add(new Timing(strings));
        }
        return timings;
    }
    public static ArrayList<String[]> getTimefromDate(String s){
        ArrayList<String[]> output = timingMap.get(s);
        return output!=null ? output : new ArrayList<String[]>();
    }
    public static boolean isMultiple(){
        return dates.size()>1;
    }

}
