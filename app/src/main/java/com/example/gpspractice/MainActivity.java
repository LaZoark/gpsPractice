package com.example.gpspractice;

import android.*;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends AppCompatActivity
        implements LocationListener, OnMapReadyCallback { //實作 OnMapReadyCallback 介面

    private GoogleMap map;  // 操控地圖的物件
    LatLng currPoint;   //儲存目前的位置
    TextView txv;

    static final int MIN_TIME = 5000; //位置更新條件：5000 毫秒
    static final float MIN_DIST = 0;   //位置更新條件：5 公尺
    LocationManager mgr;    // 定位管理員

    boolean isGPSEnabled;      //GPS定位是否可用
    boolean isNetworkEnabled;  //網路定位是否可用
    boolean permissionRequested = false;  //是否已經向使用者要求過權限

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        幹就是這邊在智障花我6小時媽的
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);

        //FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        //fab.setOnClickListener(new View.OnClickListener() {
        //    @Override
        //    public void onClick(View view) {
        //        Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
        //        .setAction("Action", null).show();
        //    }
        //});

        // 取得系統服務的LocationManager物件
        mgr = (LocationManager) getSystemService(LOCATION_SERVICE);

        txv = (TextView) findViewById(R.id.txv);

        //取得佈局上的 map 元件
        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);  //註冊 Google Map onMapReady 事件監聽器

        // 檢查若尚未授權, 則向使用者要求定位權限
        checkPermission();
    }

    @Override
    protected void onResume() {
        super.onResume();
        enableLocationUpdates(true);    //開啟定位更新功能
    }

    @Override
    protected void onPause() {
        super.onPause();
        enableLocationUpdates(false);    //關閉定位更新功能
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) { // 依照選項的 id 來處理
            case R.id.mark:
                map.clear();
                map.addMarker(new MarkerOptions()
                        .position(map.getCameraPosition().target)
                        .title("到此一遊"));
                break;
            case R.id.satellite:
                item.setChecked(!item.isChecked()); // 切換功能表項目的打勾狀態
                if(item.isChecked())               // 設定是否顯示衛星圖
                    map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                else
                    map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                break;
            case R.id.traffic:
                item.setChecked(!item.isChecked()); // 切換功能表項目的打勾狀態
                map.setTrafficEnabled(item.isChecked()); // 設定是否顯示交通圖
                break;
            case R.id.currLoction:
                map.animateCamera( // 將地圖中心點移到目前位置
                        CameraUpdateFactory.newLatLng(currPoint));
                break;
            case R.id.setGPS:
                Intent i = new Intent( // 利用 Intent 啟動系統的定位服務設定
                        Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(i);
                break;
            case R.id.about:
                new AlertDialog.Builder(this) // 用交談窗顯示程式版本與版權聲明
                        .setTitle("關於 我的地圖")
                        .setMessage("我的地圖 體驗版 v1.0\nCopyright 2017 Flag Corp.")
                        .setPositiveButton("關閉", null)
                        .show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onLocationChanged(Location location) {
        if(location != null) { // 如果可以取得座標
            txv.setText(String.format("緯度 %.4f, 經度 %.4f (%s 定位 )",
                    location.getLatitude(),  // 目前緯度
                    location.getLongitude(), // 目前經度
                    location.getProvider()));// 定位方式

            currPoint = new LatLng(                //依照目前經緯度建立LatLng 物件
                    location.getLatitude(), location.getLongitude());
            if (map != null) { // 如果 Google Map 已經啟動完畢
                map.animateCamera(CameraUpdateFactory.newLatLng(currPoint)); // 將地圖中心點移到目前位置
                map.addMarker(new MarkerOptions().position(currPoint).title("目前位置")); //標記目前位置
            }
        }
        else { // 無法取得座標
            txv.setText("暫時無法取得定位資訊...");
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    //檢查若尚未授權, 則向使用者要求定位權限
    private void checkPermission() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 200);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 200){
            if (grantResults.length >= 1 &&
                    grantResults[0] != PackageManager.PERMISSION_GRANTED) {  // 使用者不允許權限
                Toast.makeText(this, "程式需要定位權限才能運作", Toast.LENGTH_LONG).show();
            }
        }
    }

    //開啟或關閉定位更新功能
    private void enableLocationUpdates(boolean isTurnOn) {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED)
        {  // 使用者已經允許定位權限
            if (isTurnOn) {
                //檢查 GPS 與網路定位是否可用
                isGPSEnabled = mgr.isProviderEnabled(LocationManager.GPS_PROVIDER);
                isNetworkEnabled = mgr.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

                if (!isGPSEnabled && !isNetworkEnabled) {
                    // 無提供者, 顯示提示訊息
                    Toast.makeText(this, "請確認已開啟定位功能!", Toast.LENGTH_LONG).show();
                }
                else {
                    Toast.makeText(this, "取得定位資訊中...", Toast.LENGTH_LONG).show();
                    if (isGPSEnabled)
                        mgr.requestLocationUpdates(   //向 GPS 定位提供者註冊位置事件監聽器
                                LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DIST, this);
                    if (isNetworkEnabled)
                        mgr.requestLocationUpdates(   //向網路定位提供者註冊位置事件監聽器
                                LocationManager.NETWORK_PROVIDER, MIN_TIME, MIN_DIST, this);
                }
            }
            else {
                mgr.removeUpdates(this);    //停止監聽位置事件
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {  //Google Map 啟動完畢可以使用
        map = googleMap;  //取得 Google Map 物件, 此物件可以操控地圖

        map.setMapType(GoogleMap.MAP_TYPE_NORMAL); //設定地圖為普通街道模式
        map.moveCamera(CameraUpdateFactory.zoomTo(18));  //將地圖縮放級數改為 18
    }
}
