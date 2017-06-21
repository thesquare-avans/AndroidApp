package me.thesquare.interfaces;

import org.json.JSONObject;

/**
 * Created by Cliff Sestig on 19-Jun-17.
 */

public abstract interface ApiResponse {
    void on(boolean success, JSONObject data);
}

