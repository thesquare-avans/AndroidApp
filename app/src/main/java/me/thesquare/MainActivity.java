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
    private Record record;
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
            serverConnection = new Socket("145.49.7.49",1312);
            fsdClient = new FSDClient(null, serverConnection.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        ImageButton btn2 = (ImageButton)findViewById(R.id.btnSwitch);
        //set items
        List<chatItem> test = new ArrayList<>();
        chatItem c = new chatItem();
        // end items
        ListView listView = (ListView) findViewById(R.id.lvChat);
        chatListViewAdapter adapter = new chatListViewAdapter(this,getLayoutInflater(), (ArrayList<chatItem>) test);

        listView.setAdapter(adapter);

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
        } else {
            record.start();
        }
    }

    public void onPause() {
        super.onPause();
        Log.e(TAG, "onPause");
        record.releaseMediaRecorder();
        record.releaseCamera();
    }

    protected void onResume(){
        super.onResume();
    }
}