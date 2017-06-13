package me.thesquare;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.util.Arrays;

/**
 * Created by Anthony on 13-6-2017.
 */

public class FSDClient implements FragmentWriter {
    //The signature
    private PrivateKey privateKey;
    //The socket to output to
    private OutputStream outputStream;

    public FSDClient(PrivateKey privateKey, OutputStream outputStream){
        this.outputStream = outputStream;
        this.privateKey = privateKey;
    }

    private void hashSignAndSendData(byte[] fragment){
            try {
                Signature signature = Signature.getInstance("SHA256withRSA");
                signature.initSign(privateKey);
                signature.update(fragment);

                int totalLength = fragment.length + 4 + 256;
                // The length of the data+length+signed and hashed data
                byte[] bytesPackageLength = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(totalLength).array();
                byte[] signedSignature = signature.sign();

                byte[] allData = new byte[totalLength];
                //copy length to array
                System.arraycopy(bytesPackageLength,0,allData,0,bytesPackageLength.length);
                //Copy signed data to array
                System.arraycopy(signedSignature,0,allData,bytesPackageLength.length,signedSignature.length);
                //Copy video data to array
                System.arraycopy(fragment,0,allData,bytesPackageLength.length+signedSignature.length,fragment.length);
                //Write to the socket
                outputStream.write(fragment);

            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (SignatureException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InvalidKeyException e) {
                e.printStackTrace();
            }
    }

    @Override
    public void writeFragment(byte[] fragment) {
        hashSignAndSendData(fragment);
    }
}
