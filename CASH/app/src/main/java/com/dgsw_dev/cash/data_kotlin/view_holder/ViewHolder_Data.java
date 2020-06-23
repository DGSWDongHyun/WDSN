package com.dgsw_dev.cash.data_kotlin.view_holder;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.dgsw_dev.cash.R;
import com.dgsw_dev.cash.data_kotlin.DataSubject;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ViewHolder_Data extends BaseAdapter {
    // Adapter에 추가된 데이터를 저장하기 위한 ArrayList
    private ArrayList<DataSubject> listViewItemList = new ArrayList<DataSubject>() ;

    // ListViewAdapter의 생성자
    public ViewHolder_Data() {

    }

    // Adapter에 사용되는 데이터의 개수를 리턴. : 필수 구현
    @Override
    public int getCount() {
        return listViewItemList.size() ;
    }

    // position에 위치한 데이터를 화면에 출력하는데 사용될 View를 리턴. : 필수 구현
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final int pos = position;
        final Context context = parent.getContext();

        // "listview_item" Layout을 inflate하여 convertView 참조 획득.
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.item_list, parent, false);
        }

        // 화면에 표시될 View(Layout이 inflate된)으로부터 위젯에 대한 참조 획득


        // Data Set(listViewItemList)에서 position에 위치한 데이터 참조 획득
        DataSubject listViewItem = listViewItemList.get(position);

        // 아이템 내 각 위젯에 데이터 반영
        TextView text_now = convertView.findViewById(R.id.time_now);
        ImageView img_time = convertView.findViewById(R.id.time_img);
        Date date = new Date();
        SimpleDateFormat sdformat = new SimpleDateFormat("HH");

        if("12".contains(sdformat.toString())){
               img_time.setImageResource(R.drawable.w_sunny);
        }else if("06".contains(sdformat.toString())){
               img_time.setImageResource(R.drawable.w_afternoon_2);
        }else if("18".contains(sdformat.toString())){
                img_time.setImageResource(R.drawable.w_afternoon);
        }else if("20".contains(sdformat.toString())){
                img_time.setImageResource(R.drawable.w_night);
        }


        return convertView;
    }

    // 지정한 위치(position)에 있는 데이터와 관계된 아이템(row)의 ID를 리턴. : 필수 구현
    @Override
    public long getItemId(int position) {
        return position ;
    }

    // 지정한 위치(position)에 있는 데이터 리턴 : 필수 구현
    @Override
    public Object getItem(int position) {
        return listViewItemList.get(position) ;
    }

    // 아이템 데이터 추가를 위한 함수. 개발자가 원하는대로 작성 가능.
    public void addItem(String title, String time, Boolean task_over) {
        DataSubject item = new DataSubject(title, time, task_over);

        item.setSubjectName(title);
        item.setToTime(time);
        item.setOverTime(task_over);


        listViewItemList.add(item);
    }
}