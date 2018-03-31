package android.coolweather.com.coolweather.util;

import android.coolweather.com.coolweather.db.City;
import android.coolweather.com.coolweather.db.County;
import android.coolweather.com.coolweather.db.Province;
import android.coolweather.com.coolweather.gson.ForecastWeather;
import android.coolweather.com.coolweather.gson.Weather;
import android.text.TextUtils;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by HP on 2018/3/28.
 */

public class Utility {
    /*
    * 解析和处理服务器返回的省级数据
    * */

    public static boolean handleProvinceResponse(String response){
        if(!TextUtils.isEmpty(response)){
            //检查工具类 检查两种非空
            try{
                JSONArray allProvinces = new JSONArray(response);
                for(int i=0; i<allProvinces.length(); i++){
                    JSONObject provinceObject = allProvinces.getJSONObject(i);
                    Province province = new Province();
                    province.setProvinceName(provinceObject.getString("name"));
                    province.setProvinceCode(provinceObject.getInt("id"));//就是Json里的数据头名
                    province.save();
                }
                return true;
            }catch(JSONException e){
                e.printStackTrace();
            }
        }
        return false;
    }

    /*
    * 解析和处理服务器的市级数据
    * */

    public static boolean handleCityResponse(String response, int provinceId){
        if(!TextUtils.isEmpty(response)){
            try{
                JSONArray allCitys = new JSONArray(response);
                for(int i=0; i<allCitys.length(); i++){
                    JSONObject cityObject = allCitys.getJSONObject(i);
                    City city = new City();
                    city.setCityName(cityObject.getString("name"));
                    city.setCityCode(cityObject.getInt("id"));
                    city.setProvinceId(provinceId);//跟代码逻辑没多少关系吧， 主要是数据完整性把
                    city.save();
                }
                return true;
            }catch(JSONException e){
                e.printStackTrace();
            }
        }
        return false;
    }

    /*
    * 县级
    * */

    public static boolean handleCountyResponse(String response, int CityId){
        if(!TextUtils.isEmpty(response)){
            try{
                JSONArray allCountys = new JSONArray(response);
                for(int i=0; i<allCountys.length(); i++){
                    JSONObject countyObject = allCountys.getJSONObject(i);
                    County county = new County();
                    county.setCountyName(countyObject.getString("name"));
                    county.setWeatherId(countyObject.getString("weather_id"));
                    county.setCityId(CityId);
                    county.save();
                }
                return true;
            }catch(JSONException e){
                e.printStackTrace();
            }
        }
        return false;
    }

    /*
    * 将返回的JSON数据解析成Weather实体类
    * */
    public static Weather handleWeatherResponse(String response){
        try{
            JSONObject jsonObject = new JSONObject(response);
            JSONArray jsonArray = jsonObject.getJSONArray("HeWeather6");
            String weatherContent = jsonArray.getJSONObject(0).toString();
            return new Gson().fromJson(weatherContent, Weather.class);
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    /*
    * 将返回的JSON数据解析成ForecastWeather类
    * */
    public static ForecastWeather handleForecastWeatherResponse(String response){
        try{
            JSONObject jsonObject = new JSONObject(response);
            JSONArray jsonArray = jsonObject.getJSONArray("HeWeather6");
            String weatherContent = jsonArray.getJSONObject(0).toString();
            return new Gson().fromJson(weatherContent, ForecastWeather.class);
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
