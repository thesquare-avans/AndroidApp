package me.thesquare.models;

/**
 * Created by ruben on 14-6-2017.
 */

public class ResponseModel {

    private String payload;
    private String signature;

    public ResponseModel(String payload, String signature) {
        this.payload = payload;
        this.signature = signature;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }
}
