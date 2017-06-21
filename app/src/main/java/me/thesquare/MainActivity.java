package me.thesquare;

import android.content.Intent;
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
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import me.thesquare.apiresponses.StreamResponse;
import me.thesquare.models.StreamModel;


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

        TextureView textureView = (TextureView) findViewById(R.id.texture);
        stopWatch = (Chronometer) findViewById(R.id.stopWatch);

        chatInput = (EditText) findViewById(R.id.chatinput);
        ListView listView = (ListView) findViewById(R.id.lvChat);
        permissionHandler = new PermissionHandler(this.getApplicationContext());
        chatAdapter = new ChatListViewAdapter(this, getLayoutInflater(), (ArrayList<ChatItem>) chat);
        TextView txtSatosi = (TextView) findViewById(R.id.txtSatosi);
        Intent intent = new Intent();
        String intentsatosi = intent.getStringExtra("getSatosi");
        txtSatosi.setText(intentsatosi);



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

        KeyManager manager = ((TheSquareApplication) this.getApplication()).keyManager;
        ApiHandler handler = new ApiHandler(manager, this);

        handler.startStream("Stream 1", new StreamResponse() {
            @Override
            public void on(StreamModel streamModel) {
                Log.e(TAG, streamModel.toString());
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