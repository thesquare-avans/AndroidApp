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
    // genereren en verifieren keys

    public byte[] generateKey() throws NoSuchAlgorithmException {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        pair = kpg.generateKeyPair();
        byte[] pri = pair.getPrivate().getEncoded();
        byte[] pub = pair.getPublic().getEncoded();

        return pub;
    }
}


