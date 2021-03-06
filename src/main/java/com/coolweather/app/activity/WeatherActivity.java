package com.coolweather.app.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.text.Layout;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.coolweather.app.R;
import com.coolweather.app.service.AutoUpdateService;
import com.coolweather.app.util.HttpCallbackListener;
import com.coolweather.app.util.HttpUtil;
import com.coolweather.app.util.Utility;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Created by lpfox on 2016.12.07.
 */
public class WeatherActivity extends Activity implements View.OnClickListener,NavigationView.OnNavigationItemSelectedListener{

    private LinearLayout weatherInfoLayout;
    /**
     * 用于显示城市名
     */
    private TextView cityNameText;
    /**
     * 用于显示发布时间
     */
    private TextView[] publishText = new TextView[7];
    /**
     * 用于显示天气描述信息
     */
    private ImageView[] weatherDespText = new ImageView[6];
    private TextView weatherDesp;
    /**
     * 用于显示气温1
     */
    private TextView[] temp1Text = new TextView[7];
    /**
     * 用于显示气温2
     */
    private TextView[] temp2Text = new TextView[7];
    /**
     * 用于显示当前日期
     */
    private TextView currentDateText;
    /**
     * 菜单按钮
     */
    private Toolbar menuButton;
    /**
     * 菜单布局
     */
    private DrawerLayout itemLayout;
    //左拉控件
    private NavigationView navigationView;
    /**
     * 切换城市
     */
    //private Button switchCity;
    /**
     * 更新天气按钮
     */
    private Button refreshWeather;

    private static final String TAG = "WeatherActivity";
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            /*case R.id.switch_city:
                Intent intent = new Intent(this, ChooseAreaActivity.class);
                intent.putExtra("from_weather_activity",true);
                startActivity(intent);
                finish();
                break;*/
            case R.id.refresh_weather:
                publishText[6].setText("同步中...");
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                String weatherCode = prefs.getString("city_name","");
                if (!TextUtils.isEmpty(weatherCode)){
                    queryWeatherInfo(weatherCode,"city");
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.item_layout);

        //初始化个控件
        weatherInfoLayout = (LinearLayout)findViewById(R.id.weather_info_layout);
        cityNameText = (TextView)findViewById(R.id.city_name);
        publishText[3] = (TextView)findViewById(R.id.tommorowC1);
        publishText[4] = (TextView)findViewById(R.id.tommorowD1);
        publishText[5] = (TextView)findViewById(R.id.tommorowE1);
        publishText[6] = (TextView)findViewById(R.id.publish_text);

        weatherDesp = (TextView)findViewById(R.id.weather_desp);
        weatherDespText[0] = (ImageView)findViewById(R.id.yesterday2);
        weatherDespText[1] = (ImageView)findViewById(R.id.today2);
        weatherDespText[2] = (ImageView)findViewById(R.id.tommorowB2);
        weatherDespText[3] = (ImageView)findViewById(R.id.tommorowC2);
        weatherDespText[4] = (ImageView)findViewById(R.id.tommorowD2);
        weatherDespText[5] = (ImageView)findViewById(R.id.tommorowE2);


        temp1Text[0] = (TextView)findViewById(R.id.yesterday3);
        temp1Text[1] = (TextView)findViewById(R.id.today3);
        temp1Text[2] = (TextView)findViewById(R.id.tommorowB3);
        temp1Text[3] = (TextView)findViewById(R.id.tommorowC3);
        temp1Text[4] = (TextView)findViewById(R.id.tommorowD3);
        temp1Text[5] = (TextView)findViewById(R.id.tommorowE3);
        temp1Text[6] = (TextView)findViewById(R.id.temp1);

        temp2Text[0] = (TextView)findViewById(R.id.yesterday4);
        temp2Text[1] = (TextView)findViewById(R.id.today4);
        temp2Text[2] = (TextView)findViewById(R.id.tommorowB4);
        temp2Text[3] = (TextView)findViewById(R.id.tommorowC4);
        temp2Text[4] = (TextView)findViewById(R.id.tommorowD4);
        temp2Text[5] = (TextView)findViewById(R.id.tommorowE4);
        temp2Text[6] = (TextView)findViewById(R.id.temp2);

