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
import java.util.Calendar;
import java.util.Locale;

public class Check_Activity extends AppCompatActivity {

    private static final String TAG = "Check_Activity";
    private static final int ALARM_REQUEST_CODE = 12345; // アラームを1つに固定

    // ===================================================================
    // ▼▼▼ 機能の切り替え ▼▼▼
    // 機能1（ランダムな時間を表示）を使いたい場合 → true
    // 機能2（規定時間の10分後を表示）を使いたい場合 → false
    private static final boolean USE_RANDOM_DISPLAY_TIME = true;
    // ===================================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cheack);

        Intent intent = getIntent();

        // 1. Main_Activityから渡された時間を取得
        int standardHour = intent.getIntExtra(Main_Activity.STANDARD_HOUR_DATA, 0);
        int standardMin = intent.getIntExtra(Main_Activity.STANDARD_MIN_DATA, 0);
        int standardSec = intent.getIntExtra("standard_sec", 0);
        int fakeHour = intent.getIntExtra(Main_Activity.FAKE_HOUR_DATA, 0);
        int fakeMin = intent.getIntExtra(Main_Activity.FAKE_MIN_DATA, 0);
        int fakeSec = intent.getIntExtra("fake_sec", 0);

        Log.d(TAG, "取得した規定時間: " + standardHour + ":" + standardMin);

        // 2. アラーム停止画面に「表示する」時刻を計算
        int displayHour, displayMin, displaySec;

        if (USE_RANDOM_DISPLAY_TIME) {

            Log.d(TAG, "機能1のロジックを実行します。");

            // --- 機能1：規定時間とフェイク時間の間の「ランダムな時間」を計算 ---
            Calendar standardCal = Calendar.getInstance();
            standardCal.set(Calendar.HOUR_OF_DAY, standardHour);
            standardCal.set(Calendar.MINUTE, standardMin);
            standardCal.set(Calendar.SECOND, standardSec);

            Calendar fakeCal = Calendar.getInstance();
            fakeCal.set(Calendar.HOUR_OF_DAY, fakeHour);
            fakeCal.set(Calendar.MINUTE, fakeMin);
            fakeCal.set(Calendar.SECOND, fakeSec);

            long startTime = Math.min(standardCal.getTimeInMillis(), fakeCal.getTimeInMillis());
            long endTime = Math.max(standardCal.getTimeInMillis(), fakeCal.getTimeInMillis());
            long randomTimeMillis = startTime + (long) (Math.random() * (endTime - startTime));

            Calendar displayTime = Calendar.getInstance();
            displayTime.setTimeInMillis(randomTimeMillis);

            displayHour = displayTime.get(Calendar.HOUR_OF_DAY);
            displayMin = displayTime.get(Calendar.MINUTE);
            displaySec = displayTime.get(Calendar.SECOND);

        } else {
            // --- 機能2 ---
            Log.d(TAG, "機能2のロジックを実行します。");
            Calendar displayTime = Calendar.getInstance();
            displayTime.set(Calendar.HOUR_OF_DAY, standardHour);
            displayTime.set(Calendar.MINUTE, standardMin);
            displayTime.set(Calendar.SECOND, standardSec);
            Log.d(TAG, "10分加算前の表示時間: " + displayTime.getTime().toString());

            displayTime.add(Calendar.MINUTE, 10); // 10分加算
            Log.d(TAG, "10分加算後の表示時間: " + displayTime.getTime().toString());

            displayHour = displayTime.get(Calendar.HOUR_OF_DAY);
            displayMin = displayTime.get(Calendar.MINUTE);
            displaySec = displayTime.get(Calendar.SECOND);
        }

        Log.d(TAG, "表示時間の計算完了。 setupAlarmを呼び出します。");

        // 3. アラームを「規定時間」にセットする
        setupAlarm(standardHour, standardMin, standardSec, displayHour, displayMin, displaySec);


        // 4. 画面の表示を更新
        TextView standardTimeDisplay = findViewById(R.id.standard_time_display);
        TextView fakeTimeDisplay = findViewById(R.id.fake_time_display);
        displayTime(standardTimeDisplay, standardHour, standardMin, standardSec);
        displayTime(fakeTimeDisplay, fakeHour, fakeMin, fakeSec); // フェイク時間も参考として表示

