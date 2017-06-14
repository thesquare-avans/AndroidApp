package me.thesquare;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import com.github.zagum.switchicon.SwitchIconView;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

/**
 * Created by jensderond on 13/06/2017.
 */

public class PermissionsActivity extends Activity implements EasyPermissions.PermissionCallbacks {
    private static final String TAG = "Permission Activity";
    private SwitchIconView cameraIconView, micIconView;
    private PermissionHandler permissionHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initDatabase();
        setContentView(R.layout.activity_permissions);
        cameraIconView = (SwitchIconView) findViewById(R.id.cameraIconView);
        micIconView = (SwitchIconView) findViewById(R.id.micIconView);
        final View cameraButton = findViewById(R.id.buttonCam);
        View micButton = findViewById(R.id.buttonMic);
        permissionHandler = new PermissionHandler(this,this.getApplicationContext());

        boolean[] perms = permissionHandler.checkPermissions();
        setIcons(perms);

        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                permissionHandler.cameraTask();
            }
        });
        micButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {permissionHandler.microphoneTask();
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        boolean[] perms = permissionHandler.checkPermissions();
        setIcons(perms);
    }

    private void setIcons(boolean[] perms){
        if ( perms[0] ) {
            Log.d(TAG, "Microphone permission granted");
            micIconView.setIconEnabled(true, true);
        }
        else {
            Log.d(TAG, "Microphone permission not granted");
            micIconView.setIconEnabled(false, true);
        }
        if ( perms[1] ) {
            Log.d(TAG, "Camera permission granted");
            cameraIconView.setIconEnabled(true, true);
        }
        else {
            Log.d(TAG, "Camera permission not granted");
            cameraIconView.setIconEnabled(false, true);
        }
        if ( perms[0] && perms[1] ){
            Intent i = new Intent(this, LoginActivity.class);
            startActivity(i);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AppSettingsDialog.DEFAULT_SETTINGS_REQ_CODE) {
            // Do something after user returned from app settings screen, like showing a Toast.
            boolean[] perms = permissionHandler.checkPermissions();
            setIcons(perms);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        Log.d("Permission", "onPermissionsGranted:" + requestCode + ":" + perms.size());
        permissionHandler.checkPermissions();
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        Log.d("Permission", "onPermissionsDenied:" + requestCode + ":" + perms.size());

        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this).build().show();
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