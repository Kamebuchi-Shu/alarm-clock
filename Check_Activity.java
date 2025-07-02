package com.example.ogatafutoshikawa.alarm_clock;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Calendar;

public class Check_Activity extends AppCompatActivity {

    /**
     * City_Activityの画面構成をするメソッド
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cheack);

        Intent intent = getIntent();
        Intent bootIntent = new Intent(Check_Activity.this, Alarm_Receiver.class);

        final PendingIntent alarmIntent = PendingIntent.getBroadcast(Check_Activity.this, 0, bootIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        final AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        int notificationId = 0;
        bootIntent.putExtra("notificationId", notificationId);

        int get_hour = intent.getIntExtra(Main_Activity.HOUR_DATA,0);
        int get_min = intent.getIntExtra(Main_Activity.MIN_DATA,0);
        //String get_prefecture = intent.getStringExtra(Main_Activity.PREFECTURE_DATA);
        //String get_city = intent.getStringExtra(Main_Activity.CITY_DATA);

        Button btnReset = this.findViewById(R.id.reset);
        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alarm.cancel(alarmIntent);
                finish();
            }
        });

        //changePre(get_prefecture);
        //changeCity(get_city);
        changeTime2(get_hour, get_min);

        Calendar startTime = Calendar.getInstance();
        startTime.set(Calendar.HOUR_OF_DAY, get_hour);
        startTime.set(Calendar.MINUTE, get_min);
        startTime.set(Calendar.SECOND, 0);
        long alarmStartTime = startTime.getTimeInMillis();

        alarm.set(
                //デバイスがスリープ状態の場合に alarm が動作した場合、alarm はデバイスを起こす
                AlarmManager.RTC_WAKEUP,
                alarmStartTime,
                alarmIntent         // PendingIntent
        );
    }

    /**
     *
     * @param h
     * @param m
     */
    @SuppressLint("SetTextI18n")
    public void changeTime2(int h, int m){
        String hspace = "";
        String mspace = "";
        if(h < 10){
            hspace = "0";
        }
        if(m < 10){
            mspace = "0";
        }
        TextView tv = findViewById(R.id.time2);
        tv.setText(hspace + h + ":" + mspace + m);
    }

    /**
     *
     * @param str
     */
    /*
    public void changePre(String str) {
        TextView tv = findViewById(R.id.prefecture2);
        tv.setText(str);
    }
    */
    /**
     *
     * @param str
     */
    /*
    public void changeCity(String str) {
        TextView tv = findViewById(R.id.city2);
        tv.setText(str);
    }
    */
}
