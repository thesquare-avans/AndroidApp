package me.thesquare;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

/**
 * Created by ruben on 13-6-2017.
 */

public class KeyManager {
    private KeyPairGenerator kpg;
    private KeyPair pair;
    private byte[] privateKey;
    private byte[] publicKey;

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
}


