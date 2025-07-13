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
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.List;

public class Alarm_Stop extends AppCompatActivity
                        implements View.OnClickListener{

    private static final String TAG = "Alarm_Stop";
    
    private MediaPlayer mp;
    private TextToSpeech textToSpeech;
    private Handler handler = new Handler();
    private Runnable speakRunnable;
    private boolean isSpeaking = false;
    private boolean isAlarmSoundPlaying = false;

    // 新機能用フィールド
    private int displayHour;
    private int displayMin;
    private int displaySec; // 秒を追加
    private String actualAlarmType;
    private int actualHour;
    private int actualMin;
    private int actualSec; // 秒を追加
    private boolean forceModeEnabled;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stop_alarm);

        // Alarm_Receiverから渡されたデータを取得
        Intent intent = getIntent();
        displayHour = intent.getIntExtra("displayHour", 0);
        displayMin = intent.getIntExtra("displayMin", 0);
        displaySec = intent.getIntExtra("displaySec", 0); // 秒も取得
        actualAlarmType = intent.getStringExtra("actualAlarmType");
        actualHour = intent.getIntExtra("actualHour", 0);
        actualMin = intent.getIntExtra("actualMin", 0);
        actualSec = intent.getIntExtra("actualSec", 0); // 秒も取得
        forceModeEnabled = intent.getBooleanExtra("forceModeEnabled", false);

        Log.d(TAG, "アラーム停止画面開始 - 表示時間: " + displayHour + ":" + displayMin + ":" + displaySec +
                   ", 実際のタイプ: " + actualAlarmType + 
                   ", 実際の時間: " + actualHour + ":" + actualMin + ":" + actualSec +
                   ", 強制モード: " + forceModeEnabled);

        // UI要素の初期化
        initializeUI();

        Button btnStop = this.findViewById(R.id.stop);
        btnStop.setOnClickListener(this);
    }

    @SuppressLint("SetTextI18n")
    private void initializeUI() {
        // 時間表示（常に規定時間を表示）- 「時間」と「分」のみ
        TextView timeDisplay = findViewById(R.id.time_display);
        String timeText = String.format("%02d:%02d", displayHour, displayMin);
        timeDisplay.setText(timeText);

        Log.d(TAG, "UI初期化完了 - 時間表示: " + timeText);
    }

    @Override
    public void onStart(){
        super.onStart();
        
        // アラーム音を先に再生
        playSelectedAlarmSound();
        
        // TextToSpeechを初期化（アラーム音再生後に音声読み上げ開始）
        initializeTextToSpeech();
    }

    private void initializeTextToSpeech() {
        textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    Log.d(TAG, "TextToSpeech初期化成功");
                    
                    // アラーム音再生後、少し遅延してから音声読み上げ開始
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            startVoiceReading();
                        }
                    }, 2000); // 2秒後に音声読み上げ開始
                } else {
                    Log.e(TAG, "TextToSpeech初期化失敗");
                }
            }
        });
    }

    private void startVoiceReading() {
        String customMessage = getCustomMessageFromAlarmList();
        
        // フェイクタイム情報に基づいてメッセージを生成
        String voiceMessage = generateVoiceMessage(customMessage);
        
        Log.d(TAG, "音声読み上げ開始: " + voiceMessage);
        
        if (!voiceMessage.isEmpty()) {
            isSpeaking = true;
            speakRunnable = new Runnable() {
                @Override
                public void run() {
                    if (isSpeaking && textToSpeech != null) {
                        // アラーム音を一時停止してから音声読み上げ
                        pauseAlarmSound();
                        
                        textToSpeech.speak(voiceMessage, TextToSpeech.QUEUE_FLUSH, null, "voiceMessage");
                        
                        // 音声読み上げ後、アラーム音を再開
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                resumeAlarmSound();
                                // 次回の音声読み上げをスケジュール（8秒間隔）
                                if (isSpeaking) {
                                    handler.postDelayed(speakRunnable, 8000);
                                }
                            }
                        }, calculateSpeechDuration(voiceMessage));
                    }
                }
            };
            handler.post(speakRunnable);
        }
    }

    private String getCustomMessageFromAlarmList() {
        try {
            // アラーム一覧のデータを読み込み
            SharedPreferences prefs = getSharedPreferences("alarm_list_prefs", MODE_PRIVATE);
            String jsonString = prefs.getString("alarm_list", "[]");
            
            Gson gson = new Gson();
            Type listType = new TypeToken<List<AlarmData>>(){}.getType();
            List<AlarmData> alarmList = gson.fromJson(jsonString, listType);
            
            if (alarmList != null) {
                // 現在のアラーム時間に一致するアラームを検索
                for (AlarmData alarm : alarmList) {
                    if (alarm.isEnabled() && 
                        ((alarm.getStandardHour() == displayHour && alarm.getStandardMin() == displayMin) ||
                         (alarm.getFakeHour() == displayHour && alarm.getFakeMin() == displayMin))) {
                        String customMessage = alarm.getCustomMessage();
                        Log.d(TAG, "一致するアラームを発見: " + customMessage);
                        return customMessage != null ? customMessage : "";
                    }
                }
            }
            
            Log.d(TAG, "一致するアラームが見つかりません");
            return "";
        } catch (Exception e) {
            Log.e(TAG, "カスタムメッセージの取得中にエラー", e);
            return "";
        }
    }

    private String generateVoiceMessage(String customMessage) {
        StringBuilder message = new StringBuilder();
        
        // カスタムメッセージが設定されている場合（シンプルに名前呼び）
        if (!customMessage.isEmpty()) {
            message.append(customMessage).append("さん、");
        }
        
        // 基本的な起床メッセージ（メイン）
        message.append("おはようございます。起きる時間です。");
        
        // フェイクタイム情報は控えめに追加（詳細はデバッグ表示のみ）
        if ("fake".equals(actualAlarmType) && !forceModeEnabled) {
            // 通常のフェイクタイムの場合のみ、さりげなく伝える
            message.append("今日も良い一日をお過ごしください。");
        }
        
        return message.toString();
    }

    private int calculateSpeechDuration(String text) {
        // 文字数に基づいて読み上げ時間を概算（1文字あたり約100ms）
        return Math.max(3000, text.length() * 100); // 最低3秒
    }

    private void pauseAlarmSound() {
        if (mp != null && mp.isPlaying()) {
            mp.pause();
            isAlarmSoundPlaying = false;
            Log.d(TAG, "アラーム音一時停止");
        }
    }

    private void resumeAlarmSound() {
        if (mp != null && !isAlarmSoundPlaying) {
            try {
                mp.start();
                isAlarmSoundPlaying = true;
                Log.d(TAG, "アラーム音再開");
            } catch (IllegalStateException e) {
                Log.e(TAG, "アラーム音再開エラー", e);
                // エラーの場合は新しいMediaPlayerを作成
                playSelectedAlarmSound();
            }
        }
    }

    private void playSelectedAlarmSound() {
        // SharedPreferencesから選択された音声のパスを取得
        SharedPreferences prefs = getSharedPreferences("alarm_prefs", MODE_PRIVATE);
        String soundPath = prefs.getString("alarm_sound", null);

        if (soundPath != null && !soundPath.equals("default")) {
            try {
                // URIから音声を再生
                Uri soundUri = Uri.parse(soundPath);
                mp = MediaPlayer.create(this, soundUri);

                if (mp != null) {
                    mp.setLooping(true); // ループ再生
                    mp.start();
                    isAlarmSoundPlaying = true;
                    Log.d(TAG, "選択された音声を再生開始: " + soundPath);
                } else {
                    Toast.makeText(this, "音声の再生に失敗しました", Toast.LENGTH_SHORT).show();
                    playDefaultSound();
                }
            } catch (Exception e) {
                Log.e(TAG, "音声ファイルの読み込みエラー", e);
                Toast.makeText(this, "音声ファイルの読み込みエラー", Toast.LENGTH_SHORT).show();
                playDefaultSound();
            }
        } else {
            // 選択された音声がない場合はデフォルト音声を再生
            playDefaultSound();
        }
    }

    private void playDefaultSound() {
        try {
            mp = MediaPlayer.create(this, R.raw.alarm);
            if (mp != null) {
                mp.setLooping(true); // ループ再生
                mp.start();
                isAlarmSoundPlaying = true;
                Log.d(TAG, "デフォルト音声を再生開始");
            } else {
                Log.e(TAG, "デフォルト音声の作成に失敗");
            }
        } catch (Exception e) {
            Log.e(TAG, "デフォルト音声再生エラー", e);
        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        isSpeaking = false;
        isAlarmSoundPlaying = false;
        
        if (handler != null && speakRunnable != null) {
            handler.removeCallbacks(speakRunnable);
        }
        
        if (textToSpeech != null) {
            textToSpeech.shutdown();
        }
        
        stopAndRelease();
        Log.d(TAG, "アラーム停止画面終了");
    }

    private void stopAndRelease(){
        if (mp != null) {
            try {
                if (mp.isPlaying()) {
                    mp.stop();
                }
                mp.release();
                mp = null;
                isAlarmSoundPlaying = false;
                Log.d(TAG, "MediaPlayer解放完了");
            } catch (Exception e) {
                Log.e(TAG, "MediaPlayer解放エラー", e);
            }
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
    }

    @Override
    public void onClick(View v) {
        Log.d(TAG, "停止ボタンクリック");
        
        // 全ての音声・音楽を停止
        isSpeaking = false;
        isAlarmSoundPlaying = false;
        
        stopAndRelease();

        // アラーム一覧画面に戻る
        Intent intent = new Intent(Alarm_Stop.this, AlarmListActivity.class);
        // 既存のAlarmListActivityインスタンスを再利用
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish(); // 現在のアクティビティを終了
    }
}