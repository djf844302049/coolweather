package android.coolweather.com.coolweather.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.coolweather.com.coolweather.gson.Weather;
import android.coolweather.com.coolweather.util.HttpUtil;
import android.coolweather.com.coolweather.util.Utility;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
//自动更新天气的服务：将更新的数据都放进缓存中，每次打开天气界面都会先访问缓存
public class AutoUpdateService extends Service {
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public int onStartCommand(Intent intent,int flags,int startId){
        updateWeather();//调用自定义方法：更新天气信息
        updateBingPic();//调用自定义方法：更新必应图片
        AlarmManager manager = (AlarmManager) getSystemService( ALARM_SERVICE );//创建定时对象
        int anHour = 8*60*60*1000;//八小时的毫秒数，间隔时间
        long triggerAtTime = SystemClock.elapsedRealtime()+anHour;//触发间隔时间
        Intent i = new Intent( this,AutoUpdateService.class );//获取一个Intent配合下面
        PendingIntent pi = PendingIntent.getService( this,0,i,0 );//获取服务
        manager.cancel( pi );//创建定时服务
        //设置定时服务的种类：参一，如果设备休眠则唤醒。参二，间隔时间，参三服务
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,triggerAtTime,pi);
        return super.onStartCommand( intent,flags,startId );//返回方法三个参数
    }

    private void updateWeather() {//更新天气信息的方法逻辑
        //获取一个缓存对象，等于建立缓存，并在缓存里获取键为weather的数据
        SharedPreferences prefe = PreferenceManager.getDefaultSharedPreferences( this );
        String weatherString = prefe.getString( "weather",null );//
        if(weatherString!=null){//如果有缓存数据，则直接解析天气数据
            Weather weather = Utility.handleWeatherRespoonse( weatherString );
            String weatherId = weather.basic.weatherId;
            String weatherUrl = "http://guolin.tech/api/weather?cityid="+weatherId+
                    "7b920e27da6f466594a5b9d23dd184b8";
            HttpUtil.sendOkHttpRequest( weatherUrl, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseText = response.body().string();
                    Weather weather = Utility.handleWeatherRespoonse( responseText );
                    if(weather!=null&&"ok".equals( weather.status )){
                        SharedPreferences.Editor editor = PreferenceManager.
                                getDefaultSharedPreferences( AutoUpdateService.this ).edit();
                        editor.putString( "weather",responseText );
                        editor.apply();
                    }
                }
            } );
        }
    }

    private void updateBingPic() {
        String requestBingPic = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest( requestBingPic, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String bingPic = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences
                        ( AutoUpdateService.this ).edit();
                editor.putString( "bing_pic",bingPic );
                editor.apply();
            }
        } );
    }

}
