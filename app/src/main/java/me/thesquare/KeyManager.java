package me.thesquare;

import android.util.Base64;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import me.thesquare.models.MessageModel;
import me.thesquare.models.ResponseModel;
import me.thesquare.models.UserModel;

/**
 * Created by ruben on 13-6-2017.
 */

public class KeyManager {
    private KeyPairGenerator kpg;
    private KeyPair pair;
    private static Gson gson = new Gson();
    private static UserModel user;
    private String decodedkey;
    private static String statickey = "LS0tLS1CRUdJTiBQVUJMSUMgS0VZLS0tLS0KTUlJQklqQU5CZ2txaGtpRzl3MEJBUUVGQUFPQ0FROEFNSUlCQ2dLQ0FRRUF6NVViUDlwQ2d6QnNxU0NyT2o2NwpDaHZtSEVyNWN4QnJYQTR2QTdJOUVWdlNubWFzSVpHdUFSRDRnSlN4SWVYNTdrd0tVUmluUXEwZHhHc01Ld3FkCjBScEY1Q0pYcTlrNGQ3MTNwZEcwcEs2TUk1MklyNzhKQ3QwRHpDR2Z6OVRwZjIwQlV5TFRkREtqVkdURkpCUDUKaW1YR21wZ3Q3RVRlZ0VYVEVCZzRnSjl3czF0cEFiMjFobExUc28yVWt5UjdzNzVhQTBSUUZZcHcvY2FBM0RlRApKdm5YemExRnd5MTF0dVBNTy84SCtjV2htWEtTSTBXQjFGWFFUbnphRm9YRjVOUU1LQ1VrMCt6UUxFSC9BL2VaCk5yS3pna09YVk5NZjlWbzEvRzBaT1d2Ync0bkhRUko4akF1QXJJclgvYkxGeG9qcEpRRXVCVy9oby9JOFIxZmIKN1FJREFRQUIKLS0tLS1FTkQgUFVCTElDIEtFWS0tLS0tCg==";
    private static Signature sig;
    private ResponseModel response;
    private String privateKey;
    private String publicKey;

    // genereren en verifieren keys

    public void generateKey() {
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(2048);
            pair = kpg.generateKeyPair();

            setPrivateKey( pair.getPrivate().getEncoded() );
            setPublicKey( pair.getPublic().getEncoded() );
        }
        catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public byte[] getPrivateKey(){
        return privateKey;
    }

    private void setPrivateKey(byte[] privateKey) {
        this.privateKey = privateKey;
    }

    public byte[] getPublicKey() {
        return publicKey;
    }

    private void setPublicKey(byte[] publicKey) {
        this.publicKey = publicKey;
    }

    public static PublicKey getPublicKey(String key) {
        try {
            byte[] bytes = Base64.decode(key.getBytes(), Base64.DEFAULT);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(bytes);
            KeyFactory kf = KeyFactory.getInstance("SHA256withRSA");

            return kf.generatePublic(keySpec);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
    public static PrivateKey getPrivateKey(String key) {
        try {
            byte[] bytes = Base64.decode(key.getBytes(), Base64.DEFAULT);
            X509EncodedKeySpec  keySpec = new X509EncodedKeySpec(bytes);

            KeyFactory kf = KeyFactory.getInstance("SHA256withRSA");

            return kf.generatePrivate(keySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static boolean verifyResponse(ResponseModel response){
        try {
            new JSONObject(response.getPayload());
        } catch (Exception e) {
            return false;
        }

        try {
            sig = Signature.getInstance("SHA256withRSA");
            sig.initVerify(getPublicKey(statickey));
            sig.update(Byte.parseByte(response.getSignature()));
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            e.printStackTrace();
        }

        try {
            return sig.verify(response.getSignature().getBytes());
        } catch (SignatureException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static String signMessage(String message) {
        MessageModel data = new MessageModel(message, Base64.encodeToString(user.getPublicKey().getBytes(), Base64.DEFAULT));

        try {
            sig = Signature.getInstance("SHA256withRSA");
            sig.initSign(getPrivateKey(user.getPrivateKey()));
            return String.valueOf(sig.sign(gson.toJson(data, MessageModel.class).getBytes(), 0, 0));
        } catch (NoSuchAlgorithmException | SignatureException | InvalidKeyException e) {
            e.printStackTrace();
        }

        return null;
    }

}

