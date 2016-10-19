package com.example.caizejian.nationalweather.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.caizejian.nationalweather.R;
import com.example.caizejian.nationalweather.model.City;
import com.example.caizejian.nationalweather.model.County;
import com.example.caizejian.nationalweather.model.NationalWeatherDB;
import com.example.caizejian.nationalweather.model.Province;
import com.example.caizejian.nationalweather.util.HttpCallbackListener;
import com.example.caizejian.nationalweather.util.HttpUtil;
import com.example.caizejian.nationalweather.util.Utility;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by caizejian on 16/10/12.
 */
public class ChooseAreaActivity extends Activity {

    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTY = 2;

    private ProgressDialog progressDialog;
    private TextView titleText;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private NationalWeatherDB nationalWeatherDB;
    private List<String> datalist = new ArrayList<String>();

    private AutoCompleteTextView autoCompleteTextView;

    /**
     *  省列表
     */
    private List<Province> provinceList;

    /**
     *  市列表
     */
    private List<City> cityList;

    /**
     *  县列表
     */
    private List<County> countyList;

    /**
     * 被选中省份
     */
    private Province selectedProvince;

    /**
     * 被选中城市
     */
    private City selectedCity;

    /**
     * 当前被选中级别
     */
    private int currentLevel;

    private boolean isFromWeatherActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        isFromWeatherActivity = getIntent().getBooleanExtra("from_weather_activity",false);


        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (prefs.getBoolean("city_selected",false)&& !isFromWeatherActivity){
            Intent intent = new Intent(this,WeatherActivity.class);
            startActivity(intent);
            finish();
            return;
        }
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.choose_area);
        listView = (ListView)findViewById(R.id.list_view);
        titleText = (TextView)findViewById(R.id.title_text);
        adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,datalist);
        listView.setAdapter(adapter);

      //  autoCompleteTextView = (AutoCompleteTextView)findViewById(R.id.auto_text);
       // autoCompleteTextView.setAdapter(adapter);

        nationalWeatherDB = NationalWeatherDB.getInstance(this);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                            @Override
                                            public void onItemClick(AdapterView<?> arg0, View view, int index, long arg3) {
                                                if (currentLevel == LEVEL_PROVINCE) {
                                                    selectedProvince = provinceList.get(index);
                                                    queryCities();
                                                } else if (currentLevel == LEVEL_CITY) {
                                                    selectedCity = cityList.get(index);
                                                    queryCounties();
                                                }else if (currentLevel == LEVEL_COUNTY){
                                                    String countyCode = countyList.get(index).getCountyCode();
                                                    Intent intent = new Intent(ChooseAreaActivity.this,WeatherActivity.class);
                                                    intent.putExtra("county_code",countyCode);
                                                    startActivity(intent);
                                                    finish();
                                                }
                                            }
                                        });
        queryProvinces();
    }

    private void queryProvinces(){
        provinceList = nationalWeatherDB.loadProvinces();
        if(provinceList.size()>0){
            datalist.clear();
            for(Province province : provinceList){
                datalist.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            titleText.setText("China");
            currentLevel = LEVEL_PROVINCE;
        }else {
            queryFromServer(null,"province");
        }
    }

    private void queryCities(){
        cityList = nationalWeatherDB.loadCities(selectedProvince.getId());
        if (cityList.size() > 0){
            datalist.clear();
            for(City city : cityList){
                datalist.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            titleText.setText(selectedProvince.getProvinceName());
            currentLevel = LEVEL_CITY;
        }else {
            queryFromServer(selectedProvince.getProvinceCode(),"city");
        }
    }

    private void queryCounties(){
       // Toast.makeText(ChooseAreaActivity.this,selectedCity.getId(),Toast.LENGTH_LONG).show();
       // finish();
        //int a = selectedCity.getId();
       // Log.d("ChooseAreaActivity",a);

        countyList = nationalWeatherDB.loadCounties(selectedCity.getId());
        if(countyList.size()>0){
            datalist.clear();
            for(County county : countyList){
                datalist.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            titleText.setText(selectedCity.getCityName());
            currentLevel = LEVEL_COUNTY;
        }else {
            queryFromServer(selectedCity.getCityCode(),"county");
        }
    }
    private void queryFromServer(final String code , final String type){
        String address;
        if (!TextUtils.isEmpty(code)){
            address = "http://www.weather.com.cn/data/list3/city"+code+".xml";
        }else{
            address = "http://weather.com.cn/data/list3/city.xml";
        }
        showProgressDialog();
        HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                boolean result = false;
                if("province".equals(type)){
                    result = Utility.handleProvincesResponse(nationalWeatherDB,response);
                }else if("city".equals(type)){
                    result = Utility.handleCitiesResponse(nationalWeatherDB,response,selectedProvince.getId());
                }else if("county".equals(type)){
                    result = Utility.handleCountiesResponse(nationalWeatherDB,response,selectedCity.getId());
                }
                if(result){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if("province".equals(type)){
                                queryProvinces();
                            }else if ("city".equals(type)){
                                queryCities();
                            }else if ("county".equals(type)){
                                queryCounties();
                            }
                        }
                    });
                }

            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(ChooseAreaActivity.this,"加载失败",Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    private void showProgressDialog(){
        if(progressDialog == null){
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("正在加载...");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

    private void closeProgressDialog(){
        if(progressDialog != null){
            progressDialog.dismiss();
        }
    }

    @Override
    public void onBackPressed(){
        if(currentLevel == LEVEL_COUNTY){
            queryCities();
        }else if(currentLevel == LEVEL_CITY){
            queryProvinces();
        }else {
            if(isFromWeatherActivity){
                Intent intent = new Intent(this,WeatherActivity.class);
                startActivity(intent);
            }
            finish();
        }
    }
}
