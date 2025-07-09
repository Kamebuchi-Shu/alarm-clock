package com.example.ogatafutoshikawa.alarm_clock;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DebugActivity extends AppCompatActivity {

    private TextView currentTimeDisplay;
    private TextView standardTimeDisplay;
    private TextView fakeTimeDisplay;
    private TextView standardAlarmStatusDisplay;
    private TextView fakeAlarmStatusDisplay;
    private TextView standardCountdownDisplay;
    private TextView fakeCountdownDisplay;
    private TextView forceModeDisplay;
    private TextView customMessageDisplay;
    private TextView audioNameDisplay;

    private Handler handler = new Handler();
    private Runnable updateRunnable;

    // 保存された設定値
    private int standardHour, standardMin, standardSec;
    private int fakeHour, fakeMin, fakeSec;
    private boolean forceModeEnabled;
    private String customMessage;
    private String selectedAudioName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug);

        // UI要素の初期化
        initializeViews();

        // 保存されたデータを読み込み
        loadSavedData();

        // 戻るボタンの設定
        Button backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());

        // 手動アラームテストボタンの設定
        Button testStandardButton = findViewById(R.id.test_standard_alarm);
        testStandardButton.setOnClickListener(v -> testStandardAlarm());

        Button testFakeButton = findViewById(R.id.test_fake_alarm);
        testFakeButton.setOnClickListener(v -> testFakeAlarm());

        // 定期更新の開始
        startPeriodicUpdate();
    }

    private void initializeViews() {
        currentTimeDisplay = findViewById(R.id.current_time_display);
        standardTimeDisplay = findViewById(R.id.standard_time_debug);
        fakeTimeDisplay = findViewById(R.id.fake_time_debug);
        standardAlarmStatusDisplay = findViewById(R.id.standard_alarm_status);
        fakeAlarmStatusDisplay = findViewById(R.id.fake_alarm_status);
        standardCountdownDisplay = findViewById(R.id.standard_countdown);
        fakeCountdownDisplay = findViewById(R.id.fake_countdown);
        forceModeDisplay = findViewById(R.id.force_mode_debug);
        customMessageDisplay = findViewById(R.id.custom_message_debug);
        audioNameDisplay = findViewById(R.id.audio_name_debug);
    }

    private void loadSavedData() {
        SharedPreferences prefs = getSharedPreferences("alarm_prefs", MODE_PRIVATE);
        
        // 時間設定を読み込み
        standardHour = prefs.getInt(Main_Activity.STANDARD_HOUR_DATA, 100);
        standardMin = prefs.getInt(Main_Activity.STANDARD_MIN_DATA, 100);
        standardSec = prefs.getInt("standard_sec", 100);
        fakeHour = prefs.getInt(Main_Activity.FAKE_HOUR_DATA, 100);
        fakeMin = prefs.getInt(Main_Activity.FAKE_MIN_DATA, 100);
        fakeSec = prefs.getInt("fake_sec", 100);
        forceModeEnabled = prefs.getBoolean(Main_Activity.FORCE_MODE_DATA, false);
        selectedAudioName = prefs.getString("alarm_sound_name", "デフォルト音声");
        
        // カスタムメッセージを読み込み
        SharedPreferences alarmPrefs = getSharedPreferences("AlarmPrefs", MODE_PRIVATE);
        customMessage = alarmPrefs.getString("customMessage", "未設定");

        updateDisplays();
    }

    @SuppressLint("SetTextI18n")
    private void updateDisplays() {
        // 現在時刻の表示
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.JAPAN);
        currentTimeDisplay.setText("現在時刻: " + sdf.format(new Date()));

        // 設定時間の表示
        fakeTimeDisplay.setText("フェイクタイム: " + formatTime(fakeHour, fakeMin, fakeSec));
        standardTimeDisplay.setText("規定時間: " + formatTime(standardHour, standardMin, standardSec));
       

        // 強制モードの表示
        forceModeDisplay.setText("強制モード: " + (forceModeEnabled ? "ON" : "OFF"));

        // カスタムメッセージの表示
        customMessageDisplay.setText("カスタムメッセージ: " + customMessage);

        // 音声名の表示
        audioNameDisplay.setText("選択音声: " + selectedAudioName);

        // アラーム状態とカウントダウンの表示
        updateAlarmStatus();
        updateCountdowns();
    }

    private String formatTime(int hour, int min, int sec) {
        if (hour >= 100 || min >= 100 || sec >= 100) {
            return "未設定";
        }
        return String.format(Locale.JAPAN, "%02d:%02d:%02d", hour, min, sec);
    }

    @SuppressLint("SetTextI18n")
    private void updateAlarmStatus() {
        // アラームが設定されているかどうかをチェック
        boolean standardAlarmSet = isAlarmSet(standardHour * 1000 + standardMin);
        boolean fakeAlarmSet = isAlarmSet(fakeHour * 1000 + fakeMin + 10000);

        standardAlarmStatusDisplay.setText("規定時間アラーム: " + (standardAlarmSet ? "設定済み" : "未設定"));
        fakeAlarmStatusDisplay.setText("フェイクタイムアラーム: " + (fakeAlarmSet ? "設定済み" : "未設定"));
    }

    private boolean isAlarmSet(int requestCode) {
        Intent intent = new Intent(this, Alarm_Receiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this, requestCode, intent, PendingIntent.FLAG_NO_CREATE);
        return pendingIntent != null;
    }

    @SuppressLint("SetTextI18n")
    private void updateCountdowns() {
        Calendar now = Calendar.getInstance();
        
        // 規定時間までのカウントダウン
        if (standardHour < 100 && standardMin < 100 && standardSec < 100) {
            long timeUntilStandard = calculateTimeUntil(now, standardHour, standardMin, standardSec);
            standardCountdownDisplay.setText("規定時間まで: " + formatDuration(timeUntilStandard));
        } else {
            standardCountdownDisplay.setText("規定時間まで: 未設定");
        }

        // フェイクタイムまでのカウントダウン
        if (fakeHour < 100 && fakeMin < 100 && fakeSec < 100) {
            long timeUntilFake = calculateTimeUntil(now, fakeHour, fakeMin, fakeSec);
            fakeCountdownDisplay.setText("フェイクタイムまで: " + formatDuration(timeUntilFake));
        } else {
            fakeCountdownDisplay.setText("フェイクタイムまで: 未設定");
        }
    }

    private long calculateTimeUntil(Calendar now, int targetHour, int targetMin, int targetSec) {
        Calendar target = Calendar.getInstance();
        target.set(Calendar.HOUR_OF_DAY, targetHour);
        target.set(Calendar.MINUTE, targetMin);
        target.set(Calendar.SECOND, targetSec);
        target.set(Calendar.MILLISECOND, 0);

        // 過去の時間の場合は翌日に設定
        if (target.getTimeInMillis() <= now.getTimeInMillis()) {
            target.add(Calendar.DAY_OF_MONTH, 1);
        }

        return target.getTimeInMillis() - now.getTimeInMillis();
    }

    private String formatDuration(long milliseconds) {
        long seconds = milliseconds / 1000;
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        seconds = seconds % 60;

        return String.format(Locale.JAPAN, "%02d時間%02d分%02d秒", hours, minutes, seconds);
    }

    private void testStandardAlarm() {
        // 規定時間のアラームを手動で発火（テスト用）
        Intent intent = new Intent(this, Alarm_Receiver.class);
        intent.putExtra("notificationId", standardHour * 1000 + standardMin);
        intent.putExtra("alarmType", "standard");
        intent.putExtra("standardHour", standardHour);
        intent.putExtra("standardMin", standardMin);
        intent.putExtra("fakeHour", fakeHour);
        intent.putExtra("fakeMin", fakeMin);
        intent.putExtra("forceModeEnabled", forceModeEnabled);
        sendBroadcast(intent);
    }

    private void testFakeAlarm() {
        // フェイクタイムのアラームを手動で発火（テスト用）
        Intent intent = new Intent(this, Alarm_Receiver.class);
        intent.putExtra("notificationId", fakeHour * 1000 + fakeMin + 10000);
        intent.putExtra("alarmType", "fake");
        intent.putExtra("standardHour", standardHour);
        intent.putExtra("standardMin", standardMin);
        intent.putExtra("fakeHour", fakeHour);
        intent.putExtra("fakeMin", fakeMin);
        intent.putExtra("forceModeEnabled", forceModeEnabled);
        sendBroadcast(intent);
    }

    private void startPeriodicUpdate() {
        updateRunnable = new Runnable() {
            @Override
            public void run() {
                updateDisplays();
                handler.postDelayed(this, 1000); // 1秒ごとに更新
            }
        };
        handler.post(updateRunnable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null && updateRunnable != null) {
            handler.removeCallbacks(updateRunnable);
        }
    }
} 