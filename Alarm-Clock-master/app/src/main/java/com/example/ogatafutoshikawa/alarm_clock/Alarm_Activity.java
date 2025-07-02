package com.example.ogatafutoshikawa.alarm_clock;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class Alarm_Activity extends AppCompatActivity {

    // SharedPreferencesで設定を保存するためのキー
    public static final String PREFS_NAME = "FakeTimePrefs";
    public static final String KEY_WAKE_HOUR = "wake_hour";
    public static final String KEY_WAKE_MINUTE = "wake_minute";
    public static final String KEY_LATE_HOUR = "late_hour";
    public static final String KEY_LATE_MINUTE = "late_minute";
    public static final String KEY_SNOOZE = "snooze";

    private EditText wakeUpHourEditText;
    private EditText wakeUpMinuteEditText;
    private EditText lateHourEditText;
    private EditText lateMinuteEditText;
    private EditText snoozeMinuteEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // このActivityが使用するレイアウトファイルを "activity_settings.xml" に変更
        setContentView(R.layout.activity_settings);

        // レイアウトファイル上のUIコンポーネントを紐付け
        wakeUpHourEditText = findViewById(R.id.edit_wake_up_hour);
        wakeUpMinuteEditText = findViewById(R.id.edit_wake_up_minute);
        lateHourEditText = findViewById(R.id.edit_late_hour);
        lateMinuteEditText = findViewById(R.id.edit_late_minute);
        snoozeMinuteEditText = findViewById(R.id.edit_snooze_minute);

        // 保存ボタンの処理
        Button saveButton = findViewById(R.id.button_save);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveSettings();
            }
        });

        // 既存の設定値を読み込んで表示
        loadSettings();
    }

    /**
     * SharedPreferencesに設定値を保存し、Main_Activityに起床希望時間を返す
     */
    private void saveSettings() {
        // 入力値を取得
        String wakeHourStr = wakeUpHourEditText.getText().toString();
        String wakeMinuteStr = wakeUpMinuteEditText.getText().toString();
        String lateHourStr = lateHourEditText.getText().toString();
        String lateMinuteStr = lateMinuteEditText.getText().toString();
        String snoozeStr = snoozeMinuteEditText.getText().toString();

        // 未入力チェック
        if (TextUtils.isEmpty(wakeHourStr) || TextUtils.isEmpty(wakeMinuteStr) ||
                TextUtils.isEmpty(lateHourStr) || TextUtils.isEmpty(lateMinuteStr) ||
                TextUtils.isEmpty(snoozeStr)) {
            Toast.makeText(this, "全ての項目を入力してください", Toast.LENGTH_SHORT).show();
            return;
        }

        // 数値に変換
        int wakeHour = Integer.parseInt(wakeHourStr);
        int wakeMinute = Integer.parseInt(wakeMinuteStr);
        int lateHour = Integer.parseInt(lateHourStr);
        int lateMinute = Integer.parseInt(lateMinuteStr);
        int snooze = Integer.parseInt(snoozeStr);

        // SharedPreferencesに保存
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(KEY_WAKE_HOUR, wakeHour);
        editor.putInt(KEY_WAKE_MINUTE, wakeMinute);
        editor.putInt(KEY_LATE_HOUR, lateHour);
        editor.putInt(KEY_LATE_MINUTE, lateMinute);
        editor.putInt(KEY_SNOOZE, snooze);
        editor.apply();

        Toast.makeText(this, "設定を保存しました", Toast.LENGTH_SHORT).show();

        // Main_Activityに結果（起床希望時間）を返す
        Intent resultIntent = new Intent();
        resultIntent.putExtra("hour", wakeHour);
        resultIntent.putExtra("min", wakeMinute);
        setResult(RESULT_OK, resultIntent);

        // この画面を閉じる
        finish();
    }

    /**
     * 既存の設定をSharedPreferencesから読み込んで表示する
     */
    private void loadSettings() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        // 保存されている値がなければデフォルト値（例：6時30分など）を読み込む
        wakeUpHourEditText.setText(String.valueOf(prefs.getInt(KEY_WAKE_HOUR, 6)));
        wakeUpMinuteEditText.setText(String.valueOf(prefs.getInt(KEY_WAKE_MINUTE, 30)));
        lateHourEditText.setText(String.valueOf(prefs.getInt(KEY_LATE_HOUR, 7)));
        lateMinuteEditText.setText(String.valueOf(prefs.getInt(KEY_LATE_MINUTE, 0)));
        snoozeMinuteEditText.setText(String.valueOf(prefs.getInt(KEY_SNOOZE, 20)));
    }
}