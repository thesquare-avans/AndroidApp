package me.thesquare.models;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

/**
 * Created by ruben on 14-6-2017.
 */

public class UserModel extends RealmObject {
    @PrimaryKey
    @Required
    private String id;
    private String username;
    private byte[] privateKey;
    private byte[] publicKey;
    private String publicKeyPem;
    private int satoshi;

    public int getSatoshi() {
        return satoshi;
    }

    public void setSatoshi(int satoshi) {
        this.satoshi = satoshi;
    }

    public String getPublicKeyPem() {
        return publicKeyPem;
    }

    public void setPublicKeyPem(String publicKeyPem) {
        this.publicKeyPem = publicKeyPem;
    }

    public UserModel() {
        this.satoshi = 0;
    }

    public void setId(String id) {
        this.id = id.toString();
    }

    public String getId() {
        return id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setPrivateKey(byte[] privateKey) {
        this.privateKey = privateKey;
    }

    public byte[] getPrivateKey() {
        return privateKey;
    }

    public void setPublicKey(byte[] publicKey) {
        this.publicKey = publicKey;
    }

    public byte[] getPublicKey() {
        return publicKey;
    }
}
