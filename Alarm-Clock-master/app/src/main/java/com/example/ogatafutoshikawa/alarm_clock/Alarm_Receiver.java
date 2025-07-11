package com.example.ogatafutoshikawa.alarm_clock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import java.util.Random;

public class Alarm_Receiver extends BroadcastReceiver {

    private static final String TAG = "Alarm_Receiver";

    @Override
    public void onReceive(Context context, Intent receivedIntent) {
        Log.d(TAG, "アラーム受信開始");

        // Check_Activityから渡された表示用の時間を取得
        int displayHour = receivedIntent.getIntExtra("displayHour", 0);
        int displayMin = receivedIntent.getIntExtra("displayMin", 0);
        int displaySec = receivedIntent.getIntExtra("displaySec", 0);

        // アラーム停止画面を起動
        Intent alarmStopIntent = new Intent(context, Alarm_Stop.class);
        alarmStopIntent.putExtra("displayHour", displayHour);
        alarmStopIntent.putExtra("displayMin", displayMin);
        alarmStopIntent.putExtra("displaySec", displaySec);

        // 新しいタスクとして起動
        alarmStopIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(alarmStopIntent);

        Log.d(TAG, "Alarm_Stop起動完了");
    }

    /**
     * フェイクタイムを使用するかどうかを決定する
     * @param forceModeEnabled 強制モードが有効かどうか
     * @return true: フェイクタイムを使用, false: 規定時間を使用
     */
    /*private boolean decideFakeTimeUsage(boolean forceModeEnabled) {
        if (forceModeEnabled) {
            Log.d(TAG, "強制モード有効 - フェイクタイムを選択");
            return true;
        }

        // 通常モード: 規定時間30%, フェイクタイム70%の確率
        Random random = new Random();
        int probability = random.nextInt(100); // 0-99の乱数

        boolean useFakeTime = probability >= 30; // 70%の確率でフェイクタイム
        
        Log.d(TAG, "確率決定: " + probability + "/100 - " + 
                   (useFakeTime ? "フェイクタイム(70%)" : "規定時間(30%)") + "を選択");
        
        return useFakeTime;
    }*/
}