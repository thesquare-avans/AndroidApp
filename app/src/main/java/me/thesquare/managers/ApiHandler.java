package me.thesquare.managers;

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
import org.spongycastle.util.io.pem.PemObject;
import org.spongycastle.util.io.pem.PemReader;

import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;

import me.thesquare.interfaces.ApiResponse;
import me.thesquare.interfaces.RegisterResponse;
import me.thesquare.interfaces.StreamResponse;
import me.thesquare.interfaces.UserResponse;
import me.thesquare.managers.KeyManager;
import me.thesquare.models.StreamModel;
import me.thesquare.models.UserModel;

/**
 * Created by ruben on 13-6-2017.
 */

public class ApiHandler {
    private static final String TAG = "APIHandler";
    private static final String API_TAG = "API ERROR";
    private static final String API_HOST = "http://api.thesquare.me" ;
    private static final String PAYLOAD = "payload";
    private static final String SUCCESS = "success";
    private static final String ERROR = "error";
    private KeyManager keyManager;
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

            HashMap<String, String> requestBody = new HashMap<>();
            requestBody.put(PAYLOAD, payload);
            requestBody.put("signature", keyManager.signMessage(payload));

            requestBodyJSON = new JSONObject(requestBody);
        }

        JsonObjectRequest jsonRequest = new JsonObjectRequest(method, (API_HOST +endPoint), requestBodyJSON, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if(!keyManager.verifyResponse(response)) {
                        JSONObject invalidSignature = new JSONObject();
                        invalidSignature.put(SUCCESS, false);
                        invalidSignature.put(ERROR, new JSONObject().put("code", "invalidResponseSignature"));
                        callback.on(false, invalidSignature);
                        return;
                    }

                    String payload = response.getString(PAYLOAD);
                    JSONObject payloadObj = new JSONObject(payload);
//                    JSONObject chatHostObj = new JSONObject("chatServer");
//                    String hostname = chatHostObj.getString("hostname");
//                    Log.d("TestHost", hostname);
                    if(!payloadObj.getBoolean(SUCCESS)) {
                        callback.on(false, payloadObj);
                        return;
                    }

                    callback.on(true, payloadObj);
                }
                catch(JSONException e) {
                    Log.d(TAG, e.getMessage());

                    try {
                        JSONObject invalidResponse = new JSONObject();
                        invalidResponse.put(SUCCESS, false);
                        invalidResponse.put(ERROR, new JSONObject().put("code", "invalidResponseSignature"));
                        callback.on(false, invalidResponse);
                        return;
                    } catch(JSONException err) {
                        Log.d(TAG, err.getMessage());
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error == null || error.networkResponse == null) {
                    try {
                        JSONObject invalidResponse = new JSONObject();
                        invalidResponse.put(SUCCESS, false);
                        invalidResponse.put(ERROR, new JSONObject().put("code", "invalidOrNoResponse"));
                        callback.on(false, invalidResponse);
                        return;
                    } catch(JSONException err) {
                        Log.d(TAG, err.getMessage());
                    }
                    return;
                }

                try {
                    JSONObject response = new JSONObject(new String(error.networkResponse.data,"UTF-8"));

                    if(!keyManager.verifyResponse(response)) {
                        JSONObject invalidSignature = new JSONObject();
                        invalidSignature.put(SUCCESS, false);
                        invalidSignature.put(ERROR, new JSONObject().put("code", "invalidResponseSignature"));
                        callback.on(false, invalidSignature);
                        return;
                    }

                    String payload = response.getString(PAYLOAD);
                    JSONObject payloadObj = new JSONObject(payload);

                    if(!payloadObj.getBoolean(SUCCESS)) {
                        callback.on(false, payloadObj);
                        return;
                    }

                    callback.on(true, payloadObj);
                } catch (UnsupportedEncodingException e) {
                    Log.d(TAG, e.getMessage());
                    // exception
                } catch (JSONException e) {
                    try {
                        JSONObject invalidResponse = new JSONObject();
                        invalidResponse.put(SUCCESS, false);
                        invalidResponse.put(ERROR, new JSONObject().put("code", "invalidOrNoResponse"));
                        callback.on(false, invalidResponse);
                        return;
                    } catch(JSONException err) {
                        Log.d(TAG, err.getMessage());
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
                    Log.d(TAG, e.getMessage());
                }
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
                        Log.d(TAG, e.getMessage());
                    }

                    return;
                }

                Log.d(API_TAG, "api not successful");
                try {
                    Log.d(API_TAG, data.getJSONObject(ERROR).toString());
                } catch (JSONException e) {
                    Log.d(TAG, e.getMessage());
                }
            }
        });
    }

    public void startStream(String title, final StreamResponse callback) {
        HashMap<String, String> params = new HashMap<>();

        params.put("title", title);

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
                        JSONObject chatserver = streamData.getJSONObject("chatServer");
                        String hostname = chatserver.getString("hostname");
                        stream.setChatServer(hostname);
                        callback.on(stream);
                    } catch (JSONException e) {
                        Log.d(TAG, e.getMessage());
                    }

                    return;
                }

                try {
                    if(data.getJSONObject(ERROR).getString("code").equals("alreadyStreaming")) {
                        getStream(data.getString("streamId"), callback);
                        return;
                    }
                } catch (JSONException e) {
                    Log.d(TAG, e.getMessage());
                }
                Log.d(API_TAG, "api not successful\n"+data.toString());
            }
        });
    }

    public void getStream(String streamId, final StreamResponse callback) {
        request(Request.Method.GET, "/v1/streams/"+streamId, null, new ApiResponse(){
            @Override
            public void on(boolean success, JSONObject data) {
                if(success) {
                    try {
                        StreamModel stream = new StreamModel();
                        JSONObject streamData = data.getJSONObject("stream");
                        stream.setId(streamData.getString("id"));
                        stream.setTitle(streamData.getString("title"));
                        JSONObject chatserver = streamData.getJSONObject("chatServer");
                        String hostname = chatserver.getString("hostname");
                        stream.setChatServer(hostname);
                        callback.on(stream);
                    } catch (JSONException e) {
                        Log.d(TAG, e.getMessage());
                    }

                    return;
                }

                Log.d(API_TAG, "api not successful\n"+data.toString());
            }
        });
    }

    public void getLoggedInUser(final UserResponse callback) {
        request(Request.Method.GET, "/v1/me", null, new ApiResponse(){
            @Override
            public void on(boolean success, JSONObject data) {
                if(success) {
                    try {
                        UserModel user = new UserModel();
                        JSONObject userData = data.getJSONObject("user");
                        user.setId(userData.getString("id"));
                        if (userData.getInt("satoshi") == 0) {
                            user.setSatoshi(1);
                        }
                        else{
                            user.setSatoshi(userData.getInt("satoshi"));
                        }
                        user.setSatoshi(userData.getInt("satoshi"));
                        user.setUsername(userData.getString("name"));
                        callback.on(user);
                    } catch (JSONException e) {
                        Log.d(TAG, e.getMessage());
                    }

                    return;
                }

                try {
                    if (data.getJSONObject(ERROR).getString("code") == "userNotFound") {
                        callback.on(null);
                        return;
                    }
                } catch (JSONException e) {
                    Log.d(TAG, e.getMessage());
                }

                Log.d(API_TAG, "api not successful\n"+data.toString());
            }
        });
    }
    public void stopStream(String streamID) {
        request(Request.Method.DELETE, "/v1/streams/"+ streamID , null, new ApiResponse(){
            @Override
            public void on(boolean success, JSONObject data) {
                if(success) {
                    Log.d(TAG, "Stream deleted!");
                    return;
                }
                Log.d(API_TAG, "api not successful\n"+data.toString());
            }
        });
    }

    public void getUser(String userId, final UserResponse callback) {
        request(Request.Method.GET, "/v1/users/"+ userId , null, new ApiResponse(){
            @Override
            public void on(boolean success, JSONObject data) {
                if(success) {
                    try {
                        UserModel user = new UserModel();
                        JSONObject userData = data.getJSONObject("user");
                        user.setId(userData.getString("id"));
                        user.setUsername(userData.getString("name"));
                        try {
                            String stringDecoded = new String(Base64.decode(userData.getString("publicKey").getBytes("UTF-8"), Base64.NO_WRAP), "UTF-8");
                            PemReader pemReader = new PemReader(new StringReader(stringDecoded));
                            PemObject pemObject = pemReader.readPemObject();

                            KeyFactory kf = KeyFactory.getInstance("RSA", "BC");
                            byte[] publicKeyContent = pemObject.getContent();

                            X509EncodedKeySpec spec = new X509EncodedKeySpec(publicKeyContent);
                            user.setPublicKey(kf.generatePublic(spec).getEncoded());
                        } catch (NoSuchAlgorithmException | IOException | NoSuchProviderException | InvalidKeySpecException e) {
                            Log.d(TAG, e.getMessage());
                        }
                        callback.on(user);
                    } catch (JSONException e) {
                        Log.d(TAG, e.getMessage());
                    }

                    return;
                }
                Log.d(API_TAG, "api not successful\n"+data.toString());
            }
        });
    }
}