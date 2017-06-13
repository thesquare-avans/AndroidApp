package me.thesquare;

import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.SignatureException;
import java.util.Arrays;

/**
 * Created by Anthony on 13-6-2017.
 */

public class FSDClient {
    //The signature
    private Signature signature;
    //All the video data
    private byte[] videoData;
    //The socket to output to
    private Socket socket;

    public FSDClient(Signature signature, Socket socket, byte[] videoData){
        this.socket = socket;
        this.signature = signature;
        this.videoData = videoData;
    }

    private void hashSignAndSendData(){
            try {
                signature.getInstance("SHA256withRSA");
                //TODO: get key
                //signature.initSign(null);
                signature.update(videoData);

                int totalLength = videoData.length + 4 + 256;
                // The length of the data+length+signed and hashed data
                byte[] bytesPackageLength = ByteBuffer.allocate(4).putInt(totalLength).array();
                byte[] signedSignature = signature.sign();


                int totalPackageLength = bytesPackageLength.length+signedSignature.length+videoData.length;

                if (totalLength != totalPackageLength){
                    throw new ArrayIndexOutOfBoundsException();
                }

                byte[] allData = new byte[totalLength];
                //copy length to array
                System.arraycopy(bytesPackageLength,0,allData,0,bytesPackageLength.length);
                //Copy signed data to array
                System.arraycopy(signedSignature,0,allData,bytesPackageLength.length,signedSignature.length);
                //Copy video data to array
                System.arraycopy(videoData,0,allData,bytesPackageLength.length+signedSignature.length,videoData.length);
                //Write to the socket
                socket.getOutputStream().write(videoData);

            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (SignatureException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ArrayIndexOutOfBoundsException e){
                //The totallength and packagelength are not equal
                e.printStackTrace();
            }

    }
}
