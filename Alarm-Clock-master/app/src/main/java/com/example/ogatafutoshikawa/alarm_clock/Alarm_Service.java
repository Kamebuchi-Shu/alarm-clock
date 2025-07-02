package com.example.ogatafutoshikawa.alarm_clock;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.IBinder;

public class Alarm_Service extends Service {

    private MediaPlayer mp;
    public static final String CHANNEL_ID = "AlarmServiceChannel";

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        createNotificationChannel();

        // ユーザーにアラームが作動中であることを示す通知（必須）
        Notification notification = new Notification.Builder(this, CHANNEL_ID)
                .setContentTitle("アラーム作動中")
                .setSmallIcon(R.mipmap.ic_launcher) // アイコンを指定
                .build();

        startForeground(1, notification);

        // アラーム音を再生
        if (mp == null) {
            mp = MediaPlayer.create(this, R.raw.alarm);
            if (mp != null) {
                mp.setLooping(true);
                mp.start();
            }
        }
        return START_STICKY;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Alarm Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            getSystemService(NotificationManager.class).createNotificationChannel(serviceChannel);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mp != null) {
            mp.stop();
            mp.release();
            mp = null;
        }
    }
}