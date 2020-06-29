package com.dgsw_dev.cash.view.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.ListView;

import com.dgsw_dev.cash.DataBinderMapperImpl;
import com.dgsw_dev.cash.R;
import com.dgsw_dev.cash.data.DataSubject;
import com.dgsw_dev.cash.view.service.Service_Overlay;
import com.dgsw_dev.cash.view.view_holder.ViewHolder_Data;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private static final int ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE = 42;
    ListView listview ;
    ArrayList<DataSubject> list;
    ViewHolder_Data adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkPermission();
        adapter = new ViewHolder_Data() ;
        list = loadSharedPreferencesList(getApplicationContext());

        // 리스트뷰 참조 및 Adapter달기
        listview = (ListView) findViewById(R.id.list_s);
        listview.setAdapter(adapter);


        for(int index = 0; index < list.size(); index++){
            adapter.addItem(list.get(index).getSubjectName(), list.get(index).getToTime(), list.get(index).getDetail_time(), list.get(index).getOverTime());
        }
    }

    public void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {   // 마시멜로우 이상일 경우
            if (!Settings.canDrawOverlays(this)) {              // 체크
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE);
            } else {
                startService(new Intent(MainActivity.this, Service_Overlay.class));
            }
        } else {
            startService(new Intent(MainActivity.this, Service_Overlay.class));
        }
    }
    public static ArrayList<DataSubject> loadSharedPreferencesList(Context context) {
        ArrayList<DataSubject> data = new ArrayList<DataSubject>();
        SharedPreferences mPrefs = context.getSharedPreferences("pref", context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = mPrefs.getString("Subject_Data", "");
        if (json.isEmpty()) {
            data = new ArrayList<DataSubject>();
        } else {
            Type type = new TypeToken<ArrayList<DataSubject>>() {
            }.getType();
            data = gson.fromJson(json, type);
        }
        return data;
    }
    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE) {
            if (!Settings.canDrawOverlays(this)) {
                finish();
            } else {
                startService(new Intent(MainActivity.this, Service_Overlay.class));
            }
        }
    }
}