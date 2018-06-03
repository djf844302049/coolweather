package android.coolweather.com.coolweather.gson;

public class AQI {
    public AQICity city;
    public class AQICity{
        public String aqi;//对应JSON数据里的aqi
        public String pm25;//对应JSON数据里的pm25
    }
}
