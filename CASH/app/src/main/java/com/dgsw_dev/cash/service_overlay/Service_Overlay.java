package com.dgsw_dev.cash.service_overlay;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;

import com.dgsw_dev.cash.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class Service_Overlay extends Service implements View.OnTouchListener {
    WindowManager wm;
    private float prevX;
    private float prevY;
    View mView;
    LinearLayout linearLayout;
    Animation fab_open, fab_close;
    FloatingActionButton fab_opener,fab;
    CardView cardView;
    WindowManager.LayoutParams params;
    Boolean openFlag = false;
    Boolean fab_opening = true;

    public Service_Overlay() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
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

            fab = mView.findViewById(R.id.close_fab);
            fab_opener = mView.findViewById(R.id.fab_open);
            cardView = mView.findViewById(R.id.card_view);
            linearLayout = mView.findViewById(R.id.linear);

            //버튼 상태 초기화(닫혀있어라!)
            fab.startAnimation(fab_close);
            cardView.startAnimation(fab_close);
            fab.setClickable(false);


            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    anim();
                    stopSelf();
                }
            });
            fab_opener.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    anim();
                }
            });

            mView.setOnTouchListener(this);
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

            fab = mView.findViewById(R.id.close_fab);
            fab_opener = mView.findViewById(R.id.fab_open);
            cardView = mView.findViewById(R.id.card_view);
            linearLayout = mView.findViewById(R.id.linear);

            fab.startAnimation(fab_close);
            cardView.startAnimation(fab_close);
            fab.setClickable(false);

            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    anim();
                    stopSelf();
                }
            });
            fab_opener.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    anim();
                }
            });

            mView.setOnTouchListener(this);
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
            fab.setClickable(false);
            openFlag = false;
            linearLayout.setVisibility(View.VISIBLE);

        } else {
            fab.startAnimation(fab_open);
            cardView.startAnimation(fab_open);
            fab.setClickable(true);
            openFlag = true;
            linearLayout.setVisibility(View.GONE);

        }
    }
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: // 처음 위치를 기억해둔다.
                prevX = event.getRawX();
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
}
