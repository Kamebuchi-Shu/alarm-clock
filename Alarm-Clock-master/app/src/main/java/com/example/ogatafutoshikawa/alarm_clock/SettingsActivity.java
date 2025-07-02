package com.example.ogatafutoshikawa.alarm_clock;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class SettingsActivity extends Activity {

    // SharedPreferencesのキー
    public static final String PREFS_NAME = "FakeTimeSettings";
    public static final String KEY_WAKE_HOUR = "wake_hour";
    public static final String KEY_WAKE_MINUTE = "wake_minute";
    public static final String KEY_LATE_HOUR = "late_hour";
    public static final String KEY_LATE_MINUTE = "late_minute";
    public static final String KEY_SNOOZE = "snooze_minute";

    // レイアウトの部品
    private EditText editWakeUpHour, editWakeUpMinute;
    private EditText editLateHour, editLateMinute;
    private EditText editSnoozeMinute;
    private Button buttonSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // レイアウトの部品をIDで取得
        editWakeUpHour = (EditText) findViewById(R.id.edit_wake_up_hour);
        editWakeUpMinute = (EditText) findViewById(R.id.edit_wake_up_minute);
        editLateHour = (EditText) findViewById(R.id.edit_late_hour);
        editLateMinute = (EditText) findViewById(R.id.edit_late_minute);
        editSnoozeMinute = (EditText) findViewById(R.id.edit_snooze_minute);
        buttonSave = (Button) findViewById(R.id.button_save);

        // 保存ボタンのクリックイベントを設定
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveSettings();
            }
        });

        // 保存されている設定値を読み込んで表示
        loadSettings();
    }

    /**
     * 入力された設定をSharedPreferencesに保存する
     */
    private void saveSettings() {
        // SharedPreferences のインスタンスを取得
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        try {
            // EditTextからテキストを取得し、整数に変換して保存
            editor.putInt(KEY_WAKE_HOUR, Integer.parseInt(editWakeUpHour.getText().toString()));
            editor.putInt(KEY_WAKE_MINUTE, Integer.parseInt(editWakeUpMinute.getText().toString()));
            editor.putInt(KEY_LATE_HOUR, Integer.parseInt(editLateHour.getText().toString()));
            editor.putInt(KEY_LATE_MINUTE, Integer.parseInt(editLateMinute.getText().toString()));
            editor.putInt(KEY_SNOOZE, Integer.parseInt(editSnoozeMinute.getText().toString()));

            // 保存を確定
            editor.apply();

            // 保存完了をユーザーに通知
            Toast.makeText(this, "設定を保存しました", Toast.LENGTH_SHORT).show();

            // 設定画面を閉じる
            finish();

        } catch (NumberFormatException e) {
            // 数字以外の文字が入力された場合のエラー処理
            Toast.makeText(this, "すべての項目に数字を入力してください", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * SharedPreferencesから設定を読み込んでEditTextに表示する
     */
    private void loadSettings() {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // キーを指定してデータを読み込む（もしデータがなければデフォルト値 -1 を使う）
        int wakeHour = settings.getInt(KEY_WAKE_HOUR, -1);
        int wakeMinute = settings.getInt(KEY_WAKE_MINUTE, -1);
        int lateHour = settings.getInt(KEY_LATE_HOUR, -1);
        int lateMinute = settings.getInt(KEY_LATE_MINUTE, -1);
        int snooze = settings.getInt(KEY_SNOOZE, -1);

        // 読み込んだ値が有効な場合のみ、EditTextにセットする
        if (wakeHour != -1) editWakeUpHour.setText(String.valueOf(wakeHour));
        if (wakeMinute != -1) editWakeUpMinute.setText(String.valueOf(wakeMinute));
        if (lateHour != -1) editLateHour.setText(String.valueOf(lateHour));
        if (lateMinute != -1) editLateMinute.setText(String.valueOf(lateMinute));
        if (snooze != -1) editSnoozeMinute.setText(String.valueOf(snooze));
    }
}