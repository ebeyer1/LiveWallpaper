package com.diamonddozen.liveweatherwallpaper;

import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.service.wallpaper.WallpaperService;
import android.view.SurfaceHolder;
import android.widget.Toast;

import com.codefreak.weatherbugapi.LiveWeather;
import com.codefreak.weatherbugapi.WeatherBugAPI;

import java.util.List;

/**
 * Created by Eric on 6/15/13.
 */
public class WeatherWallpaperService extends WallpaperService implements LocationListener, SharedPreferences.OnSharedPreferenceChangeListener {
    @Override
    public Engine onCreateEngine() {
        // hacking in the location listener set up first
        setupLocationUpdater();
        return new WeatherWallpaperEngine();
    }

    int iteration = 0;
    WeatherWallpaperEngine current;

    LocationManager locationManager;
    private void setupLocationUpdater()
    {
        locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);

        /*setMockLocation(15.387653, 73.872585, 500);*/

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(WeatherWallpaperService.this);
        int timeToWait = Integer.valueOf(prefs.getString("locationRefreshTime", "5000")); // 5 second default

        // GPS setup
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_COARSE);
        List<String> providers = locationManager.getProviders(criteria, true);
        if (providers == null || providers.size() == 0)
        {
            Toast.makeText(this.getApplicationContext(), "couldn't open gps service", Toast.LENGTH_SHORT).show();
            return;
        }
        String preferred = providers.get(0);
        locationManager.requestLocationUpdates(preferred, timeToWait, 10, this);
    }

    private void setMockLocation(double latitude, double longitude, float accuracy)
    {
        locationManager.addTestProvider(LocationManager.GPS_PROVIDER,
                "requiresNetwork" == "",
                "requiresSatellite" == "",
                "requiresCell" == "",
                "hasMonetaryCost" == "",
                "supportsAltitude" == "",
                "supportsSpeed" == "",
                "supportsBearing" == "",
                Criteria.POWER_LOW,
                Criteria.ACCURACY_COARSE);

        Location location = new Location(LocationManager.GPS_PROVIDER);

        location.setLatitude(latitude);
        location.setLongitude(longitude);
        location.setAccuracy(accuracy);

        locationManager.setTestProviderEnabled(LocationManager.GPS_PROVIDER, true);
        locationManager.setTestProviderStatus(LocationManager.GPS_PROVIDER, LocationProvider.AVAILABLE, null, System.currentTimeMillis());
        locationManager.setTestProviderLocation(LocationManager.GPS_PROVIDER, location);
    }

    @Override
    public void onLocationChanged(Location location) {
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        Toast.makeText(this.getApplicationContext(), "location changed. lat "+ latitude + ", lng = " + longitude, Toast.LENGTH_SHORT).show();
        new PrivateRetrieveWeatherByCoordsTask().execute(latitude, longitude);
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("locationRefreshTime")) {
            Toast.makeText(this.getApplicationContext(), "pref changed", Toast.LENGTH_SHORT).show();
        }
    }

    private class PrivateRetrieveWeatherByCoordsTask extends AsyncTask<Double, Void, LiveWeather>
    {
        @Override
        protected LiveWeather doInBackground(Double... doubles) {
            try
            {
                WeatherBugAPI weatherBugAPI = new WeatherBugAPI();
                return weatherBugAPI.getLiveWeatherByLatAndLong(doubles[0], doubles[1]);
            }
            catch (Exception e)
            {
                return null;
            }
        }

        @Override
        protected void onPostExecute(LiveWeather weather)
        {
            super.onPostExecute(weather);
            if (weather == null)
            {
                Toast.makeText(getApplicationContext(),"weather request failed.",Toast.LENGTH_LONG).show();
            }
            else
            {
                String weatherText = GetWeatherText(weather);
                System.out.println(weatherText);
                Toast.makeText(getApplicationContext(), weatherText, Toast.LENGTH_SHORT).show();
                if (iteration % 2 == 0)
                {
                    current.draw(Color.RED);
                }
                else {
                    current.draw(Color.BLUE);
                }
                iteration++;
            }
        }

        private String GetWeatherText(LiveWeather weather)
        {
            return String.format("Location: %s, %s\nCondition: %s", weather.getStationCityState(), weather.getStationCountry(), weather.getCurrentConditions());
        }
    }

    private class WeatherWallpaperEngine extends Engine {
        private final Handler handler = new Handler();
        private final Runnable drawRunner = new Runnable() {
            @Override
            public void run() {
                draw(Color.BLACK);
            }

        };

        public WeatherWallpaperEngine() {
            handler.post(drawRunner);
            current = this;
        }

        private void draw(int color) {
            SurfaceHolder holder = getSurfaceHolder();
            Canvas canvas = null;
            try {
                canvas = holder.lockCanvas();
                canvas.drawColor(color);
            } finally {
                if (canvas != null)
                    holder.unlockCanvasAndPost(canvas);
            }
            handler.removeCallbacks(drawRunner);
        }
    }

}
