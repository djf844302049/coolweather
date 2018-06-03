package android.coolweather.com.coolweather.gson;

import com.google.gson.annotations.SerializedName;

public class Basic {
    @SerializedName( "city")
    public String cityName;//用做JSON数据的city
    @SerializedName( "id" )
    public String weatherId;//用作json数据的天气id
    public Update update;
    public class Update{
        @SerializedName( "loc" )
        public String updateTime;//用作update中的时间
    }
}
