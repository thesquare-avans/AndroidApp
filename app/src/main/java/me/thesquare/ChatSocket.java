package me.thesquare;

/**
 * Created by ruben on 20-6-2017.
 */

import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.Ack;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.HashMap;

import me.thesquare.ApiResponses.UserResponse;
import me.thesquare.models.UserModel;

public class ChatSocket {
    private Socket mSocket;
    private String chatHostName, streamid;
    private KeyManager keyManager;
    private ApiHandler handler;
    private HashMap<String, UserModel> userCache;


    public ChatSocket(String chatHostName, String streamid, KeyManager keyManager, ApiHandler handler) {
        this.chatHostName = chatHostName;
        this.streamid = streamid;
        this.keyManager = keyManager;
        this.handler = handler;

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
            Log.d("TheSquare-Chat", e.getMessage());

            try {
                JSONObject invalidResponse = new JSONObject();
                invalidResponse.put("success", false);
                invalidResponse.put("error", new JSONObject().put("code", "invalidResponseSignature"));
                return invalidResponse;
            } catch (JSONException err) {
                Log.d("TheSquare-Chat", err.getMessage());
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
                    Log.d("SWaggerbody", body.toString());
                    if(body != null) {
                        try {
                            if(body.getBoolean("success")) {
                                UserModel sender = userCache.get(body.getJSONObject("data").getString("sender"));
                                if(sender != null) {
                                    JSONObject senderBody = verifyBody(body.getJSONObject("data").getJSONObject("data"), sender.getPublicKey());
                                    Log.d("TheSquare-Chat", senderBody.toString());
                                    addChat(sender, senderBody.getString("message"));
                                    // laat chat zien
                                    return;
                                }

                                handler.getUser(body.getJSONObject("data").getString("sender"), new UserResponse() {
                                    @Override
                                    public void on(UserModel userModel) {
                                        userCache.put(userModel.getId(), userModel);
                                        try {
                                            JSONObject senderBody = verifyBody(body.getJSONObject("data").getJSONObject("data"), userModel.getPublicKey());
                                            Log.d("TheSquare-Chat", senderBody.toString());
                                            addChat(userModel , senderBody.getString("message"));
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                        // laat chat zien
                                    }
                                });
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        } catch (URISyntaxException e) {

        }
    }

    private void identify() {
        try {
            JSONObject identifyBody = new JSONObject();
            identifyBody.put("publicKey", Base64.encodeToString(keyManager.getPublicKeyPem().getBytes("UTF-8"), Base64.NO_WRAP));
            emit("identify", identifyBody, new ChatResponse() {
                @Override
                public void on(JSONObject body) {
                    Log.d("TheSquare-Chat", body.toString());
                    try {
                        if(body.getBoolean("success")){
                            join();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (JSONException e) {
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private void addChat(UserModel userModel, String message){

    }

    private void join() {
        try {
            JSONObject joinBody = new JSONObject();
            joinBody.put("room", streamid);
            emit("join", joinBody, new ChatResponse() {
                @Override
                public void on(JSONObject body) {
                    Log.d("TheSquare-Chat", body.toString());
                }
            });
        } catch (JSONException e) {
        }
    }

    public void socketDisconnect() {
        mSocket.disconnect();
    }

    public void attemptSend(String message) {
        String trimmedMessage = message.trim();
        if (TextUtils.isEmpty(message)) {
            return;
        }
        mSocket.emit("new message", message);
    }
}
