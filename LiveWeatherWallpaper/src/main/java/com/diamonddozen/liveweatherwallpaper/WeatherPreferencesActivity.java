package com.diamonddozen.liveweatherwallpaper;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.widget.Toast;

/**
 * Created by Eric on 6/15/13.
 */
public class WeatherPreferencesActivity extends PreferenceActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.prefs);

        // We want to add a validator to the number of circles so that it only
        // accepts numbers
        Preference circlePreference = getPreferenceScreen().findPreference(
                "numberOfCircles");

        Preference locationRefreshTime = getPreferenceScreen().findPreference(
                "locationRefreshTime");

        // Add the validator
        circlePreference.setOnPreferenceChangeListener(numberCheckListener);
        locationRefreshTime.setOnPreferenceChangeListener(locationRefreshTimeListener);
    }

    /**
     * Checks that a preference is a valid numerical value
     */
    Preference.OnPreferenceChangeListener numberCheckListener = new Preference.OnPreferenceChangeListener() {

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            // Check that the string is an integer
            if (newValue != null && newValue.toString().length() > 0
                    && newValue.toString().matches("\\d*")) {
                return true;
            }
            // If now create a message to the user
            Toast.makeText(WeatherPreferencesActivity.this, "Invalid Input",
                    Toast.LENGTH_SHORT).show();
            return false;
        }
    };

    Preference.OnPreferenceChangeListener locationRefreshTimeListener = new Preference.OnPreferenceChangeListener() {

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            // Check that the string is an integer
            if (newValue != null && newValue.toString().length() > 0
                    && newValue.toString().matches("\\d*")) {
                //TODO need to updater location listener pull time
                return true;
            }
            // If now create a message to the user
            Toast.makeText(WeatherPreferencesActivity.this, "Invalid Input",
                    Toast.LENGTH_SHORT).show();
            return false;
        }
    };
}