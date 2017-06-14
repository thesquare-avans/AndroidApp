package me.thesquare;

import android.content.Context;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        realm = Realm.getDefaultInstance();
        sharedPref = getSharedPreferences("userInfo", Context.MODE_PRIVATE);

        removeAllUsers();

        txtUsername = (EditText) findViewById(R.id.editText);
        btnLogin = (Button) findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if ( checkUsername() ){
                    addUser( txtUsername.getText().toString() );
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        showUsers();
    }

    private boolean checkUsername(){
        return !Objects.equals( txtUsername.getText().toString(), "" );
    }

    private void addUser(String username){
        RealmResults<UserModel> result = realm.where( UserModel.class ).equalTo( "username", txtUsername.getText().toString() ).findAll();
        if ( result.isEmpty() && !username.equals("") ) {
            keyManager = new KeyManager();
            keyManager.generateKey();
            UserModel user = new UserModel();
            user.setUsername( txtUsername.getText().toString() );
            user.setPrivateKey( keyManager.getPrivateKey() );
            user.setPublicKey( keyManager.getPublicKey() );

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
        RealmResults<UserModel> result = realm.where(UserModel.class).findAll();

        if ( result != null && result.size() > 0) {
            for (int i = 0; i < result.size(); i++) {
                Log.d("Player name", result.get(i).getUsername());
                Log.d("Public key", result.get(i).getPublicKey().toString());
                Log.d("Private key", result.get(i).getPrivateKey().toString());
            }
        }
    }

    private void removeAllUsers(){
        RealmResults<UserModel> result = realm.where(UserModel.class).findAll();

        result.deleteAllFromRealm();
    }
}
