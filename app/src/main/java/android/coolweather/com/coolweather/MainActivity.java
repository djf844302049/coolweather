package android.coolweather.com.coolweather;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );
        //在SharedPreferences文件中读取缓存数据，如果不为null就说明之前已经请求过天气数据了
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences( this );
        if(prefs.getString( "weather",null )!=null){
            //既然已经请求过数据，就没必要再次请求，直接跳转到天气信息界面即可
            Intent intent = new Intent( this,WeatherActivity.class );
            startActivity( intent );
            finish();
        }
    }
}
