package me.thesquare;

import android.content.pm.ActivityInfo;
import android.graphics.SurfaceTexture;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    private Recorder recorder;
    private EditText chatinput;
    private String username;
    private FSDClient fsdClient;
    private ListView listView;
    private ApiHandler apiHandler;
    private Camera cam;
    private List<chatItem> chat = new ArrayList<>();
    private chatListViewAdapter chatadapter;
    private static final String TAG = "MainActivity";
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
            serverConnection = new Socket("145.49.13.101",1312);
            fsdClient = new FSDClient(null, serverConnection.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        TextureView textview = (TextureView) findViewById(R.id.texture);


        permissionHandler = new PermissionHandler(this,this.getApplicationContext());

        try {
            recorder = new Recorder(textview, fsdClient);
        }
        catch(IOException e){
            Log.e(TAG, e.getMessage());
        }

        ImageButton btn2 = (ImageButton)findViewById(R.id.btnSwitch);
        cam = new Camera(textview, btn2, this, MainActivity.this);
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
        checkPermissions();
    }

    private void checkPermissions(){
        boolean[] perms = permissionHandler.checkPermissions();
        if (! perms[0] && ! perms[1] ){
            permissionHandler.sendToPermissionsActivity(this, PermissionsActivity.class);
        }
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