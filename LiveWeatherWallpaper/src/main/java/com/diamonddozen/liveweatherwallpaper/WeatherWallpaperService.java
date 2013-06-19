package com.diamonddozen.liveweatherwallpaper;

import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
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
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.widget.Toast;

import com.codefreak.weatherbugapi.LiveWeather;
import com.codefreak.weatherbugapi.WeatherBugAPI;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Eric on 6/15/13.
 */
public class WeatherWallpaperService extends WallpaperService implements LocationListener {
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
                    current.drawBackground(Color.RED);
                }
                else {
                    current.drawBackground(Color.BLUE);
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
                drawBackground(Color.BLACK);
            }

        };
        private List<MyPoint> circles;
        private Paint paint = new Paint();
        private int width;
        int height;
        private boolean visible = true;
        private int maxNumber = 5;
        private boolean touchEnabled = false;

        public WeatherWallpaperEngine() {
            circles = new ArrayList<MyPoint>();
            paint.setAntiAlias(true);
            paint.setColor(Color.WHITE);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeJoin(Paint.Join.ROUND);
            paint.setStrokeWidth(15f);
            handler.post(drawRunner);
            current = this;
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            this.visible = visible;
            if (visible) {
                handler.post(drawRunner);
            } else {
                handler.removeCallbacks(drawRunner);
            }
        }

        @Override
        public void onSurfaceDestroyed(SurfaceHolder holder) {
            super.onSurfaceDestroyed(holder);
            this.visible = false;
            handler.removeCallbacks(drawRunner);
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format,
                                     int width, int height) {
            this.width = width;
            this.height = height;
            super.onSurfaceChanged(holder, format, width, height);
        }

        @Override
        public void onTouchEvent(MotionEvent event) {
            if (touchEnabled) {
                float x = event.getX();
                float y = event.getY();
                SurfaceHolder holder = getSurfaceHolder();
                Canvas canvas = null;
                try {
                    canvas = holder.lockCanvas();
                    if (canvas != null) {
                        canvas.drawColor(Color.BLACK);
                        circles.clear();
                        circles.add(new MyPoint(
                                String.valueOf(circles.size() + 1), x, y));
                        drawCircles(canvas, circles);

                    }
                } finally {
                    if (canvas != null)
                        holder.unlockCanvasAndPost(canvas);
                }
                super.onTouchEvent(event);
            }
        }

        private void draw() {
            SurfaceHolder holder = getSurfaceHolder();
            Canvas canvas = null;
            try {
                canvas = holder.lockCanvas();
                if (canvas != null) {
                    if (circles.size() >= maxNumber) {
                        circles.clear();
                    }
                    int x = (int) (width * Math.random());
                    int y = (int) (height * Math.random());
                    circles.add(new MyPoint(String.valueOf(circles.size() + 1),
                            x, y));
                    drawCircles(canvas, circles);
                }
            } finally {
                if (canvas != null)
                    holder.unlockCanvasAndPost(canvas);
            }
            handler.removeCallbacks(drawRunner);
            if (visible) {
                handler.postDelayed(drawRunner, 5000);
            }
        }

        public void drawBackground(int color)
        {
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
            if (visible) {
                handler.postDelayed(drawRunner, 5000);
            }
        }

        // Surface view requires that all elements are drawn completely
        private void drawCircles(Canvas canvas, List<MyPoint> circles) {
            canvas.drawColor(Color.BLACK);
            for (MyPoint point : circles) {
                canvas.drawCircle(point.x, point.y, 20.0f, paint);
            }
        }
    }

}
