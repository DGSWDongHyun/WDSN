package com.dgsw_dev.cash.view.view_holder;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.dgsw_dev.cash.R;
import com.dgsw_dev.cash.data.DataSubject;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.w3c.dom.Text;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ViewHolder_Data extends BaseAdapter {
    private ArrayList<DataSubject> listViewItemList = new ArrayList<DataSubject>() ;
    Context mContext;
    public ViewHolder_Data(Context mContext) {
        this.mContext = mContext;
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

        listViewItemList = loadSharedPreferencesList(mContext);

        // Data Set(listViewItemList)에서 position에 위치한 데이터 참조 획득
        DataSubject listViewItem = listViewItemList.get(position);

        // 아이템 내 각 위젯에 데이터 반영

        TextView subject = convertView.findViewById(R.id.subject);
        TextView times = convertView.findViewById(R.id.time);

        subject.setText(listViewItem.getSubjectName());
        times.setText(listViewItem.getDetail_time());

        TextView overtime = convertView.findViewById(R.id.overtime);

        for(int index = 0; index < listViewItemList.size(); index++){
            if(listViewItemList.get(index).getOverTime() == true){
                times.setText("누락됨");
                times.setTextColor(convertView.getResources().getColor(R.color.red));
            }
        }

        return convertView;
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
    public void addItem(String title, String time , String Detail, Boolean task_over, String date) {
        DataSubject item = new DataSubject(title, time, Detail, task_over, date);

        item.setSubjectName(title);
        item.setToTime("제출 기한 : "+time+", ~"+Detail+" 까지");
        item.setOverTime(task_over);


        listViewItemList.add(item);
    }
    public void clearAll(){
        listViewItemList.removeAll(listViewItemList);
    }
}
