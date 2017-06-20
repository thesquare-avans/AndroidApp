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
import java.util.HashMap;
import java.util.Map;

import me.thesquare.ApiResponses.RegisterResponse;
import me.thesquare.ApiResponses.StreamResponse;
import me.thesquare.ApiResponses.UserResponse;
import me.thesquare.models.StreamModel;
import me.thesquare.models.UserModel;

/**
 * Created by ruben on 13-6-2017.
 */

public class ApiHandler {
    private KeyManager keyManager;
    private String apiHost = "http://api.thesquare.me" ;
    private Context ctx;

    public ApiHandler(KeyManager keyManager,Context ctx) {
        this.keyManager = keyManager;
        this.ctx = ctx;
    }

    private void request(int method, String endPoint, JSONObject body, final ApiResponse callback)
    {
        RequestQueue queue = Volley.newRequestQueue(ctx);

        JSONObject requestBodyJSON = null;

        if(body != null) {
            String payload = body.toString();

            HashMap<String, String> requestBody = new HashMap<String, String>();
            requestBody.put("payload", payload);
            requestBody.put("signature", keyManager.signMessage(payload));

            requestBodyJSON = new JSONObject(requestBody);
        }

        JsonObjectRequest jsonRequest = new JsonObjectRequest(method, (apiHost+endPoint), requestBodyJSON, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if(!keyManager.verifyResponse(response)) {
                        JSONObject invalidSignature = new JSONObject();
                        invalidSignature.put("success", false);
                        invalidSignature.put("error", new JSONObject().put("code", "invalidResponseSignature"));
                        callback.on(false, invalidSignature);
                        return;
                    }

                    String payload = response.getString("payload");
                    JSONObject payloadObj = new JSONObject(payload);

                    if(!payloadObj.getBoolean("success")) {
                        callback.on(false, payloadObj);
                        return;
                    }

                    callback.on(true, payloadObj);
                }
                catch(JSONException e) {
                    e.getMessage();

                    try {
                        JSONObject invalidResponse = new JSONObject();
                        invalidResponse.put("success", false);
                        invalidResponse.put("error", new JSONObject().put("code", "invalidResponseSignature"));
                        callback.on(false, invalidResponse);
                        return;
                    } catch(JSONException err) {
                        err.getMessage();
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error == null || error.networkResponse == null) {
                    try {
                        JSONObject invalidResponse = new JSONObject();
                        invalidResponse.put("success", false);
                        invalidResponse.put("error", new JSONObject().put("code", "invalidOrNoResponse"));
                        callback.on(false, invalidResponse);
                        return;
                    } catch(JSONException err) {
                        err.getMessage();
                    }
                    return;
                }

                String body;
                //get status code here
                final String statusCode = String.valueOf(error.networkResponse.statusCode);
                //get response body and parse with appropriate encoding
                try {
                    JSONObject response = new JSONObject(new String(error.networkResponse.data,"UTF-8"));

                    if(!keyManager.verifyResponse(response)) {
                        JSONObject invalidSignature = new JSONObject();
                        invalidSignature.put("success", false);
                        invalidSignature.put("error", new JSONObject().put("code", "invalidResponseSignature"));
                        callback.on(false, invalidSignature);
                        return;
                    }

                    String payload = response.getString("payload");
                    JSONObject payloadObj = new JSONObject(payload);

                    if(!payloadObj.getBoolean("success")) {
                        callback.on(false, payloadObj);
                        return;
                    }

                    callback.on(true, payloadObj);
                } catch (UnsupportedEncodingException e) {
                    // exception
                } catch (JSONException e) {
                    try {
                        JSONObject invalidResponse = new JSONObject();
                        invalidResponse.put("success", false);
                        invalidResponse.put("error", new JSONObject().put("code", "invalidOrNoResponse"));
                        callback.on(false, invalidResponse);
                        return;
                    } catch(JSONException err) {
                        err.getMessage();
                    }
                }
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();

                String key = null;
                try {
                    String pemKey = keyManager.getPublicKeyPem();
                    key = "";
                    if(pemKey != null) {
                        key = Base64.encodeToString(pemKey.getBytes("UTF-8"), Base64.NO_WRAP);
                    }
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                //String pubkey = new String(key);
                headers.put("Content-Type", "application/json; charset=utf-8");
                headers.put("X-PublicKey", key);
                return headers;
            }
        };

        queue.add(jsonRequest);
    }

    public void register(final UserModel userModel, final RegisterResponse callback) {
        HashMap<String, String> params = new HashMap<>();

        params.put("name", userModel.getUsername());
        HashMap<String, String> requestBody = new HashMap<String, String>();

        JSONObject parameters = new JSONObject(params);

        request(Request.Method.POST, "/v1/register", parameters, new ApiResponse(){
            @Override
            public void on(boolean success, JSONObject data) {
                if(success) {
                    try {
                        String user = data.getString("user");
                        JSONObject userObj = new JSONObject(user);
                        String id = userObj.getString("id");

                        userModel.setId(id);
                        callback.on();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    return;
                }

                Log.d("API ERROR", "api not successful");
                try {
                    Log.d("API ERROR", data.getJSONObject("error").toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void startStream(String title, final StreamResponse callback) {
        HashMap<String, String> params = new HashMap<>();

        params.put("title", title);
        HashMap<String, String> requestBody = new HashMap<String, String>();

        JSONObject parameters = new JSONObject(params);

        request(Request.Method.POST, "/v1/streams", parameters, new ApiResponse(){
            @Override
            public void on(boolean success, JSONObject data) {
                if(success) {
                    try {
                        StreamModel stream = new StreamModel();
                        JSONObject streamData = data.getJSONObject("stream");
                        stream.setId(streamData.getString("id"));
                        stream.setTitle(streamData.getString("title"));

                        callback.on(stream);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    return;
                }

                Log.d("API ERROR", "api not successful\n"+data.toString());
            }
        });
    }

    public void getLoggedInUser(final UserResponse callback) {
        HashMap<String, String> params = new HashMap<>();

        request(Request.Method.GET, "/v1/me", null, new ApiResponse(){
            @Override
            public void on(boolean success, JSONObject data) {
                if(success) {
                    try {
                        UserModel user = new UserModel();
                        JSONObject userData = data.getJSONObject("user");
                        user.setId(userData.getString("id"));
                        // TODO: zet alle velden die je terug krijgt van de API en je lokaal opslaat

                        callback.on(user);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    return;
                }

                try {
                    if (data.getJSONObject("error").getString("code") == "userNotFound") {
                        callback.on(null);
                        return;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                Log.d("API ERROR", "api not successful\n"+data.toString());
            }
        });
    }

}