        currentDateText = (TextView)findViewById(R.id.current_date);
        //左拉菜单
        itemLayout = (DrawerLayout)findViewById(R.id.item_layout);
        navigationView = (NavigationView)findViewById(R.id.nav_view);
        menuButton = (Toolbar)findViewById(R.id.menu_button);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this,itemLayout, menuButton, R.string.navigation_drawer_open,R.string.navigation_drawer_close);
        itemLayout.setDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        String countyCode = getIntent().getStringExtra("county_code");
        if(!TextUtils.isEmpty(countyCode)){
            //有县级代号时查询天气
            publishText[6].setText("同步中...");
            //weatherInfoLayout.setVisibility(View.INVISIBLE);
            //cityNameText.setVisibility(View.INVISIBLE);
            queryWeatherCode(countyCode);
        }else{
            //没有县级代号显示本地天气
            showWeather();
        }
        //switchCity.setOnClickListener(this);
        //refreshWeather.setOnClickListener(this);
        refreshWeather = (Button)findViewById(R.id.refresh_weather);
        refreshWeather.setOnClickListener(this);
    }

    /**
     * 查询县级代号所对应的天气代号
     * @param countyCode
     */
    private void queryWeatherCode(String countyCode){
        String address = "http://www.weather.com.cn/data/list3/city"+countyCode+".xml";
        queryFromServer(address, "countyCode");
    }

    /**
     * 查询县级代号对应的天气
     * @param
     */
    private void queryWeatherInfo(String weatherCode,String type){
        if ("city".equals(type)){
            try {
                weatherCode = URLEncoder.encode(weatherCode,"UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        String address = "http://wthrcdn.etouch.cn/weather_mini?"+type+"="+weatherCode;
        Log.d(TAG,"address");
        queryFromServer(address, "weatherCode");
    }

    /**
     * 根据传入的地址和类型，从服务器查询天气代号或天气
     * @param address
     * @param type
     */
    private void queryFromServer(final String address, final String type){
        HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                if ("countyCode".equals(type)){
                    if (!TextUtils.isEmpty(response)){
                        //从服务器返回的数据中解析出天气代号
                        String[] array = response.split("\\|");
                        if (array != null && array.length == 2){
                            String weatherCode = array[1];
                            Log.d(TAG,weatherCode.toString());
                            queryWeatherInfo(weatherCode,"citykey");
                        }
                    }
                }else if ("weatherCode".equals(type)){
                    //处理服务器返回的天气信息
                    Utility.handleWeatherResponse(WeatherActivity.this, response);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showWeather();
                        }
                    });
                }
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        publishText[6].setText("同步失败");
                    }
                });
            }
        });
    }

    /**
     * 从SharedPreferences文件中读取存储的天气信息，并显示在界面上
     */
    private void showWeather(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        cityNameText.setText(prefs.getString("city_name",""));
        /*temp1Text.setText(prefs.getString("temp1",""));
        temp2Text.setText(prefs.getString("temp2",""));
        weatherDespText.setText(prefs.getString("weather_desp",""));
        publishText.setText("今天"+prefs.getString("publish_time","")+"发布");
        currentDateText.setText(prefs.getString("current_date",""));
        weatherInfoLayout.setVisibility(View.INVISIBLE);
        cityNameText.setVisibility(View.VISIBLE);*/
        for (int i = 0; i < 6; i++){
            //日期
            if (i > 2){
                String t = prefs.getString("publish_time["+i+"]","");
                String[] tstrings=t.split("期");
                t=tstrings[1];
                publishText[i].setText("周"+t);
            }
        /*天气*/
            String w=prefs.getString("weather_desp["+i+"]","");
            switch (w){
                case "晴":weatherDespText[i].setImageResource(R.mipmap.qing);break;
                case "阴":weatherDespText[i].setImageResource(R.mipmap.yin);break;
                case "多云":
                    weatherDespText[i].setImageResource(R.mipmap.duoyun);
                    break;
                case "小雨":
                    weatherDespText[i].setImageResource(R.mipmap.xiaoyu);
                    break;
                case "中雨":
                    weatherDespText[i].setImageResource(R.mipmap.yu);
                    break;
                case "大雨":
                    weatherDespText[i].setImageResource(R.mipmap.yu);
                    break;
                case "阵雨":
                    weatherDespText[i].setImageResource(R.mipmap.xiaoyu);
                    break;
                case "雷阵雨":
                    weatherDespText[i].setImageResource(R.mipmap.leizhenyu);
                    break;
                case "小雪":
                    weatherDespText[i].setImageResource(R.mipmap.xiaoxue);
                    break;
                case "中雪":
                    weatherDespText[i].setImageResource(R.mipmap.xiaoxue);
                    break;
                case "大雪":
                    weatherDespText[i].setImageResource(R.mipmap.daxue);
                    break;
                default:
                    weatherDespText[i].setImageResource(R.mipmap.qita);
                    break;
            }
            /*温度*/
            String s=prefs.getString("temp1["+i+"]","");
            String[] strings=s.split(" ");
            s=strings[1];
            s=s.replace('℃','°');
            Log.d("WeatherActivity","temp1Text["+i+"]:"+s);
            temp1Text[i].setText(s);
            s=prefs.getString("temp2["+i+"]","");
            strings=s.split(" ");
            s=strings[1];
            s=s.replace('℃','°');
            Log.d("WeatherActivity","temp2Text["+i+"]:"+s);
            temp2Text[i].setText(s);
            Log.d("WeatherActivity",i+"");
        }
        temp1Text[6].setText(prefs.getString("temp1[1]",""));
        temp2Text[6].setText(prefs.getString("temp2[1]",""));
        weatherDesp.setText(prefs.getString("weather_desp[1]",""));
        publishText[6].setText(prefs.getString("publish_time[1]","")+"发布");
        currentDateText.setText(prefs.getString("current_date",""));
        //weatherInfoLayout.setVisibility(View.VISIBLE);
        //cityNameText.setVisibility(View.VISIBLE);
        Intent intent=new Intent(this, AutoUpdateService.class);
        startService(intent);


    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch(id){
            case R.id.select_city:
                Intent intent=new Intent(this,ChooseAreaActivity.class);
                intent.putExtra("from_weather_activity",true);
                startActivity(intent);
                finish();
                break;
            case R.id.share:
                Toast.makeText(WeatherActivity.this, "正在开发中...", Toast.LENGTH_SHORT).show();
                break;
        }
        return false;
    }
}
