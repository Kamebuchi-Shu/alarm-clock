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
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Calendar;


public class Check_Activity extends AppCompatActivity {

    private static final String TAG = "Check_Activity"; // ログ用タグ

    // 新機能用のフィールド
    private int standardHour;
    private int standardMin;
    private int standardSec; // 秒を追加
    private int fakeHour;
    private int fakeMin;
    private int fakeSec; // 秒を追加
    private boolean forceModeEnabled;

    /**
     * Check_Activityの画面構成をするメソッド
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cheack);

        Intent intent = getIntent();

        // 新機能: 2つの時間とフォースモードを取得
        standardHour = intent.getIntExtra(Main_Activity.STANDARD_HOUR_DATA, 0);
        standardMin = intent.getIntExtra(Main_Activity.STANDARD_MIN_DATA, 0);
        standardSec = intent.getIntExtra("standard_sec", 0); // 秒も取得
        fakeHour = intent.getIntExtra(Main_Activity.FAKE_HOUR_DATA, 0);
        fakeMin = intent.getIntExtra(Main_Activity.FAKE_MIN_DATA, 0);
        fakeSec = intent.getIntExtra("fake_sec", 0); // 秒も取得
        forceModeEnabled = intent.getBooleanExtra(Main_Activity.FORCE_MODE_DATA, false);

        // 1. 規定時間とフェイクタイムをCalendarオブジェクトに変換
        Calendar standardCal = Calendar.getInstance();
        standardCal.set(Calendar.HOUR_OF_DAY, standardHour);
        standardCal.set(Calendar.MINUTE, standardMin);
        standardCal.set(Calendar.SECOND, standardSec);
        standardCal.set(Calendar.MILLISECOND, 0);
        long standardTimeMillis = standardCal.getTimeInMillis();

        Calendar fakeCal = Calendar.getInstance();
        fakeCal.set(Calendar.HOUR_OF_DAY, fakeHour);
        fakeCal.set(Calendar.MINUTE, fakeMin);
        fakeCal.set(Calendar.SECOND, fakeSec);
        fakeCal.set(Calendar.MILLISECOND, 0);
        long fakeTimeMillis = fakeCal.getTimeInMillis();

        // 2. 2つの時刻の範囲を決定 (どちらが先でも良いように)
        long startTime = Math.min(standardTimeMillis, fakeTimeMillis);
        long endTime = Math.max(standardTimeMillis, fakeTimeMillis);

        // 3. 範囲内でランダムな時刻をミリ秒単位で生成
        long randomTimeMillis = startTime + (long) (Math.random() * (endTime - startTime));

        // 4. 生成したランダムな時刻をCalendarオブジェクトに設定
        Calendar alarmTime = Calendar.getInstance();
        alarmTime.setTimeInMillis(randomTimeMillis);

        // UI要素の取得
        TextView standardTimeDisplay = findViewById(R.id.standard_time_display);
        TextView fakeTimeDisplay = findViewById(R.id.fake_time_display);
        TextView forceModeStatus = findViewById(R.id.force_mode_status);
        TextView probabilityInfo = findViewById(R.id.probability_info_display);

        // 時間表示の更新
        displayTime(standardTimeDisplay, standardHour, standardMin, standardSec);
        displayTime(fakeTimeDisplay, fakeHour, fakeMin, fakeSec);

        // フォースモードの状態表示
        String modeText = forceModeEnabled ? "ON (フェイクタイム100%)" : "OFF";
        forceModeStatus.setText(modeText);

        // 確率情報の表示
        String probText = forceModeEnabled ? "フェイクタイム: 100%" : "規定時間: 30% / フェイクタイム: 70%";
        probabilityInfo.setText(probText);

        // 標準時間用のリクエストコード（規定時間用）
        int standardRequestCode = standardHour * 1000 + standardMin;
        // フェイクタイム用のリクエストコード（+10000で区別）
        int fakeRequestCode = fakeHour * 1000 + fakeMin + 10000;

        // 標準時間のアラーム設定
        //setupAlarm(standardHour, standardMin, standardSec, requestCode, "standard");
        
        // フェイクタイムのアラーム設定
        //setupAlarm(fakeHour, fakeMin, fakeSec, requestCode, "fake");

        // 過去の時刻になった場合は翌日に設定
        if (alarmTime.getTimeInMillis() <= System.currentTimeMillis()) {
            alarmTime.add(Calendar.DAY_OF_YEAR, 1);
        }

        // CalendarからDateオブジェクトに変換して、見やすい形式でログ出力
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.JAPAN);
        Log.d("AlarmDebug", "--- アラーム設定デバッグ ---");
        Log.d("AlarmDebug", "規定時刻: " + standardHour + ":" + standardMin + ":" + standardSec);
        Log.d("AlarmDebug", "フェイク時刻: " + fakeHour + ":" + fakeMin + ":" + fakeSec);
        Log.d("AlarmDebug", "計算されたランダム時刻: " + sdf.format(alarmTime.getTime()));

        Intent bootIntent = new Intent(Check_Activity.this, Alarm_Receiver.class);

        // 表示用には「規定時刻」を渡す
        bootIntent.putExtra("displayHour", standardHour);
        bootIntent.putExtra("displayMin", standardMin);
        bootIntent.putExtra("displaySec", standardSec);

        // --- ▼▼▼ デバッグ用のログを追加 ▼▼▼ ---
        Log.d("AlarmDebug", "Intentに詰めた表示用の時刻: " + standardHour + ":" + standardMin + ":" + standardSec);
        Log.d("AlarmDebug", "--------------------------");

        int requestCode = 12345;

        PendingIntent alarmIntent = PendingIntent.getBroadcast(
                Check_Activity.this,
                requestCode,
                bootIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        // ★実際にアラームを鳴らすのは、計算した「ランダムな時刻（alarmTime）」
        alarm.set(
                AlarmManager.RTC_WAKEUP,
                alarmTime.getTimeInMillis(),
                alarmIntent
        );

        // 画面の表示は「規定時刻」のままにしておく
        standardTimeDisplay = findViewById(R.id.standard_time_display);
        displayTime(standardTimeDisplay, standardHour, standardMin, standardSec);


        Button btnReset = this.findViewById(R.id.reset);
        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 全てのアラームをキャンセル
                cancelAllAlarms();
                finish();
            }
        });


    }

    // 時間を表示する共通メソッド
    @SuppressLint("SetTextI18n")
    private void displayTime(TextView textView, int hour, int min, int sec) {
        String hspace = hour < 10 ? "0" : "";
        String mspace = min < 10 ? "0" : "";
        String sspace = sec < 10 ? "0" : "";
        textView.setText(hspace + hour + ":" + mspace + min + ":" + sspace + sec);
    }

    // アラーム設定の共通メソッド
    private void setupAlarm(int hour, int min, int sec, int requestCode, String alarmType) {
        // アラーム用のIntent
        Intent bootIntent = new Intent(Check_Activity.this, Alarm_Receiver.class);
        bootIntent.putExtra("notificationId", requestCode);
        bootIntent.putExtra("alarmType", alarmType); // "standard" または "fake"
        bootIntent.putExtra("standardHour", standardHour);
        bootIntent.putExtra("standardMin", standardMin);
        bootIntent.putExtra("fakeHour", fakeHour);
        bootIntent.putExtra("fakeMin", fakeMin);
        bootIntent.putExtra("forceModeEnabled", forceModeEnabled);

        // PendingIntentの作成
        final PendingIntent alarmIntent = PendingIntent.getBroadcast(
                Check_Activity.this,
                requestCode,
                bootIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        final AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        // アラーム時間の設定
        Calendar startTime = Calendar.getInstance();
        startTime.set(Calendar.HOUR_OF_DAY, hour);
        startTime.set(Calendar.MINUTE, min);
        startTime.set(Calendar.SECOND, sec); // 設定した秒を使用
        startTime.set(Calendar.MILLISECOND, 0);
        long alarmStartTime = startTime.getTimeInMillis();

        // 過去の時間に設定された場合は翌日にする
        if (alarmStartTime <= System.currentTimeMillis()) {
            alarmStartTime += 24 * 60 * 60 * 1000; // 1日追加
            Log.d(TAG, "過去の時刻設定を検出、翌日に設定: " + hour + ":" + min + " (" + alarmType + ")");
        }

        // Androidバージョンに応じたアラーム設定
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarm.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    alarmStartTime,
                    alarmIntent
            );
            Log.d(TAG, "setExactAndAllowWhileIdleを使用");
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarm.setExact(
                    AlarmManager.RTC_WAKEUP,
                    alarmStartTime,
                    alarmIntent
            );
            Log.d(TAG, "setExactを使用");
        } else {
            alarm.set(
                    AlarmManager.RTC_WAKEUP,
                    alarmStartTime,
                    alarmIntent
            );
            Log.d(TAG, "setを使用");
        }

        Log.d(TAG, "アラーム設定: " + hour + ":" + min + " (" + alarmType + ", リクエストコード: " + requestCode + ")");
    }

    // 全てのアラームをキャンセルする
    private void cancelAllAlarms() {
        AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        
        int standardRequestCode = standardHour * 1000 + standardMin;
        int fakeRequestCode = fakeHour * 1000 + fakeMin + 10000;

        // 標準時間のアラームをキャンセル
        Intent standardIntent = new Intent(this, Alarm_Receiver.class);
        PendingIntent standardPendingIntent = PendingIntent.getBroadcast(
                this, standardRequestCode, standardIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarm.cancel(standardPendingIntent);

        // フェイクタイムのアラームをキャンセル
        Intent fakeIntent = new Intent(this, Alarm_Receiver.class);
        PendingIntent fakePendingIntent = PendingIntent.getBroadcast(
                this, fakeRequestCode, fakeIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarm.cancel(fakePendingIntent);

        Log.d(TAG, "全てのアラームをキャンセルしました");
    }
}
