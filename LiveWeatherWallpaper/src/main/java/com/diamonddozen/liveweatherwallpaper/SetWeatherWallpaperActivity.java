package com.diamonddozen.liveweatherwallpaper;

import android.app.Activity;
import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

/**
 * Created by Eric on 6/15/13.
 */
public class SetWeatherWallpaperActivity extends Activity implements SharedPreferences.OnSharedPreferenceChangeListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onClick(View view) {
        Intent intent = new Intent();
        if(Build.VERSION.SDK_INT >= 16)
        {
            intent.setAction(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);
            intent.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, new ComponentName(this, WeatherWallpaperService.class));
        }
        else
        {
            intent.setAction(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER);
        }
        startActivity(intent);
    }

    public void changePrefs(View view)
    {
        Intent intent = new Intent(this, WeatherPreferencesActivity.class);
        startActivity(intent);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("locationRefreshTime")) {
            Toast.makeText(this.getApplicationContext(), "pref changed", Toast.LENGTH_SHORT).show();
        }
    }
}