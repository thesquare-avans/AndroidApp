package me.thesquare;

import android.app.Application;

import me.thesquare.managers.KeyManager;

/**
 * Created by larsh on 20-6-2017.
 */

public class TheSquareApplication extends Application {
    public KeyManager keyManager;

    TheSquareApplication() {
        this.keyManager = new KeyManager();
    }
}
