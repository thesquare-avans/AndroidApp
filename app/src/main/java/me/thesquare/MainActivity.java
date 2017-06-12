package me.thesquare;

import android.content.pm.ActivityInfo;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    private TextureView textureView;
    private Camera cam;
    private Record record;
    private int fileCount;
    private static final String TAG = "AndroidCameraApi";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        textureView = (TextureView) findViewById(R.id.texture);


        assert textureView != null;

        record = new Record(this, textureView);
        fileCount = 1;
        ImageButton btn2 = (ImageButton)findViewById(R.id.btnSwitch);
        //set items
        List<chatItem> test = new ArrayList<>();
        chatItem c = new chatItem();
        c.chatname = "lars1337";
        c.chattext = "Hallo, ik ben een Larsson!";
        chatItem chat2 = new chatItem();
        chat2.chatname = "TheRubenGames";
        chat2.chattext = "Play the 2 mana card kappa :Omegalul:";
        chatItem chat3 = new chatItem();
        chat3.chatname = "CliffordLovesChicks";
        chat3.chattext = "Show boobs please!";
        chatItem chat4 = new chatItem();
        chat4.chatname = "AntonTestoBom";
        chat4.chattext = "Even een boterhammetje eten! #pindakaas";
        test.add(c);
        test.add(chat2);
        test.add(chat3);
        test.add(chat4);
        // end items
        ListView listView = (ListView) findViewById(R.id.lvChat);
        chatListViewAdapter adapter = new chatListViewAdapter(this,getLayoutInflater(), (ArrayList<chatItem>) test);

        listView.setAdapter(adapter);

//        cam = new Camera(textureView, btn2, this, MainActivity.this);



    }

    public void onCaptureClick(View view) {
        if (record.getRecordingState()) {
            // BEGIN_INCLUDE(stop_release_media_recorder)

            // stop recording and release camera
            record.Capture();
            fileCount++;
        } else {
            record.setFilename(String.valueOf(fileCount));
            record.prepareTask();
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
        record.removeTempFiles();
    }

    protected void onResume(){
        super.onResume();
//        cam.resume();
    }
}