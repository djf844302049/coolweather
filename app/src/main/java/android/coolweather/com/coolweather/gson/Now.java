package android.coolweather.com.coolweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * <pre>
 *      author  :yutao
 *      time    :2018/06/02
 *      desc    :
 * </pre>
 */
public class Now {
    @SerializedName( "tmp" )
    public String temperature;
    @SerializedName( "cond" )
    public More more;
    public class More{
        @SerializedName( "txt" )
        public String info;
    }
}
