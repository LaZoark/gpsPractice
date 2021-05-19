package com.example.gpspractice;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class MainActivity extends AppCompatActivity
        implements LocationListener {

    static final int MIN_TIME = 5000; //位置更新條件：5000 毫秒
    static final float MIN_DIST = 0;   //位置更新條件：5 公尺
    LocationManager mgr;    // 定位管理員
    TextView txvLoc;
    TextView txvSetting;

    boolean isGPSEnabled;      //GPS定位是否可用
    boolean isNetworkEnabled;  //網路定位是否可用

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txvLoc = (TextView) findViewById(R.id.txvLoc);
        txvSetting = (TextView) findViewById(R.id.txvSetting);

        // 取得系統服務的LocationManager物件
        mgr = (LocationManager) getSystemService(LOCATION_SERVICE);

        // 檢查若尚未授權, 則向使用者要求定位權限
        checkPermission();
    }

    @Override
    protected void onResume() {
        super.onResume();

        txvLoc.setText("尚未取得定位資訊");  //清除之前的定位資訊
        enableLocationUpdates(true);    //開啟定位更新功能

        String str="GPS定位:"+ (isGPSEnabled?"開啟":"關閉");
        str += "\n網路定位:"+ (isNetworkEnabled?"開啟":"關閉");
        txvSetting.setText(str);    //顯示 GPS 與網路定位是否可用
    }

    @Override
    protected void onPause() {
        super.onPause();

        enableLocationUpdates(false);    //關閉定位更新功能
    }
    
    @Override
    public void onLocationChanged(Location location) { // 位置變更事件
        String str="目前定位提供者:"+location.getProvider();
        str += String.format("\n緯度:%.6f\n經度:%.6f\n高度:%.2f公尺",
                location.getLatitude(),		// 緯度
                location.getLongitude(),	// 經度
                location.getAltitude());	// 高度
        txvLoc.setText(str);
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

    // 顯示手機的定位設定畫面
    public void setup(View v) {
        Intent it =	// 使用Intent物件啟動"定位"設定程式
                new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(it);
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
}
