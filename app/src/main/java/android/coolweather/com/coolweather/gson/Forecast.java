package android.coolweather.com.coolweather.gson;

import com.google.gson.annotations.SerializedName;

import javax.xml.transform.Templates;

/**
 * Created by HP on 2018/3/29.
 */

public class Forecast {

    public String date;

    @SerializedName("cond_txt_d")
    public String day_text;

    @SerializedName("cond_txt_n")
    public String night_text;

    public String pop;

    public String tmp_max;

    public String tmp_min;

}
