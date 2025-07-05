package com.example.ogatafutoshikawa.alarm_clock;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.speech.tts.TextToSpeech;
import android.content.SharedPreferences;
import android.net.Uri;

public class Alarm_Stop extends AppCompatActivity
                        implements View.OnClickListener{

    private static final String TAG = "Alarm_Stop";
    
    private MediaPlayer mp;
    private TextToSpeech textToSpeech;
    private Handler handler = new Handler();
    private Runnable speakRunnable;
    private boolean isSpeaking = false;

    // 新機能用フィールド
    private int displayHour;
    private int displayMin;
    private String actualAlarmType;
    private int actualHour;
    private int actualMin;
    private boolean forceModeEnabled;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stop_alarm);

        // Alarm_Receiverから渡されたデータを取得
        Intent intent = getIntent();
        displayHour = intent.getIntExtra("displayHour", 0);
        displayMin = intent.getIntExtra("displayMin", 0);
        actualAlarmType = intent.getStringExtra("actualAlarmType");
        actualHour = intent.getIntExtra("actualHour", 0);
        actualMin = intent.getIntExtra("actualMin", 0);
        forceModeEnabled = intent.getBooleanExtra("forceModeEnabled", false);

        Log.d(TAG, "アラーム停止画面開始 - 表示時間: " + displayHour + ":" + displayMin + 
                   ", 実際のタイプ: " + actualAlarmType + 
                   ", 実際の時間: " + actualHour + ":" + actualMin + 
                   ", 強制モード: " + forceModeEnabled);

        // UI要素の初期化
        initializeUI();

        Button btnStop = this.findViewById(R.id.stop);
        btnStop.setOnClickListener(this);
    }

    @SuppressLint("SetTextI18n")
    private void initializeUI() {
        // 時間表示（常に規定時間を表示）
        TextView timeDisplay = findViewById(R.id.time_display);
        String timeText = String.format("%02d:%02d", displayHour, displayMin);
        timeDisplay.setText(timeText);

        // アラーム情報表示
        TextView alarmInfo = findViewById(R.id.alarm_info);
        alarmInfo.setText("規定時間です"); // 常に規定時間と表示

        // デバッグ情報表示
        TextView debugInfo = findViewById(R.id.debug_info);
        String debugText = String.format("実際: %s (%02d:%02d) | 強制: %s", 
                actualAlarmType != null ? ("fake".equals(actualAlarmType) ? "フェイク" : "規定") : "不明",
                actualHour, actualMin,
                forceModeEnabled ? "ON" : "OFF");
        debugInfo.setText(debugText);

        Log.d(TAG, "UI初期化完了 - " + debugText);
    }

    @Override
    public void onStart(){
        super.onStart();
        playSelectedAlarmSound();
        if(mp == null) {
            mp = MediaPlayer.create(this, R.raw.alarm);
        }
        //mp.start();

        textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    SharedPreferences prefs = getSharedPreferences("AlarmPrefs", MODE_PRIVATE);
                    final String customMessage = prefs.getString("customMessage", "");
                    if (!customMessage.isEmpty()) {
                        isSpeaking = true;
                        speakRunnable = new Runnable() {
                            @Override
                            public void run() {
                                if (isSpeaking) {
                                    textToSpeech.speak(customMessage, TextToSpeech.QUEUE_FLUSH, null, null);
                                    handler.postDelayed(this, 5000); // 5秒ごとに繰り返し
                                }
                            }
                        };
                        handler.post(speakRunnable);
                    }
                }
            }
        });
    }

    private void playSelectedAlarmSound() {
        // SharedPreferencesから選択された音声のパスを取得
        SharedPreferences prefs = getSharedPreferences("alarm_prefs", MODE_PRIVATE);
        String soundPath = prefs.getString("alarm_sound", null);

        if (soundPath != null) {
            try {
                // URIから音声を再生
                Uri soundUri = Uri.parse(soundPath);
                mp = MediaPlayer.create(this, soundUri);

                if (mp != null) {
                    mp.start();
                } else {
                    Toast.makeText(this, "音声の再生に失敗しました", Toast.LENGTH_SHORT).show();
                    playDefaultSound();
                }
            } catch (Exception e) {
                Toast.makeText(this, "音声ファイルの読み込みエラー", Toast.LENGTH_SHORT).show();
                playDefaultSound();
            }
        } else {
            // 選択された音声がない場合はデフォルト音声を再生
            playDefaultSound();
        }
    }

    private void playDefaultSound() {
        mp = MediaPlayer.create(this, R.raw.alarm);
        if (mp != null) {
            mp.start();
        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        isSpeaking = false;
        handler.removeCallbacks(speakRunnable);
        if (textToSpeech != null) {
            textToSpeech.shutdown();
        }
        stopAndRelease();
    }

    private void stopAndRelease(){
        if (mp != null) {
            mp.stop();
            mp.release();
            mp = null;
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
    }

    @Override
    public void onClick(View v) {
        stopAndRelease();

        // 直接メイン画面に戻る
        Intent intent = new Intent(Alarm_Stop.this, Main_Activity.class);
        // 既存のMainActivityインスタンスを再利用
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish(); // 現在のアクティビティを終了
    }
}