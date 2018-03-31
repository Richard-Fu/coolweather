package android.coolweather.com.coolweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by HP on 2018/3/29.
 */

public class Now {

    @SerializedName("tmp")
    public String temperature;

    @SerializedName("fl")
    public String h_temperature;

    @SerializedName("hum")
    public String  humidity;

    @SerializedName("pcpn")
    public String  precipitation;

    @SerializedName("vis")
    public String visibility;

    @SerializedName("wind_dir")
    public String wind_direction;

    @SerializedName("wind_spd")
    public String wind_speed;

}
