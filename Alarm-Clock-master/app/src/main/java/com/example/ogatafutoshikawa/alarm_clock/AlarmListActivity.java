package com.example.ogatafutoshikawa.alarm_clock;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.Calendar;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AlarmListActivity extends AppCompatActivity {
    
    private static final String TAG = "AlarmListActivity";
    private static final String PREFS_NAME = "alarm_list_prefs";
    private static final String ALARM_LIST_KEY = "alarm_list";
    private static final int REQUEST_ADD_ALARM = 1;
    private static final int REQUEST_EDIT_ALARM = 2;
    private static final int MAX_ALARMS = 5;
    
    private RecyclerView recyclerView;
    private AlarmAdapter adapter;
    private List<AlarmData> alarmList;
    private Button addButton;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_list);
        
        Log.d(TAG, "onCreate開始");
        
        // 古い設定データをクリア（重複アラーム登録を防ぐ）
        clearOldSettings();
        
        initializeViews();
        setupRecyclerView();
        loadAlarmList();
        
        Log.d(TAG, "onCreate完了");
    }
    
    private void initializeViews() {
        recyclerView = findViewById(R.id.alarm_recycler_view);
        addButton = findViewById(R.id.add_alarm_button);
        
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNewAlarm();
            }
        });
    }
    
    private void setupRecyclerView() {
        alarmList = new ArrayList<>();
        adapter = new AlarmAdapter(alarmList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        
        // アラームクリックリスナー（編集用）
        adapter.setOnAlarmClickListener(new AlarmAdapter.OnAlarmClickListener() {
            @Override
            public void onAlarmClick(AlarmData alarm, int position) {
                editAlarm(alarm, position);
            }
        });
        
        // アラームON/OFFトグルリスナー
        adapter.setOnAlarmToggleListener(new AlarmAdapter.OnAlarmToggleListener() {
            @Override
            public void onAlarmToggle(AlarmData alarm, int position, boolean isEnabled) {
                alarm.setEnabled(isEnabled);
                
                if (isEnabled) {
                    // アラームを登録
                    registerAlarm(alarm);
                } else {
                    // アラームを解除
                    unregisterAlarm(alarm);
                }
                
                saveAlarmList();
                
                // アダプターに変更を通知して表示を更新
                adapter.notifyItemChanged(position);
                
                Log.d(TAG, "アラーム " + alarm.getAlarmId() + " の状態を " + (isEnabled ? "ON" : "OFF") + " に変更");
            }
        });
        
        // 削除ボタンリスナー
        adapter.setOnDeleteClickListener(new AlarmAdapter.OnDeleteClickListener() {
            @Override
            public void onDeleteClick(AlarmData alarm, int position) {
                deleteAlarm(position);
            }
        });
    }
    
    private void addNewAlarm() {
        if (alarmList.size() >= MAX_ALARMS) {
            Toast.makeText(this, "アラームは" + MAX_ALARMS + "個まで設定できます", Toast.LENGTH_SHORT).show();
            return;
        }
        
        Intent intent = new Intent(this, AlarmEditActivity.class);
        intent.putExtra("is_new", true);
        intent.putExtra("alarm_id", getNextAlarmId());
        intent.putExtra("position", alarmList.size());
        startActivityForResult(intent, REQUEST_ADD_ALARM);
    }
    
    private void editAlarm(AlarmData alarm, int position) {
        Intent intent = new Intent(this, AlarmEditActivity.class);
        intent.putExtra("is_new", false);
        intent.putExtra("alarm_id", alarm.getAlarmId());
        intent.putExtra("position", position);
        intent.putExtra("standard_hour", alarm.getStandardHour());
        intent.putExtra("standard_min", alarm.getStandardMin());
        intent.putExtra("standard_sec", alarm.getStandardSec());
        intent.putExtra("fake_hour", alarm.getFakeHour());
        intent.putExtra("fake_min", alarm.getFakeMin());
        intent.putExtra("fake_sec", alarm.getFakeSec());
        intent.putExtra("days", alarm.getDays());
        intent.putExtra("enabled", alarm.isEnabled());
        intent.putExtra("audio_name", alarm.getAudioName());
        intent.putExtra("custom_message", alarm.getCustomMessage());
        intent.putExtra("force_mode", alarm.isForceModeEnabled());
        intent.putExtra("late_offset_minutes", alarm.getLateOffsetMinutes());
        startActivityForResult(intent, REQUEST_EDIT_ALARM);
    }
    
    private void deleteAlarm(int position) {
        if (position >= 0 && position < alarmList.size()) {
            AlarmData alarm = alarmList.get(position);
            
            // 確認ダイアログを表示
            new android.support.v7.app.AlertDialog.Builder(this)
                .setTitle("アラーム削除")
                .setMessage("このアラームを削除しますか？")
                .setPositiveButton("削除", (dialog, which) -> {
                    // アラームが有効な場合は解除
                    if (alarm.isEnabled()) {
                        unregisterAlarm(alarm);
                    }
                    
                    alarmList.remove(position);
                    adapter.notifyItemRemoved(position);
                    adapter.notifyItemRangeChanged(position, alarmList.size());
                    saveAlarmList();
                    Log.d(TAG, "アラーム " + alarm.getAlarmId() + " を削除");
                })
                .setNegativeButton("キャンセル", null)
                .show();
        }
    }
    
    private void loadAlarmList() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String jsonString = prefs.getString(ALARM_LIST_KEY, "[]");
        
        Gson gson = new Gson();
        Type listType = new TypeToken<List<AlarmData>>(){}.getType();
        List<AlarmData> loadedList = gson.fromJson(jsonString, listType);
        
        if (loadedList != null) {
            alarmList.clear();
            alarmList.addAll(loadedList);
            sortAlarmList();
            adapter.notifyDataSetChanged();
            
            // 有効なアラームを自動的に登録
            for (AlarmData alarm : alarmList) {
                if (alarm.isEnabled()) {
                    registerAlarm(alarm);
                    Log.d(TAG, "起動時にアラーム " + alarm.getAlarmId() + " を自動登録");
                }
            }
            
            Log.d(TAG, "アラームリストを読み込み: " + alarmList.size() + "件");
        }
    }
    
    private void saveAlarmList() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        Gson gson = new Gson();
        String jsonString = gson.toJson(alarmList);
        
        prefs.edit()
            .putString(ALARM_LIST_KEY, jsonString)
            .apply();
        
        Log.d(TAG, "アラームリストを保存: " + alarmList.size() + "件");
    }
    
    private void sortAlarmList() {
        Collections.sort(alarmList, new Comparator<AlarmData>() {
            @Override
            public int compare(AlarmData a1, AlarmData a2) {
                int time1 = a1.getStandardHour() * 60 + a1.getStandardMin();
                int time2 = a2.getStandardHour() * 60 + a2.getStandardMin();
                return Integer.compare(time1, time2);
            }
        });
    }
    
    private int getNextAlarmId() {
        int maxId = 0;
        for (AlarmData alarm : alarmList) {
            if (alarm.getAlarmId() > maxId) {
                maxId = alarm.getAlarmId();
            }
        }
        return maxId + 1;
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (resultCode == RESULT_OK && data != null) {
            AlarmData alarm = createAlarmFromIntent(data);
            
            if (requestCode == REQUEST_ADD_ALARM) {
                alarmList.add(alarm);
                sortAlarmList();
                adapter.notifyDataSetChanged();
                
                // 新しいアラームが有効な場合はAlarmManagerに登録
                if (alarm.isEnabled()) {
                    registerAlarm(alarm);
                    Log.d(TAG, "新しいアラーム " + alarm.getAlarmId() + " をAlarmManagerに登録");
                }
                
                Log.d(TAG, "新しいアラームを追加: " + alarm.getAlarmId());
            } else if (requestCode == REQUEST_EDIT_ALARM) {
                int position = data.getIntExtra("position", -1);
                if (position >= 0 && position < alarmList.size()) {
                    // 古いアラームを一旦解除
                    AlarmData oldAlarm = alarmList.get(position);
                    if (oldAlarm.isEnabled()) {
                        unregisterAlarm(oldAlarm);
                        Log.d(TAG, "編集前のアラーム " + oldAlarm.getAlarmId() + " を解除");
                    }
                    
                    // 新しいアラームデータに更新
                    alarmList.set(position, alarm);
                    sortAlarmList();
                    adapter.notifyDataSetChanged();
                    
                    // 新しいアラームが有効な場合は登録
                    if (alarm.isEnabled()) {
                        registerAlarm(alarm);
                        Log.d(TAG, "編集後のアラーム " + alarm.getAlarmId() + " をAlarmManagerに登録");
                    }
                    
                    Log.d(TAG, "アラームを更新: " + alarm.getAlarmId());
                }
            }
            
            saveAlarmList();
        }
    }
    
    private AlarmData createAlarmFromIntent(Intent data) {
        int alarmId = data.getIntExtra("alarm_id", 1);
        AlarmData alarm = new AlarmData(alarmId);
        
        alarm.setStandardHour(data.getIntExtra("standard_hour", 7));
        alarm.setStandardMin(data.getIntExtra("standard_min", 0));
        alarm.setStandardSec(data.getIntExtra("standard_sec", 0));
        alarm.setFakeHour(data.getIntExtra("fake_hour", 6));
        alarm.setFakeMin(data.getIntExtra("fake_min", 45));
        alarm.setFakeSec(data.getIntExtra("fake_sec", 0));
        alarm.setDays(data.getBooleanArrayExtra("days"));
        alarm.setEnabled(data.getBooleanExtra("enabled", true));  // デフォルトを true に変更
        alarm.setAudioName(data.getStringExtra("audio_name"));
        alarm.setCustomMessage(data.getStringExtra("custom_message"));
        alarm.setForceModeEnabled(data.getBooleanExtra("force_mode", false));
        alarm.setLateOffsetMinutes(data.getIntExtra("late_offset_minutes", 15));
        
        return alarm;
    }
    
    // アラームを登録する
    private void registerAlarm(AlarmData alarm) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        
        // 規定時間のアラームを登録
        int standardRequestCode = alarm.getAlarmId() * 1000 + alarm.getStandardHour() * 100 + alarm.getStandardMin();
        setupAlarm(alarmManager, alarm, alarm.getStandardHour(), alarm.getStandardMin(), alarm.getStandardSec(), 
                  standardRequestCode, "standard");
        
        // フェイクタイムのアラームを登録
        int fakeRequestCode = alarm.getAlarmId() * 1000 + alarm.getFakeHour() * 100 + alarm.getFakeMin() + 10000;
        setupAlarm(alarmManager, alarm, alarm.getFakeHour(), alarm.getFakeMin(), alarm.getFakeSec(), 
                  fakeRequestCode, "fake");
        
        Log.d(TAG, "アラーム " + alarm.getAlarmId() + " を登録しました");
    }
    
    // アラームを解除する
    private void unregisterAlarm(AlarmData alarm) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        
        // 規定時間のアラームを解除
        int standardRequestCode = alarm.getAlarmId() * 1000 + alarm.getStandardHour() * 100 + alarm.getStandardMin();
        cancelAlarm(alarmManager, alarm, standardRequestCode);
        
        // フェイクタイムのアラームを解除
        int fakeRequestCode = alarm.getAlarmId() * 1000 + alarm.getFakeHour() * 100 + alarm.getFakeMin() + 10000;
        cancelAlarm(alarmManager, alarm, fakeRequestCode);
        
        Log.d(TAG, "アラーム " + alarm.getAlarmId() + " を解除しました");
    }
    
    // アラーム設定の共通メソッド
    private void setupAlarm(AlarmManager alarmManager, AlarmData alarm, int hour, int min, int sec, 
                           int requestCode, String alarmType) {
        // アラーム用のIntent
        Intent bootIntent = new Intent(this, Alarm_Receiver.class);
        bootIntent.putExtra("notificationId", requestCode);
        bootIntent.putExtra("alarmType", alarmType);
        bootIntent.putExtra("standardHour", alarm.getStandardHour());
        bootIntent.putExtra("standardMin", alarm.getStandardMin());
        bootIntent.putExtra("fakeHour", alarm.getFakeHour());
        bootIntent.putExtra("fakeMin", alarm.getFakeMin());
        bootIntent.putExtra("forceModeEnabled", alarm.isForceModeEnabled());
        bootIntent.putExtra("lateOffsetMinutes", alarm.getLateOffsetMinutes());
        
        // PendingIntentの作成
        PendingIntent alarmIntent = PendingIntent.getBroadcast(
                this,
                requestCode,
                bootIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );
        
        // アラーム時間の設定
        Calendar startTime = Calendar.getInstance();
        startTime.set(Calendar.HOUR_OF_DAY, hour);
        startTime.set(Calendar.MINUTE, min);
        startTime.set(Calendar.SECOND, sec);
        startTime.set(Calendar.MILLISECOND, 0);
        long alarmStartTime = startTime.getTimeInMillis();
        
        // 過去の時間に設定された場合は翌日にする
        if (alarmStartTime <= System.currentTimeMillis()) {
            alarmStartTime += 24 * 60 * 60 * 1000; // 1日追加
            Log.d(TAG, "過去の時刻設定を検出、翌日に設定: " + hour + ":" + min + " (" + alarmType + ")");
        }
        
        // Androidバージョンに応じたアラーム設定
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    alarmStartTime,
                    alarmIntent
            );
            Log.d(TAG, "setExactAndAllowWhileIdleを使用");
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    alarmStartTime,
                    alarmIntent
            );
            Log.d(TAG, "setExactを使用");
        } else {
            alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    alarmStartTime,
                    alarmIntent
            );
            Log.d(TAG, "setを使用");
        }
        
        Log.d(TAG, "アラーム設定: " + hour + ":" + min + " (" + alarmType + ", リクエストコード: " + requestCode + ")");
    }
    
    // アラーム解除の共通メソッド
    private void cancelAlarm(AlarmManager alarmManager, AlarmData alarm, int requestCode) {
        Intent intent = new Intent(this, Alarm_Receiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );
        
        alarmManager.cancel(pendingIntent);
        Log.d(TAG, "アラーム解除: リクエストコード " + requestCode);
    }
    
    // 古い設定データをクリアする
    private void clearOldSettings() {
        try {
            // 古いMain_Activityのアラーム設定をクリア
            SharedPreferences oldPrefs = getSharedPreferences("alarm_prefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = oldPrefs.edit();
            editor.clear();
            editor.apply();
            
            // AlarmPrefsもクリア（カスタムメッセージの古いデータを削除）
            SharedPreferences alarmPrefs = getSharedPreferences("AlarmPrefs", MODE_PRIVATE);
            SharedPreferences.Editor alarmEditor = alarmPrefs.edit();
            alarmEditor.clear();
            alarmEditor.apply();
            
            // 他の古いSharedPreferencesもクリア
            SharedPreferences[] oldPrefsArray = {
                getSharedPreferences("audio_prefs", MODE_PRIVATE),
                getSharedPreferences("AUDIO_PREFS", MODE_PRIVATE)
            };
            
            for (SharedPreferences prefs : oldPrefsArray) {
                SharedPreferences.Editor ed = prefs.edit();
                ed.clear();
                ed.apply();
            }
            
            // 既存の古いアラームもキャンセル
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            
            // 一般的な古いリクエストコードをキャンセル
            int[] oldRequestCodes = {40, 10040, 140, 10140}; // 0:40のパターンなど
            for (int requestCode : oldRequestCodes) {
                Intent intent = new Intent(this, Alarm_Receiver.class);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(
                        this, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                alarmManager.cancel(pendingIntent);
            }
            
            Log.d(TAG, "古い設定データをクリアしました");
        } catch (Exception e) {
            Log.e(TAG, "古い設定データのクリア中にエラー", e);
        }
    }
} 