package me.thesquare;

import android.app.Notification;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;

/**
 * Created by ruben on 13-6-2017.
 */

public class ApiHandler {
    KeyManager keys;

    public void authenticate() {
        //uri van api
        String url = "http://my-json-feed";

        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    public void onResponse(JSONObject response) {
                        String username = (response.toString());
                    }
                }, new Response.ErrorListener() {


                    public void onErrorResponse(VolleyError error) {
                        // TODO Auto-generated method stub

                    }
                });
    }
}