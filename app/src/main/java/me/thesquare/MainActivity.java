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
    private EditText chatInput;
    private FSDClient fsdClient;
    private ListView listView;
    private List<ChatItem> chat = new ArrayList<>();
    private ChatListViewAdapter chatAdapter;
    private PermissionHandler permissionHandler;
    private Chronometer stopWatch;
    private boolean isStarted;
    private Socket serverConnection;

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
        chatInput = (EditText)findViewById(R.id.chatinput);
        listView = (ListView) findViewById(R.id.lvChat);
        listView = (ListView) findViewById(R.id.lvChat);
        permissionHandler = new PermissionHandler(this,this.getApplicationContext());
        chatAdapter = new ChatListViewAdapter(this,getLayoutInflater(), (ArrayList<ChatItem>) chat);


        try {
            serverConnection = new Socket("145.49.7.49",1312);
            fsdClient = new FSDClient(null, serverConnection.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        // TODO: don't use try/catch and change server settings

        assert textureView != null;

        try {
            recorder = new Recorder(textureView, fsdClient);
        } catch (IOException e) {
            Log.d(TAG, e.getMessage());
        }

        listView.setAdapter(chatAdapter);
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
        addChat.setChatname("You");
        if(chatInput.getText().toString().equals("")){
            Toast.makeText(this, "You did not enter a valid message", Toast.LENGTH_SHORT).show();
        } else {
            addChat.setChattext(chatInput.getText().toString());
            chat.add(addChat);
            chatInput.setText("");
            chatAdapter.notifyDataSetChanged();
            int chatsize = chat.size();
            if (chatsize >= 5) {
                chat.remove(0);
                chatAdapter.notifyDataSetChanged();
            }
        }
    }

    public void onCaptureClick(View view) {
        if (!isStarted){
            stopWatch.setBase(SystemClock.elapsedRealtime());
            stopWatch.start();
            isStarted = true;
        }
        if (recorder.getRecordingState()) {
            Log.d(TAG, "Already recording");
        } else {
            recorder.start();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            recorder.stop();
        } catch (InterruptedException e) {
            Log.d(TAG, e.getMessage());
        }
        recorder.releaseMediaRecorder();
        recorder.releaseCamera();
    }
}