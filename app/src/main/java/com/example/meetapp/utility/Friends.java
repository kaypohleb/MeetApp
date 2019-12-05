package com.example.meetapp.utility;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import org.json.JSONArray;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public final class Friends {;
    private static ArrayList<String> user_ids = new ArrayList<>();
    private static ArrayList<String> user_name = new ArrayList<>();
    private static ArrayList<Invitee> invitees = new ArrayList<>();

    public static String swapIdForName(String id){
        return user_name.get(user_ids.indexOf(id));
    }
    public static String swapNameForId(String name){
        return user_ids.get(user_name.indexOf(name));
    }
    public static void addFriend(String id, String name){
        user_ids.add(id);
        user_name.add(name);
    }
    public static boolean hasName(String name){
        return user_name.contains(name);
    }

    public static void updateFriendList(Context context,String url){
        RequestQueue queue = Volley.newRequestQueue(context);
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url+ Credentials.getId(), null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                Log.d("Response", response.toString());
                Gson gson = new Gson();
                Type type = new TypeToken<List<InviteDetails>>(){}.getType();
                ArrayList<InviteDetails> friendList = gson.fromJson(response.toString(), type);
                for (InviteDetails friend : friendList){
                    if(!Friends.hasName(friend.username)){
                        Friends.addFriend(String.valueOf(friend.user_id),friend.username);
                    }
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Error", error.toString());
            }
        });
        queue.add(jsonArrayRequest);
    }

    public static void updateInviteList(Context context, String url, String event_id){
        RequestQueue queue = Volley.newRequestQueue(context);
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET,url+event_id, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                Log.d("Response", response.toString());


                invitees = parseJSON(response.toString());
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Error", error.toString());
                invitees = new ArrayList<>();
            }
        });
        queue.add(jsonArrayRequest);

    }

    public static ArrayList<Invitee> getInvitees() {
        return invitees;
    }

    public static ArrayList<String> getUser_name() {
        return user_name;
    }

    public static ArrayList<Invitee> parseJSON(String jsonString) {
        Gson gson = new Gson();
        Type type = new TypeToken<List<Invitee>>(){}.getType();
        ArrayList<Invitee> inviteList = gson.fromJson(jsonString, type);
        for (Invitee friend : inviteList){
            Log.i("Organized Details", friend.user_id + "-" + friend.interest);
        }
        return inviteList;
    }

    public class Invitee{
        public String invite_id;
        public String user_id;
        public String priority;
        public String interest;
    }

    class InviteDetails{
        public String username;
        public String user_id;
    }
}
