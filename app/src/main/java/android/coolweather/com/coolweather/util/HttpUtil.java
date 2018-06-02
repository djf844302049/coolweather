package android.coolweather.com.coolweather.util;

import okhttp3.OkHttpClient;
import okhttp3.Request;

//全国所有省市县数据都是从服务器端获取的，因此这里和服务器交互需要这个类
public class HttpUtil {//sendOkHttpRequest是和服务器进行交互的方法，发起一条Http请求
    //只需要调用它，传入请求地址，并注册一个回调来处理服务器响应就可以了。
    public static void sendOkHttpRequest(String address,okhttp3.Callback callback){
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(address).build();
        client.newCall( request ).enqueue( callback );
    }
}
