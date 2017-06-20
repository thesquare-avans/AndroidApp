package me.thesquare;

import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;
import org.spongycastle.util.io.pem.PemObject;
import org.spongycastle.util.io.pem.PemReader;
import org.spongycastle.util.io.pem.PemWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

/**
 * Created by ruben on 13-6-2017.
 */

public class KeyManager {
    private static final String TAG = "KeyManager";
    private final static char[] hexArray = "0123456789abcdef".toCharArray();
    private KeyPairGenerator kpg;
    private KeyPair pair;
    private String serverPublicKeyPem = "-----BEGIN PUBLIC KEY-----\n" +
            "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAz5UbP9pCgzBsqSCrOj67\n" +
            "ChvmHEr5cxBrXA4vA7I9EVvSnmasIZGuARD4gJSxIeX57kwKURinQq0dxGsMKwqd\n" +
            "0RpF5CJXq9k4d713pdG0pK6MI52Ir78JCt0DzCGfz9Tpf20BUyLTdDKjVGTFJBP5\n" +
            "imXGmpgt7ETegEXTEBg4gJ9ws1tpAb21hlLTso2UkyR7s75aA0RQFYpw/caA3DeD\n" +
            "JvnXza1Fwy11tuPMO/8H+cWhmXKSI0WB1FXQTnzaFoXF5NQMKCUk0+zQLEH/A/eZ\n" +
            "NrKzgkOXVNMf9Vo1/G0ZOWvbw4nHQRJ8jAuArIrX/bLFxojpJQEuBW/ho/I8R1fb\n" +
            "7QIDAQAB\n" +
            "-----END PUBLIC KEY-----";
    private static Signature sig;
    private PrivateKey privateKey;
    private PublicKey publicKey;
    private PublicKey serverPublicKey;
    private String publicKeyPem;

    public KeyManager() {
        try {
            PemReader pemReader = new PemReader(new StringReader(this.serverPublicKeyPem));
            PemObject pemObject = pemReader.readPemObject();

            KeyFactory kf = KeyFactory.getInstance("RSA", "BC");
            byte[] publicKeyContent = pemObject.getContent();

            X509EncodedKeySpec spec = new X509EncodedKeySpec(publicKeyContent);
            this.serverPublicKey = kf.generatePublic(spec);
        } catch (NoSuchAlgorithmException | IOException | NoSuchProviderException | InvalidKeySpecException e) {
            e.printStackTrace();
        }
    }


    public void generateKey() {
        try {
            kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(2048);
            pair = kpg.generateKeyPair();

            this.privateKey = pair.getPrivate();
            this.publicKey = pair.getPublic();

            StringWriter writer = new StringWriter();
            PemWriter pemWriter = new PemWriter(writer);

            pemWriter.writeObject(new PemObject("PUBLIC KEY", this.publicKey.getEncoded()));
            pemWriter.flush();
            pemWriter.close();
            this.publicKeyPem = writer.toString();
        }
        catch (NoSuchAlgorithmException | IOException e) {
            Log.d(TAG, e.getMessage());
        }
    }


    public PrivateKey getPrivateKey() {
        return privateKey;
    }


    public PublicKey getPublicKey() {
        return publicKey;


    }

    public String getPublicKeyPem() {
        return this.publicKeyPem;
    }

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len/2];

        for(int i = 0; i < len; i+=2){
            data[i/2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i+1), 16));
        }

        return data;
    }

    public boolean verifyResponse(JSONObject response){
        String payload;
        try {
            payload = response.getString("payload");
        } catch (JSONException e) {
            Log.d("TheSquare", "Missing payload");
            return false;
        }

        String signature;
        try {
            signature = response.getString("signature");
        } catch (JSONException e) {
            Log.d("TheSquare", "Missing signature");
            return false;
        }

        try {

            sig = Signature.getInstance("SHA256withRSA");
            sig.initVerify(this.serverPublicKey);
            sig.update(payload.getBytes("UTF-8"));
            return sig.verify(hexStringToByteArray(signature));
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            Log.d("TheSquare", "No algorithm, invalid key or invalid signature");
            Log.d(TAG, e.getMessage());
            return false;
        } catch (UnsupportedEncodingException e) {
            Log.d("TheSquare", "Unsupported encoding");
            Log.d(TAG, e.getMessage());
            return false;
        }
    }

    public String signMessage(String message) {
        try {
            sig = Signature.getInstance("SHA256withRSA");
            sig.initSign(getPrivateKey());
            sig.update(message.getBytes("UTF-8"));

            return bytesToHex(sig.sign());
        } catch (NoSuchAlgorithmException | SignatureException | InvalidKeyException | UnsupportedEncodingException e) {
            Log.d(TAG, e.getMessage());
        }

        return null;
    }

    public void setPublicKey(PublicKey publicKey) {
        this.publicKey = publicKey;
    }

    public void setPublicKeyPem(String publicKeyPem) {
        this.publicKeyPem = publicKeyPem;
    }

    public void setPrivateKey(PrivateKey privateKey) {
        this.privateKey = privateKey;
    }
}

