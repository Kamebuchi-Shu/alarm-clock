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

        // --- 1. Main_Activityから渡された時間を取得 ---
        int standardHour = intent.getIntExtra(Main_Activity.STANDARD_HOUR_DATA, 0);
        int standardMin = intent.getIntExtra(Main_Activity.STANDARD_MIN_DATA, 0);
        int standardSec = intent.getIntExtra("standard_sec", 0);
        int fakeHour = intent.getIntExtra(Main_Activity.FAKE_HOUR_DATA, 0);
        int fakeMin = intent.getIntExtra(Main_Activity.FAKE_MIN_DATA, 0);
        int fakeSec = intent.getIntExtra("fake_sec", 0);

        // --- 2. 規定時間とフェイク時間の間で、ランダムなアラーム時刻を計算 ---
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

        Calendar alarmTime = Calendar.getInstance();
        alarmTime.setTimeInMillis(randomTimeMillis);

        // 過去の時刻になった場合は翌日に設定
        if (alarmTime.getTimeInMillis() <= System.currentTimeMillis()) {
            alarmTime.add(Calendar.DAY_OF_YEAR, 1);
        }


        // --- 3. アラームを「1つだけ」セットする ---
        Intent bootIntent = new Intent(Check_Activity.this, Alarm_Receiver.class);
        // 表示用には「規定時刻」の情報を渡す
        bootIntent.putExtra("displayHour", standardHour);
        bootIntent.putExtra("displayMin", standardMin);
        bootIntent.putExtra("displaySec", standardSec);

        int requestCode = 12345; // アラームは1つなので、リクエストコードは固定でOK
        PendingIntent alarmIntent = PendingIntent.getBroadcast(
                Check_Activity.this,
                requestCode,
                bootIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        // Androidバージョンに応じてアラームを設定
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarm.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmTime.getTimeInMillis(), alarmIntent);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarm.setExact(AlarmManager.RTC_WAKEUP, alarmTime.getTimeInMillis(), alarmIntent);
        } else {
            alarm.set(AlarmManager.RTC_WAKEUP, alarmTime.getTimeInMillis(), alarmIntent);
        }


        // --- 4. 画面の表示を更新 ---
        // UI要素を取得
        TextView standardTimeDisplay = findViewById(R.id.standard_time_display);
        TextView fakeTimeDisplay = findViewById(R.id.fake_time_display);

        // 時間表示を更新（★ここで規定時刻とフェイク時刻が正しく表示されます）
        displayTime(standardTimeDisplay, standardHour, standardMin, standardSec);
        displayTime(fakeTimeDisplay, fakeHour, fakeMin, fakeSec);

        // 不要になった確率情報などは非表示にするか、文言を変更するとより分かりやすくなります
        findViewById(R.id.force_mode_status).setVisibility(View.GONE);
        findViewById(R.id.probability_info_display).setVisibility(View.GONE);


        // --- 5. リセットボタンの設定 ---
        Button btnReset = findViewById(R.id.reset);
        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 設定した1つのアラームをキャンセル
                alarm.cancel(alarmIntent);
                finish();
            }
        });

        // --- デバッグ用のログ出力 ---
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.JAPAN);
        Log.d("AlarmDebug", "--- アラーム設定完了 ---");
        Log.d("AlarmDebug", "規定時刻: " + standardHour + ":" + standardMin + ":" + standardSec);
        Log.d("AlarmDebug", "フェイク時刻: " + fakeHour + ":" + fakeMin + ":" + fakeSec);
        Log.d("AlarmDebug", "セットされたアラーム時刻: " + sdf.format(alarmTime.getTime()));
        Log.d("AlarmDebug", "--------------------------");
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
