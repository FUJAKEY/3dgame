package com.example.forestcoins.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.forestcoins.R;

public class MainActivity extends AppCompatActivity {

    public static final String PREFS_NAME = "forest_settings";
    public static final String PREF_QUALITY = "quality";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.playButton).setOnClickListener(this::onPlayClicked);
        findViewById(R.id.settingsButton).setOnClickListener(this::onSettingsClicked);
        ensureDefaultSettings();
    }

    private void ensureDefaultSettings() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        if (!prefs.contains(PREF_QUALITY)) {
            prefs.edit().putString(PREF_QUALITY, "medium").apply();
        }
    }

    private void onPlayClicked(View view) {
        startActivity(new Intent(this, GameActivity.class));
    }

    private void onSettingsClicked(View view) {
        startActivity(new Intent(this, SettingsActivity.class));
    }
}
