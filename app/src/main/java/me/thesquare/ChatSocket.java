package me.thesquare;

/**
 * Created by ruben on 20-6-2017.
 */

import android.text.TextUtils;
import android.util.Log;

import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import java.net.URISyntaxException;

public class ChatSocket {
    private static final String TAG = "ChatSocket";
    private Socket mSocket;

    public void socketConnect() {
        try {
            mSocket = IO.socket("http://chat.socket.io");
            mSocket.connect();
        } catch (URISyntaxException e) {
            Log.d(TAG, e.getMessage());
        }
    }

    public void socketDisconnect (){
        mSocket.disconnect();
    }

    public void attemptSend(String message){
        String trimmedMessage = message.trim();
        if (TextUtils.isEmpty(trimmedMessage)) {
            return;
        }
        mSocket.emit("new message", trimmedMessage);
    }
}
