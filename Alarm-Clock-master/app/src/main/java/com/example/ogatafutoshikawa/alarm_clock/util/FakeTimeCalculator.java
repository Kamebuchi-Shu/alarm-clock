package com.example.ogatafutoshikawa.alarm_clock.util;

import java.util.Calendar;
import com.example.ogatafutoshikawa.alarm_clock.data.FakeTimeSettings;

// フェイクタイムを計算するロジックをまとめたクラス
public class FakeTimeCalculator {

    /**
     * 現在時刻と設定を基に、表示すべき時刻（本物 or 偽物）を計算して返す
     * @param realCurrentTime 実際の現在時刻
     * @param settings フェイクタイム設定
     * @return 表示すべき時刻 (Calendarオブジェクト)
     */
    public static Calendar calculateFakeTime(Calendar realCurrentTime, FakeTimeSettings settings) {
        // --- 目標起床時刻をCalendarオブジェクトに変換 ---
        Calendar targetWakeUpTime = Calendar.getInstance();
        targetWakeUpTime.set(Calendar.HOUR_OF_DAY, settings.wakeHour);
        targetWakeUpTime.set(Calendar.MINUTE, settings.wakeMinute);
        targetWakeUpTime.set(Calendar.SECOND, 0);

        // --- 遅刻ギリギリ時刻をCalendarオブジェクトに変換 ---
        Calendar lateTime = Calendar.getInstance();
        lateTime.set(Calendar.HOUR_OF_DAY, settings.lateHour);
        lateTime.set(Calendar.MINUTE, settings.lateMinute);
        lateTime.set(Calendar.SECOND, 0);

        // --- 実際の時間と目標起床時間の差をミリ秒で計算 ---
        long diffFromWakeUp = realCurrentTime.getTimeInMillis() - targetWakeUpTime.getTimeInMillis();

        // 目標時刻より前か、二度寝許容時間を超えていたら、本物の時刻を表示
        if (diffFromWakeUp < 0 || diffFromWakeUp > settings.snooze * 60 * 1000) {
            return realCurrentTime;
        }

        // --- フェイクタイムの計算 ---
        // (実際の経過時間) / (二度寝許容時間) = (偽りの経過時間) / (目標と遅刻の差)
        // これを解くと、 (偽りの経過時間) = (実際の経過時間) * (目標と遅刻の差) / (二度寝許容時間)

        long fakeProgressMillis = (long) ((double) diffFromWakeUp *
                (double) (lateTime.getTimeInMillis() - targetWakeUpTime.getTimeInMillis()) /
                (double) (settings.snooze * 60 * 1000));

        // 計算した偽の経過時間を、目標起床時間に足して、表示すべき偽の時刻を算出
        Calendar fakeTimeToDisplay = (Calendar) targetWakeUpTime.clone();
        fakeTimeToDisplay.add(Calendar.MILLISECOND, (int) fakeProgressMillis);

        return fakeTimeToDisplay;
    }
}