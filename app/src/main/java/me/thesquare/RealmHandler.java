package me.thesquare;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;
import me.thesquare.models.UserModel;

/**
 * Created by ruben on 19-6-2017.
 */

public class RealmHandler {
    private Realm realm;

    public RealmHandler(){
        realm = Realm.getDefaultInstance();
    }

    public UserModel getUser(String username){
        RealmResults<UserModel> result = realm.where( UserModel.class ).equalTo( "username", username).findAll();
        List<UserModel> user = realm.copyFromRealm(result);
        UserModel userModel = user.get(0);
        return userModel;
    }
}
