package com.example.meetapp.utility;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

public class AvailableTimes {
    ArrayList<String> Dates;
    HashMap<String,ArrayList<String>> timingMap;

    AvailableTimes(ArrayList<String> list){

    }
    public HashMap<String, ArrayList<String>> getTimingMap() {
        return timingMap;
    }
}