        // 5. リセットボタンの設定
        Button btnReset = findViewById(R.id.reset);
        btnReset.setOnClickListener(v -> {
            cancelAlarm();
            finish();
        });

        // 6. デバッグログの出力
        logDebugInfo(standardHour, standardMin, standardSec, displayHour, displayMin, displaySec);
    }

    /**
     * アラームを設定するメソッド
     * @param alarmHour   実際に鳴らす時間（時）
     * @param alarmMin    実際に鳴らす時間（分）
     * @param alarmSec    実際に鳴らす時間（秒）
     * @param displayHour 停止画面に表示する時間（時）
     * @param displayMin  停止画面に表示する時間（分）
     * @param displaySec  停止画面に表示する時間（秒）
     */
    private void setupAlarm(int alarmHour, int alarmMin, int alarmSec, int displayHour, int displayMin, int displaySec) {
        Intent bootIntent = new Intent(this, Alarm_Receiver.class);
        // 停止画面に表示するための時間をIntentに詰める
        bootIntent.putExtra("displayHour", displayHour);
        bootIntent.putExtra("displayMin", displayMin);
        bootIntent.putExtra("displaySec", displaySec);

        PendingIntent alarmIntent = PendingIntent.getBroadcast(
                this, ALARM_REQUEST_CODE, bootIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        // ★★★ nullチェックを追加 ★★★
        if (alarm == null) {
            Log.e(TAG, "AlarmManagerの取得に失敗しました。");
            return; // ここで処理を終了
        }

        // 実際に鳴らす時刻（規定時間）を設定
        Calendar alarmTime = Calendar.getInstance();
        alarmTime.set(Calendar.HOUR_OF_DAY, alarmHour);
        alarmTime.set(Calendar.MINUTE, alarmMin);
        alarmTime.set(Calendar.SECOND, alarmSec);
        alarmTime.set(Calendar.MILLISECOND, 0);

        // 過去の時刻の場合は翌日にする
        if (alarmTime.getTimeInMillis() <= System.currentTimeMillis()) {
            alarmTime.add(Calendar.DAY_OF_YEAR, 1);
        }

        // アラームをセット
        // Androidバージョンに応じたアラーム設定
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // API 23以上
            alarm.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmTime.getTimeInMillis(), alarmIntent);
            Log.d(TAG, "setExactAndAllowWhileIdleを使用しました。");
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // API 19以上
            alarm.setExact(AlarmManager.RTC_WAKEUP, alarmTime.getTimeInMillis(), alarmIntent);
            Log.d(TAG, "setExactを使用しました。");
        } else {
            // それより古いバージョン
            alarm.set(AlarmManager.RTC_WAKEUP, alarmTime.getTimeInMillis(), alarmIntent);
            Log.d(TAG, "setを使用しました。");
        }
    }

    /**
     * 設定されたアラームをキャンセルする
     */
    private void cancelAlarm() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, Alarm_Receiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, ALARM_REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
            Log.d(TAG, "アラームをキャンセルしました。");
        }
    }

    /**
     * 時間をTextViewに表示する共通メソッド
     */
    @SuppressLint("SetTextI18n")
    private void displayTime(TextView textView, int hour, int min, int sec) {
        textView.setText(String.format(Locale.JAPAN, "%02d:%02d:%02d", hour, min, sec));
    }

    /**
     * デバッグ情報をLogcatに出力する
     */
    private void logDebugInfo(int alarmH, int alarmM, int alarmS, int displayH, int displayM, int displayS) {
        Log.d(TAG, "--- アラーム設定情報 ---");
        Log.d(TAG, "実際に鳴る時刻: " + String.format(Locale.JAPAN, "%02d:%02d:%02d", alarmH, alarmM, alarmS));
        Log.d(TAG, "画面に表示される時刻: " + String.format(Locale.JAPAN, "%02d:%02d:%02d", displayH, displayM, displayS));
        Log.d(TAG, "使用した機能: " + (USE_RANDOM_DISPLAY_TIME ? "機能1 (ランダム表示)" : "機能2 (10分後表示)"));
        Log.d(TAG, "----------------------");
    }
}