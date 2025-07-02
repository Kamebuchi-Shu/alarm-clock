package com.example.ogatafutoshikawa.alarm_clock;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Calendar;

public class Check_Activity extends AppCompatActivity {

    private static final String TAG = "Check_Activity"; // ログ用タグ

    /**
     * City_Activityの画面構成をするメソッド
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cheack);

        Intent intent = getIntent();

        // 時間と分を取得
        int get_hour = intent.getIntExtra(Main_Activity.HOUR_DATA, 0);
        int get_min = intent.getIntExtra(Main_Activity.MIN_DATA, 0);
        //String get_prefecture = intent.getStringExtra(Main_Activity.PREFECTURE_DATA);
        //String get_city = intent.getStringExtra(Main_Activity.CITY_DATA);

        // 一意なリクエストコード生成 (時間と分を組み合わせ)
        int requestCode = get_hour * 100 + get_min;

        // アラーム用のIntent
        Intent bootIntent = new Intent(Check_Activity.this, Alarm_Receiver.class);
        bootIntent.putExtra("notificationId", requestCode);

        // PendingIntentの作成 (FLAG_UPDATE_CURRENTを使用)
        final PendingIntent alarmIntent = PendingIntent.getBroadcast(
                Check_Activity.this,
                requestCode,  // 一意なリクエストコード
                bootIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        final AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

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

        // アラーム時間の設定
        Calendar startTime = Calendar.getInstance();
        startTime.set(Calendar.HOUR_OF_DAY, get_hour);
        startTime.set(Calendar.MINUTE, get_min);
        startTime.set(Calendar.SECOND, 0);
        startTime.set(Calendar.MILLISECOND, 0);
        long alarmStartTime = startTime.getTimeInMillis();

        // 過去の時間に設定された場合は翌日にする
        if (alarmStartTime <= System.currentTimeMillis()) {
            alarmStartTime += 24 * 60 * 60 * 1000; // 1日追加
            Log.d(TAG, "過去の時刻設定を検出、翌日に設定: " + get_hour + ":" + get_min);
        }

        // Androidバージョンに応じたアラーム設定
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android 6.0+ (Dozeモード対策)
            alarm.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    alarmStartTime,
                    alarmIntent
            );
            Log.d(TAG, "setExactAndAllowWhileIdleを使用");
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // Android 4.4+
            alarm.setExact(
                    AlarmManager.RTC_WAKEUP,
                    alarmStartTime,
                    alarmIntent
            );
            Log.d(TAG, "setExactを使用");
        } else {
            // 旧バージョン
            alarm.set(
                    AlarmManager.RTC_WAKEUP,
                    alarmStartTime,
                    alarmIntent
            );
            Log.d(TAG, "setを使用");
        }

        Log.d(TAG, "アラーム設定: " + get_hour + ":" + get_min + " (リクエストコード: " + requestCode + ")");
    }

    /**
     *
     * @param h
     * @param m
     */
    @SuppressLint("SetTextI18n")
    public void changeTime2(int h, int m){
        String hspace = h < 10 ? "0" : "";
        String mspace = m < 10 ? "0" : "";
        TextView tv = findViewById(R.id.time2);
        tv.setText(hspace + h + ":" + mspace + m);
    }

    /**
     *
     * @param str
     */
    /*public void changePre(String str) {
        TextView tv = findViewById(R.id.prefecture2);
        tv.setText(str);
    }*/

    /**
     *
     * @param str
     */
    /*public void changeCity(String str) {
        TextView tv = findViewById(R.id.city2);
        tv.setText(str);
    }*/
}
