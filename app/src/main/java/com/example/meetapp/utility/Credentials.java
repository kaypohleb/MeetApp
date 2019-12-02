package com.example.meetapp.utility;

import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Credentials {
    private static String name;
    private static String id;
    private static String email;
    private static GoogleSignInOptions gso;



    public static GoogleSignInOptions getGso() {
        return gso;
    }

    public static String getId() {
        return id;
    }

    public static String getEmail() {
        return email;
    }

    public static String getName() {
        return name;
    }

    public static void setEmail(String email) {
        Credentials.email = email;
    }

    public static void setId(String id) {
        Credentials.id = id;
    }

    public static void setName(String name) {
        Credentials.name = name;
    }

    public static void setGso(GoogleSignInOptions gso) {
        Credentials.gso = gso;
    }

    public static JSONObject toJsonObject() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("user_id", Credentials.getId().toString());
        jsonObject.put("username", Credentials.getName().toString());
        return jsonObject;
    }
    public static Map toMap() {
        Map<String,String> map = new HashMap<>();
        map.put("user_id", Credentials.getId().toString());
        map.put("username", Credentials.getName().toString());
        return map;
    }

}
