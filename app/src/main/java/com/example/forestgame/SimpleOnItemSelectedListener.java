package com.example.forestgame;

import android.view.View;
import android.widget.AdapterView;

public class SimpleOnItemSelectedListener implements AdapterView.OnItemSelectedListener {
    public interface Callback {
        void onSelected(int position);
    }

    private final Callback callback;

    public SimpleOnItemSelectedListener(Callback callback) {
        this.callback = callback;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (callback != null) {
            callback.onSelected(position);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // No-op
    }
}
