package com.example.forestcoins.ui;

import android.content.SharedPreferences;
import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.example.forestcoins.game.ForestCoinGame;
import com.example.forestcoins.game.QualityLevel;

public class GameActivity extends AndroidApplication {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
        config.useImmersiveMode = true;
        config.useAccelerometer = false;
        config.useCompass = false;
        config.numSamples = 4;
        SharedPreferences prefs = getSharedPreferences(MainActivity.PREFS_NAME, MODE_PRIVATE);
        String quality = prefs.getString(MainActivity.PREF_QUALITY, "medium");
        QualityLevel level = QualityLevel.fromPreference(quality);
        initialize(new ForestCoinGame(level), config);
    }
}
