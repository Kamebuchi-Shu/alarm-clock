package com.example.ogatafutoshikawa.alarm_clock;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Switch;
import android.widget.EditText;
import android.widget.CheckBox;
import android.widget.LinearLayout;

public class AlarmEditActivity extends AppCompatActivity implements View.OnClickListener {

    // データ受け渡しの際に使うkey
    public static final int REQUEST_AUDIO = 3;
    public static final int REQUEST_STANDARD_TIME = 4;
    public static final int REQUEST_FAKE_TIME = 5;
    
    // 新機能用データキー
    public static final String STANDARD_HOUR_DATA = "standard_hour";
    public static final String STANDARD_MIN_DATA = "standard_min";
    public static final String FAKE_HOUR_DATA = "fake_hour";
    public static final String FAKE_MIN_DATA = "fake_min";
    public static final String FORCE_MODE_DATA = "force_mode";

    // 変数宣言
    private String selectedAudioName = "デフォルト音声";
    private int alarmId;
    private int position;
    private int standardHour = 7;
    private int standardMin = 0;
    private int standardSec = 0;
    private int fakeHour = 6;
    private int fakeMin = 45;
    private int fakeSec = 0;
    private boolean forceModeEnabled = false;
    private String customMessage = "";
    private boolean[] days = new boolean[7];
    private boolean alarmEnabled = true;  // デフォルトで有効に変更
    private boolean isNewAlarm = false;

    // 曜日用チェックボックス
    private CheckBox[] dayCheckboxes = new CheckBox[7];
    private String[] dayNames = {"日", "月", "火", "水", "木", "金", "土"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.set);

            // Intent からデータを取得
            loadIntentData();

            // 各ボタンのオブジェクト化
            Button btnStandardTime = findViewById(R.id.standard_time);
            Button btnFakeTime = findViewById(R.id.fake_time);
            Switch forceModeSwitch = findViewById(R.id.force_mode_switch);
            Button btnAudio = findViewById(R.id.audio_select);
            Button btnSave = findViewById(R.id.cheack); // 保存ボタンとして使用
            EditText customMessageEditText = findViewById(R.id.customMessageEditText);

            // nullチェック
            if (btnStandardTime == null || btnFakeTime == null || forceModeSwitch == null || 
                btnAudio == null || btnSave == null || customMessageEditText == null) {
                Log.e("AlarmEditActivity", "必要なViewが見つかりません");
                finish();
                return;
            }

            // 各ボタンのクリックリスナー設定
            btnStandardTime.setOnClickListener(this);
            btnFakeTime.setOnClickListener(this);
            btnAudio.setOnClickListener(this);
            btnSave.setOnClickListener(this);

            // 保存ボタンのテキストを変更
            btnSave.setText(isNewAlarm ? "追加" : "保存");

