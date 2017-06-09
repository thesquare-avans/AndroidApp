package me.thesquare;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static android.R.id.list;
import static me.thesquare.R.id.btnSwitch;
import static me.thesquare.R.id.text;

public class MainActivity extends AppCompatActivity  {

    private TextureView textureView;
    private Camera cam;
    private static final String TAG = "AndroidCameraApi";

    public MainActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        textureView = (TextureView) findViewById(R.id.texture);
        assert textureView != null;

        ImageButton btn2 = (ImageButton)findViewById(R.id.btnSwitch);

        //set items
        List<chatItem> test = new ArrayList<>();
        chatItem c = new chatItem();
        c.chatname = "lars";
        c.chattext = "sdfasdfasdfsdfasdfasdf";
        test.add(c);
        // end items
        ListView listView = (ListView) findViewById(R.id.lvChat);
        chatListViewAdapter adapter = new chatListViewAdapter(this,getLayoutInflater(), (ArrayList<chatItem>) test);

        listView.setAdapter(adapter);

        cam = new Camera(textureView, btn2, this, MainActivity.this);
    }

    public void onPause() {
        super.onPause();
        Log.e(TAG, "onPause");
        cam.closeCamera();
        cam.stopBackgroundThread();

    }

    protected void onResume(){
        super.onResume();
        cam.resume();
    }
}