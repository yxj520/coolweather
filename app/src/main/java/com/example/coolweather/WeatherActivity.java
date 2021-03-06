package com.example.coolweather;

import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.coolweather.gson.Forecast;
import com.example.coolweather.gson.Weather;
import com.example.coolweather.utils.HttpUtil;
import com.example.coolweather.utils.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {



    public SwipeRefreshLayout swipeRefresh;

    private ImageView bingPicImg;
    private ScrollView weatherLayout;
    private TextView titleCity;
    private TextView titleUpdateTime;
    private  TextView degreeText;
    private  TextView weatherInfoText;
    private LinearLayout forecatLayout;
    private TextView aqiText;
    private  TextView pm25Text;
    private  TextView comfortText;
    private  TextView carWashText;
    private  TextView sportText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        //android version >=5
        if(Build.VERSION.SDK_INT >=21){
            View decorView =getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            );
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }



        setContentView(R.layout.activity_weather);

        weatherLayout=findViewById(R.id.weather_layout);
        titleCity=findViewById(R.id.title_city);
        titleUpdateTime=findViewById(R.id.title_update_time);
        degreeText=findViewById(R.id.degree_text);
        weatherInfoText=findViewById(R.id.weather_info_text);
        aqiText=findViewById(R.id.aqi_text);
        pm25Text=findViewById(R.id.pm25_text);
        comfortText=findViewById(R.id.comfort_text);
        carWashText=findViewById(R.id.car_wash_text);
        forecatLayout=findViewById(R.id.forecast_layout);
        sportText=findViewById(R.id.sport_text);
        bingPicImg=findViewById(R.id.bing_pic_img);

        //swipeRefresh=(SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        //swipeRefresh.setColorSchemeResources(R.color.colorPrimary);

        SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString=prefs.getString("weather",null);
        final String weatherId;

        if(weatherString !=null){

            //get data from cache
            Weather weather= Utility.handleWeatherResponse(weatherString);
            weatherId=weather.basic.weatherId;

            showWeatherInfo(weather);

        }else{
             weatherId=getIntent().getStringExtra("weather_id");
            weatherLayout.setVisibility(View.INVISIBLE);
            requestWeather(weatherId);
        }

//        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
//            @Override
//            public void onRefresh() {
//                requestWeather(weatherId);
//            }
//        });

        String bingPic=prefs.getString("bing_pic",null);
        if(bingPic !=null){
            Glide.with(this).load(bingPic).into(bingPicImg);
        }else{
            loadBingPic();
        }




    }

    private void requestWeather(String weatherId) {

        String weatherUrl="http://guolin.tech/api/weather?cityid="+weatherId+
                "&key=bc0418b57b2d4918819d3974ac1285d9";
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i("weatherActi6666666", "onFailure: --------------------------------------");
                e.printStackTrace();
                Log.i("weatherActi", "onFailure: 获取天气失败:"+e);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this,"获取天气失败",Toast.LENGTH_SHORT).show();
                     //swipeRefresh.setRefreshing(false);

                    }
                });


            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
          final String responseText=response.body().string();
                Log.i("响应", "onResponse: "+responseText);

          final Weather weather=Utility.handleWeatherResponse(responseText);

           runOnUiThread(new Runnable() {
               @Override
               public void run() {
                   if(weather!=null && "ok".equals(weather.status)){
                       SharedPreferences.Editor editor=PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this)
                               .edit();
                       editor.putString("weather",responseText);
                       editor.apply();
                       showWeatherInfo(weather);


                   }else {
                       Toast.makeText(WeatherActivity.this,"获取天气失败",Toast.LENGTH_SHORT).show();
                   }
                 //swipeRefresh.setRefreshing(false);


               }
           });



            }
        });

        loadBingPic();

    }

    /**
     * load picture
     */
    private void loadBingPic() {

             String requestBingPic="http://guolin.tech/api/bing_pic";
             HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
                 @Override
                 public void onFailure(Call call, IOException e) {

                     Log.e("picture", "onFailure: 加载图片失败"+e );
                 }

                 @Override
                 public void onResponse(Call call, Response response) throws IOException {

                      final String bingPic=response.body().string();
                      SharedPreferences.Editor editor=PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                      editor.putString("bing_pic",bingPic);
                      editor.apply();
                      runOnUiThread(new Runnable() {
                          @Override
                          public void run() {
                              Glide.with(WeatherActivity.this).load(bingPic).into(bingPicImg);
                          }
                      });


                 }
             });


    }

    private  void showWeatherInfo(Weather weather){

        String cityName=weather.basic.cityName;
        String updateTime=weather.basic.update.updateTime.split(" ")[1];
         String degree =weather.now.more.info;
         String weatherInfo=weather.now.more.info;
         titleCity.setText(cityName);
         titleUpdateTime.setText(updateTime);
         degreeText.setText(degree);
         weatherInfoText.setText(weatherInfo);
         forecatLayout.removeAllViews();
         for(Forecast forecast:weather.forecastList){

             View view= LayoutInflater.from(this).inflate(R.layout.forecast_item,forecatLayout,false);

             TextView dateText=view.findViewById(R.id.date_text);
             TextView infoText=view.findViewById(R.id.info_text);
             TextView maxText=view.findViewById(R.id.max_text);
             TextView minText=view.findViewById(R.id.min_text);
             dateText.setText(forecast.date);
             infoText.setText(forecast.more.info);
             maxText.setText(forecast.temperature.max);
             minText.setText(forecast.temperature.min);
             forecatLayout.addView(view);
         }

         if(weather.aqi !=null){
             aqiText.setText(weather.aqi.city.aqi);
             pm25Text.setText(weather.aqi.city.pm25);
         }

         String comfort ="舒适度:"+weather.suggestion.comfort.info;
         String carWash="洗车指数:"+weather.suggestion.carWash.info;
         String sport="运动指数:"+weather.suggestion.sport.info;
         comfortText.setText(comfort);
         carWashText.setText(carWash);
         sportText.setText(sport);
         weatherLayout.setVisibility(View.VISIBLE);

    }
}
