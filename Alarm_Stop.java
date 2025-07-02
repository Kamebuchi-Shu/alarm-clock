package com.example.ogatafutoshikawa.alarm_clock;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.speech.tts.TextToSpeech;
import android.content.SharedPreferences;

public class Alarm_Stop extends AppCompatActivity
                        implements View.OnClickListener{

    private MediaPlayer mp;
    private TextToSpeech textToSpeech;
    private Handler handler = new Handler();
    private Runnable speakRunnable;
    private boolean isSpeaking = false;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stop_alarm);

        Button btnStop = this.findViewById(R.id.stop);
        btnStop.setOnClickListener(this);
    }

    @Override
    public void onStart(){
        super.onStart();
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
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
    }

    @Override
    public void onClick(View v) {
        // stopボタンが押されたとき
        isSpeaking = false;
        handler.removeCallbacks(speakRunnable);
        if (textToSpeech != null) {
            textToSpeech.stop();
        }
        stopAndRelease();

        finishAffinity();
    }
}