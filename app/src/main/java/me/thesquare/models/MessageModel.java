package me.thesquare.models;

/**
 * Created by ruben on 14-6-2017.
 */

public class MessageModel {

    private String payload;
    private String publicKey;

    public MessageModel(String payload, String publicKey) {
        this.payload = payload;
        this.publicKey = publicKey;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }
}
