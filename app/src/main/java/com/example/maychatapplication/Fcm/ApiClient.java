package com.example.maychatapplication.Fcm;


import android.content.Context;
import android.os.StrictMode;
import android.util.Log;
import android.widget.Toast;


import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.maychatapplication.Model.GroupMessage;
import com.example.maychatapplication.Model.SingleMessage;
import com.example.maychatapplication.Model.Users;
import com.example.maychatapplication.Utilities.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ApiClient {
    private static final String BASE_URL = "https://fcm.googleapis.com/fcm/send";
    private static final String SERVER_KEY = "key=AAAA8CII5SY:APA91bFwSJdAQwsg3Wwp1fJjzWEYPgRWVGbKnZRwd2AJnAmDXRbasuUNhVfbhbHsahe3wRM4hxwuXPUhDlky1Ic0-IYuWiaUD9j6EHYmcF9IYaAq6rqR_OBt5MexbdTjkf6KoVsm9R7G";

    public static void  pushNotificationSingleMSG(Context context,String token, Users e, SingleMessage msg){
        StrictMode.ThreadPolicy
                policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        RequestQueue queue = Volley.newRequestQueue(context);
        try {
            JSONObject jsonObject = new JSONObject();

            jsonObject.put("to", token);
            JSONObject body = new JSONObject();
            body.put(Constants.KEY_NAME, e.getFullName());
            body.put(Constants.KEY_MESSAGE, msg.getMessage());
            body.put(Constants.KEY_AVATAR, e.getAvatar());
            body.put(Constants.REMOTE_MSG_STATUS, msg.isSeen());
            jsonObject.put("data", body);

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, BASE_URL, jsonObject, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Log.d("API", response.toString());
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(context.getApplicationContext(), error.getMessage()+"", Toast.LENGTH_SHORT).show();
                }
            }){
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String,String> params = new HashMap<>();
                    params.put("Content-Type", "application/json");
                    params.put("Authorization", SERVER_KEY);
                    return params;
                }
            };
            queue.add(request);
        } catch (JSONException ex) {
            ex.printStackTrace();
        }

    }
    public static void  pushNotificationGroupMSG(Context context,List<String> Token, Users e, GroupMessage msg){
        StrictMode.ThreadPolicy
                policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        RequestQueue queue = Volley.newRequestQueue(context);
        try {
            JSONObject jsonObject = new JSONObject();
            JSONArray jsonArray = new JSONArray();
            for (int i =0; i < Token.size(); i++){
                jsonArray.put(Token.get(i));
            }

            jsonObject.put("to", Token);
            JSONObject body = new JSONObject();
            body.put(Constants.KEY_NAME, e.getFullName());
            body.put(Constants.KEY_MESSAGE, msg.getMessage());
            body.put(Constants.KEY_AVATAR, e.getAvatar());
            body.put(Constants.REMOTE_MSG_STATUS, msg.isSeen());
            jsonObject.put("data", body);

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, BASE_URL, jsonObject, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Log.d("APII", response.toString());
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(context.getApplicationContext(), error.getMessage()+"", Toast.LENGTH_SHORT).show();
                }
            }){
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String,String> params = new HashMap<>();
                    params.put("Content-Type", "application/json");
                    params.put("Authorization", SERVER_KEY);
                    return params;
                }
            };
            queue.add(request);
        } catch (JSONException ex) {
            ex.printStackTrace();
        }

    }
}
