package me.thesquare.activities;

import android.content.Context;
import android.content.SharedPreferences;

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
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import me.thesquare.managers.ChatSocket;
import me.thesquare.R;
import me.thesquare.TheSquareApplication;
import me.thesquare.adapters.ChatListViewAdapter;
import me.thesquare.interfaces.StreamResponse;
import me.thesquare.interfaces.UserResponse;
import me.thesquare.managers.ApiHandler;
import me.thesquare.managers.FSDClient;
import me.thesquare.managers.KeyManager;
import me.thesquare.managers.PermissionHandler;
import me.thesquare.managers.Recorder;
import me.thesquare.models.ChatItem;
import me.thesquare.models.StreamModel;
import me.thesquare.models.UserModel;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final String SERVER_IP = "145.49.13.101";
    private Recorder recorder;
    private EditText chatInput;
    private FSDClient fsdClient;
    private List<ChatItem> chat = new ArrayList<>();
    private PermissionHandler permissionHandler;
    private Chronometer stopWatch;
    private boolean isStarted;
    private ChatSocket chatSocket;
    private TextView satosi;
    private ApiHandler handler;
    private StreamModel streamModel;
    private KeyManager manager;
    private String currentUser;
    private Socket serverConnection;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        isStarted = false;
        super.onCreate(savedInstanceState);

        SharedPreferences sharedPref = getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        currentUser = sharedPref.getString("cur_user", null);
        setContentView(R.layout.activity_main);
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectNetwork() // or .detectAll() for all detectable problems
                .penaltyDialog()  //show a dialog
                .permitNetwork() //permit Network access
                .build());

        TextureView textureView = (TextureView) findViewById(R.id.texture);
        stopWatch = (Chronometer) findViewById(R.id.stopWatch);
        TextView txtSatoshi = (TextView) findViewById(R.id.txtSatoshi);
        txtSatoshi.setText(String.valueOf(1));
        stopWatch.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener()
        {

            KeyManager keyManager = ((TheSquareApplication) getApplication()).keyManager;

            @Override
            public void onChronometerTick(Chronometer chronometer)
            {
                int elapsedMillis = (int) (SystemClock.elapsedRealtime() - chronometer.getBase());
                int seconds = elapsedMillis / 1000;

                if (seconds % 3600 == 0 && seconds != 0){
                    ApiHandler apihandler = new ApiHandler(keyManager, getApplicationContext());
                    apihandler.getLoggedInUser(new UserResponse() {
                        @Override
                        public void on(UserModel userModel) {
                            if(userModel != null)
                            {
                                String satoshi = Integer.toString(userModel.getSatoshi());
                                TextView txtSatoshi = (TextView) findViewById(R.id.txtSatoshi);
                                txtSatoshi.setText(satoshi);
                            }
                        }
                    });
                }
            }
        });
        chatInput = (EditText) findViewById(R.id.chatinput);
        ListView listView = (ListView) findViewById(R.id.lvChat);
        permissionHandler = new PermissionHandler(this.getApplicationContext());
        ChatListViewAdapter chatAdapter = new ChatListViewAdapter(this, getLayoutInflater(), (ArrayList<ChatItem>) chat);



        try {
            serverConnection = new Socket(SERVER_IP, 1234);
            fsdClient = new FSDClient(null, serverConnection.getOutputStream());
        } catch (IOException e) {
            Log.d(TAG, e.getMessage());
        }
        // TODO: don't use try/catch and change server settings

        assert textureView != null;

        try {
            recorder = new Recorder(textureView, fsdClient);
        } catch (IOException e) {
            Log.d(TAG, e.getMessage());
        }

        listView.setAdapter(chatAdapter);
        startStream();
    }

    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
        recorder.releaseMediaRecorder();
        recorder.releaseCamera();
    }

    protected void onResume(){
        super.onResume();
        checkPermissions();
    }

    private void startStream(){

        manager = ((TheSquareApplication) this.getApplication()).keyManager;
        handler = new ApiHandler(manager, this);
        final MainActivity thisInstance = this;

        handler.startStream(currentUser + "_Stream", new StreamResponse() {
            @Override
            public void on(StreamModel streamModel) {
                chatSocket = new ChatSocket(streamModel.getChatserver(), streamModel.getId(), manager, handler, thisInstance);
                chatSocket.socketConnect();
                Log.d(TAG, streamModel.toString());
            }
        });
    }

    private void checkPermissions(){
        boolean[] perms = permissionHandler.checkPermissions();
        if (! perms[0] && ! perms[1] ){
            permissionHandler.sendToPermissionsActivity(this, PermissionsActivity.class);
        }
    }

    public void AddChat(String username , String message){
        ChatItem addChat = new ChatItem();
        addChat.setChatname(username);
        addChat.setChattext(message);
        chat.add(addChat);
        int chatSize = chat.size();
        if (chatSize >= 5) {
            chat.remove(0);
        }
    }

    public void sendChat(){
        if(chatInput.getText().toString().equals("")){
            Toast.makeText(this, "Please fill in the field.", Toast.LENGTH_SHORT).show();
        } else {
            AddChat("You", chatInput.getText().toString());
            chatInput.setText("");
        }
    }

    public void onCaptureClick(View view) {
        if (!isStarted){
            stopWatch.setBase(SystemClock.elapsedRealtime());
            recorder.start();
            stopWatch.start();
            isStarted = true;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(streamModel != null) {
            handler.stopStream(streamModel.getId());
        }
        try {
            recorder.stop();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Log.d(TAG, e.getMessage());
        }
        recorder.releaseMediaRecorder();
        recorder.releaseCamera();
        try {
            serverConnection.close();
        } catch (IOException e) {
            Log.d(TAG, e.getMessage());
        }
    }

}