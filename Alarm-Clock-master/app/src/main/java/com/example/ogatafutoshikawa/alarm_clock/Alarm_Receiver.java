package com.example.ogatafutoshikawa.alarm_clock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class Alarm_Receiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent receivedIntent) {
        // アラーム停止画面を起動
        Intent alarmStopIntent = new Intent(context, Alarm_Stop.class);
        // 新しいタスクとして起動（画面起動に必要）
        alarmStopIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(alarmStopIntent);
    }
}