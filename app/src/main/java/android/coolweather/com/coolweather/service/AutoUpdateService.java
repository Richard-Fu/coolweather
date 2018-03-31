package android.coolweather.com.coolweather.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.coolweather.com.coolweather.WeatherActivity;
import android.coolweather.com.coolweather.gson.ForecastWeather;
import android.coolweather.com.coolweather.gson.Weather;
import android.coolweather.com.coolweather.util.HttpUtil;
import android.coolweather.com.coolweather.util.Utility;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;

import com.bumptech.glide.Glide;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class AutoUpdateService extends Service {
    public AutoUpdateService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        updateWeather();
        updateBingPic();
        AlarmManager manager = (AlarmManager)getSystemService(ALARM_SERVICE);
        int anHour = 8 * 60 * 60 * 1000;//8个小时
        long triggerAtTime = SystemClock.elapsedRealtime() + anHour;
        Intent i = new Intent(this, AutoUpdateService.class);
        PendingIntent pi = PendingIntent.getService(this, 0, i, 0);
        manager.cancel(pi);//把之前那个任务去掉
        manager.set(AlarmManager.ELAPSED_REALTIME, triggerAtTime, pi);
        return super.onStartCommand(intent, flags, startId);
    }

    /*
    * 更新天气
    * */
    private void updateWeather(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        final String weatherString = prefs.getString("weather", null);
        if(weatherString != null){
            //有缓存的时候  才更新  没有就不用执行了
            Weather weather = Utility.handleWeatherResponse(weatherString);
            //目的是获得  城市名
            String cityName = weather.basic.cityName;

            final String weatherUrl = "https://free-api.heweather.com/s6/weather/now?location=" + cityName +
                    "&key=2c9846e2f43c4763833efd592106c3b4";
            final String forecastUrl ="https://free-api.heweather.com/s6/weather/forecast?location=" + cityName +
                    "&key=2c9846e2f43c4763833efd592106c3b4";

            HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    //更新后的数据存到Shared就好
                    String responseText = response.body().string();
                    Weather weather = Utility.handleWeatherResponse(responseText);
                    if(weather != null && "ok".equals(weather.status)){
                        SharedPreferences.Editor editor = PreferenceManager.
                                getDefaultSharedPreferences(AutoUpdateService.this).edit();
                        editor.putString("weather", responseText);
                        editor.apply();
                    }
                }
            });

            HttpUtil.sendOkHttpRequest(forecastUrl, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    //更新后的数据存到Shared就好
                    String responseText = response.body().string();
                    ForecastWeather forecastweather = Utility.handleForecastWeatherResponse(responseText);
                    if(forecastweather != null && "ok".equals(forecastweather.status)){
                        SharedPreferences.Editor editor = PreferenceManager.
                                getDefaultSharedPreferences(AutoUpdateService.this).edit();
                        editor.putString("forecast", responseText);
                        editor.apply();
                    }
                }
            });
        }
    }

    /*
    * 更新每日一图
    * */
    private void updateBingPic(){
        String requestBingPic = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String bingPic = response.body().string();
                //Log.d("bing", "onResponse: "+bingPic);
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this)
                        .edit();
                editor.putString("bing_pic", bingPic);
                editor.apply();
            }
        });
    }
}
