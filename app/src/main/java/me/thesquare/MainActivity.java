package me.thesquare;

import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectNetwork() // or .detectAll() for all detectable problems
                .penaltyDialog()  //show a dialog
                .permitNetwork() //permit Network access
                .build());
        // TODO: don't use try/catch and change server settings
        Socket serverConnection;

        try {
            serverConnection = new Socket("145.49.7.49",1312);
            fsdClient = new FSDClient(null, serverConnection.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        textureView = (TextureView) findViewById(R.id.texture);
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

        ImageButton btn2 = (ImageButton)findViewById(R.id.btnSwitch);
        chatinput = (EditText)findViewById(R.id.chatinput);
        listView = (ListView) findViewById(R.id.lvChat);
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

        addChat.chatname = username;
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