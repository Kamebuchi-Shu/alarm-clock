package com.example.ogatafutoshikawa.alarm_clock.data;

// 設定値をまとめて保持するためのクラス
public class FakeTimeSettings {
    // ↓ すべての変数に "public" を追加
    public final int wakeHour;
    public final int wakeMinute;
    public final int lateHour;
    public final int lateMinute;
    public final int snooze;
    public final boolean isEnabled;

    public FakeTimeSettings(int wakeHour, int wakeMinute, int lateHour, int lateMinute, int snooze, boolean isEnabled) {
        this.wakeHour = wakeHour;
        this.wakeMinute = wakeMinute;
        this.lateHour = lateHour;
        this.lateMinute = lateMinute;
        this.snooze = snooze;
        this.isEnabled = isEnabled;
    }
}