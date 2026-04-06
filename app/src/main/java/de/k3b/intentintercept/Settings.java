package de.k3b.intentintercept;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

public class Settings {
    private final Context context;
    private final SharedPreferences prefs;

    public Settings(Context context) {
        this.context = context;
        prefs = PreferenceManager
                .getDefaultSharedPreferences(context);
    }

    public boolean reportWithMatchingActivities() {
        return prefs.getBoolean(context.getString(R.string.pref_report_matching_activities_enabled), true);
    }
}
