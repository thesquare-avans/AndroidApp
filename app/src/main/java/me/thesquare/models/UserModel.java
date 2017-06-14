package me.thesquare.models;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

/**
 * Created by ruben on 14-6-2017.
 */

public class UserModel extends RealmObject {
    @PrimaryKey
    @Required
    private String username;
    private String publicKey, privateKey;
  
    public UserModel() {
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }
  
}
