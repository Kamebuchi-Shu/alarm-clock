package com.example.ogatafutoshikawa.alarm_clock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class Alarm_Receiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent receivedIntent) {
        // 1. アラーム音を鳴らすサービスを起動
        Intent serviceIntent = new Intent(context, Alarm_Service.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent);
        } else {
            context.startService(serviceIntent);
        }

        // 2. アラーム画面（Alarm_Stop）を起動
        Intent activityIntent = new Intent(context, Alarm_Stop.class);
        activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(activityIntent);
    }
}