package me.thesquare.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.List;
import java.util.Objects;

import io.realm.Realm;
import io.realm.RealmResults;
import me.thesquare.R;
import me.thesquare.TheSquareApplication;
import me.thesquare.interfaces.RegisterResponse;
import me.thesquare.interfaces.UserResponse;
import me.thesquare.managers.ApiHandler;
import me.thesquare.managers.KeyManager;
import me.thesquare.managers.PermissionHandler;
import me.thesquare.models.UserModel;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private Realm realm;
    private EditText txtUsername;
    private KeyManager keyManager;
    private SharedPreferences sharedPref;
    private PermissionHandler permissionHandler;
    private ApiHandler apihandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        permissionHandler = new PermissionHandler(this.getApplicationContext());
        checkPermissions();

        realm = Realm.getDefaultInstance();
        keyManager = ((TheSquareApplication) this.getApplication()).keyManager;

        sharedPref = getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        String currentUser = sharedPref.getString("cur_user", null);
        userExists(currentUser);

        txtUsername = (EditText) findViewById(R.id.editText);
        Button btnLogin = (Button) findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if ( checkUsername() ){
                    userExists(txtUsername.getText().toString());
                    addUser( txtUsername.getText().toString() );
                }
                else {
                    Toast toast =  Toast.makeText( getApplicationContext(), "Please fill in the login field.", Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        });
    }

    private boolean userExists(String currentUser){

        if (currentUser != null) {
            RealmResults<UserModel> result = realm.where( UserModel.class ).equalTo( "username", currentUser).findAll();
            final List<UserModel> user = realm.copyFromRealm(result);
            if(user.isEmpty()) {
                return false;
            }

            UserModel userModel = user.get(0);

            if(userModel == null) {
                return false;
            }

            try {
                KeyFactory kf = KeyFactory.getInstance("RSA");
                try {
                    keyManager.setPrivateKey(kf.generatePrivate(new PKCS8EncodedKeySpec(userModel.getPrivateKey())));
                    keyManager.setPublicKey(kf.generatePublic(new X509EncodedKeySpec(userModel.getPublicKey())));
                    keyManager.setPublicKeyPem(userModel.getPublicKeyPem());

                    apihandler = new ApiHandler(keyManager, this);
                    apihandler.getLoggedInUser(new UserResponse() {
                       @Override
                       public void on(UserModel userModel) {
                           if(userModel != null)
                           {
                               Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                               String satoshi = Integer.toString(userModel.getSatoshi());
                               intent.putExtra("getSatoshi", satoshi);
                               startActivity(intent);
                               finish();
                           }
                       }
                    });

                    // kopieer waarden van callback UserModel naar de userModel die al bestaat
                } catch (InvalidKeySpecException e) {
                    Log.d(TAG, e.getMessage());
                }
            } catch (NoSuchAlgorithmException e) {
                Log.d(TAG, e.getMessage());
            }
            return true;
        }
        return false;
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
            keyManager = ((TheSquareApplication) this.getApplication()).keyManager;
            keyManager.generateKey();
            final UserModel user = new UserModel();
            // Set the user ID from the API response
            user.setId("");
            user.setUsername( txtUsername.getText().toString() );
            user.setPrivateKey( keyManager.getPrivateKey().getEncoded() );
            user.setPublicKey( keyManager.getPublicKey().getEncoded() );
            user.setPublicKeyPem( keyManager.getPublicKeyPem() );
            apihandler = new ApiHandler(keyManager, this);
            apihandler.register(user, new RegisterResponse(){
                @Override
                public void on(){
                    realm = Realm.getDefaultInstance();
                    realm.beginTransaction();
                    realm.insert(user);
                    realm.commitTransaction();
                    Toast.makeText(LoginActivity.this, "Gebruiker: " + user.getUsername() + " toegevoegd!", Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(i);
                    finish();

                }
            });

            addToSharedPref(user);
            return;
        }


    }

    private void addToSharedPref(UserModel user){
        if (user != null) {
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("cur_user", user.getUsername());
            editor.apply();
        }
    }

    private void showUsers(){
        realm = Realm.getDefaultInstance();
        RealmResults<UserModel> result = realm.where(UserModel.class).findAll();

        if ( result != null && result.size() > 0) {
            for (int i = 0; i < result.size(); i++) {
                Log.d( TAG, "Name" + result.get(i).getUsername() );
                Log.d( TAG, "Public key" + result.get(i).getPublicKey().toString() );
                Log.d( TAG, "Private key" + result.get(i).getPrivateKey().toString() );
            }
        }
    }

    private void removeAllUsers(){
        realm = Realm.getDefaultInstance();
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