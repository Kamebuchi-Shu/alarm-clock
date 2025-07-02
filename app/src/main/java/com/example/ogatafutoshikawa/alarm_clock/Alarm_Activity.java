package com.example.ogatafutoshikawa.alarm_clock;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TimePicker;
import android.widget.Toast;
import java.util.Calendar;
import android.widget.RelativeLayout;

public class Alarm_Activity extends AppCompatActivity implements View.OnClickListener {

    private InputMethodManager mInputMethodManager;
    private RelativeLayout mLayout;
    private int hour;
    private int min;

    @Override
    public void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        setContentView(R.layout.alarm);

        mLayout = findViewById(R.id.mainLayout);
        mInputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        Button btnSet = findViewById(R.id.set);
        Button btnCancel = findViewById(R.id.cancel);
        TimePicker tPicker = findViewById(R.id.timePicker);

        btnSet.setOnClickListener(this);
        btnCancel.setOnClickListener(this);
        tPicker.setIs24HourView(true);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mInputMethodManager.hideSoftInputFromWindow(mLayout.getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);
        mLayout.requestFocus();
        return false;
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        int id = v.getId();

        TimePicker tPicker = findViewById(R.id.timePicker);
        hour = tPicker.getCurrentHour();
        min = tPicker.getCurrentMinute();

        if (id == R.id.set) {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(System.currentTimeMillis());
            cal.set(Calendar.HOUR_OF_DAY, hour);
            cal.set(Calendar.MINUTE, min);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);

            if (cal.getTimeInMillis() < System.currentTimeMillis()) {
                cal.add(Calendar.DAY_OF_YEAR, 1);
            }

            // 音声設定を保存
            saveAlarmWithSound();

            bundle.putInt("hour", hour);
            bundle.putInt("min", min);
            intent.putExtras(bundle);
            setResult(RESULT_OK, intent);
            finish();
        } else if (id == R.id.cancel) {
            finish();
        }
    }

    // 音声とアラーム設定を保存
    private void saveAlarmWithSound() {
        AudioItem selectedAudio = AudioSelectActivity.getSelectedAudioItem(this);
        SharedPreferences prefs = getSharedPreferences("alarm_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        // アラーム時間を保存
        editor.putInt("alarm_hour", hour);
        editor.putInt("alarm_minute", min);

        if (selectedAudio != null) {
            // 選択された音声を保存
            editor.putString("alarm_sound", selectedAudio.getPath());
            editor.putString("alarm_sound_name", selectedAudio.getName());
            Toast.makeText(this,
                    "アラームと音声設定を保存しました: " + selectedAudio.getName(),
                    Toast.LENGTH_SHORT).show();
        } else {
            // デフォルト音声（リソース）の情報を保存
            editor.putString("alarm_sound", "default");
            editor.putString("alarm_sound_name", "デフォルト音声");
            Toast.makeText(this,
                    "デフォルト音声を使用します",
                    Toast.LENGTH_SHORT).show();
        }
        editor.apply();
    }
}