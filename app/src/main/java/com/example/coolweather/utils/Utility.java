package com.example.coolweather.utils;

import android.text.TextUtils;

import com.example.coolweather.db.City;
import com.example.coolweather.db.County;
import com.example.coolweather.db.Province;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * 解析json数据
 */
public class Utility {

    public static boolean handleProvinceResponse(String response){

        if(!TextUtils.isEmpty(response)){

            try{
                JSONArray allProvinces=new JSONArray(response);
                for (int i = 0; i <allProvinces.length() ; i++) {
                    JSONObject province=allProvinces.getJSONObject(i);

                    Province province1=new Province();
                    province1.setProvinceName(province.getString("name"));
                    province1.setProvinceCode(province.getInt("id"));
                    province1.save();

                }

                return  true;

            }catch (Exception e){
                e.printStackTrace();
            }



        }

return  false;

    }



    public static boolean handleCityResponse(String response,int provinceId){

        if(!TextUtils.isEmpty(response)){

            try{
                JSONArray allProvinces=new JSONArray(response);
                for (int i = 0; i <allProvinces.length() ; i++) {
                    JSONObject province=allProvinces.getJSONObject(i);

                    City province1=new City();
                    province1.setCityName(province.getString("name"));
                    province1.setCityCode(province.getInt("id"));
                    province1.setProvinceId(provinceId);

                    province1.save();

                }

                return  true;

            }catch (Exception e){
                e.printStackTrace();
            }



        }

        return  false;

    }

    public static boolean handleCountyResponse(String response,int cityId){

        if(!TextUtils.isEmpty(response)){

            try{
                JSONArray allProvinces=new JSONArray(response);
                for (int i = 0; i <allProvinces.length() ; i++) {
                    JSONObject province=allProvinces.getJSONObject(i);

                    County province1=new County();
                    province1.setCountyName(province.getString("name"));
                    province1.setWeatherId(province.getString("weather_id"));
                    province1.setCityId(cityId);

                    province1.save();

                }

                return  true;

            }catch (Exception e){
                e.printStackTrace();
            }



        }

        return  false;

    }



}
