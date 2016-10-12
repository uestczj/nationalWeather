package com.example.caizejian.nationalweather.util;

import android.text.TextUtils;

import com.example.caizejian.nationalweather.model.City;
import com.example.caizejian.nationalweather.model.County;
import com.example.caizejian.nationalweather.model.NationalWeatherDB;
import com.example.caizejian.nationalweather.model.Province;

/**
 * Created by caizejian on 16/10/12.
 */
public class Utility {

    /**
     * 解析和处理服务器返回省级数据
     */
    public synchronized static boolean handleProvincesResponse(NationalWeatherDB nationalWeatherDB, String response){
        if(!TextUtils.isEmpty(response)){
            String[] allProvinces = response.split(",");
            if(allProvinces != null && allProvinces.length>0){
                for (String p : allProvinces){
                    String[] array = p.split("\\|");
                    Province province = new Province();
                    province.setProvinceCode(array[0]);
                    province.setProvinceName(array[1]);

                    nationalWeatherDB.saveProvince(province);
                }
                return true;
            }
        }
        return false;
    }

    /**
     * 解析和处理服务器返回市级数据
     */
    public static boolean handleCitiesResponse(NationalWeatherDB nationalWeatherDB,String response,int provinceId){
        if(!TextUtils.isEmpty(response)){
            String[] allCities = response.split(",");
            if(allCities != null && allCities.length > 0){
                for(String c : allCities){
                    String[] array = c.split("\\|");
                    City city = new City();
                    city.setCityCode(array[0]);
                    city.setCityName(array[1]);
                    city.setProvinceId(provinceId);

                    nationalWeatherDB.saveCity(city);
                }
                return true;
            }
        }
        return false;
    }

    /**
     * 解析和处理服务器返回县级数据
     */
    public static boolean handleCountiesResponse(NationalWeatherDB nationalWeatherDB,String response,int cityId){
        if(!TextUtils.isEmpty(response)){
            String[]allCounties = response.split(",");
            if(allCounties != null && allCounties.length > 0){
                for(String c : allCounties){
                    String[]array = c.split("//|");
                    County county = new County();
                    county.setCountyCode(array[0]);
                    county.setCountyName(array[1]);
                    county.setCityId(cityId);

                    nationalWeatherDB.saveCounty(county);
                }
                return true;
            }
        }
        return false;
    }
}
