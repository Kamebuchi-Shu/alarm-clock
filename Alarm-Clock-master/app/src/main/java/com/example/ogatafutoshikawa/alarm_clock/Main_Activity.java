package com.example.ogatafutoshikawa.alarm_clock;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class Main_Activity extends AppCompatActivity
        implements View.OnClickListener{

    // データ受け渡しの際に使うkey
    // REQUEST_TIME は、今回「設定画面」を呼び出すためのキーとして流用します。
    public static final int REQUEST_TIME = 0;
    public static final int REQUEST_PREFECTURE= 1; // 天気機能は使いませんが、念のため残しておきます
    public static final int REQUEST_CITY= 2; // 同上

    // 各Activityへデータを渡す際のキー
    public static final String PRE_NUM= "num";
    public static final String HOUR_DATA= "hour";
    public static final String MIN_DATA= "min";
    public static final String PREFECTURE_DATA= "prefecture";
    public static final String CITY_DATA = "city";


    // ユーザーが設定した起床希望時間を格納するフィールド
    private int wakeUpHour = -1; // -1は未設定状態を示す
    private int wakeUpMinute = -1;

    // --- 天気関連の変数は今回は使用しないため、コメントアウトまたは削除しても構いません ---
    String pre_str = null;
    String city_str = null;
    String save_pre = null;
    String pre_key = null;
    String city_key = null;
    String place_key = null;
    int pre_num = 0;
    // --- ここまで ---


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.set);

        // 各ボタンのオブジェクト化
        // 「時間」ボタンは事実上「設定」ボタンとして機能します。
        Button btnTime = this.findViewById(R.id.time);
        Button btnPrefecture = this.findViewById(R.id.prefecture); // 天気機能用
        Button btnCity = this.findViewById(R.id.city); // 天気機能用
        Button btnCheck = this.findViewById(R.id.cheack); // アラーム確定ボタン

        // 各ボタンのクリック判定
        btnTime.setOnClickListener(this);
        btnPrefecture.setOnClickListener(this);
        btnCity.setOnClickListener(this);
        btnCheck.setOnClickListener(this);

        // フェイクタイム設定ボタンを追加（set.xmlに追加した場合）
        // Button btnOpenSettings = findViewById(R.id.button_open_settings);
        // btnOpenSettings.setOnClickListener(this);

        // 以前設定した起床希望時間を読み込んで表示
        loadWakeUpTime();
    }


    /**
     * ボタンのクリックイベントを処理するメソッド
     * @param v
     */
    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.time) { // 時間ボタンが押されたら設定画面へ
            // Alarm_Activity（現在は設定画面）を呼び出す
            Intent intent = new Intent(Main_Activity.this, Alarm_Activity.class);
            startActivityForResult(intent, REQUEST_TIME);
        } else if (id == R.id.cheack) { // OKボタンが押されたらアラームをセット
            // 起床希望時間が設定されていればCheck_Activityを起動
            if (wakeUpHour != -1 && wakeUpMinute != -1) {
                Intent intent = new Intent(Main_Activity.this, Check_Activity.class);
                intent.putExtra(HOUR_DATA, wakeUpHour);
                intent.putExtra(MIN_DATA, wakeUpMinute);

                // --- 天気関連のデータも渡す場合はここに記述 ---
                // 今回はアラーム時間のみ渡す
                // intent.putExtra(PREFECTURE_DATA, pre_str);
                // intent.putExtra(CITY_DATA, city_str);
                // SharedPreferences data = getSharedPreferences("DataSave", Context.MODE_PRIVATE);
                // SharedPreferences.Editor editor = data.edit();
                // editor.putString("Save", place_key);
                // editor.apply();
                // --- ここまで ---

                startActivity(intent);
            }
        }
        // --- 天気関連のボタン処理は今回は不要 ---
        // else if (id == R.id.prefecture) { ... }
        // else if (id == R.id.city) { ... }
        // --- ここまで ---
    }


    /**
     * 他のActivityからデータを受け取るメソッド
     * @param requestCode
     * @param resultCode
     * @param intent
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        // 設定画面（Alarm_Activity）からの結果を受け取る
        if (requestCode == REQUEST_TIME && resultCode == RESULT_OK) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                // 返された起床希望時間で変数を更新
                wakeUpHour = bundle.getInt("hour");
                wakeUpMinute = bundle.getInt("min");
                // 画面の時刻表示を更新
                changeTime(wakeUpHour, wakeUpMinute);
            }
        }

        // --- 天気関連のActivityからの結果処理は今回は不要 ---
        // switch(requestCode){ ... }
        // --- ここまで ---
    }

    /**
     * 時間表示用のUI（Button）のテキストを更新するメソッド
     * @param hour 時
     * @param min 分
     */
    @SuppressLint("SetTextI18n")
    public void changeTime(int hour, int min) {
        Button timeButton = findViewById(R.id.time);
        if (hour != -1 && min != -1) {
            String h_space = (hour < 10) ? "0" : "";
            String m_space = (min < 10) ? "0" : "";
            timeButton.setText(h_space + hour + ":" + m_space + min);
        } else {
            // 未設定の場合はデフォルトの表示に戻す
            timeButton.setText("---");
        }
    }

    /**
     * アプリ起動時に、以前保存した起床希望時間を読み込んで表示する
     */
    private void loadWakeUpTime() {
        SharedPreferences prefs = getSharedPreferences(Alarm_Activity.PREFS_NAME, Context.MODE_PRIVATE);
        wakeUpHour = prefs.getInt(Alarm_Activity.KEY_WAKE_HOUR, -1);
        wakeUpMinute = prefs.getInt(Alarm_Activity.KEY_WAKE_MINUTE, -1);
        changeTime(wakeUpHour, wakeUpMinute);
    }

    // --- 天気関連のchangePrefecture, changeCityメソッドは今回は不要 ---
    // public void changePrefecture(String str) { ... }
    // public void changeCity(String str) { ... }
    // --- ここまで ---
}