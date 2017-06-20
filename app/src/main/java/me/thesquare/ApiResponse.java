package me.thesquare;

import org.json.JSONObject;

import me.thesquare.models.UserModel;

/**
 * Created by Cliff Sestig on 19-Jun-17.
 */

public abstract interface ApiResponse {
    void on(boolean success, JSONObject data);
}

