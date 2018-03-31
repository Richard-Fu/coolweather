package android.coolweather.com.coolweather.gson;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by HP on 2018/3/30.
 */

public class ForecastWeather {

    @SerializedName("daily_forecast")
    public List<Forecast> forecastList;

    public String status;
}