            // 強制モードスイッチのリスナー設定
            forceModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                forceModeEnabled = isChecked;
                String text = isChecked ? "強制モード: ON" : "強制モード: OFF";
                forceModeSwitch.setText(text);
            });

            // カスタムメッセージの設定
            customMessageEditText.setText(customMessage);

            // 曜日選択チェックボックスを動的に作成
            createDayCheckboxes();

            // 初期値を表示
            updateDisplay();
            
            Log.d("AlarmEditActivity", "onCreate完了");
            
        } catch (Exception e) {
            Log.e("AlarmEditActivity", "onCreate エラー", e);
            finish();
        }
    }

    private void loadIntentData() {
        Intent intent = getIntent();
        alarmId = intent.getIntExtra("alarm_id", 1);
        position = intent.getIntExtra("position", 0);
        isNewAlarm = intent.getBooleanExtra("is_new", false);
        standardHour = intent.getIntExtra("standard_hour", 7);
        standardMin = intent.getIntExtra("standard_min", 0);
        standardSec = intent.getIntExtra("standard_sec", 0);
        fakeHour = intent.getIntExtra("fake_hour", 6);
        fakeMin = intent.getIntExtra("fake_min", 45);
        fakeSec = intent.getIntExtra("fake_sec", 0);
        forceModeEnabled = intent.getBooleanExtra("force_mode", false);
        selectedAudioName = intent.getStringExtra("audio_name");
        customMessage = intent.getStringExtra("custom_message");
        
        // 新規アラーム追加時はデフォルトでON、編集時は既存の値を使用
        if (isNewAlarm) {
            alarmEnabled = true;
        } else {
            alarmEnabled = intent.getBooleanExtra("enabled", false);
        }
        
        if (selectedAudioName == null) selectedAudioName = "デフォルト音声";
        if (customMessage == null) customMessage = "";
        
        // 新規アラーム追加時はカスタムメッセージを空文字列に設定
        if (isNewAlarm) {
            customMessage = "";
        }
        
        boolean[] intentDays = intent.getBooleanArrayExtra("days");
        if (intentDays != null) {
            days = intentDays;
        }
    }

    private void createDayCheckboxes() {
        try {
            // ConstraintLayoutを取得
            android.support.constraint.ConstraintLayout mainLayout = findViewById(R.id.mainLayout);
            if (mainLayout == null) {
                Log.e("AlarmEditActivity", "mainLayoutが見つかりません");
                return;
            }
            
            // 曜日選択用のLinearLayoutを作成
            LinearLayout dayLayout = new LinearLayout(this);
            dayLayout.setId(View.generateViewId());
            dayLayout.setOrientation(LinearLayout.HORIZONTAL);
            dayLayout.setPadding(8, 8, 8, 8);
            dayLayout.setGravity(Gravity.CENTER);
            
            // 曜日チェックボックスを追加（ラベルなしでコンパクトに）
            for (int i = 0; i < 7; i++) {
                CheckBox checkbox = new CheckBox(this);
                checkbox.setText(dayNames[i]);
                checkbox.setChecked(days[i]);
                checkbox.setTextSize(14);
                checkbox.setPadding(4, 4, 4, 4);
                
                // LinearLayout.LayoutParamsで間隔を調整
                LinearLayout.LayoutParams checkboxParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                );
                checkboxParams.setMargins(2, 0, 2, 0);
                checkbox.setLayoutParams(checkboxParams);
                
                dayCheckboxes[i] = checkbox;
                dayLayout.addView(checkbox);
            }
            
            // ConstraintLayoutに追加
            android.support.constraint.ConstraintLayout.LayoutParams params = 
                new android.support.constraint.ConstraintLayout.LayoutParams(
                    android.support.constraint.ConstraintLayout.LayoutParams.MATCH_PARENT,
                    android.support.constraint.ConstraintLayout.LayoutParams.WRAP_CONTENT
                );
            
            // days_placeholderの位置に配置
            params.topToTop = R.id.days_placeholder;
            params.bottomToBottom = R.id.days_placeholder;
            params.startToStart = android.support.constraint.ConstraintLayout.LayoutParams.PARENT_ID;
            params.endToEnd = android.support.constraint.ConstraintLayout.LayoutParams.PARENT_ID;
            params.leftMargin = 16;
            params.rightMargin = 16;
            
            mainLayout.addView(dayLayout, params);
            
            Log.d("AlarmEditActivity", "曜日選択チェックボックスを作成しました");
            
        } catch (Exception e) {
            Log.e("AlarmEditActivity", "曜日チェックボックス作成エラー", e);
        }
    }

    @SuppressLint("SetTextI18n")
    private void updateDisplay() {
        try {
            // 時間表示を更新
            changeStandardTime(standardHour, standardMin, standardSec);
            changeFakeTime(fakeHour, fakeMin, fakeSec);
            
            // 音声名を表示
            changeAudioName(selectedAudioName);
            
            // 強制モードスイッチの状態を設定
            Switch forceModeSwitch = findViewById(R.id.force_mode_switch);
            if (forceModeSwitch != null) {
                forceModeSwitch.setChecked(forceModeEnabled);
                String text = forceModeEnabled ? "強制モード: ON" : "強制モード: OFF";
                forceModeSwitch.setText(text);
            }
            
            // 曜日チェックボックスの状態を設定
            for (int i = 0; i < 7; i++) {
                if (dayCheckboxes[i] != null) {
                    dayCheckboxes[i].setChecked(days[i]);
                }
            }
            
            Log.d("AlarmEditActivity", "updateDisplay完了");
            
        } catch (Exception e) {
            Log.e("AlarmEditActivity", "updateDisplay エラー", e);
        }
    }

    @SuppressLint("SetTextI18n")
    public void changeStandardTime(int hour, int min, int sec) {
        try {
            Button btn = findViewById(R.id.standard_time);
            if (btn != null) {
                String hspace = hour < 10 ? "0" : "";
                String mspace = min < 10 ? "0" : "";
                String sspace = sec < 10 ? "0" : "";
                btn.setText(hspace + hour + ":" + mspace + min + ":" + sspace + sec);
            }
        } catch (Exception e) {
            Log.e("AlarmEditActivity", "changeStandardTime エラー", e);
        }
    }

    @SuppressLint("SetTextI18n")
    public void changeFakeTime(int hour, int min, int sec) {
        try {
            Button btn = findViewById(R.id.fake_time);
            if (btn != null) {
                String hspace = hour < 10 ? "0" : "";
                String mspace = min < 10 ? "0" : "";
                String sspace = sec < 10 ? "0" : "";
                btn.setText(hspace + hour + ":" + mspace + min + ":" + sspace + sec);
            }
        } catch (Exception e) {
            Log.e("AlarmEditActivity", "changeFakeTime エラー", e);
        }
    }

    private void changeAudioName(String name) {
        try {
            TextView tv = findViewById(R.id.audio_name);
            if (tv != null) {
                if (name != null && !name.isEmpty()) {
                    tv.setText(name);
                } else {
                    tv.setText("デフォルト音声");
                }
            }
        } catch (Exception e) {
            Log.e("AlarmEditActivity", "changeAudioName エラー", e);
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.standard_time) {
            Intent intent = new Intent(this, Alarm_Activity.class);
            intent.putExtra("time_type", "standard");
            startActivityForResult(intent, REQUEST_STANDARD_TIME);
        } else if (id == R.id.fake_time) {
            Intent intent = new Intent(this, Alarm_Activity.class);
            intent.putExtra("time_type", "fake");
            startActivityForResult(intent, REQUEST_FAKE_TIME);
        } else if (id == R.id.audio_select) {
            Intent intent = new Intent(this, AudioSelectActivity.class);
            startActivityForResult(intent, REQUEST_AUDIO);
        } else if (id == R.id.cheack) {
            // 保存処理
            saveAlarmData();
        }
    }

    private void saveAlarmData() {
        try {
            // カスタムメッセージを取得
            EditText customMessageEditText = findViewById(R.id.customMessageEditText);
            if (customMessageEditText != null) {
                customMessage = customMessageEditText.getText().toString();
            }
            
            // 曜日選択を取得
            for (int i = 0; i < 7; i++) {
                if (dayCheckboxes[i] != null) {
                    days[i] = dayCheckboxes[i].isChecked();
                }
            }
            
            // 結果をAlarmListActivityに返す
            Intent resultIntent = new Intent();
            resultIntent.putExtra("alarm_id", alarmId);
            resultIntent.putExtra("position", position);
            resultIntent.putExtra("is_new", isNewAlarm);
            resultIntent.putExtra("standard_hour", standardHour);
            resultIntent.putExtra("standard_min", standardMin);
            resultIntent.putExtra("standard_sec", standardSec);
            resultIntent.putExtra("fake_hour", fakeHour);
            resultIntent.putExtra("fake_min", fakeMin);
            resultIntent.putExtra("fake_sec", fakeSec);
            resultIntent.putExtra("days", days);
            resultIntent.putExtra("enabled", alarmEnabled);
            resultIntent.putExtra("audio_name", selectedAudioName);
            resultIntent.putExtra("custom_message", customMessage);
            resultIntent.putExtra("force_mode", forceModeEnabled);
            
            setResult(RESULT_OK, resultIntent);
            
            Log.d("AlarmEditActivity", "saveAlarmData完了");
            finish();
            
        } catch (Exception e) {
            Log.e("AlarmEditActivity", "saveAlarmData エラー", e);
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (resultCode != RESULT_OK) return;
        if (intent == null) return;

        if (requestCode == REQUEST_AUDIO) {
            if (intent.hasExtra(AudioSelectActivity.EXTRA_AUDIO_NAME)) {
                selectedAudioName = intent.getStringExtra(AudioSelectActivity.EXTRA_AUDIO_NAME);
                changeAudioName(selectedAudioName);
            }
            return;
        }

        Bundle bundle = intent.getExtras();
        if (bundle == null) return;

        switch (requestCode) {
            case REQUEST_STANDARD_TIME:
                standardHour = bundle.getInt("hour");
                standardMin = bundle.getInt("min");
                standardSec = bundle.getInt("sec", 0);
                changeStandardTime(standardHour, standardMin, standardSec);
                break;

            case REQUEST_FAKE_TIME:
                fakeHour = bundle.getInt("hour");
                fakeMin = bundle.getInt("min");
                fakeSec = bundle.getInt("sec", 0);
                changeFakeTime(fakeHour, fakeMin, fakeSec);
                break;
        }
    }
} 