package com.coolweather.app.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.coolweather.app.model.City;
import com.coolweather.app.db.CoolWeatherDB;
import com.coolweather.app.model.County;
import com.coolweather.app.model.Province;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by lpfox on 2016.12.05.
 */
public class Utility {

    /**
     * 解析和处理服务器返回的省级数据
     * @param coolWeatherDB
     * @param response
     * @return
     */
    public synchronized static boolean handleProvincesResponse(CoolWeatherDB coolWeatherDB,
                                                               String response){
        if (!TextUtils.isEmpty(response)){
            String[] allProvinces = response.split(",");
            if (allProvinces != null && allProvinces.length > 0){
                for (String p : allProvinces){
                    String array[] = p.split("\\|");
                    Province province = new Province();
                    province.setProvinceCode(array[0]);
                    province.setProvinceName(array[1]);
                    //将解析出来的数据存储到province表
                    coolWeatherDB.saveProvince(province);
                }
                return true;
            }
        }
        return false;
    }

    /**
     * 解析和处理服务器返回的城市数据
     * @param coolWeatherDB
     * @param response
     * @param provinceId
     * @return
     */
    public static boolean handleCitiesResponse(CoolWeatherDB coolWeatherDB, String response, int provinceId){
        if (!TextUtils.isEmpty(response)){
            String[] allCities = response.split(",");
            if (allCities != null && allCities.length > 0){
                for (String p :allCities){
                    String array[] = p.split("\\|");
                    City city = new City();
                    city.setCityCode(array[0]);
                    city.setCityName(array[1]);
                    city.setProvinceId(provinceId);
                    //将解析出来的数据存储到city表
                    coolWeatherDB.saveCity(city);
                }
                return true;
            }

        }
        return false;
    }

    /**
     * 解析和处理服务器返回的县级数据
     * @param coolWeatherDB
     * @param response
     * @param cityId
     * @return
     */
    public static boolean handleCountiesResponse(CoolWeatherDB coolWeatherDB, String response, int cityId){
        if (!TextUtils.isEmpty(response)){
            String[] allCounties = response.split(",");
            if (allCounties != null && allCounties.length > 0){
                for (String p : allCounties){
                    String[] array = p.split("\\|");
                    County county = new County();
                    county.setCountyCode(array[0]);
                    county.setCountyName(array[1]);
                    county.setCityId(cityId);
                    //将解析出的数据保存到county表
                    coolWeatherDB.saveCounty(county);
                }
                return true;
            }
        }
        return false;
    }

    /**
     * 解析服务器返回的JSON数据，并将解析后的数据存储在本地
     * @param context
     * @param response
     */
    public static void handleWeatherResponse(Context context,String response){
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONObject data = jsonObject.getJSONObject("data");
            JSONArray forecast = data.getJSONArray("forecast");
            JSONObject yesterday = data.getJSONObject("yesterday");

            String cityName = data.getString("city");
            JSONObject[] array = new JSONObject[6];
            array[0] = data.getJSONObject("yesterday");
            String[] temp1 = new String[6];
            String[] temp2 = new String[6];
            String[] weatherDesp=new String[6];
            String[] publishTime=new String[6];
            temp1[0]=array[0].getString("high");
            //Log.d("Utility","temp1[0]:"+temp1[0].toString());
            temp2[0]=array[0].getString("low");
            //Log.d("Utility","temp2[0]:"+temp2[0].toString());
            weatherDesp[0]=array[0].getString("type");
            //Log.d("Utility","weatherDesp[0]:"+weatherDesp[0].toString());
            publishTime[0]=array[0].getString("date");
            //Log.d("Utility","publishTime[0]:"+publishTime[0].toString());
            for (int i=1;i<6;i++){
                array[i]=forecast.getJSONObject(i-1);
                temp1[i]=array[i].getString("high");
                //Log.d("Utility","temp1["+i+"]:"+temp1[i].toString());
                temp2[i]=array[i].getString("low");
                //Log.d("Utility","temp2["+i+"]:"+temp2[i].toString());
                weatherDesp[i]=array[i].getString("type");
                //Log.d("Utility","weatherDesp["+i+"]:"+weatherDesp[i].toString());
                publishTime[i]=array[i].getString("date");
                //Log.d("Utility","publishTime["+i+"]:"+publishTime[i].toString());
            }

            saveWeatherInfo(context,cityName,temp1,temp2,weatherDesp,publishTime);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 将服务器返回的所有天气信息存储到sharedPreferences文件中
     * @param context
     * @param cityName
     * @param temp1
     * @param temp2
     * @param weatherDesp
     * @param publishTime
     */
    public static void saveWeatherInfo(Context context,String cityName,
                                       String[] temp1,String[] temp2,String[] weatherDesp,
                                       String[] publishTime){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年M月d日", Locale.CHINA);
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putBoolean("city_selected",true);
        editor.putString("city_name",cityName);
        for (int i = 0; i < 6; i++){
            editor.putString("temp1["+i+"]",temp1[i]);
            editor.putString("temp2["+i+"]",temp2[i]);
            editor.putString("weather_desp["+i+"]",weatherDesp[i]);
            editor.putString("publish_time["+i+"]",publishTime[i]);
        }
        editor.putString("current_date",sdf.format(new Date()));
        editor.commit();
    }
}
