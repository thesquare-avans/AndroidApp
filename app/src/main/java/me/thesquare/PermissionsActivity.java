package me.thesquare;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.github.zagum.switchicon.SwitchIconView;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

/**
 * Created by jensderond on 13/06/2017.
 */

public class PermissionsActivity extends Activity implements EasyPermissions.PermissionCallbacks {
    private static final int RC_CAMERA_PERM = 111;
    private static final int RC_MIC_PERM = 112;
    private SwitchIconView cameraIconView, micIconView;
    private Button btnEnter;
    private boolean micPerm = false, camPerm = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initDatabase();
        setContentView(R.layout.activity_permissions);
        cameraIconView = (SwitchIconView) findViewById(R.id.cameraIconView);
        micIconView = (SwitchIconView) findViewById(R.id.micIconView);
        final View cameraButton = findViewById(R.id.buttonCam);
        btnEnter = (Button) findViewById(R.id.btnEnter);
        View micButton = findViewById(R.id.buttonMic);
        checkPermissions();

        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraTask();
            }
        });
        micButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                microphoneTask();
            }
        });

        btnEnter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(view.getContext(),MainActivity.class);
                startActivityForResult(i,0);

            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        checkPermissions();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @AfterPermissionGranted(RC_CAMERA_PERM)
    public void cameraTask() {
        if (EasyPermissions.hasPermissions(this, Manifest.permission.CAMERA)) {
            cameraIconView.setIconEnabled(true,true);
            // Have permission, do the thing!
            Toast.makeText(this, "We already have the camera permission", Toast.LENGTH_LONG).show();

        } else {
            // Ask for one permission
            EasyPermissions.requestPermissions(this, getString(R.string.permission_text_cam),
                    RC_CAMERA_PERM, Manifest.permission_group.CAMERA);
        }
    }

    @AfterPermissionGranted(RC_MIC_PERM)
    public void microphoneTask() {
        if (EasyPermissions.hasPermissions(this, Manifest.permission.RECORD_AUDIO)) {
            // Have permission, do the thing!
            micIconView.setIconEnabled(true,true);
            Toast.makeText(this, "We already have the microphone permission", Toast.LENGTH_LONG).show();
        } else {
            // Ask for one permission
            EasyPermissions.requestPermissions(this, getString(R.string.permission_text_mic),
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

        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this).build().show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == AppSettingsDialog.DEFAULT_SETTINGS_REQ_CODE) {
            // Do something after user returned from app settings screen, like showing a Toast.
            checkPermissions();
        }
    }

    public void checkPermissions(){
        if (EasyPermissions.hasPermissions(this, Manifest.permission.RECORD_AUDIO)) {
            // Have the Microphone permission, do the thing!
            micIconView.setIconEnabled(true,true);
            micPerm = true;
        }
        if (!EasyPermissions.hasPermissions(this, Manifest.permission.RECORD_AUDIO)) {
            // No Microphone permission!
            micIconView.setIconEnabled(false,true);
            micPerm = false;
        }
        if (EasyPermissions.hasPermissions(this, Manifest.permission.CAMERA)) {
            // Have the Camera permission, do the thing!
            cameraIconView.setIconEnabled(true,true);
            camPerm = true;
        }
        if (!EasyPermissions.hasPermissions(this, Manifest.permission.CAMERA)) {
            // No Camera permission!
            cameraIconView.setIconEnabled(false,true);
            camPerm = false;
        }
        if (micPerm && camPerm){
            btnEnter.setEnabled(true);
        }
        else {
            btnEnter.setEnabled(false);
        }
    }
    private void initDatabase(){
        Realm.init(this);
        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder()
                .deleteRealmIfMigrationNeeded()
                .build();
        Realm.setDefaultConfiguration(realmConfiguration);
    }
}
