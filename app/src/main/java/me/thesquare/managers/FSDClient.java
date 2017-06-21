package me.thesquare.managers;

import android.util.Log;

import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.PrivateKey;

import me.thesquare.interfaces.FragmentWriter;

/**
 * Created by Anthony on 13-6-2017.
 */

public class FSDClient implements FragmentWriter {
    private static final String TAG = "FSDClient";
    private PrivateKey privateKey;
    private OutputStream outputStream;

    public FSDClient(PrivateKey privateKey, OutputStream outputStream){
        this.outputStream = outputStream;
        this.privateKey = privateKey;
    }

    private void hashSignAndSendData(byte[] fragment){
            try {
                //Signature signature = Signature.getInstance("SHA256withRSA");
                //signature.initSign(privateKey);
                //signature.update(fragment);

                int totalLength = fragment.length;
                // The length of the data+length+signed and hashed data
                byte[] bytesPackageLength = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(totalLength).array();
                outputStream.write(bytesPackageLength);
                //byte[] signedSignature = signature.sign();

                //Copy signed data to array
                //System.arraycopy(signedSignature,0,allData,bytesPackageLength.length,signedSignature.length);
                outputStream.write(new byte[256]);
                outputStream.write(fragment);

            } catch (Exception e) {
                Log.d(TAG, e.getMessage());
            }
    }

    @Override
    public void writeFragment(byte[] fragment) {
        hashSignAndSendData(fragment);
    }
}
