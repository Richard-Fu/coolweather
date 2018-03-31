package android.coolweather.com.coolweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by HP on 2018/3/29.
 */

public class Basic {

    @SerializedName("location")
    public String cityName;

    @SerializedName("cid")
    public String weatherId;


}
