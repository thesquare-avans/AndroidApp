package me.thesquare;

import android.content.SharedPreferences;
import android.util.Base64;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.Key;
import java.util.HashMap;
import java.util.Map;

import me.thesquare.models.PayloadModel;
import me.thesquare.models.ResponseModel;
import me.thesquare.models.UserModel;

/**
 * Created by ruben on 13-6-2017.
 */

public class ApiHandler {
    private UserModel user;
    private KeyManager keyManager;

    public void authenticate() {
        //uri van api
        String url = "http://api.thesquare.me/v1/me";

        StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Gson gson = new Gson();
                ResponseModel model = gson.fromJson(response, ResponseModel.class);

                if(keyManager.verifyResponse(model)) {
                    PayloadModel payload = gson.fromJson(model.getPayload(), PayloadModel.class);
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
    }

    public Map<String, String> getHeaders() throws AuthFailureError {
        Map<String, String>  params = new HashMap<String, String>();
        params.put("X-PublicKey", user.getPublicKey());
        params.put("Content-Type", "application/json; charset=utf-8");

        return params;
    }
}