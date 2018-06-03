package android.coolweather.com.coolweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * <pre>
 *      author  :yutao
 *      time    :2018/06/02
 *      desc    :
 * </pre>
 */
public class Suggestion {
    @SerializedName( "comf" )
    public Comfort comfort;
    @SerializedName( "cw" )
    public CarWash carWash;
    public Sport sport;
    public class Comfort{
        @SerializedName( "txt" )
        public String info;
    }
    public class CarWash{
        @SerializedName( "txt" )
        public String info;
    }
    public class Sport{
        @SerializedName( "txt" )
        public String info;
    }
}
