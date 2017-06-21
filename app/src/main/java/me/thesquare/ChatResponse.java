package me.thesquare;

import org.json.JSONObject;

/**
 * Created by ruben on 21-6-2017.
 */

public interface ChatResponse {
    public void on(JSONObject body);
}
