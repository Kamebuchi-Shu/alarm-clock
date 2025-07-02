package com.example.ogatafutoshikawa.alarm_clock;

import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.content.Intent;

public class Alarm_Stop extends AppCompatActivity
        implements View.OnClickListener{

    private MediaPlayer mp;

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
        playSelectedAlarmSound();
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
