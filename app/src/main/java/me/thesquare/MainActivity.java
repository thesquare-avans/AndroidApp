package me.thesquare;

import android.content.pm.ActivityInfo;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceView;
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
    private SurfaceView surfaceView;

    private Recorder recorder;
    private int fileCount;
    private EditText chatinput;
    private String username;
    private FSDClient FSDClient;
    private ListView listView;
    private ApiHandler apiHandler;
    private List<chatItem> chat = new ArrayList<>();
    private chatListViewAdapter chatadapter;
    private static final String TAG = "AndroidCameraApi";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        surfaceView = (SurfaceView) findViewById(R.id.texture);
        assert surfaceView != null;
        try {
            recorder = new Recorder(surfaceView, (FragmentWriter) FSDClient);
        }
        catch(IOException e){
            Log.e(TAG, e.getMessage());
        }
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
        recorder.start();
    }



    public void onPause() {
        super.onPause();
        Log.e(TAG, "onPause");

    }

    protected void onResume(){
        super.onResume();

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