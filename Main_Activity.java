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
import android.widget.EditText;
import android.content.SharedPreferences;

/**
 * メインのクラス。AppCompatActivityクラスを継承。View.OnClickListenerをインポートしている。
 * @version 1.0
 * @author Ogata
 */
public class Main_Activity extends AppCompatActivity
                            implements View.OnClickListener{

    //データ受け渡しの際に使うkey
    /**Alarm_Activityクラスへの受け渡しの認証キー*/
    public static final int REQUEST_TIME = 0;

    /**Prefecture_Activityクラスへの受け渡しの認証キー*/
    public static final int REQUEST_PREFECTURE= 1;

    /**City_Activityクラスへの受け渡しの認証キー*/
    public static final int REQUEST_CITY= 2;

    /**City_Activityクラスにどの県を選択したかのデータを渡す際に使う認証キー*/
    public static final String PRE_NUM= "num";

    /**Check_Activityクラスに"時間"のデータを渡す際に使う認証キー*/
    public static final String HOUR_DATA= "0";

    /**Check_Activityクラスに"分"のデータを渡す際に使う認証キー*/
    public static final String MIN_DATA= "1";

    /**Check_Activityクラスに"県"のデータを渡す際に使う認証キー*/
    public static final String PREFECTURE_DATA= "prefecture";

    /**Check_Activityクラスに"市"のデータを渡す際に使う認証キー*/
    public static final String CITY_DATA = "city";

    //変数宣言
    /**Prefecture_Activityクラスから受け取ったデータを格納するフィールド*/
    String pre_str = null;

    /**City_Activityクラスから受け取ったデータを格納するフィールド*/
    String city_str = null;

    /**pre_strのデータを保存するためのフィールド（変更した時Cityのところを空白にするため）*/
    String save_pre = null;

    /**県の番号。01~47の2桁*/
    String pre_key = null;

    /**市の番号。県によってバラバラの４桁*/
    String city_key = null;

    /**県の番号と市の番号を足したもの*/
    String place_key = null;

    /**県の番号。市の番号を検索する際に単純化させるため*/
    int pre_num = 0;

    /**Alarm_Activityクラスから受け取った"時間"のデータを格納するフィールド*/
    int hour = 100;

    /**Alarm_Activityクラスから受け取った"分"のデータを格納するフィールド*/
    int min = 100;


    /**
     * Main_Activityの画面を構成するメソッド
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.set);

        //各ボタンのオブジェクト化
        Button btnTime = this.findViewById(R.id.time);
        //Button btnPrefecture = this.findViewById(R.id.prefecture);
        //Button btnCity = this.findViewById(R.id.city);
        Button btnCheack = this.findViewById(R.id.cheack);

        //各ボタンのクリック判定
        btnTime.setOnClickListener(this);
        //btnPrefecture.setOnClickListener(this);
        //btnCity.setOnClickListener(this);
        btnCheack.setOnClickListener(this);
    }


    /**
     * 時間のブロックを更新するメソッド
     * @param hour
     * @param min
     */
    //時間のブロックに指定した時間を表示
    @SuppressLint("SetTextI18n")
    public void changeTime(int hour, int min) {
        TextView tv = findViewById(R.id.time);
        String hspace = "";
        String mspace = "";
        if(hour < 10){
            hspace = "0";
        }
        if(min < 10){
            mspace = "0";
        }
        tv.setText(hspace + hour + ":"+ mspace + min);
    }

    /**
     * 県のブロックを更新するメソッド
     * @param str
     */
    //県のブロックに指定した県を表示
    /*
    public void changePrefecture(String str) {
        TextView tv = findViewById(R.id.prefecture);
        tv.setText(str);
    }
    */

    /**
     * 市のブロックを更新するメソッド
     * @param str
     */
    //市のブロックに指定した県を表示
    /*
    public void changeCity(String str) {
        TextView tv = findViewById(R.id.city);
        tv.setText(str);
    }
    */

    /**
     * クリックイベントのメソッド
     * @param v
     */
    //各クリックのイベント
    @Override
    public void onClick(View v) {
        int id = v.getId();
        if(id == R.id.time) {
            Intent intent1 = new Intent(Main_Activity.this, Alarm_Activity.class);
            startActivityForResult(intent1,REQUEST_TIME);
        } 
        /*
        else if (id == R.id.prefecture) {
            Intent intent2 = new Intent(Main_Activity.this, Prefecture_Activity.class);
            startActivityForResult(intent2,REQUEST_PREFECTURE);
        } else if (id == R.id.city) {
            if(pre_num != 0) {
                Intent intent3 = new Intent(Main_Activity.this, City_Activity.class);
                intent3.putExtra(PRE_NUM,pre_num);
                startActivityForResult(intent3,REQUEST_CITY);
            }
        } 
        */
        else if (id ==R. id.cheack) {
            if(hour < 100 && min < 100) {
                //place_key = pre_key + city_key;
                Intent intent4 = new Intent(Main_Activity.this, Check_Activity.class);
                intent4.putExtra(HOUR_DATA, hour);
                intent4.putExtra(MIN_DATA, min);
                //intent4.putExtra(PREFECTURE_DATA, pre_str);
                //intent4.putExtra(CITY_DATA, city_str);

                //SharedPreferences data = getSharedPreferences("DataSave", Context.MODE_PRIVATE);
                //SharedPreferences.Editor editor = data.edit();
                //editor.putString("Save", place_key);
                //editor.apply();

                // ここでEditTextの内容を保存
                EditText customMessageEditText = findViewById(R.id.customMessageEditText);
                String customMessage = customMessageEditText.getText().toString();
                SharedPreferences prefs = getSharedPreferences("AlarmPrefs", MODE_PRIVATE);
                prefs.edit().putString("customMessage", customMessage).apply();

                startActivity(intent4);
            }
        }
    }

    /**
     * 各Activityからのデータを受け取るメソッド
     * @param requestCode
     * @param resultCode
     * @param intent
     */
    //他のActivityからのデータの受け取り
    @Override
    protected  void onActivityResult(int requestCode, int resultCode, Intent intent){
        super.onActivityResult(requestCode, resultCode, intent);
        Bundle bundle;

        switch(requestCode){
            /*
            case (REQUEST_PREFECTURE):
                if(resultCode == RESULT_OK){
                    bundle = intent.getExtras();
                    assert bundle != null;
                    pre_str = bundle.getString("prefecture");
                    pre_key = bundle.getString("pre_key");
                    pre_num = bundle.getInt("pre_num");
                    changePrefecture(pre_str);
                    if(save_pre != pre_str){
                        changeCity("---");
                    }
                    save_pre = pre_str;
                }
                break;

            case (REQUEST_CITY):
                if(resultCode == RESULT_OK){
                    bundle = intent.getExtras();
                    assert bundle != null;
                    city_str= bundle.getString("city");
                    city_key = bundle.getString("city_key");
                    changeCity(city_str);
                }
                break;
            */
            case(REQUEST_TIME):
                if(resultCode == RESULT_OK) {
                    bundle = intent.getExtras();
                    assert bundle != null;
                    hour = bundle.getInt("hour");
                    min = bundle.getInt("min");
                    changeTime(hour,min);
                }
                break;
        }
    }
}
