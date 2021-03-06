package com.example.coolweather.fragment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.coolweather.R;
import com.example.coolweather.WeatherActivity;
import com.example.coolweather.db.City;
import com.example.coolweather.db.County;
import com.example.coolweather.db.Province;
import com.example.coolweather.gson.Weather;
import com.example.coolweather.utils.HttpUtil;
import com.example.coolweather.utils.Utility;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ChooseAreaFragment extends Fragment {


    public static final int LEVEL_PROVINCE=0;
    public static final int LEVEL_CITY=1;
    public static final int  LEVEL_COUNTY=2;
    private ProgressDialog progressDialog;
    private TextView titleText;
    private Button backButton;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private List<String> dataList=new ArrayList<>();

    private  List<Province> provinceList;
    private  List<City>  cityList;
    private  List<County>  countyList;

    private Province selectedProvinced;

    private  City selectedCity;


    private  int currentLevle;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.choose_area,container,false);
        titleText=view.findViewById(R.id.title_text);
        backButton =view.findViewById(R.id.back_button);
        listView=view.findViewById(R.id.list_view);
        adapter=new ArrayAdapter<>(getContext(),android.R.layout.simple_list_item_1,dataList);

        listView.setAdapter(adapter);
        return  view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if(currentLevle==LEVEL_PROVINCE){
                    selectedProvinced=provinceList.get(position);
                    queryCities();

                }else if(currentLevle == LEVEL_CITY){
                    selectedCity=cityList.get(position);
                    queryCounties();


                }else if(currentLevle== LEVEL_COUNTY){
                    String weatherId=countyList.get(position).getWeatherId();
                    Intent intent=new Intent(getActivity(), WeatherActivity.class);

                    //带着参数跳转
                    intent.putExtra("weather_id",weatherId);
                    startActivity(intent);
                    getActivity().finish();

                }
            }
        });
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(currentLevle ==LEVEL_COUNTY){
                    queryCities();
                }else if(currentLevle ==LEVEL_CITY){
                    queryProvinces();
                }
            }
        });

        queryProvinces();
    }

    private  void queryProvinces(){

        titleText.setText("中国");
        backButton.setVisibility(View.GONE);
      provinceList= DataSupport.findAll(Province.class);
      if(provinceList.size()>0){
          dataList.clear();
          for (Province province:provinceList){
          dataList.add(province.getProvinceName());
      }
    adapter.notifyDataSetChanged();
          listView.setSelection(0);
          currentLevle=LEVEL_PROVINCE;



    }else {
          String address="http://guolin.tech/api/china";
          queryFromServer(address,"province");
      }

}


    private  void queryCities(){

        titleText.setText(selectedProvinced.getProvinceName());
        backButton.setVisibility(View.VISIBLE);
        cityList= DataSupport.where("provinceid=?",String.valueOf(selectedProvinced.getId())).find(City.class);
        if(cityList.size()>0){
            dataList.clear();
            for (City city:cityList){
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevle=LEVEL_CITY;



        }else {
            int provinceCode=selectedProvinced.getProvinceCode();
            String address="http://guolin.tech/api/china/"+provinceCode;
            queryFromServer(address,"city");
        }

    }

    private  void queryCounties(){

        titleText.setText(selectedCity.getCityName());
        backButton.setVisibility(View.VISIBLE);
        countyList= DataSupport.where("cityid=?",String.valueOf(selectedCity.getId())).find(County.class);
        if(countyList.size()>0){
            dataList.clear();
            for (County county:countyList){
                dataList.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevle=LEVEL_COUNTY;



        }else {
            int provinceCode=selectedProvinced.getProvinceCode();
            int cityCode=selectedCity.getCityCode();
            String address="http://guolin.tech/api/china/"+provinceCode+"/"+cityCode;
            queryFromServer(address,"county");
        }

    }

    private void queryFromServer(String address, final String type) {
    showProgressDialog();

        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

                Log.i("ll", "onFailure: ---------------失败原因:"+e);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(getContext(),"加载失败",Toast.LENGTH_SHORT).show();
                    }
                });

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                String responseText =response.body().string();
                boolean result=false;
                if("province".equals(type)){
                    result= Utility.handleProvinceResponse(responseText);
                }else if("city".equals(type)){
                    result= Utility.handleCityResponse(responseText,selectedProvinced.getId());

                }else if("county".equals(type)){
                    result= Utility.handleCountyResponse(responseText,selectedCity.getId());

                }

                if(result){
 getActivity().runOnUiThread(new Runnable() {
     @Override
     public void run() {
         closeProgressDialog();
         if("province".equals(type)){

             queryProvinces();

         }else if("city".equals(type)){
  queryCities();
         }else if("county".equals(type)){
             queryCounties();
         }

     }
 });



                }

            }
        });

    }

    private void showProgressDialog() {
        if(progressDialog==null){
            progressDialog=new ProgressDialog(getActivity());
            progressDialog.setMessage("正在加载....");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();

    }

    private  void closeProgressDialog(){

            if(progressDialog !=null){
                progressDialog.dismiss();
            }

    }

}


