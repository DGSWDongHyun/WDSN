package com.dgsw_dev.cash.view.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.WindowDecorActionBar;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.dgsw_dev.cash.R;
import com.dgsw_dev.cash.data.DataSubject;
import com.dgsw_dev.cash.view.service.Service_Overlay;
import com.dgsw_dev.cash.view.util.SwipeDismissListViewTouchListener;
import com.dgsw_dev.cash.view.view_holder.ViewHolder_Data;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {
    private static final int ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE = 42;
    ListView listview;
    ArrayList<DataSubject> list;
    SwipeRefreshLayout swipe;
    ViewHolder_Data adapter;
    TextView time_now;
    ImageView img;
    Date date;
    SimpleDateFormat simpleDateFormat;
    LinearLayout sub_loading;
    public final static int FULLTIME_OF_DAY = 24;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkPermission();
        adapter = new ViewHolder_Data(getApplicationContext());
        list = loadSharedPreferencesList(getApplicationContext());
        sub_loading = findViewById(R.id.sub_loading);

        // 리스트뷰 참조 및 Adapter 달기
        listview = (ListView) findViewById(R.id.list_s);
        swipe = findViewById(R.id.swipe);
        swipe.setOnRefreshListener(this);
        listview.setAdapter(adapter);

        LottieAnimationView animationView = (LottieAnimationView) findViewById(R.id.animation_view);
        animationView.setAnimation("book_stack.json");
        animationView.loop(true);
        animationView.playAnimation();
        sub_loading.setVisibility(View.VISIBLE);

        new Handler().postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                Animation animation = new AlphaAnimation(1, 0);
                animation.setDuration(700);
                sub_loading.setVisibility(View.GONE);
                sub_loading.setAnimation(animation);
            }
        }, 4000);

        time_now = findViewById(R.id.time_now);
        img = findViewById(R.id.time_img);

        returnTimes(date, simpleDateFormat);

        for (int index = 0; index < list.size(); index++) {
            adapter.addItem(list.get(index).getSubjectName(), list.get(index).getToTime(), list.get(index).getDetail_time(), list.get(index).getOverTime(), list.get(index).getDate());
            adapter.notifyDataSetChanged();
        }


        SwipeDismissListViewTouchListener touchListener =
                new SwipeDismissListViewTouchListener(listview,
                        new SwipeDismissListViewTouchListener.DismissCallbacks() {
                            @Override
                            public boolean canDismiss(int position) {
                                return true;
                            }

                            @Override
                            public void onDismiss(ListView listView, int[] reverseSortedPositions) {
                                for (int position : reverseSortedPositions) {
                                    list.remove(position);
                                    if(list.isEmpty()){
                                        adapter.clearAll();
                                    }
                                }
                                saveSharedPreferencesList(getApplicationContext(),list);
                                list = loadSharedPreferencesList(getApplicationContext());
                                adapter.clearAll();
                                for (int index = 0; index < list.size(); index++) {
                                    adapter.addItem(list.get(index).getSubjectName(), list.get(index).getToTime(), list.get(index).getDetail_time(), list.get(index).getOverTime(), list.get(index).getDate());
                                    adapter.notifyDataSetChanged();
                                }
                            }
                        });
        listview.setOnTouchListener(touchListener);
        listview.setOnScrollListener(touchListener.makeScrollListener());

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
    public void returnTimes(Date time, SimpleDateFormat format){

        time = new Date();
        format = new SimpleDateFormat ( "HH");

        int hour = Integer.parseInt(format.format(time));

        if(hour > 6 && hour <= 12){
            time_now.setText("아침");
            img.setImageResource(R.drawable.w_beforenoon_2);
        }else if(hour > 12 && hour <= 18){
            time_now.setText("낮");
            img.setImageResource(R.drawable.w_sunny);
        }else if(hour > 18 && hour <= 21){
            time_now.setText("저녁");
            img.setImageResource(R.drawable.w_afternoon);
        }else if(hour > 21 || hour <= 6){
            time_now.setText("밤");
            img.setImageResource(R.drawable.w_night);
        }
    }
    @Override
    public void onRefresh() {
        swipe.setRefreshing(true);

        adapter.clearAll();
        list = loadSharedPreferencesList(getApplicationContext());
        returnTimes(date, simpleDateFormat);

        for(int index = 0; index < list.size(); index++){
            adapter.addItem(list.get(index).getSubjectName(), list.get(index).getToTime(), list.get(index).getDetail_time(),list.get(index).getOverTime(), list.get(index).getDate());
            adapter.notifyDataSetChanged();
        }

        new Handler().postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                swipe.setRefreshing(false);
            }
        }, 200);
    }
    public static void saveSharedPreferencesList(Context context, ArrayList<DataSubject> Subject) {
        SharedPreferences mPrefs = context.getSharedPreferences("pref",context.MODE_PRIVATE);
        SharedPreferences.Editor prefsEditor = mPrefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(Subject);
        prefsEditor.putString("Subject_Data", json);
        prefsEditor.commit();
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