package com.example.meetapp.utility;

import java.util.ArrayList;

public class SelectedTiming {
    private static ArrayList<String[]> timingSelected;

    public SelectedTiming(){
        timingSelected = new ArrayList<>();
    }
    public static void addSelected(String start, String end){
        timingSelected.add(new String[]{start,end});
    }

    public static ArrayList<String[]> getTimingSelected() {
        return timingSelected;
    }
}
