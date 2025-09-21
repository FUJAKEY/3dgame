package com.example.forestcoins.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.forestcoins.R;

public class SettingsActivity extends AppCompatActivity {

    private RadioGroup qualityGroup;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        qualityGroup = findViewById(R.id.qualityGroup);
        findViewById(R.id.backButton).setOnClickListener(view -> finish());
        initState();
        qualityGroup.setOnCheckedChangeListener((group, checkedId) -> saveQuality());
    }

    private void initState() {
        SharedPreferences prefs = getSharedPreferences(MainActivity.PREFS_NAME, MODE_PRIVATE);
        String quality = prefs.getString(MainActivity.PREF_QUALITY, "medium");
        switch (quality) {
            case "low":
                ((RadioButton) findViewById(R.id.qualityLow)).setChecked(true);
                break;
            case "high":
                ((RadioButton) findViewById(R.id.qualityHigh)).setChecked(true);
                break;
            default:
                ((RadioButton) findViewById(R.id.qualityMedium)).setChecked(true);
                break;
        }
    }

    private void saveQuality() {
        SharedPreferences prefs = getSharedPreferences(MainActivity.PREFS_NAME, MODE_PRIVATE);
        String value = "medium";
        int checkedId = qualityGroup.getCheckedRadioButtonId();
        if (checkedId == R.id.qualityLow) {
            value = "low";
        } else if (checkedId == R.id.qualityHigh) {
            value = "high";
        }
        prefs.edit().putString(MainActivity.PREF_QUALITY, value).apply();
    }
}
