package me.thesquare.managers;

/**
 * Created by ruben on 20-6-2017.
 */

import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.widget.EditText;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.Ack;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.HashMap;

import me.thesquare.activities.MainActivity;
import me.thesquare.interfaces.ChatResponse;
import me.thesquare.interfaces.UserResponse;
import me.thesquare.managers.ApiHandler;
import me.thesquare.managers.KeyManager;
import me.thesquare.models.UserModel;

public class ChatSocket {
    private static final String TAG = "ChatSocket";
    private Socket mSocket;
    private String chatHostName, streamid;
    private KeyManager keyManager;
    private ApiHandler handler;
    private HashMap<String, UserModel> userCache;
    private MainActivity mainActivity;

    public ChatSocket(String chatHostName, String streamid, KeyManager keyManager, ApiHandler handler, MainActivity mainActivity) {
        this.chatHostName = chatHostName;
        this.streamid = streamid;
        this.keyManager = keyManager;
        this.handler = handler;
        this.mainActivity = mainActivity;
        this.userCache = new HashMap<>();
    }

    private JSONObject signBody(JSONObject body) {
        JSONObject requestBodyJSON = null;

        String payload = body.toString();

        HashMap<String, String> requestBody = new HashMap<>();
        requestBody.put("payload", payload);
        requestBody.put("signature", keyManager.signMessage(payload));

        requestBodyJSON = new JSONObject(requestBody);

        return requestBodyJSON;
    }

    public JSONObject verifyBody(JSONObject body, byte[] key) {
        try {
            if (!keyManager.verifyResponse(body, key)) {
                JSONObject invalidSignature = new JSONObject();
                invalidSignature.put("success", false);
                invalidSignature.put("error", new JSONObject().put("code", "invalidResponseSignature"));
                return invalidSignature;
            }

            String payload = body.getString("payload");
            JSONObject payloadObj = new JSONObject(payload);

            return payloadObj;
        } catch (JSONException e) {
            Log.d(TAG, e.getMessage());

            try {
                JSONObject invalidResponse = new JSONObject();
                invalidResponse.put("success", false);
                invalidResponse.put("error", new JSONObject().put("code", "invalidResponseSignature"));
                return invalidResponse;
            } catch (JSONException err) {
                Log.d(TAG, err.getMessage());
            }
        }

        return null;
    }

    private void emit(String event, JSONObject body, final ChatResponse callback) {
        Ack ack = null;
        if (callback != null) {
            ack = new Ack() {
                @Override
                public void call(Object... args) {
                    JSONObject responseData = (JSONObject) args[0];

                    JSONObject verifiedBody = verifyBody(responseData, null);
                    callback.on(verifiedBody);
                }
            };
        }

        mSocket.emit(event, signBody(body), ack);
    }

    private void on(String event, final ChatResponse callback) {
        mSocket.on(event, new Emitter.Listener() {
            @Override
            public void call(Object... args) {

//                Ack ack = (Ack) args[args.length - 1];
                JSONObject body = (JSONObject) args[0];
                JSONObject verifiedBody = verifyBody(body, null);

                Log.d("todoruben", "implement later");
                callback.on(verifiedBody);
            }
        });
    }

    public void socketConnect() {
        try {
            mSocket = IO.socket(chatHostName);
            mSocket.connect();

            identify();
            on("message", new ChatResponse() {
                @Override
                public void on(final JSONObject body) {
                    Log.d(TAG, body.toString());
                    if(body != null) {
                        try {
                            if(body.getBoolean("success")) {
                                UserModel sender = userCache.get(body.getJSONObject("data").getString("sender"));
                                if(sender != null) {
                                    JSONObject senderBody = verifyBody(body.getJSONObject("data").getJSONObject("data"), sender.getPublicKey());
                                    Log.d(TAG, senderBody.toString());
                                    addChat(sender, senderBody.getString("message"));

                                    return;
                                }

                                handler.getUser(body.getJSONObject("data").getString("sender"), new UserResponse() {
                                    @Override
                                    public void on(UserModel userModel) {
                                        userCache.put(userModel.getId(), userModel);
                                        try {
                                            JSONObject senderBody = verifyBody(body.getJSONObject("data").getJSONObject("data"), userModel.getPublicKey());
                                            Log.d(TAG, senderBody.toString());
                                            addChat(userModel , senderBody.getString("message"));
                                        } catch (JSONException e) {
                                            Log.d(TAG, e.getMessage());
                                        }
                                    }
                                });
                            }
                        } catch (JSONException e) {
                            Log.d(TAG, e.getMessage());
                        }
                    }
                }
            });
        } catch (URISyntaxException e) {
            Log.d(TAG, e.getMessage());
        }
    }

    public void identify() {
        try {
            JSONObject identifyBody = new JSONObject();
            identifyBody.put("publicKey", Base64.encodeToString(keyManager.getPublicKeyPem().getBytes("UTF-8"), Base64.NO_WRAP));
            emit("identify", identifyBody, new ChatResponse() {
                @Override
                public void on(JSONObject body) {
                    Log.d(TAG, body.toString());
                    try {
                        if(body.getBoolean("success")){
                            join();
                        }
                    } catch (JSONException e) {
                        Log.d(TAG, e.getMessage());
                    }
                }
            });
        } catch (JSONException | UnsupportedEncodingException e) {
            Log.d(TAG, e.getMessage());
        }
    }

    private void addChat(UserModel userModel, String message){
        mainActivity.AddChat(userModel.getUsername(), message);
    }

    private void join() {
        try {
            JSONObject joinBody = new JSONObject();
            joinBody.put("room", streamid);
            emit("join", joinBody, new ChatResponse() {
                @Override
                public void on(JSONObject body) {
                    Log.d(TAG, body.toString());
                }
            });
        } catch (JSONException e) {
            Log.d(TAG, e.getMessage());
        }
    }
    
    private void sendMessage(String message) {
        try {
            JSONObject messageBody = new JSONObject();
            messageBody.put("room", streamid);
            messageBody.put("message", message); 
            emit("message", messageBody, new ChatResponse() {
                @Override
                public void on(JSONObject body) {
                    // shit hit the fan, versturen chat gaat fout om wat voor reden dan ook
                    Log.d(TAG, body.toString());
                }
            });
        } catch (JSONException e) {
            Log.d(TAG, e.getMessage());
        }
    }

    public void socketDisconnect (){
        mSocket.disconnect();
    }
}
