package me.thesquare;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Objects;

import io.realm.Realm;
import io.realm.RealmResults;
import me.thesquare.models.UserModel;

public class LoginActivity extends AppCompatActivity {
    private Realm realm;
    private Button btnLogin;
    private EditText txtUsername;
    private KeyManager keyManager;
    private SharedPreferences sharedPref;
    private PermissionHandler permissionHandler;
    private ApiHandler apihandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        permissionHandler = new PermissionHandler(this,this.getApplicationContext());
        checkPermissions();

        sharedPref = getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        txtUsername = (EditText) findViewById(R.id.editText);
        btnLogin = (Button) findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if ( checkUsername() ){
                    addUser( txtUsername.getText().toString() );
                    Intent i = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(i);
                }
                else {
                    Context context = getApplicationContext();
                    CharSequence text = "Please fill in the login field.";
                    int duration = Toast.LENGTH_SHORT;
                    Toast toast =  Toast.makeText(context,text,duration);
                    toast.show();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkPermissions();
    }

    private boolean checkUsername(){
        return !Objects.equals( txtUsername.getText().toString(), "" );
    }

    private void addUser(String username){
        realm = Realm.getDefaultInstance();
        RealmResults<UserModel> result = realm.where( UserModel.class ).equalTo( "username", txtUsername.getText().toString() ).findAll();
        if ( result.isEmpty() && !username.equals("") ) {
            keyManager = new KeyManager();
            keyManager.generateKey();
            UserModel user = new UserModel();
            // Set the user ID from the API response
            // user.setId();
            user.setUsername( txtUsername.getText().toString() );
            user.setPrivateKey( keyManager.getPrivateKey().getEncoded() );
            user.setPublicKey( keyManager.getPublicKey().getEncoded() );
            keyManager.setUser(user);
            apihandler = new ApiHandler(keyManager);
            apihandler.register(txtUsername.getText().toString(), this);
            apihandler.authenticate(txtUsername.getText().toString(), keyManager.getPublicKey().toString());

            /*
              transaction to the database to update a player
             */
            realm.beginTransaction();
            realm.copyToRealmOrUpdate(user);
            realm.commitTransaction();

            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("cur_user", user.getUsername());
            editor.apply();

            Toast.makeText(LoginActivity.this,
                    "Gebruiker: " + user.getUsername() + " toegevoegd!", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void showUsers(){
        realm = Realm.getDefaultInstance();
        RealmResults<UserModel> result = realm.where(UserModel.class).findAll();

        if ( result != null && result.size() > 0) {
            for (int i = 0; i < result.size(); i++) {
                Log.d( "Name", result.get(i).getUsername() );
                Log.d( "Public key", result.get(i).getPublicKey().toString() );
                Log.d( "Private key", result.get(i).getPrivateKey().toString() );
            }
        }
    }

    private void removeAllUsers(){
        RealmResults<UserModel> result = realm.where(UserModel.class).findAll();

        result.deleteAllFromRealm();
    }

    private void checkPermissions(){
        boolean[] perms = permissionHandler.checkPermissions();
        if (! perms[0] || ! perms[1] ){
            permissionHandler.sendToPermissionsActivity(this, PermissionsActivity.class);
        }
    }
}
