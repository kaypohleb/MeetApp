package com.example.meetapp.utility;

import android.util.Log;

import com.example.meetapp.utility.Credentials;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BusyDates {
    private static List<ArrayList<String>> busyTimes = new ArrayList<>();;



    public static void addDates(String[] arr){
        busyTimes.add(new ArrayList<String>(Arrays.asList(arr)));
    }
    public static List<ArrayList<String>> getBusyTimes(){
        return busyTimes;
    }
    public static JSONObject toJSON() {
        JSONObject jsonObject = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        try {
            jsonObject.put("user_id", Credentials.getId());

        for(ArrayList<String> arr : busyTimes){
            JSONArray innerjsonArray = new JSONArray();
            for(String s: arr) {
                innerjsonArray.put(s);
            }
            jsonArray.put(innerjsonArray);
        }
        jsonObject.put("time",jsonArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.i("toJSON",jsonObject.toString());

        return jsonObject;
    }

}

