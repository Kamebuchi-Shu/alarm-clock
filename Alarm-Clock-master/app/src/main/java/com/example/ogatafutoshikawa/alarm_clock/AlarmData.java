package com.example.ogatafutoshikawa.alarm_clock;

import java.util.Arrays;

public class AlarmData {
    private int alarmId;
    private int standardHour;
    private int standardMin;
    private int standardSec;
    private int fakeHour;
    private int fakeMin;
    private int fakeSec;
    private boolean[] days; // 0=日, 1=月, 2=火, 3=水, 4=木, 5=金, 6=土
    private boolean isEnabled;
    private String audioName;
    private String customMessage;
    private boolean forceModeEnabled;

    // コンストラクタ
    public AlarmData(int alarmId) {
        this.alarmId = alarmId;
        this.standardHour = 7;
        this.standardMin = 0;
        this.standardSec = 0;
        this.fakeHour = 6;
        this.fakeMin = 45;
        this.fakeSec = 0;
        this.days = new boolean[7]; // 全て false で初期化
        this.isEnabled = false;
        this.audioName = "デフォルト音声";
        this.customMessage = "";
        this.forceModeEnabled = false;
    }

    // ゲッター・セッター
    public int getAlarmId() { return alarmId; }
    public void setAlarmId(int alarmId) { this.alarmId = alarmId; }

    public int getStandardHour() { return standardHour; }
    public void setStandardHour(int standardHour) { this.standardHour = standardHour; }

    public int getStandardMin() { return standardMin; }
    public void setStandardMin(int standardMin) { this.standardMin = standardMin; }

    public int getStandardSec() { return standardSec; }
    public void setStandardSec(int standardSec) { this.standardSec = standardSec; }

    public int getFakeHour() { return fakeHour; }
    public void setFakeHour(int fakeHour) { this.fakeHour = fakeHour; }

    public int getFakeMin() { return fakeMin; }
    public void setFakeMin(int fakeMin) { this.fakeMin = fakeMin; }

    public int getFakeSec() { return fakeSec; }
    public void setFakeSec(int fakeSec) { this.fakeSec = fakeSec; }

    public boolean[] getDays() { return days; }
    public void setDays(boolean[] days) { this.days = days; }

    public boolean isEnabled() { return isEnabled; }
    public void setEnabled(boolean enabled) { this.isEnabled = enabled; }

    public String getAudioName() { return audioName; }
    public void setAudioName(String audioName) { this.audioName = audioName; }

    public String getCustomMessage() { return customMessage; }
    public void setCustomMessage(String customMessage) { this.customMessage = customMessage; }

    public boolean isForceModeEnabled() { return forceModeEnabled; }
    public void setForceModeEnabled(boolean forceModeEnabled) { this.forceModeEnabled = forceModeEnabled; }

    // ヘルパーメソッド
    public String getStandardTimeString() {
        return String.format("%02d:%02d", standardHour, standardMin);
    }

    public String getFakeTimeString() {
        return String.format("%02d:%02d", fakeHour, fakeMin);
    }

    public String getDaysString() {
        String[] dayNames = {"日", "月", "火", "水", "木", "金", "土"};
        StringBuilder sb = new StringBuilder();
        boolean hasSelectedDays = false;
        
        for (int i = 0; i < days.length; i++) {
            if (days[i]) {
                if (hasSelectedDays) sb.append("、");
                sb.append(dayNames[i]);
                hasSelectedDays = true;
            }
        }
        
        if (!hasSelectedDays) {
            return "設定なし";
        }
        
        // 毎日の場合
        if (Arrays.equals(days, new boolean[]{true, true, true, true, true, true, true})) {
            return "毎日";
        }
        
        // 平日の場合
        if (Arrays.equals(days, new boolean[]{false, true, true, true, true, true, false})) {
            return "平日";
        }
        
        // 週末の場合
        if (Arrays.equals(days, new boolean[]{true, false, false, false, false, false, true})) {
            return "週末";
        }
        
        return sb.toString();
    }

    public void setAllDays(boolean enabled) {
        for (int i = 0; i < days.length; i++) {
            days[i] = enabled;
        }
    }

    public void setWeekdays(boolean enabled) {
        // 月曜日から金曜日 (1-5)
        for (int i = 1; i <= 5; i++) {
            days[i] = enabled;
        }
        // 土日は無効
        days[0] = false;
        days[6] = false;
    }

    public void setWeekends(boolean enabled) {
        // 日曜日と土曜日 (0, 6)
        days[0] = enabled;
        days[6] = enabled;
        // 平日は無効
        for (int i = 1; i <= 5; i++) {
            days[i] = false;
        }
    }

    public boolean hasAnyDaySet() {
        for (boolean day : days) {
            if (day) return true;
        }
        return false;
    }
} 