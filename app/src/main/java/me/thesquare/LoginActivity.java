package me.thesquare;

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
import me.thesquare.ApiResponses.RegisterResponse;
import me.thesquare.models.UserModel;

public class LoginActivity extends AppCompatActivity {
    private Realm realm;
    private Button btnLogin;
    private EditText txtUsername;
    private KeyManager keyManager;
    private SharedPreferences sharedPref;
    private PermissionHandler permissionHandler;
    private String current_user;
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
        current_user = sharedPref.getString("cur_user", null);
        if(userExists(current_user)) {
            return;
        }

        txtUsername = (EditText) findViewById(R.id.editText);
        btnLogin = (Button) findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if ( checkUsername() ){
                    addUser( txtUsername.getText().toString() );

                }
                else {
                    Toast toast =  Toast.makeText( getApplicationContext(), "Please fill in the login field.", Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        });
    }

    private boolean userExists(String current_user){

        if (current_user != null) {
            RealmResults<UserModel> result = realm.where( UserModel.class ).equalTo( "username", current_user).findAll();
            List<UserModel> user = realm.copyFromRealm(result);
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

                    // TODO: kijk of user ook op server bestaat (/v1/me)
                    // kopieer waarden van callback UserModel naar de userModel die al bestaat
                } catch (InvalidKeySpecException e) {
                    e.printStackTrace();
                }

                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
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
                    Realm realm = Realm.getDefaultInstance();
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
                Log.d( "Name", result.get(i).getUsername() );
                Log.d( "Public key", result.get(i).getPublicKey().toString() );
                Log.d( "Private key", result.get(i).getPrivateKey().toString() );
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
