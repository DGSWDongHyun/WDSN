package com.dgsw_dev.cash.view.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.cardview.widget.CardView;

import com.dgsw_dev.cash.R;
import com.dgsw_dev.cash.data.DataSubject;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class Service_Overlay extends Service implements View.OnTouchListener {
    WindowManager wm;
    private float prevY;
    View mView;
    int prev_Width, prev_Height;
    Animation fab_open, fab_close;
    FloatingActionButton fab_opener,fab;
    CardView cardView, today, tomrrow, after_tomorrow, add;
    WindowManager.LayoutParams params;
    TimePicker times;
    ArrayList<DataSubject> list = new ArrayList<>();
    Boolean openFlag = false;
    TextView tv;
    Button button_select;
    String dialog = "";

    public Service_Overlay() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onCreate() {
        setTheme(R.style.AppTheme);
        super.onCreate();
        fab_open = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_open);
        fab_close = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.fab_close);

        LayoutInflater inflate = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
             params = new WindowManager.LayoutParams(
                    /*ViewGroup.LayoutParams.MATCH_PARENT*/ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                            | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                            | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                    PixelFormat.TRANSLUCENT);
            params.gravity = Gravity.RIGHT | Gravity.TOP;
            mView = inflate.inflate(R.layout.overlay_service, null);

            init_Layout();

            wm.addView(mView, params);
        }else if(Build.VERSION.SDK_INT < Build.VERSION_CODES.O){
          params = new WindowManager.LayoutParams(
                    /*ViewGroup.LayoutParams.MATCH_PARENT*/ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                            | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                            | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                    PixelFormat.TRANSLUCENT);
            params.gravity = Gravity.RIGHT | Gravity.TOP;
            mView = inflate.inflate(R.layout.overlay_service, null);

            init_Layout();

            wm.addView(mView, params);
        }

    }
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_REDELIVER_INTENT;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if(wm != null) {
            if(mView != null) {
                wm.removeView(mView);
                mView = null;
            }
            wm = null;
        }
    }
    public void anim() {

        if (openFlag) {
            fab.startAnimation(fab_close);
            cardView.startAnimation(fab_close);
            new Handler().postDelayed(new Runnable()
            {
                @Override
                public void run()
                {
                    fab.setVisibility(View.GONE);
                    cardView.setVisibility(View.GONE);
                }
            }, 600);
            fab.setClickable(false);
            openFlag = false;

        } else {
            fab.startAnimation(fab_open);
            fab.setVisibility(View.VISIBLE);
            cardView.startAnimation(fab_open);
            cardView.setVisibility(View.VISIBLE);
            fab.setClickable(true);
            openFlag = true;


        }
    }
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: // 처음 위치를 기억해둔다.
                prevY = event.getRawY();
                break;

            case MotionEvent.ACTION_MOVE:
                float rawY = event.getRawY(); // 절대 Y 좌표값을 가져온다.

                // 이동한 위치에서 처음 위치를 빼서 이동한 거리를 구한다.
                float y = rawY - prevY;

                setCoordinateUpdate(y);
                prevY = rawY;
                break;
        }
        return false;
    }

    /**
     * 이동한 거리를 x, y를 넘겨 LayoutParams에 갱신한다.
     * @param y
     */
    private void setCoordinateUpdate(float y) {
        if (params != null) {
            params.y += (int) y;

            wm.updateViewLayout(mView, params);
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void init_Layout(){
        fab = mView.findViewById(R.id.close_fab);
        fab_opener = mView.findViewById(R.id.fab_open);
        cardView = mView.findViewById(R.id.card_view);
        today = mView.findViewById(R.id.today_card);
        tomrrow = mView.findViewById(R.id.tomorrow_card);
        after_tomorrow = mView.findViewById(R.id.next_tomorrow_card);
        tv = mView.findViewById(R.id.selected_text);
        add = mView.findViewById(R.id.add_card);
        button_select = mView.findViewById(R.id.submenu);
        times = mView.findViewById(R.id.time_picker);

        prev_Width = fab.getWidth();
        prev_Height = fab.getHeight();



        //버튼 상태 초기화(닫혀있어라!)
        cardView.setVisibility(View.GONE);
        fab.setVisibility(View.GONE);
        fab.setClickable(false);

        add.setOnClickListener(v -> {
            init_dialog();
            anim();
        });
        button_select.setOnClickListener(v->{
            PopupMenu p = new PopupMenu(
                    getApplicationContext(), // 현재 화면의 제어권자
                    v); // anchor : 팝업을 띄울 기준될 위젯
            getMenuInflater().inflate(R.menu.menu_service, p.getMenu());
            // 이벤트 처리
            p.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    button_select.setText(item.getTitle());
                    return false;
                }
            });
            p.show(); // 메뉴를 띄우기
        });

        fab.setOnClickListener(v->{
            anim();
            stopSelf();
        });
        fab_opener.setOnLongClickListener(v->{
            fab_opener.setOnTouchListener(this);
            return false;
        });
        fab_opener.setOnClickListener(v->{
            anim();
        });
        today.setOnClickListener(v->{
                text_changed(tv, "오늘");
        });
        tomrrow.setOnClickListener(v->{
                text_changed(tv, "내일");
        });
        after_tomorrow.setOnClickListener(v->{
                text_changed(tv, "모레");
        });
    }
    public void text_changed(TextView tv, String content){
        switch (content){
            case "오늘":
                today.setCardBackgroundColor(getResources().getColor(R.color.green));
                tomrrow.setCardBackgroundColor(getResources().getColor(R.color.colorPrimary));
                after_tomorrow.setCardBackgroundColor(getResources().getColor(R.color.colorPrimary));
                break;
            case "내일":
                today.setCardBackgroundColor(getResources().getColor(R.color.colorPrimary));
                tomrrow.setCardBackgroundColor(getResources().getColor(R.color.green));
                after_tomorrow.setCardBackgroundColor(getResources().getColor(R.color.colorPrimary));
                break;
            case "모레":
                today.setCardBackgroundColor(getResources().getColor(R.color.colorPrimary));
                tomrrow.setCardBackgroundColor(getResources().getColor(R.color.colorPrimary));
                after_tomorrow.setCardBackgroundColor(getResources().getColor(R.color.green));
                break;
        }
         tv.setText(content);
        dialog = content;
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void init_dialog(){
        list = loadSharedPreferencesList(getApplicationContext());
        today.setCardBackgroundColor(getResources().getColor(R.color.colorPrimary));
        tomrrow.setCardBackgroundColor(getResources().getColor(R.color.colorPrimary));
        after_tomorrow.setCardBackgroundColor(getResources().getColor(R.color.colorPrimary));

        list.add(new DataSubject(button_select.getText().toString(), dialog, " ~ "+times.getHour()+":"+times.getMinute()+"까지",false));
        Toast.makeText(getApplicationContext(), "과제 등록됨 : "+button_select.getText()+", 제출 기한 : "+dialog+ " ~"+times.getHour()+":"+times.getMinute()+"까지",Toast.LENGTH_LONG).show();;
        saveSharedPreferencesList(getApplicationContext(), list);

        text_changed(tv, "종료일을 선택해주세요.");
        button_select.setText("과제 선택하기");
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

    public MenuInflater getMenuInflater() {
        return new MenuInflater(this);
    }
}
