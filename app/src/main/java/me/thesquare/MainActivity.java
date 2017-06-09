package me.thesquare;


import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.TextureView;
import android.widget.ImageButton;
import android.widget.ListView;
import java.util.ArrayList;
import java.util.List;
import net.majorkernelpanic.streaming.SessionBuilder;
import net.majorkernelpanic.streaming.gl.SurfaceView;
import net.majorkernelpanic.streaming.rtsp.RtspServer;


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