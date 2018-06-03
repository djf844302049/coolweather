package android.coolweather.com.coolweather.gson;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * <pre>
 *      author  :yutao
 *      time    :2018/06/02
 *      desc    :
 * </pre>
 */
public class Weather {//总实例类用来引用刚刚创建的各个实体类
    public String status;//成功是OK失败是具体原因，这个字段也是会有的
    public Basic basic;
    public AQI aqi;
    public Now now;
    public Suggestion suggestion;
    @SerializedName( "daily_forecast" )
    public List<Forecast> forecastList;//因为daily_forecast是数组
}
