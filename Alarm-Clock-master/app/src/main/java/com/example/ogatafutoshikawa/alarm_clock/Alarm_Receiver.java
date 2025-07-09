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

        // Check_Activityから渡されたデータを取得
        String alarmType = receivedIntent.getStringExtra("alarmType"); // "standard" or "fake"
        int standardHour = receivedIntent.getIntExtra("standardHour", 0);
        int standardMin = receivedIntent.getIntExtra("standardMin", 0);
        int fakeHour = receivedIntent.getIntExtra("fakeHour", 0);
        int fakeMin = receivedIntent.getIntExtra("fakeMin", 0);
        boolean forceModeEnabled = receivedIntent.getBooleanExtra("forceModeEnabled", false);

        Log.d(TAG, "受信データ - タイプ: " + alarmType + 
                   ", 規定時間: " + standardHour + ":" + standardMin + 
                   ", フェイクタイム: " + fakeHour + ":" + fakeMin + 
                   ", 強制モード: " + forceModeEnabled);

        // 確率的分岐ロジック
        boolean shouldUseFakeTime = decideFakeTimeUsage(forceModeEnabled);
        
        Log.d(TAG, "決定結果: " + (shouldUseFakeTime ? "フェイクタイム" : "規定時間") + "を使用");

        // アラーム停止画面を起動
        Intent alarmStopIntent = new Intent(context, Alarm_Stop.class);
        
        // 常に規定時間を表示させるため、規定時間を渡す
        alarmStopIntent.putExtra("displayHour", standardHour);
        alarmStopIntent.putExtra("displayMin", standardMin);
        alarmStopIntent.putExtra("displaySec", 0); // 現状では秒は0で固定
        
        // どちらの時間が実際に使われたかの情報も渡す（デバッグ用）
        alarmStopIntent.putExtra("actualAlarmType", shouldUseFakeTime ? "fake" : "standard");
        alarmStopIntent.putExtra("actualHour", shouldUseFakeTime ? fakeHour : standardHour);
        alarmStopIntent.putExtra("actualMin", shouldUseFakeTime ? fakeMin : standardMin);
        alarmStopIntent.putExtra("actualSec", 0); // 現状では秒は0で固定
        alarmStopIntent.putExtra("forceModeEnabled", forceModeEnabled);
        
        // 新しいタスクとして起動（画面起動に必要）
        alarmStopIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(alarmStopIntent);
        
        Log.d(TAG, "Alarm_Stop起動完了");
    }

    /**
     * フェイクタイムを使用するかどうかを決定する
     * @param forceModeEnabled 強制モードが有効かどうか
     * @return true: フェイクタイムを使用, false: 規定時間を使用
     */
    private boolean decideFakeTimeUsage(boolean forceModeEnabled) {
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
    }
}