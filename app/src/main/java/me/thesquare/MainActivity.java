package me.thesquare;

import android.content.pm.ActivityInfo;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
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
    private EditText chatinput;
    private String username;
    ListView listView;
    ApiHandler apiHandler;
    private List<chatItem> chat = new ArrayList<>();
    private chatListViewAdapter chatadapter;
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
        chat.add(c);
        chat.add(chat2);
        chat.add(chat3);
        chat.add(chat4);
        // end items
        chatinput = (EditText)findViewById(R.id.chatinput);
        listView = (ListView) findViewById(R.id.lvChat);
        chatadapter = new chatListViewAdapter(this,getLayoutInflater(), (ArrayList<chatItem>) chat);


        listView.setAdapter(chatadapter);

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
    public void AddChat(View view){

        chatItem addChat = new chatItem();
        username = "Testuser";
        addChat.chatname = username;
        addChat.chattext = chatinput.getText().toString();
        chat.add(addChat);
        chatinput.setText("");
        chatadapter.notifyDataSetChanged();
        int chatsize = chat.size();
        if (chatsize >= 5){
            chat.remove(0);
            chatadapter.notifyDataSetChanged();
        }
    }

}