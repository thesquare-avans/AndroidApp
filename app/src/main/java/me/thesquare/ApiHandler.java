package me.thesquare;

import android.content.Context;
import android.util.Base64;
import android.util.Log;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.UnsupportedEncodingException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;
import io.realm.Realm;
import me.thesquare.models.PayloadModel;
import me.thesquare.models.ResponseModel;
import me.thesquare.models.UserModel;

/**
 * Created by ruben on 13-6-2017.
 */

public class ApiHandler {
    private UserModel user;
    private KeyManager keyManager;
    private String username, publickey;

    public ApiHandler(KeyManager keyManager) {
        this.keyManager = keyManager;
    }

    public void register(String username, Context ctx, final VolleyCallback callback) {
        RequestQueue queue = Volley.newRequestQueue(ctx);

        publickey = keyManager.getPublicKey().toString();
        String registerurl = "http://api.thesquare.me/v1/register";
        HashMap<String, String> params = new HashMap<String, String>();

        params.put("name", username);
        HashMap<String, String> requestBody = new HashMap<String, String>();

        JSONObject parameters = new JSONObject(params);

        String payload = parameters.toString();
        requestBody.put("payload", payload);
        requestBody.put("signature", keyManager.signMessage(payload));


        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.POST, registerurl, new JSONObject(requestBody), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    String payload = response.getString("payload");
                    JSONObject payloadObj = new JSONObject(payload);
                    String user = payloadObj.getString("user");
                    JSONObject userObj = new JSONObject(user);
                    String id = userObj.getString("id");
                    String name = userObj.getString("name");

                    callback.onSuccess(id, name);
                }
                catch(JSONException e)
                {
                    e.getMessage();
                }
                if(keyManager.verifyResponse(response)) {
                    Log.d("TheSquare", "Signature valid");
                }else{
                    Log.d("TheSquare", "Signature invalid");
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error == null || error.networkResponse == null) {
                    return;
                }

                String body;
                //get status code here
                final String statusCode = String.valueOf(error.networkResponse.statusCode);
                //get response body and parse with appropriate encoding
                try {
                    String message = new String(error.networkResponse.data,"UTF-8");
                    Log.d("tags",message);
                } catch (UnsupportedEncodingException e) {
                    // exception
                }
            }
        }){
            @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> headers = new HashMap<String, String>();

                    String key = null;
                    try {
                        key = Base64.encodeToString(keyManager.getPublicKeyPem().getBytes("UTF-8"), Base64.NO_WRAP);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    //String pubkey = new String(key);
                    headers.put("Content-Type", "application/json");
                    headers.put("X-PublicKey", key);
                    return headers;
            }
        };

        queue.add(jsonRequest);
    }

    public void chatService(String username, Context ctx, final VolleyCallback callback) throws InvalidKeySpecException {
        RequestQueue queue = Volley.newRequestQueue(ctx);

        RealmHandler handler = new RealmHandler();
        UserModel user = handler.getUser(username);

        byte[] publicKeyByte = Base64.encode(user.getPublicKey(), 0);
        publickey = new String(publicKeyByte);
        String registerurl = "http://api.thesquare.me/v1/streams";
        HashMap<String, String> params = new HashMap<String, String>();

        params.put("name", username);
        HashMap<String, String> requestBody = new HashMap<String, String>();

        JSONObject parameters = new JSONObject(params);
        KeyFactory kf = null;
        try {
            kf = KeyFactory.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        PrivateKey tempKey  = kf.generatePrivate(new X509EncodedKeySpec(user.getPrivateKey()));

        keyManager.setPrivateKey(tempKey);
        //keyManager.setPublicKey(kf.generatePublic(new X509EncodedKeySpec(user.getPublicKey())));

        String payload = parameters.toString();
        requestBody.put("payload", payload);
        requestBody.put("signature", keyManager.signMessage(payload));


        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.POST, registerurl, new JSONObject(requestBody), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    String payload = response.getString("payload");
                    JSONObject payloadObj = new JSONObject(payload);


                    Log.d("Response", payload);

                 //   callback.onSuccess(id, name);
                }
                catch(Exception e)
                {
                    e.getMessage();
                }
                if(keyManager.verifyResponse(response)) {
                    Log.d("TheSquare", "Signature valid");
                }else{
                    Log.d("TheSquare", "Signature invalid");
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                String body;
                //get status code here
                final String statusCode = String.valueOf(error.networkResponse.statusCode);
                //get response body and parse with appropriate encoding
                try {
                    String message = new String(error.networkResponse.data,"UTF-8");
                    Log.d("tags",message);
                } catch (UnsupportedEncodingException e) {
                    // exception
                }
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();

                String key = null;
                try {
                    key = Base64.encodeToString(keyManager.getPublicKeyPem().getBytes("UTF-8"), Base64.NO_WRAP);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                //String pubkey = new String(key);
                headers.put("Content-Type", "application/json");
                headers.put("X-PublicKey", key);
                return headers;
            }
        };

        queue.add(jsonRequest);

    }

    public Map<String, String> getHeaders() throws AuthFailureError {
        Map<String, String> params = new HashMap<String, String>();
        params.put("X-PublicKey", user.getPublicKey().toString());
        params.put("Content-Type", "application/json; charset=utf-8");

        return params;
    }
}