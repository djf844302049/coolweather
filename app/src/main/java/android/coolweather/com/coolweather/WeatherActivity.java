package android.coolweather.com.coolweather;

import android.content.Intent;
import android.content.SharedPreferences;
import android.coolweather.com.coolweather.gson.Forecast;
import android.coolweather.com.coolweather.gson.Weather;
import android.coolweather.com.coolweather.service.AutoUpdateService;
import android.coolweather.com.coolweather.util.HttpUtil;
import android.coolweather.com.coolweather.util.Utility;
import android.graphics.Color;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
//显示天气信息的逻辑
public class WeatherActivity extends AppCompatActivity {
    public DrawerLayout drawerLayout;//3滑动菜单布局
    private Button navButton;//3切换城市按钮
    public SwipeRefreshLayout swipeRefresh;//2声明下拉刷新布局
    private ScrollView weatherLayout;
    private TextView titleCity;
    private TextView titleUpdateTime;
    private TextView degreeText;
    private TextView weatherInfoText;
    private LinearLayout forecastLayout;
    private TextView aqiText;
    private TextView pm25Text;
    private TextView comfortText;
    private TextView carWashText;
    private TextView sportText;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        if(Build.VERSION.SDK_INT>=21){//只有当api21以上，也就是安卓5.0以上才会执行
            View decorView = getWindow().getDecorView();//拿到当前活动的DecorView
            //改变系统UI的显示，这里的大写字母表示活动布局会显示在状态栏上
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN |
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE );
            getWindow().setStatusBarColor( Color.TRANSPARENT );//最后将状态栏设置为透明色
        }
        setContentView( R.layout.activity_weather );
        //初始化各控件
        weatherLayout = findViewById( R.id.weather_layout );
        titleCity = findViewById( R.id.title_city );
        titleUpdateTime = findViewById( R.id.title_update_time );
        degreeText = findViewById( R.id.degree_text );
        weatherInfoText = findViewById( R.id.weather_info_text );
        forecastLayout = findViewById( R.id.forecast_layout );
        aqiText = findViewById( R.id.aqi_text );
        pm25Text = findViewById( R.id.pm25_text );
        comfortText = findViewById( R.id.comfort_text );
        carWashText = findViewById( R.id.car_wash_text );
        sportText = findViewById( R.id.sport_text );
        imageView = findViewById( R.id.bing_pic_img );
        swipeRefresh = findViewById( R.id.swipe_refresh );//2获取下拉刷新
        swipeRefresh.setColorSchemeResources( R.color.colorPrimary );//2下拉刷新进度条设置颜色
        drawerLayout = findViewById( R.id.drawer_layout );//3滑动菜单布局
        navButton = findViewById( R.id.nav_button );//3切换城市按钮
        navButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer( GravityCompat.START );//3打开滑动菜单
            }
        } );
        //获取存储对象，类似于缓存器，获取方式默认。
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences( this );
        //-这边插入一个背景图片，也可插到后面.搜索缓存中的bing_pic
        String bing = prefs.getString( "bing_pic",null );
        if(bing!=null){//-如果缓存中存在这种图片，那就直接调用Clide.with将图片设置到into的控件里
            Glide.with( this ).load( bing ).into( imageView );
        }else{//如果没有就调用自定义方法去服务器获取图片
            loadBingPic();
        }
        String weatherString = prefs.getString( "weather",null );//从缓存中获取数据
        final String weatherId;//2声明地方的天气id
        if (weatherString!=null){//如果有缓存直接分析天气数据
            Weather weather = Utility.handleWeatherRespoonse( weatherString );//解析数据
            weatherId = weather.basic.weatherId;//2记录城市天气id
            showWeatherInfo(weather);//自定义方法：处理并展示数据
        }else{//没有缓存时就去服务器查询天气
            //从Intent中获取城市天气id
            weatherId = getIntent().getStringExtra( "weather_id" );
            weatherLayout.setVisibility( View.INVISIBLE );//将ScrollView隐藏，否则空数据界面很奇怪
            requestWeather(weatherId);//调用自定义方法向服务器请求天气数据
        }
        swipeRefresh.setOnRefreshListener( new SwipeRefreshLayout.OnRefreshListener() {
            @Override//2下拉刷新监听器
            public void onRefresh() {//当触发下拉时
                requestWeather( weatherId );//调用下面自定义的获取天气的方法
            }
        } );
    }
    public void requestWeather(final String weatherId){//根据城市id请求城市天气信息
        String weatherUrl = "http://guolin.tech/api/weather?cityid="+weatherId+
                "&key=7b920e27da6f466594a5b9d23dd184b8";//创建一个获取信息的URL
        //util包中的自定义类和方法：用来向上面的地址发出请求，参一是URL地址，参二是响应操作
        HttpUtil.sendOkHttpRequest( weatherUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {//数据响应失败则
                e.printStackTrace();
                runOnUiThread( new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText( WeatherActivity.this,"获取天气失败",
                                Toast.LENGTH_SHORT ).show();
                        swipeRefresh.setRefreshing( false );//2表示下拉刷新事件结束，隐藏进度条
                    }
                } );
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {//响应成功
                final String responseText = response.body().string();//获取服务器返回的数据
                //下面时自定义类和方法，用作解析返回的数据，转成Weather对象。
                final Weather weather = Utility.handleWeatherRespoonse( responseText );
                runOnUiThread( new Runnable() {//将当前切换到主线程
                    @Override
                    public void run() {//判断，如果服务器返回的status是ok，就说明请求天气成功
                        if (weather!=null&&"ok".equals( weather.status )){
                            //下面方式是将返回的数据缓存到SharedPreferences
                            SharedPreferences.Editor editor = PreferenceManager.
                                    getDefaultSharedPreferences( WeatherActivity.this )
                                    .edit();//
                            editor.putString( "wearher",responseText );
                            editor.apply();
                            showWeatherInfo(weather);//调用自定义方法处理并展示数据
                            Intent intent = new Intent( WeatherActivity.this,
                                    AutoUpdateService.class );//4获取到天气就开启定时服务
                            startService( intent );//4开启服务
                        }else {
                            Toast.makeText( WeatherActivity.this,"获取天气信息失败",
                                    Toast.LENGTH_SHORT ).show();
                        }
                        swipeRefresh.setRefreshing( false );//2表示下拉刷新事件结束，隐藏进度条
                    }
                } );
            }
        } );
        loadBingPic();//每次获取天气的信息的时候都加载必应每日一图
    }
    //处理并展示Weather实体中的数据,参数就是获取到的天气数据
    private void showWeatherInfo(Weather weather){
        //从weather中获取想要的数据并存储
        String cityName = weather.basic.cityName;
        String updateTime = weather.basic.update.updateTime.split( "" )[1];
        String degress = weather.now.temperature +"°C";
        String weatherInfo = weather.now.more.info;
        //将获取到的数据显示到相应的控件上。
        titleCity.setText( cityName );
        titleUpdateTime.setText( updateTime );
        degreeText.setText( degress );
        weatherInfoText.setText( weatherInfo );
        forecastLayout.removeAllViews();//把布局中的控件全部移除，得到一个空的布局forecastLayout
        //未来几天天气预报中，用for循环来处理每天的天气信息
        for(Forecast forecast:weather.forecastList){//从服务器中获取到的也是数组，遍历它
            //动态加载forecast_item.xml布局并设置相应的数据，然后添加到父布局当中
            //创建一个布局填充器：参一：子布局，参二：父布局，参三默认
            View view = LayoutInflater.from( this ).inflate( R.layout.forecast_item,forecastLayout,
                    false );
            //获取子布局的控件
            TextView dateText = view.findViewById( R.id.date_text );
            TextView infoText = view.findViewById( R.id.info_text );
            TextView maxText = view.findViewById( R.id.max_text );
            TextView minText = view.findViewById( R.id.min_text );
            //设置子布局TextView的显示数据
            dateText.setText( forecast.date );
            infoText.setText( forecast.more.info );
            maxText.setText( forecast.temperature.max );
            minText.setText( forecast.temperature.min );
            forecastLayout.addView( view );//将布局填充器返回的view添加到父布局中
        }
        if (weather.aqi!=null){
            aqiText.setText( weather.aqi.city.aqi );//设置控件显示数据
            pm25Text.setText( weather.aqi.city.pm25 );
        }
        //从weather中获取想要的数据并设置到控件上
        String comfort = "舒适度"+weather.suggestion.comfort.info;
        String carWash = "洗车指数"+weather.suggestion.carWash.info;
        String sport = "运动建议"+weather.suggestion.sport.info;
        comfortText.setText( comfort );
        carWashText.setText( carWash );
        sportText.setText( sport );
        weatherLayout.setVisibility( View.VISIBLE );//最后要将ScrollView重新设为可见
    }
    private void loadBingPic() {//去服务器获取图片的方法
        final String requestBingPic = "http://guolin.tech/api/bing_pic";//获取必应一图的地址
        HttpUtil.sendOkHttpRequest( requestBingPic, new Callback() {//响应上面的地址
            @Override
            public void onFailure(Call call, IOException e) {//失败就打印错误
                e.printStackTrace();
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String bing = response.body().string();//获取到必应背景图连接
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences
                        ( WeatherActivity.this ).edit();//缓存器
                editor.putString( "bing_pic",bing );//缓存到上面
                editor.apply();
                runOnUiThread( new Runnable() {
                    @Override
                    public void run() {
                        //切换到主线程用Glide加载图片
                        Glide.with( WeatherActivity.this ).load( bing ).into( imageView );
                    }
                } );
            }
        } );
    }
}
