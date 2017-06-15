package me.thesquare;

import android.content.pm.ActivityInfo;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    private TextureView textureView;
    private Camera cam;
    private Record record;
    private int fileCount;
    private FSDClient fsdClient;
    private static final String TAG = "AndroidCameraApi";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectNetwork() // or .detectAll() for all detectable problems
                .penaltyDialog()  //show a dialog
                .permitNetwork() //permit Network access
                .build());
        setContentView(R.layout.activity_main);
        textureView = (TextureView) findViewById(R.id.texture);


        assert textureView != null;

        Socket serverConnection;

        try {
            serverConnection = new Socket("192.168.0.105",1312);
            fsdClient = new FSDClient(null, serverConnection.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

//        record = new Record(this, textureView);
        fileCount = 1;
        ImageButton btn2 = (ImageButton)findViewById(R.id.btnSwitch);
        //set items
        List<chatItem> test = new ArrayList<>();
        chatItem c = new chatItem();
        // end items
        ListView listView = (ListView) findViewById(R.id.lvChat);
        chatListViewAdapter adapter = new chatListViewAdapter(this,getLayoutInflater(), (ArrayList<chatItem>) test);

        listView.setAdapter(adapter);

//        cam = new Camera(textureView, btn2, this, MainActivity.this);

        try {
            record = new Record(textureView, fsdClient);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void onCaptureClick(View view) {
        if (record.getRecordingState()) {
            // BEGIN_INCLUDE(stop_release_media_recorder)

            // stop recording and release camera
            record.Capture();
            fileCount++;
        } else {
            record.setFilename(String.valueOf(fileCount));
            record.start();
        }
    }

    public void onPause() {
        super.onPause();
        Log.e(TAG, "onPause");
//        cam.closeCamera();
//        cam.stopBackgroundThread();
        // if we are using MediaRecorder, release it first
        record.releaseMediaRecorder();
        // release the camera immediately on pause event
        record.releaseCamera();
//        record.removeTempFiles();
    }

    protected void onResume(){
        super.onResume();
//        cam.resume();
    }
}