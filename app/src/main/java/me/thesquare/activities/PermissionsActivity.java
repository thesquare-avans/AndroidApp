package me.thesquare.activities;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.github.zagum.switchicon.SwitchIconView;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.single.CompositePermissionListener;
import com.karumi.dexter.listener.single.PermissionListener;
import com.karumi.dexter.listener.single.SnackbarOnDeniedPermissionListener;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import me.thesquare.R;
import me.thesquare.managers.PermissionHandler;

/**
 * Created by jensderond on 13/06/2017.
 */

public class PermissionsActivity extends Activity {
    private ViewGroup rootView;

    private static final String TAG = "Permission Activity";
    private SwitchIconView cameraIconView;
    private SwitchIconView micIconView;
    private PermissionHandler permissionHandler;
    private PermissionListener cameraPermissionListener;
    private PermissionListener microphonePermissionListener;
    private PermissionRequestErrorListener errorListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initDatabase();
        setContentView(R.layout.activity_permissions);

        rootView = (LinearLayout) findViewById(R.id.permLinLayout);
        cameraIconView = (SwitchIconView) findViewById(R.id.cameraIconView);
        micIconView = (SwitchIconView) findViewById(R.id.micIconView);
        final View cameraButton = findViewById(R.id.buttonCam);
        View micButton = findViewById(R.id.buttonMic);

        createPermissionListeners();

        boolean[] perms = permissionHandler.checkPermissions();
        setIcons(perms);

        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCameraPermissionButtonClicked();
            }
        });
        micButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onMicrophonePermissionButtonClicked();
            }
        });

    }

    private void initDatabase(){
        Realm.init(this);
        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder()
                .deleteRealmIfMigrationNeeded()
                .build();
        Realm.setDefaultConfiguration(realmConfiguration);
    }

    private void createPermissionListeners(){

        permissionHandler = new PermissionHandler(this,this.getApplicationContext());

        microphonePermissionListener = new CompositePermissionListener(permissionHandler,
                SnackbarOnDeniedPermissionListener.Builder.with(rootView,
                        "All permissions are required for this application to function")
                        .withOpenSettingsButton(R.string.permission_rationale_settings_button_text)
                        .withCallback(new Snackbar.Callback() {
                            @Override public void onShown(Snackbar snackbar) {
                                super.onShown(snackbar);
                            }

                            @Override public void onDismissed(Snackbar snackbar, int event) {
                                super.onDismissed(snackbar, event);
                            }
                        })
                        .build());
        cameraPermissionListener = new CompositePermissionListener(permissionHandler,
                SnackbarOnDeniedPermissionListener.Builder.with(rootView,
                        "All permissions are required for this application to function")
                        .withOpenSettingsButton(R.string.permission_rationale_settings_button_text)
                        .withCallback(new Snackbar.Callback() {
                            @Override public void onShown(Snackbar snackbar) {
                                super.onShown(snackbar);
                            }

                            @Override public void onDismissed(Snackbar snackbar, int event) {
                                super.onDismissed(snackbar, event);
                            }
                        })
                        .build());

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

    public void onCameraPermissionButtonClicked() {
        new Thread(new Runnable() {
            @Override public void run() {
                Dexter.withActivity(PermissionsActivity.this)
                        .withPermission(Manifest.permission.CAMERA)
                        .withListener(cameraPermissionListener)
                        .withErrorListener(errorListener)
                        .onSameThread()
                        .check();
            }
        }).start();
    }

    public void onMicrophonePermissionButtonClicked() {
        new Thread( new Runnable() {
            @Override public void run() {
                Dexter.withActivity(PermissionsActivity.this)
                        .withPermission(Manifest.permission.RECORD_AUDIO)
                        .withListener(microphonePermissionListener)
                        .withErrorListener(errorListener)
                        .onSameThread()
                        .check();
            }
        }).start();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public void showPermissionRationale(final PermissionToken token) {
        new AlertDialog.Builder(this).setTitle(R.string.permission_rationale_title)
                .setMessage(R.string.permission_rationale_message)
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        token.cancelPermissionRequest();
                    }
                })
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        token.continuePermissionRequest();
                    }
                })
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override public void onDismiss(DialogInterface dialog) {
                        token.cancelPermissionRequest();
                    }
                })
                .show();
    }
}