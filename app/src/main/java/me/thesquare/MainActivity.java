package me.thesquare;

import android.app.Activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import android.os.Bundle;
import android.os.StrictMode;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import me.thesquare.ApiResponses.StreamResponse;
import me.thesquare.ApiResponses.UserResponse;
import me.thesquare.models.StreamModel;
import me.thesquare.models.UserModel;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final String SERVER_IP = "145.49.13.101";
    private Recorder recorder;
    private EditText chatInput;
    private FSDClient fsdClient;
    private List<ChatItem> chat = new ArrayList<>();
    private ChatListViewAdapter chatAdapter;
    private PermissionHandler permissionHandler;
    private Chronometer stopWatch;
    private boolean isStarted;
    private ChatSocket chatSocket;
    private TextView satosi;
    private ApiHandler handler;
    private StreamModel streamModel;
    private Button btnExit;
    private KeyManager manager;

    private String current_user;

    private Socket serverConnection;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        isStarted = false;
        super.onCreate(savedInstanceState);

        SharedPreferences sharedPref = getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        current_user = sharedPref.getString("cur_user", null);
        setContentView(R.layout.activity_main);
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectNetwork() // or .detectAll() for all detectable problems
                .penaltyDialog()  //show a dialog
                .permitNetwork() //permit Network access
                .build());

        TextureView textureView = (TextureView) findViewById(R.id.texture);
        stopWatch = (Chronometer) findViewById(R.id.stopWatch);
        stopWatch.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener()
        {

            KeyManager keyManager = ((TheSquareApplication) getApplication()).keyManager;

            @Override
            public void onChronometerTick(Chronometer chronometer)
            {
                int elapsedMillis = (int) (SystemClock.elapsedRealtime() - chronometer.getBase());
                int seconds = (int) elapsedMillis / 1000;

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
        btnExit = (Button) findViewById(R.id.btnExit);
        ListView listView = (ListView) findViewById(R.id.lvChat);
        permissionHandler = new PermissionHandler(this.getApplicationContext());
        chatAdapter = new ChatListViewAdapter(this, getLayoutInflater(), (ArrayList<ChatItem>) chat);
        satosi = (TextView) findViewById(R.id.txtSatosi);
        Intent intent = new Intent();
        String intentsatosi = intent.getStringExtra("getSatosi");
        satosi.setText(intentsatosi);



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
        test();
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

    private void test(){

        manager = ((TheSquareApplication) this.getApplication()).keyManager;
        handler = new ApiHandler(manager, this);

        handler.startStream(current_user + "_Stream", new StreamResponse() {
            @Override
            public void on(StreamModel streamModel) {
                chatSocket = new ChatSocket(streamModel.getChatserver(), streamModel.getId(), manager, handler);
                chatSocket.socketConnect();
                Log.e("gekke chat", streamModel.toString());
            }
        });
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
            Toast.makeText(this, "Please fill in the field!", Toast.LENGTH_SHORT).show();
        } else {
            addChat.setChattext(chatInput.getText().toString());
            chat.add(addChat);
            chatInput.setText("");
            chatAdapter.notifyDataSetChanged();
            int chatSize = chat.size();
            if (chatSize >= 5) {
                chat.remove(0);
                chatAdapter.notifyDataSetChanged();
            }
        }
    }

    public void onCaptureClick(View view) {
        if (!isStarted){
            stopWatch.setBase(SystemClock.elapsedRealtime());
            this.test();
            stopWatch.start();
            isStarted = true;
        }
        else {
            Toast.makeText(this, "test", Toast.LENGTH_SHORT).show();
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