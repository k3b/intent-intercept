package de.k3b.intentintercept;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import de.k3b.intentintercept.R;

public class SettingsActivity extends AppCompatActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

        getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, new SettingsFragment()).commit();
    }
}
