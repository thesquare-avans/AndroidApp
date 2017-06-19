package me.thesquare;

import android.os.Bundle;
import android.os.StrictMode;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private TextureView textureView;
    private Recorder recorder;
    private EditText chatinput;
    private String username;
    private FSDClient fsdClient;
    private ListView listView;
    private ApiHandler apiHandler;
    private List<ChatItem> chat = new ArrayList<>();
    private ChatListViewAdapter chatadapter;
    private PermissionHandler permissionHandler;
    private Chronometer stopWatch;
    private boolean isStarted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        isStarted = false;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectNetwork() // or .detectAll() for all detectable problems
                .penaltyDialog()  //show a dialog
                .permitNetwork() //permit Network access
                .build());

        textureView = (TextureView) findViewById(R.id.texture);
        stopWatch = (Chronometer) findViewById(R.id.stopWatch);
        chatinput = (EditText)findViewById(R.id.chatinput);
        listView = (ListView) findViewById(R.id.lvChat);
        // TODO: don't use try/catch and change server settings
        Socket serverConnection;

        try {
            serverConnection = new Socket("145.49.7.49",1312);
            fsdClient = new FSDClient(null, serverConnection.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        assert textureView != null;

        try {
            recorder = new Recorder(textureView, fsdClient);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //set items
        List<ChatItem> test = new ArrayList<>();
        ChatItem c = new ChatItem();
        // end items
        listView = (ListView) findViewById(R.id.lvChat);
        ChatListViewAdapter adapter = new ChatListViewAdapter(this,getLayoutInflater(), (ArrayList<ChatItem>) test);

        permissionHandler = new PermissionHandler(this,this.getApplicationContext());

        try {
            recorder = new Recorder(textureView, fsdClient);
        }
        catch(IOException e){
            Log.e(TAG, e.getMessage());
        }
        chatadapter = new ChatListViewAdapter(this,getLayoutInflater(), (ArrayList<ChatItem>) chat);

        listView.setAdapter(chatadapter);
    }

    public void onPause() {
        super.onPause();
        Log.e(TAG, "onPause");
        recorder.releaseMediaRecorder();
        recorder.releaseCamera();
    }

    protected void onResume(){
        super.onResume();
        checkPermissions();
    }

    private void checkPermissions(){
        boolean[] perms = permissionHandler.checkPermissions();
        if (! perms[0] && ! perms[1] ){
            permissionHandler.sendToPermissionsActivity(this, PermissionsActivity.class);
        }
    }

    public void AddChat(View view){
        ChatItem addChat = new ChatItem();
        addChat.chatname = "You";
        if(chatinput.getText().toString().equals("")){
            Toast.makeText(this, "You did not enter a valid message", Toast.LENGTH_SHORT).show();
        } else {
            addChat.chattext = chatinput.getText().toString();
            chat.add(addChat);
            chatinput.setText("");
            chatadapter.notifyDataSetChanged();
            int chatsize = chat.size();
            if (chatsize >= 5) {
                chat.remove(0);
                chatadapter.notifyDataSetChanged();
            }
        }
    }

    public void onCaptureClick(View view) {
        if (!isStarted){
            //stopWatch.setBase(SystemClock.elapsedRealtime() - (59* 60000 + 0 * 1000));
            stopWatch.setBase(SystemClock.elapsedRealtime());


            stopWatch.start();
            isStarted = true;
        }
        if (recorder.getRecordingState()) {
            // BEGIN_INCLUDE(stop_release_media_recorder)
            Log.d(TAG, "Already recording");
            // stop recording and release camera
        } else {
            recorder.start();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        recorder.releaseMediaRecorder();
        recorder.releaseCamera();
    }
}