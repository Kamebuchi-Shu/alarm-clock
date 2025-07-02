package com.example.ogatafutoshikawa.alarm_clock;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import com.example.ogatafutoshikawa.alarm_clock.data.FakeTimeSettings;
import com.example.ogatafutoshikawa.alarm_clock.util.FakeTimeCalculator;


/**
 * アラーム鳴動時に表示される画面。
 * フェイクタイム表示機能と連携させた完成版。
 */
public class Alarm_Stop extends Activity implements View.OnClickListener {

    private TextView timeTextView;
    private FakeTimeSettings fakeTimeSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // このActivityが使用するレイアウトファイルを "stop_alarm.xml" に設定
        setContentView(R.layout.stop_alarm);

        Button btnStop = (Button) findViewById(R.id.stop);
        btnStop.setOnClickListener(this);

        // レイアウトに追加した時刻表示用のTextViewを紐付け
        timeTextView = (TextView) findViewById(R.id.text_view_time);

        // 1. 保存されているフェイクタイム設定を読み込む
        loadFakeTimeSettings();

        // 2. 時刻を表示する（この瞬間に本物か偽物かが決まる）
        displayTime();

        // 3. アラーム音を鳴らすサービスを起動
        Intent serviceIntent = new Intent(getApplicationContext(), Alarm_Service.class);
        startService(serviceIntent);
    }

    /**
     * SharedPreferencesからフェイクタイム設定を読み込む
     */
    private void loadFakeTimeSettings() {
        SharedPreferences prefs = getSharedPreferences(Alarm_Activity.PREFS_NAME, Context.MODE_PRIVATE);
        // 保存された値を読み込む（なければデフォルト値を使う）
        int wakeHour = prefs.getInt(Alarm_Activity.KEY_WAKE_HOUR, 6);
        int wakeMinute = prefs.getInt(Alarm_Activity.KEY_WAKE_MINUTE, 0);
        int lateHour = prefs.getInt(Alarm_Activity.KEY_LATE_HOUR, 6);
        int lateMinute = prefs.getInt(Alarm_Activity.KEY_LATE_MINUTE, 30);
        int snooze = prefs.getInt(Alarm_Activity.KEY_SNOOZE, 20);

        // 読み込んだ値でFakeTimeSettingsオブジェクトを生成
        this.fakeTimeSettings = new FakeTimeSettings(wakeHour, wakeMinute, lateHour, lateMinute, snooze, true);
    }

    /**
     * 計算された時刻（本物or偽物）を画面に表示する
     */
    private void displayTime() {
        if (this.fakeTimeSettings == null) {
            return;
        }

        // 実際の現在時刻を取得
        Calendar realCurrentTime = Calendar.getInstance();

        // FakeTimeCalculatorを使って、表示すべき時刻を計算
        Calendar fakeTimeToDisplay = FakeTimeCalculator.calculateFakeTime(realCurrentTime, fakeTimeSettings);

        // 計算結果を "HH:mm" 形式の文字列に変換
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        String timeString = timeFormat.format(fakeTimeToDisplay.getTime());

        // 画面のTextViewに文字列を設定
        timeTextView.setText(timeString);
    }

    @Override
    public void onClick(View v) {
        // アラーム音を鳴らしているサービスを停止する
        Intent intent = new Intent(getApplicationContext(), Alarm_Service.class);
        stopService(intent);

        // この画面を閉じる
        finish();
    }
}