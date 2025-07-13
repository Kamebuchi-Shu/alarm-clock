package com.example.ogatafutoshikawa.alarm_clock;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Switch;
import android.widget.EditText;

public class Main_Activity extends AppCompatActivity implements View.OnClickListener {

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
    private String selectedAudioName = "デフォルト音声"; // 選択された音声名を保持
    
    // 新機能用変数
    private int standardHour = 100;
    private int standardMin = 100;
    private int standardSec = 100; // 規定時間の秒を追加
    private int fakeHour = 100;
    private int fakeMin = 100;
    private int fakeSec = 100; // フェイクタイムの秒を追加
    private boolean forceModeEnabled = false;
    private String customMessage = ""; // カスタムメッセージ用変数

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.set);

        // 各ボタンのオブジェクト化
        Button btnStandardTime = findViewById(R.id.standard_time);
        Button btnFakeTime = findViewById(R.id.fake_time);
        Switch forceModeSwitch = findViewById(R.id.force_mode_switch);

        Button btnCheack = findViewById(R.id.cheack);
        Button btnAudio = findViewById(R.id.audio_select);
        Button btnDebug = findViewById(R.id.debug_button);
        EditText customMessageEditText = findViewById(R.id.customMessageEditText);

        // 各ボタンのクリックリスナー設定
        btnStandardTime.setOnClickListener(this);
        btnFakeTime.setOnClickListener(this);

        btnCheack.setOnClickListener(this);
        btnAudio.setOnClickListener(this);
        btnDebug.setOnClickListener(this);

        // 強制モードスイッチのリスナー設定
        forceModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            forceModeEnabled = isChecked;
            String text = isChecked ? getString(R.string.force_mode_on) : getString(R.string.force_mode_off);
            forceModeSwitch.setText(text);
            saveForceMode();
        });

        // 保存された音声名を読み込んで表示
        loadSelectedAudioName();
        
        // 保存された時間設定を読み込んで表示
        loadTimeSettings();
        
        // カスタムメッセージの読み込みと設定
        loadCustomMessage(customMessageEditText);
        
        // カスタムメッセージ入力の変更監視
        setupCustomMessageListener(customMessageEditText);
    }

    // 保存された音声名を読み込む
    private void loadSelectedAudioName() {
        SharedPreferences prefs = getSharedPreferences("alarm_prefs", MODE_PRIVATE);
        String savedName = prefs.getString("alarm_sound_name", null);

        if (savedName != null) {
            selectedAudioName = savedName;
        }

        // 音声名を表示
        changeAudioName(selectedAudioName);
    }

    // 保存された時間設定を読み込む
    private void loadTimeSettings() {
        SharedPreferences prefs = getSharedPreferences("alarm_prefs", MODE_PRIVATE);
        standardHour = prefs.getInt(STANDARD_HOUR_DATA, 100);
        standardMin = prefs.getInt(STANDARD_MIN_DATA, 100);
        standardSec = prefs.getInt("standard_sec", 100); // 秒も読み込み
        fakeHour = prefs.getInt(FAKE_HOUR_DATA, 100);
        fakeMin = prefs.getInt(FAKE_MIN_DATA, 100);
        fakeSec = prefs.getInt("fake_sec", 100); // 秒も読み込み
        forceModeEnabled = prefs.getBoolean(FORCE_MODE_DATA, false);

        // 時間設定を表示
        if (standardHour < 100 && standardMin < 100 && standardSec < 100) {
            changeStandardTime(standardHour, standardMin, standardSec);
        }
        if (fakeHour < 100 && fakeMin < 100 && fakeSec < 100) {
            changeFakeTime(fakeHour, fakeMin, fakeSec);
        }

        // 強制モードスイッチの状態を設定
        Switch forceModeSwitch = findViewById(R.id.force_mode_switch);
        forceModeSwitch.setChecked(forceModeEnabled);
        String text = forceModeEnabled ? getString(R.string.force_mode_on) : getString(R.string.force_mode_off);
        forceModeSwitch.setText(text);
    }

    // 強制モードの状態を保存
    private void saveForceMode() {
        SharedPreferences prefs = getSharedPreferences("alarm_prefs", MODE_PRIVATE);
        prefs.edit()
                .putBoolean(FORCE_MODE_DATA, forceModeEnabled)
                .apply();
    }

    // カスタムメッセージを読み込む
    private void loadCustomMessage(EditText customMessageEditText) {
        SharedPreferences prefs = getSharedPreferences("AlarmPrefs", MODE_PRIVATE);
        customMessage = prefs.getString("customMessage", "");
        customMessageEditText.setText(customMessage);
    }

    // カスタムメッセージの変更監視を設定
    private void setupCustomMessageListener(EditText customMessageEditText) {
        customMessageEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                // フォーカスが外れた時にカスタムメッセージを保存
                saveCustomMessage(customMessageEditText.getText().toString());
            }
        });
    }

    // カスタムメッセージを保存
    private void saveCustomMessage(String message) {
        customMessage = message;
        SharedPreferences prefs = getSharedPreferences("AlarmPrefs", MODE_PRIVATE);
        prefs.edit()
                .putString("customMessage", customMessage)
                .apply();
    }



    @SuppressLint("SetTextI18n")
    public void changeStandardTime(int hour, int min, int sec) {
        Button btn = findViewById(R.id.standard_time);
        String hspace = hour < 10 ? "0" : "";
        String mspace = min < 10 ? "0" : "";
        String sspace = sec < 10 ? "0" : "";
        btn.setText(hspace + hour + ":" + mspace + min + ":" + sspace + sec);
    }

    @SuppressLint("SetTextI18n")
    public void changeFakeTime(int hour, int min, int sec) {
        Button btn = findViewById(R.id.fake_time);
        String hspace = hour < 10 ? "0" : "";
        String mspace = min < 10 ? "0" : "";
        String sspace = sec < 10 ? "0" : "";
        btn.setText(hspace + hour + ":" + mspace + min + ":" + sspace + sec);
    }



    // 選択された音声名を表示
    private void changeAudioName(String name) {
        TextView tv = findViewById(R.id.audio_name);
        if (name != null && !name.isEmpty()) {
            tv.setText(name);
        } else {
            tv.setText("デフォルト音声");
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
        } else if (id == R.id.debug_button) {
            Intent intent = new Intent(this, DebugActivity.class);
            startActivity(intent);
        } else if (id == R.id.cheack) {
            if (standardHour < 100 && standardMin < 100 && standardSec < 100 && 
                fakeHour < 100 && fakeMin < 100 && fakeSec < 100) {
                Intent intent = new Intent(this, Check_Activity.class);
                intent.putExtra(STANDARD_HOUR_DATA, standardHour);
                intent.putExtra(STANDARD_MIN_DATA, standardMin);
                intent.putExtra("standard_sec", standardSec); // 秒も追加
                intent.putExtra(FAKE_HOUR_DATA, fakeHour);
                intent.putExtra(FAKE_MIN_DATA, fakeMin);
                intent.putExtra("fake_sec", fakeSec); // 秒も追加
                intent.putExtra(FORCE_MODE_DATA, forceModeEnabled);
                intent.putExtra("audio_name", selectedAudioName); // 音声名を渡す
                
                // カスタムメッセージも保存してから渡す
                EditText customMessageEditText = findViewById(R.id.customMessageEditText);
                saveCustomMessage(customMessageEditText.getText().toString());
                intent.putExtra("custom_message", customMessage); // カスタムメッセージを渡す

                // 設定を保存
                saveTimeSettings();

                startActivity(intent);
            } else {
                String message = "設定されていない項目: ";
                if (standardHour >= 100 || standardMin >= 100 || standardSec >= 100) message += "規定時間 ";
                if (fakeHour >= 100 || fakeMin >= 100 || fakeSec >= 100) message += "フェイクタイム ";

                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            }
        }
    }

    // 時間設定を保存する
    private void saveTimeSettings() {
        SharedPreferences prefs = getSharedPreferences("alarm_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        // 時間設定を保存
        editor.putInt(STANDARD_HOUR_DATA, standardHour);
        editor.putInt(STANDARD_MIN_DATA, standardMin);
        editor.putInt("standard_sec", standardSec); // 秒も保存
        editor.putInt(FAKE_HOUR_DATA, fakeHour);
        editor.putInt(FAKE_MIN_DATA, fakeMin);
        editor.putInt("fake_sec", fakeSec); // 秒も保存
        editor.putBoolean(FORCE_MODE_DATA, forceModeEnabled);

        // 音声設定を保存
        editor.putString("alarm_sound_name", selectedAudioName);
        
        // カスタムメッセージを保存（両方のSharedPreferencesに保存）
        editor.putString("customMessage", customMessage);
        
        editor.apply();
        
        // AlarmPrefsにもカスタムメッセージを保存（Alarm_Stopで使用）
        SharedPreferences alarmPrefs = getSharedPreferences("AlarmPrefs", MODE_PRIVATE);
        alarmPrefs.edit()
                .putString("customMessage", customMessage)
                .apply();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        // 結果が正常でない場合は処理をスキップ
        if (resultCode != RESULT_OK) return;
        if (intent == null) return;

        // 音声選択結果は必ずEXTRAがあるのでbundle不要
        if (requestCode == REQUEST_AUDIO) {
            // 音声選択結果を処理
            if (intent.hasExtra(AudioSelectActivity.EXTRA_AUDIO_NAME)) {
                selectedAudioName = intent.getStringExtra(AudioSelectActivity.EXTRA_AUDIO_NAME);
                changeAudioName(selectedAudioName);

                // 音声名を保存
                SharedPreferences prefs = getSharedPreferences("alarm_prefs", MODE_PRIVATE);
                prefs.edit()
                        .putString("alarm_sound_name", selectedAudioName)
                        .apply();
            }
            return; // 他の処理をスキップ
        }

        // その他のリクエストはbundleを使用
        Bundle bundle = intent.getExtras();
        if (bundle == null) return;

        switch (requestCode) {
            case REQUEST_STANDARD_TIME:
                standardHour = bundle.getInt("hour");
                standardMin = bundle.getInt("min");
                standardSec = bundle.getInt("sec", 0); // 秒も取得、デフォルト0
                changeStandardTime(standardHour, standardMin, standardSec);
                break;

            case REQUEST_FAKE_TIME:
                fakeHour = bundle.getInt("hour");
                fakeMin = bundle.getInt("min");
                fakeSec = bundle.getInt("sec", 0); // 秒も取得、デフォルト0
                changeFakeTime(fakeHour, fakeMin, fakeSec);
                break;


        }
    }
}