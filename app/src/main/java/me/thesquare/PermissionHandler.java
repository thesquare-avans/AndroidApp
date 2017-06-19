package me.thesquare;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import pub.devrel.easypermissions.EasyPermissions;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

/**
 * Created by jensderond on 14/06/2017.
 */

public class PermissionHandler implements PermissionListener {
    private Context context;
    private final Activity activity;
    private final PermissionsActivity permissionsActivity;
    private boolean micPerm = false, camPerm = false;
    private boolean[] perms = new boolean[2];

    public PermissionHandler(Activity activity, Context context){
        this.activity = activity;
        this.context = context;
        this.permissionsActivity = null;
    }

    public PermissionHandler(PermissionsActivity permissionsActivity, Context context){
        this.activity = null;
        this.context = context;
        this.permissionsActivity = permissionsActivity;
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
        i.addFlags(FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
    }


    @Override public void onPermissionGranted(PermissionGrantedResponse response) {
        permissionsActivity.showPermissionGranted(response.getPermissionName());
        checkPermissions();
    }

    @Override public void onPermissionDenied(PermissionDeniedResponse response) {
        permissionsActivity.showPermissionDenied(response.getPermissionName(), response.isPermanentlyDenied());
        checkPermissions();
    }

    @Override public void onPermissionRationaleShouldBeShown(PermissionRequest permission,
                                                             PermissionToken token) {
        permissionsActivity.showPermissionRationale(token);
    }
}
