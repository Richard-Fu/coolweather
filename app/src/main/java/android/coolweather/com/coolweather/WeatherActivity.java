package android.coolweather.com.coolweather;

import android.content.Intent;
import android.content.SharedPreferences;
import android.coolweather.com.coolweather.gson.Forecast;
import android.coolweather.com.coolweather.gson.ForecastWeather;
import android.coolweather.com.coolweather.gson.Weather;
import android.coolweather.com.coolweather.service.AutoUpdateService;
import android.coolweather.com.coolweather.util.HttpUtil;
import android.coolweather.com.coolweather.util.Utility;
import android.graphics.Color;
import android.media.Image;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import org.w3c.dom.Text;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {

    public DrawerLayout drawerLayout;

    private Button navButton;

    public SwipeRefreshLayout swipeRefreshLayout;

    private String mCityName;

    private ScrollView weatherLayout;

    private TextView titleCity;

    private TextView titleUpdateTime;

    private TextView degreeText;

    private TextView weatherInfoText;

    private LinearLayout forecastLayout;

    private TextView wind_sText;

    private TextView wind_dText;

    private TextView comfortText;

    private TextView carWashText;

    private TextView sportText;

    private ImageView bingPicImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(Build.VERSION.SDK_INT >= 21){
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.activity_weather);
        //初始化控件
        drawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        navButton = (Button)findViewById(R.id.nav_button);
        swipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.swipe_refresh);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
        weatherLayout = (ScrollView) findViewById(R.id.weather_layout);
        titleCity = (TextView)findViewById(R.id.title_city);
        titleUpdateTime = (TextView) findViewById(R.id.title_update_time);
        degreeText = (TextView) findViewById(R.id.degree_text);
        weatherInfoText = (TextView) findViewById(R.id.weather_info_text);

        //Linearlayout竟然也可以用
        forecastLayout = (LinearLayout) findViewById(R.id.forecast_layout);
        wind_sText = (TextView)findViewById(R.id.wind_speed);
        wind_dText = (TextView)findViewById(R.id.wind_direction);
        comfortText = (TextView)findViewById(R.id.comfort_text);
        carWashText = (TextView) findViewById(R.id.car_wash_text);
        sportText = (TextView) findViewById(R.id.sport_text);
        bingPicImg = (ImageView) findViewById(R.id.bing_pic_img);

        //SharedPreferences 存储机制
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = prefs.getString("weather", null);//第二个参数是默认值
        String forecastString = prefs.getString("forecast", null);
        if(weatherString != null && forecastString != null){
            //有缓存时直接解析天气数据
            Weather weather = Utility.handleWeatherResponse(weatherString);
            ForecastWeather forecastWeather = Utility.handleForecastWeatherResponse(forecastString);
            mCityName = weather.basic.cityName;
            showWeatherInfo(weather);
            showForecastInfo(forecastWeather);
        }else{
            //无缓存去服务器查询
            mCityName = getIntent().getStringExtra("cityName");//通过上一个活动传入的
            weatherLayout.setVisibility(View.INVISIBLE);//先隐藏View  等加载完了再显示
            requestWeather(mCityName);
        }
        /*//无缓存去服务器查询
        String cityName = getIntent().getStringExtra("cityName");//通过上一个活动传入的
        weatherLayout.setVisibility(View.INVISIBLE);//先隐藏View  等加载完了再显示
        requestWeather(cityName);*/

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestWeather(mCityName);//调用  向网络请求数据
            }
        });

        navButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        String bingPic = prefs.getString("bing_pic", null);
        if(bingPic != null){
           showBingPic(bingPic);
        }else{
            loadBingPic();
        }

    }

    /*
    * 根据城市请求城市天气
    * */
    public void requestWeather(final String cityName){

        final String weatherUrl = "https://free-api.heweather.com/s6/weather/now?location=" + cityName +
                "&key=2c9846e2f43c4763833efd592106c3b4";
        final String forecastUrl ="https://free-api.heweather.com/s6/weather/forecast?location=" + cityName +
                "&key=2c9846e2f43c4763833efd592106c3b4";
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this, "获取天气失败", Toast.LENGTH_SHORT).show();
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                final Weather weather = Utility.handleWeatherResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(weather != null && "ok".equals(weather.status)){
                            SharedPreferences.Editor editor = PreferenceManager.
                                    getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("weather", responseText);
                            editor.apply();
                            mCityName = weather.basic.cityName;//这一步真牛逼
                            showWeatherInfo(weather);
                        }else {
                            Toast.makeText(WeatherActivity.this, "失败"+responseText, Toast.LENGTH_SHORT).show();
                            swipeRefreshLayout.setRefreshing(false);
                        }

                    }
                });
            }
        });
        HttpUtil.sendOkHttpRequest(forecastUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this, "获取预报失败", Toast.LENGTH_SHORT).show();
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                final ForecastWeather forecast = Utility.handleForecastWeatherResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(forecast != null && "ok".equals(forecast.status)){
                            SharedPreferences.Editor editor = PreferenceManager.
                                    getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("forecast", responseText);
                            /*for(Forecast f : forecast.forecastList){
                                Log.d("说到底", "run: "+f.date);
                            }*/
                            editor.apply();
                            showForecastInfo(forecast);
                        }else {
                            Toast.makeText(WeatherActivity.this, "fore失败"+responseText, Toast.LENGTH_SHORT).show();
                        }
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
            }
        });
        loadBingPic();
    }

    /*
    * 处理并展示Weather实体类的数据
    * */
    private void showWeatherInfo(Weather weather){
        String cityName = weather.basic.cityName;
        String update_time = weather.update.update_time;
        String degree = weather.now.temperature + "°C";
        String wind_speed = weather.now.wind_speed;
        String wind_direction = weather.now.wind_direction;
        titleUpdateTime.setText(update_time);
        titleCity.setText(cityName);
        degreeText.setText(degree);
        wind_sText.setText(wind_speed);
        wind_dText.setText(wind_direction);
        weatherLayout.setVisibility(View.VISIBLE);

        //开启后台更新服务
        Intent intent = new Intent(this, AutoUpdateService.class);
        startService(intent);
    }

    /*
    * 处理预报数据
    * */
    private void showForecastInfo(ForecastWeather weather){

        forecastLayout.removeAllViews();
        weather.forecastList.remove(0);
        for(Forecast forecast : weather.forecastList){
            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item,
                    forecastLayout, false);
            TextView dateText = (TextView)view.findViewById(R.id.date_text);
            TextView dayText = (TextView)view.findViewById(R.id.day_text);
            TextView nightText = (TextView)view.findViewById(R.id.night_text);
            TextView popText = (TextView)view.findViewById(R.id.pop_text);
            TextView maxText = (TextView)view.findViewById(R.id.max_text);
            TextView minText = (TextView)view.findViewById(R.id.min_text);
           //Log.d("镖旗", "showForecastInfo: "+forecast.date);
            dateText.setText(forecast.date);
            dayText.setText(forecast.day_text);
            nightText.setText(forecast.night_text);
            popText.setText(forecast.pop+" %");
            maxText.setText(forecast.tmp_max+"°C");
            minText.setText(forecast.tmp_min+"°C");
            forecastLayout.addView(view);
        }
    }

    /*
    * 加载必应每日一图
    * */
    private void loadBingPic(){
        String requestBingPic = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String bingPic = response.body().string();
                Log.d("bing", "onResponse: "+bingPic);
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this)
                        .edit();
                editor.putString("bing_pic", bingPic);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(bingPic).into(bingPicImg);
                    }
                });
            }
        });
    }

    /*
    * 加载图 UI
    * */
    private void showBingPic(final String imgURL){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Glide.with(WeatherActivity.this).load(imgURL).into(bingPicImg);
            }
        });
    }
}
