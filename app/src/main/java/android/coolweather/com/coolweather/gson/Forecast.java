package android.coolweather.com.coolweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * <pre>
 *      author  :yutao
 *      time    :2018/06/02
 *      desc    :
 * </pre>
 */
public class Forecast {
    public String date;
    @SerializedName( "tmp" )
    public Temperature temperature;
    @SerializedName( "cond" )
    public More more;
    public class Temperature{
        public String max;
        public String min;
    }
    public class More{
        @SerializedName( "txt_d" )
        public String info;
    }
}
