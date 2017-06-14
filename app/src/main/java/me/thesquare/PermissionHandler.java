package me.thesquare;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.widget.Toast;

import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

/**
 * Created by jensderond on 14/06/2017.
 */

public class PermissionHandler extends Activity implements EasyPermissions.PermissionCallbacks {
    private static final int RC_CAMERA_PERM = 111;
    private static final int RC_MIC_PERM = 112;
    private Context context;
    private Activity activity;
    private boolean micPerm = false, camPerm = false;
    private boolean[] perms = new boolean[2];

    public PermissionHandler(Activity activity, Context context){
        this.activity = activity;
        this.context = context;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        activity.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @AfterPermissionGranted(RC_CAMERA_PERM)
    public void cameraTask() {
        if (EasyPermissions.hasPermissions(context, Manifest.permission.CAMERA)) {
            // Have permission, do the thing!
            Toast.makeText(context, "We already have the camera permission", Toast.LENGTH_LONG).show();

        } else {
            // Ask for one permission
            EasyPermissions.requestPermissions(activity, context.getString(R.string.permission_text_cam),
                    RC_CAMERA_PERM, Manifest.permission_group.CAMERA);
        }
    }

    @AfterPermissionGranted(RC_MIC_PERM)
    public void microphoneTask() {
        if (EasyPermissions.hasPermissions(context, Manifest.permission.RECORD_AUDIO)) {
            // Have permission, do the thing!
            Toast.makeText(context, "We already have the microphone permission", Toast.LENGTH_LONG).show();
        } else {
            // Ask for one permission
            EasyPermissions.requestPermissions(activity, context.getString(R.string.permission_text_mic),
                    RC_MIC_PERM, Manifest.permission_group.MICROPHONE);
        }
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        Log.d("Permission", "onPermissionsGranted:" + requestCode + ":" + perms.size());
        checkPermissions();
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        Log.d("Permission", "onPermissionsDenied:" + requestCode + ":" + perms.size());
    }

    public boolean[] checkPermissions(){
        if (EasyPermissions.hasPermissions(context, Manifest.permission.RECORD_AUDIO)) {
            // Have the Microphone permission, do the thing!
            micPerm = true;
        }
        if (!EasyPermissions.hasPermissions(context, Manifest.permission.RECORD_AUDIO)) {
            // No Microphone permission!
            micPerm = false;
        }
        if (EasyPermissions.hasPermissions(context, Manifest.permission.CAMERA)) {
            // Have the Camera permission, do the thing!
            camPerm = true;
        }
        if (!EasyPermissions.hasPermissions(context, Manifest.permission.CAMERA)) {
            // No Camera permission!
            camPerm = false;
        }
        perms[0] = micPerm;
        perms[1] = camPerm;
        return perms;
    }

    public void sendToPermissionsActivity(Context from, Class to ){
        Intent i = new Intent(from, to);
        context.startActivity(i);
    }
}
