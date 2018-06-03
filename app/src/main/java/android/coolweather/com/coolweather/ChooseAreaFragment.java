package android.coolweather.com.coolweather;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.coolweather.com.coolweather.db.City;
import android.coolweather.com.coolweather.db.County;
import android.coolweather.com.coolweather.db.Province;
import android.coolweather.com.coolweather.util.HttpUtil;
import android.coolweather.com.coolweather.util.Utility;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
//这个碎片就是用于选择省市县的。
public class ChooseAreaFragment extends Fragment {
    public static final int LEVEL_PROVINCE = 0 ;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTY = 2;
    private ProgressDialog progressDialog;//进度对话框
    private TextView titleText;//标题
    private Button backButton;//返回按钮
    private ListView listView;//布局里内容控件，就是显示省市县的滚动控件
    private ArrayAdapter<String> adapter;//给上面使用的适配器
    private List<String> dataList = new ArrayList<>(  );//集合，缓存所有读取到的省市县
    private List<Province> provincesList;//省列表
    private List<City> cityList;//市列表
    private List<County> countyList;//县列表
    private Province selectedProvince;//选中的省份
    private City selectedCity;//选中的城市
    private int currentLevel;//当前选中的级别
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState){
        //inflater是布局填充器的意思，下行代码就是绑定了布局为choose_area布局，
        View view = inflater.inflate( R.layout.choose_area,container,false );
        titleText = view.findViewById( R.id.title_text );//获取布局里控件的id，这个是标题栏
        backButton = view.findViewById( R.id.back_button );//返回按钮
        listView = view.findViewById( R.id.list_view );//滚动控件
        //系统的适配器
        adapter = new ArrayAdapter<>( getContext(),android.R.layout.simple_list_item_1,dataList );
        listView.setAdapter( adapter );//上行代码初始化了ArrayAdapter,并设置为listView的适配器
        return view;
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated( savedInstanceState );
        listView.setOnItemClickListener( new AdapterView.OnItemClickListener() {
            @Override//给listView设置点击事件，点击某个省就会进来
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(currentLevel == LEVEL_PROVINCE){//如果是点击了省，那就查询市级
                    selectedProvince = provincesList.get( position );
                    queryCities();
                }else if(currentLevel == LEVEL_CITY){//如果点击了市，那就查询县级
                    selectedCity = cityList.get( position );
                    queryCounties();
                }else if(currentLevel == LEVEL_COUNTY){//如果点击了县，则直接跳转到天气信息界面
                    String weatherId = countyList.get(position).getWeatherId();//获取当前城市天气id
                    Intent intent = new Intent( getActivity(),WeatherActivity.class );
                    intent.putExtra( "weather_id",weatherId );//传递了城市对应的天气id
                    startActivity( intent );
                    getActivity().finish();
                }
            }
        } );
        backButton.setOnClickListener( new View.OnClickListener(){
            @Override//给button设置点击事件
            public void onClick(View v){
                if(currentLevel == LEVEL_COUNTY){//如果当前是县级，点击后，就查询到市级
                    queryCities();
                }else if(currentLevel == LEVEL_CITY){//如果当前是市级，点击后，就查询到省级
                    queryProvinces();
                }
            }
        } );
        queryProvinces();//从这里开始加载省级数据
    }
    //查询全国所有的省，优先从数据库查询，没查到去服务器上查
    private void queryProvinces(){
        titleText.setText( "中国" );//先把标题设置成中国
        backButton.setVisibility( View.GONE );//将返回按钮隐藏起来，因为省级列表已经不能再返回了
        provincesList = DataSupport.findAll( Province.class );//读取省级数据
        if(provincesList.size()>0){//如果读取到
            dataList.clear();
            for(Province province : provincesList){
                dataList.add( province.getProvinceName() );//遍历所有数据存储到dataList
            }
            //通过一个外部的方法控制如果适配器的内容改变时需要强制调用getView来刷新每个Item的内容。
            adapter.notifyDataSetChanged();
            listView.setSelection( 0 );//将第position个item显示在listView的最上面一项,这是第0个
            currentLevel = LEVEL_PROVINCE;
        }else {//如果不能读取到则去服务中查询
            String address = "http://guolin.tech/api/china";//这个是要查询的URL
            queryFromServer(address,"province");//这个就是从服务器中查询的自定义方法
        }
    }
    //查询选中省内所有的市，优先从数据库查询，如果没有再去服务器上查询
    private void queryCities() {
        //设置标题为：选中的省份调用实体类getProvinceName省份的名称
        titleText.setText( selectedProvince.getProvinceName() );
        backButton.setVisibility( View.VISIBLE );//将返回按钮显示
        cityList = DataSupport.where( "provinceid = ?",String.valueOf(
                selectedProvince.getId() ) ).find( City.class );//读取市级数据
        if(cityList.size()>0){//如果读取到则
            dataList.clear();//清空缓存后
            for(City city:cityList){//遍历将获取到的城市加进集合里
                dataList.add( city.getCityName() );
            }
            adapter.notifyDataSetChanged();//刷新
            listView.setSelection( 0 );//如上
            currentLevel = LEVEL_CITY;//设置级别为市级，
        }else {//如果没有读取到
            int provinceCode = selectedProvince.getProvinceCode();
            //接口组装出一个请求地址
            String address = "http://guolin.tech/api/china/"+provinceCode;
            queryFromServer(address,"city");//调用这个方法来从服务器上查询数据
        }
    }
    //查询选中市内所有的县，优先从数据库查询，如果没查到再去服务器上查询
    private void queryCounties(){
        titleText.setText( selectedCity.getCityName() );
        backButton.setVisibility( View.VISIBLE );
        countyList = DataSupport.where( "cityid=?",String.valueOf(
                selectedCity.getId() ) ).find( County.class );
        if(countyList.size()>0){
            dataList.clear();
            for (County county: countyList){
                dataList.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_COUNTY;//设置级别为县
        }else {
            int provinceCode = selectedProvince.getProvinceCode();
            int cityCode = selectedCity.getCityCode();
            String address = "http://guolin.tech/api/china/"+provinceCode+"/"+cityCode;
            queryFromServer(address,"county");
        }
    }
    //根据传入的地址和类型从服务器上查询省市县数据，传入的参数是URL和省市县的字符串
    private void queryFromServer(String address,final String type){
        showProgressDialog();//自定义方法：显示进度对话框
        HttpUtil.sendOkHttpRequest( address, new Callback() {//向服务器发送请求，传入俩参数
            @Override//响应的数据回调到这个方法里
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();//将数据以字符串形式存储
                boolean result = false;
                if("province".equals( type )){//如果传入的字符串是省则
                    //下面方法来解析和处理服务器返回的数据，并存储到数据中，是Util包的自定义方法
                    result = Utility.handleProvinceResponse( responseText );
                }else if ("city".equals( type )){
                    result = Utility.handleCityResponse( responseText,selectedProvince.getId() );
                }else if("county".equals( type )){
                    result = Utility.handleCountyResponse( responseText,selectedCity.getId() );
                }
                if(result){//根据结果，因为涉及到UI操作，所以借助了runOnUiThread方法来切换到主线程
                    getActivity().runOnUiThread( new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();//自定义方法：关闭进度框
                            if("province".equals( type )){
                                queryProvinces();//因为数据库已经存在数据，所以会直接将数据显示在界面
                            }else if("city".equals( type )){
                                queryCities();
                            }else if("county".equals( type )){
                                queryCounties();
                            }
                        }
                    } );
                }
            }
            @Override
            public void onFailure(Call call,IOException e){//如果有异常，则
                //通过runOnUiThread()方法回到主线程处理逻辑
                getActivity().runOnUiThread( new Runnable() {
                    @RequiresApi(api = Build.VERSION_CODES.M)//忽略这句
                    @Override
                    public void run() {
                        closeProgressDialog();//自定义方法：关闭进度对话框
                        Toast.makeText( getContext(),"加载失败",Toast.LENGTH_SHORT ).show();
                    }
                } );
            }
        } );
    }
    //显示进度对话框
    private void showProgressDialog(){
        if(progressDialog == null ){
            progressDialog = new ProgressDialog( getActivity() );//创建对话框对象
            progressDialog.setMessage( "正在加载，请稍后..." );//设置对话框内容
            //dialog弹出后，如果点击屏幕dialog不消失；点击物理返回键dialog消失
            progressDialog.setCanceledOnTouchOutside( false );
        }
        progressDialog.show();//显示对话框
    }
    private void closeProgressDialog(){//关闭进度对话框
        if(progressDialog!=null){
            progressDialog.dismiss();//让对话框消失
        }
    }
}
