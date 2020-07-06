package com.dgsw_dev.cash.view.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
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
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.cardview.widget.CardView;
import androidx.core.app.NotificationCompat;

import com.dgsw_dev.cash.R;
import com.dgsw_dev.cash.data.DataSubject;
import com.dgsw_dev.cash.view.activity.MainActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import static java.lang.Thread.sleep;

public class Service_Overlay extends Service implements View.OnTouchListener {
    private static final String NOTIFICATION_CHANNEL_ID = "1234";
    WindowManager wm;
    private float prevY;
    int index = 0;
    View mView;
    int prev_Width, prev_Height;
    Animation fab_open, fab_close, fab_rotate , fab_backward;
    FloatingActionButton fab_opener,fab;
    CardView cardView, today, tomrrow, after_tomorrow, add;
    WindowManager.LayoutParams params;
    TimePicker times;
    ArrayList<DataSubject> list = new ArrayList<>();
    Boolean openFlag = false;
    TextView tv;
    Button button_select;
    String dialog = "";
    long time;

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
        onThreadService();
        fab_open = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_open);
        fab_close = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.fab_close);
        fab_rotate = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate_forward);
        fab_backward = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate_backward);

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
            fab_opener.startAnimation(fab_backward);
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
            fab_opener.startAnimation(fab_rotate);
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

        prev_Width = fab_opener.getWidth();
        prev_Height = fab_opener.getHeight();



        //버튼 상태 초기화(닫혀있어라!)
        cardView.setVisibility(View.GONE);
        fab.setVisibility(View.GONE);
        fab.setClickable(false);

        add.setOnClickListener(v -> {
            init_dialog(dialog);
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
    public void init_dialog(String content){
        list = loadSharedPreferencesList(getApplicationContext());
        today.setCardBackgroundColor(getResources().getColor(R.color.colorPrimary));
        tomrrow.setCardBackgroundColor(getResources().getColor(R.color.colorPrimary));
        after_tomorrow.setCardBackgroundColor(getResources().getColor(R.color.colorPrimary));



        String year = new SimpleDateFormat("yyyy").format(new Date(System.currentTimeMillis()));
        String month = new SimpleDateFormat("MM").format(new Date(System.currentTimeMillis()));
        String day = new SimpleDateFormat("dd").format(new Date(System.currentTimeMillis()));

        Calendar calendar = new GregorianCalendar(Integer.parseInt(year),
                Integer.parseInt(month),
                Integer.parseInt(day),
                times.getHour(),
                times.getMinute());

        if(content.equals("오늘")){
             time = calendar.getTimeInMillis();
        }else if(content.equals("내일")){

            calendar.add(Calendar.DAY_OF_WEEK, 1);
            time = calendar.getTimeInMillis();

        }else if(content.equals("모레")){
            calendar.add(Calendar.DAY_OF_WEEK, 2);
            time = calendar.getTimeInMillis();
        }

        String simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm").format(time);

        list.add(new DataSubject(button_select.getText().toString(), dialog, " ~ "+times.getHour()+":"+times.getMinute()+"까지",false, simpleDateFormat));

        Toast.makeText(getApplicationContext(), "과제 등록됨 : "+button_select.getText()+", 제출 기한 : "+dialog+ " ~"+times.getHour()+":"+times.getMinute()+"까지",Toast.LENGTH_LONG).show();
        Log.e("COMPLETED : ",simpleDateFormat);
        saveSharedPreferencesList(getApplicationContext(), list);

        text_changed(tv, "종료일을 선택해주세요.");
        button_select.setText("과제 선택하기");
    }
    public void onThreadService(){
        final Handler handler = new Handler();
        list = loadSharedPreferencesList(getApplicationContext());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    for (int size = 0; size < list.size(); size++) {
                        Date current_data = new Date(System.currentTimeMillis());
                        Date saved_data = new SimpleDateFormat("yyyy/MM/dd HH:mm").parse(list.get(size).getDate());
                        if(!list.isEmpty()){
                            assert saved_data != null;
                            Log.e("LOG", list.get(size).getOverTime()+"\n");
                            int compare = current_data.compareTo(saved_data);
                            if(compare < 0 && !list.get(index).getOverTime()){
                                list.get(index).setOverTime(true);
                                Log.e("LOG", list.get(size).getOverTime()+"\n");
                                saveSharedPreferencesList(getApplicationContext(), list);
                                Log.e("SUCCESS", saved_data.toString());
                                index++;
                                NotificationSomethings();
                                Log.e("SUCCESS", "TRUE");
                                handler.postDelayed(this, 60000);
                            }else{
                                Log.e("ERROR", "THERE IS NO TRUE");
                                handler.postDelayed(this, 60000);
                            }
                        }else{
                            Log.e("ERROR", "LIST IS EMPTY");
                            handler.postDelayed(this, 60000);
                        }
                    }
                } catch (Exception e) {
                    Log.e("ERROR", e.getMessage());
                    e.printStackTrace();
                }
                index = 0;
            }
        },10000);

    }
    public void NotificationSomethings() {


        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK) ;
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent,  PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher_foreground)) //BitMap 이미지 요구
                .setContentTitle("과제 종료 시간이 초과된 과제가 있습니다.")
                .setContentText(index +" 건의 종료 시간이 초과된 과제가 있습니다.")
                // 더 많은 내용이라서 일부만 보여줘야 하는 경우 아래 주석을 제거하면 setContentText에 있는 문자열 대신 아래 문자열을 보여줌
                //.setStyle(new NotificationCompat.BigTextStyle().bigText("더 많은 내용을 보여줘야 하는 경우..."))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent) // 사용자가 노티피케이션을 탭시 ResultActivity로 이동하도록 설정
                .setAutoCancel(true);

        //OREO API 26 이상에서는 채널 필요
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            builder.setSmallIcon(R.drawable.ic_launcher_foreground); //mipmap 사용시 Oreo 이상에서 시스템 UI 에러남
            CharSequence channelName  = "과제 종료 시간이 초과된 과제가 있습니다.";
            String description = "얼른 확인하세요!";
            int importance = NotificationManager.IMPORTANCE_HIGH;

            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName , importance);
            channel.setDescription(description);

            // 노티피케이션 채널을 시스템에 등록
            assert notificationManager != null;
            notificationManager.createNotificationChannel(channel);

        }else builder.setSmallIcon(R.mipmap.ic_launcher); // Oreo 이하에서 mipmap 사용하지 않으면 Couldn't create icon: StatusBarIcon 에러남

        assert notificationManager != null;
        notificationManager.notify(1234, builder.build()); // 고유숫자로 노티피케이션 동작시킴

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
