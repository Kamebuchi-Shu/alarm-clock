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

public class Main_Activity extends AppCompatActivity implements View.OnClickListener {

    // データ受け渡しの際に使うkey
    public static final int REQUEST_TIME = 0;
    public static final int REQUEST_PREFECTURE = 1;
    public static final int REQUEST_CITY = 2;
    public static final int REQUEST_AUDIO = 3;
    public static final int REQUEST_STANDARD_TIME = 4;
    public static final int REQUEST_FAKE_TIME = 5;

    public static final String PRE_NUM = "num";
    public static final String HOUR_DATA = "0";
    public static final String MIN_DATA = "1";
    public static final String PREFECTURE_DATA = "prefecture";
    public static final String CITY_DATA = "city";
    
    // 新機能用データキー
    public static final String STANDARD_HOUR_DATA = "standard_hour";
    public static final String STANDARD_MIN_DATA = "standard_min";
    public static final String FAKE_HOUR_DATA = "fake_hour";
    public static final String FAKE_MIN_DATA = "fake_min";
    public static final String FORCE_MODE_DATA = "force_mode";

    // 変数宣言
    private String pre_str = null;
    private String city_str = null;
    private String save_pre = null;
    private String pre_key = null;
    private String city_key = null;
    private String place_key = null;
    private int pre_num = 0;
    private int hour = 100;
    private int min = 100;
    private String selectedAudioName = "デフォルト音声"; // 選択された音声名を保持
    
    // 新機能用変数
    private int standardHour = 100;
    private int standardMin = 100;
    private int fakeHour = 100;
    private int fakeMin = 100;
    private boolean forceModeEnabled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.set);

        // 各ボタンのオブジェクト化
        Button btnStandardTime = findViewById(R.id.standard_time);
        Button btnFakeTime = findViewById(R.id.fake_time);
        Switch forceModeSwitch = findViewById(R.id.force_mode_switch);
        //Button btnPrefecture = findViewById(R.id.prefecture);
        //Button btnCity = findViewById(R.id.city);
        Button btnCheack = findViewById(R.id.cheack);
        Button btnAudio = findViewById(R.id.audio_select);

        // 各ボタンのクリックリスナー設定
        btnStandardTime.setOnClickListener(this);
        btnFakeTime.setOnClickListener(this);
        //btnPrefecture.setOnClickListener(this);
        //btnCity.setOnClickListener(this);
        btnCheack.setOnClickListener(this);
        btnAudio.setOnClickListener(this);

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
        fakeHour = prefs.getInt(FAKE_HOUR_DATA, 100);
        fakeMin = prefs.getInt(FAKE_MIN_DATA, 100);
        forceModeEnabled = prefs.getBoolean(FORCE_MODE_DATA, false);

        // 時間設定を表示
        if (standardHour < 100 && standardMin < 100) {
            changeStandardTime(standardHour, standardMin);
        }
        if (fakeHour < 100 && fakeMin < 100) {
            changeFakeTime(fakeHour, fakeMin);
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

    @SuppressLint("SetTextI18n")
    public void changeTime(int hour, int min) {
        TextView tv = findViewById(R.id.time);
        String hspace = hour < 10 ? "0" : "";
        String mspace = min < 10 ? "0" : "";
        tv.setText(hspace + hour + ":" + mspace + min);
    }

    @SuppressLint("SetTextI18n")
    public void changeStandardTime(int hour, int min) {
        Button btn = findViewById(R.id.standard_time);
        String hspace = hour < 10 ? "0" : "";
        String mspace = min < 10 ? "0" : "";
        btn.setText(hspace + hour + ":" + mspace + min);
    }

    @SuppressLint("SetTextI18n")
    public void changeFakeTime(int hour, int min) {
        Button btn = findViewById(R.id.fake_time);
        String hspace = hour < 10 ? "0" : "";
        String mspace = min < 10 ? "0" : "";
        btn.setText(hspace + hour + ":" + mspace + min);
    }

    /*public void changePrefecture(String str) {
        TextView tv = findViewById(R.id.prefecture);
        tv.setText(str);
    }

    public void changeCity(String str) {
        TextView tv = findViewById(R.id.city);
        tv.setText(str);
    }*/

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
        } /*else if (id == R.id.prefecture) {
            Intent intent = new Intent(this, Prefecture_Activity.class);
            startActivityForResult(intent, REQUEST_PREFECTURE);
        } else if (id == R.id.city) {
            if (pre_num != 0) {
                Intent intent = new Intent(this, City_Activity.class);
                intent.putExtra(PRE_NUM, pre_num);
                startActivityForResult(intent, REQUEST_CITY);
            } else {
                Toast.makeText(this, "まず都道府県を選択してください", Toast.LENGTH_SHORT).show();
            }
        } */else if (id == R.id.audio_select) {
            Intent intent = new Intent(this, AudioSelectActivity.class);
            startActivityForResult(intent, REQUEST_AUDIO);
        } else if (id == R.id.cheack) {
            if (standardHour < 100 && standardMin < 100 && fakeHour < 100 && fakeMin < 100) {
                place_key = pre_key + city_key;

                Intent intent = new Intent(this, Check_Activity.class);
                intent.putExtra(STANDARD_HOUR_DATA, standardHour);
                intent.putExtra(STANDARD_MIN_DATA, standardMin);
                intent.putExtra(FAKE_HOUR_DATA, fakeHour);
                intent.putExtra(FAKE_MIN_DATA, fakeMin);
                intent.putExtra(FORCE_MODE_DATA, forceModeEnabled);
                /*intent.putExtra(PREFECTURE_DATA, pre_str);
                intent.putExtra(CITY_DATA, city_str);*/
                intent.putExtra("audio_name", selectedAudioName); // 音声名を渡す

                // 設定を保存
                saveTimeSettings();

                startActivity(intent);
            } else {
                String message = "設定されていない項目: ";
                if (standardHour >= 100 || standardMin >= 100) message += "規定時間 ";
                if (fakeHour >= 100 || fakeMin >= 100) message += "フェイクタイム ";
                /*if (pre_str == null) message += "都道府県 ";
                if (city_str == null) message += "市区町村";*/

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
        editor.putInt(FAKE_HOUR_DATA, fakeHour);
        editor.putInt(FAKE_MIN_DATA, fakeMin);
        editor.putBoolean(FORCE_MODE_DATA, forceModeEnabled);

        // 音声設定を保存
        editor.putString("alarm_sound_name", selectedAudioName);

        editor.apply();
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
            /*case REQUEST_PREFECTURE:
                pre_str = bundle.getString("prefecture");
                pre_key = bundle.getString("pre_key");
                pre_num = bundle.getInt("pre_num");
                changePrefecture(pre_str);
                if (save_pre == null || !save_pre.equals(pre_str)) {
                    city_str = null; // 都道府県が変わったら市区町村をリセット
                    changeCity("---");
                }
                save_pre = pre_str;
                break;

            case REQUEST_CITY:
                city_str = bundle.getString("city");
                city_key = bundle.getString("city_key");
                changeCity(city_str);
                break;*/

            case REQUEST_STANDARD_TIME:
                standardHour = bundle.getInt("hour");
                standardMin = bundle.getInt("min");
                changeStandardTime(standardHour, standardMin);
                break;

            case REQUEST_FAKE_TIME:
                fakeHour = bundle.getInt("hour");
                fakeMin = bundle.getInt("min");
                changeFakeTime(fakeHour, fakeMin);
                break;

            case REQUEST_TIME:
                hour = bundle.getInt("hour");
                min = bundle.getInt("min");
                changeTime(hour, min);
                break;
        }
    }
}