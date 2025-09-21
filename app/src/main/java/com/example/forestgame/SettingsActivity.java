package com.example.forestgame;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {
    public static final String PREFS_NAME = "forest_prefs";
    public static final String KEY_QUALITY = "quality";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Spinner spinner = findViewById(R.id.qualitySpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.quality_options,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int currentQuality = preferences.getInt(KEY_QUALITY, 1);
        spinner.setSelection(currentQuality);

        spinner.setOnItemSelectedListener(new SimpleOnItemSelectedListener(position ->
                preferences.edit().putInt(KEY_QUALITY, position).apply()));
    }
}
