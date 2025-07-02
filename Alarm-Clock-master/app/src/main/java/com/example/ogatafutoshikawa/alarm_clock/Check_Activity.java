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
import android.widget.Toast;

import java.util.Calendar;

public class Check_Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cheack);

        Intent intent = getIntent();
        int get_hour = intent.getIntExtra(Main_Activity.HOUR_DATA, -1);
        int get_min = intent.getIntExtra(Main_Activity.MIN_DATA, -1);

        if (get_hour == -1 || get_min == -1) {
            Toast.makeText(this, "時刻が設定されていません。", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // --- アラーム設定処理 ---
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent bootIntent = new Intent(Check_Activity.this, Alarm_Receiver.class);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(
                Check_Activity.this, 0, bootIntent, PendingIntent.FLAG_UPDATE_CURRENT
        );

        // Calendarを使ってアラーム時刻を設定
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis());
        cal.set(Calendar.HOUR_OF_DAY, get_hour);
        cal.set(Calendar.MINUTE, get_min);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        // 過去の時刻なら次の日に設定
        if (cal.getTimeInMillis() < System.currentTimeMillis()) {
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }

        // ご指定の通り、setメソッドを使用してアラームを登録
        alarmManager.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), alarmIntent);

        Toast.makeText(this, "アラームを設定しました", Toast.LENGTH_SHORT).show();


        // --- UIとリセットボタンの処理 ---
        Button btnReset = this.findViewById(R.id.reset);
        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alarmManager.cancel(alarmIntent);
                Toast.makeText(Check_Activity.this, "アラームを解除しました", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        // UIの表示更新
        changeTime2(get_hour, get_min);
        String get_prefecture = intent.getStringExtra(Main_Activity.PREFECTURE_DATA);
        String get_city = intent.getStringExtra(Main_Activity.CITY_DATA);
        changePre(get_prefecture);
        changeCity(get_city);
    }

    // (以下の表示用メソッドは変更ありません)
    @SuppressLint("SetTextI18n")
    public void changeTime2(int h, int m){
        String hspace = (h < 10) ? "0" : "";
        String mspace = (m < 10) ? "0" : "";
        TextView tv = findViewById(R.id.time2);
        tv.setText(hspace + h + ":" + mspace + m);
    }

    public void changePre(String str) {
        if(str != null) {
            TextView tv = findViewById(R.id.prefecture2);
            tv.setText(str);
        }
    }

    public void changeCity(String str) {
        if(str != null) {
            TextView tv = findViewById(R.id.city2);
            tv.setText(str);
        }
    }
}