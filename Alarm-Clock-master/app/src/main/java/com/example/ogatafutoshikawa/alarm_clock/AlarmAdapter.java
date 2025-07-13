package com.example.ogatafutoshikawa.alarm_clock;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import java.util.List;

public class AlarmAdapter extends RecyclerView.Adapter<AlarmAdapter.AlarmViewHolder> {

    private List<AlarmData> alarmList;
    private OnAlarmClickListener onAlarmClickListener;
    private OnAlarmToggleListener onAlarmToggleListener;
    private OnDeleteClickListener onDeleteClickListener;

    // インターフェース定義
    public interface OnAlarmClickListener {
        void onAlarmClick(AlarmData alarm, int position);
    }

    public interface OnAlarmToggleListener {
        void onAlarmToggle(AlarmData alarm, int position, boolean isEnabled);
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(AlarmData alarm, int position);
    }

    // コンストラクタ
    public AlarmAdapter(List<AlarmData> alarmList) {
        this.alarmList = alarmList;
    }

    // リスナー設定
    public void setOnAlarmClickListener(OnAlarmClickListener listener) {
        this.onAlarmClickListener = listener;
    }

    public void setOnAlarmToggleListener(OnAlarmToggleListener listener) {
        this.onAlarmToggleListener = listener;
    }

    public void setOnDeleteClickListener(OnDeleteClickListener listener) {
        this.onDeleteClickListener = listener;
    }

    @Override
    public AlarmViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.alarm_list_item, parent, false);
        return new AlarmViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(AlarmViewHolder holder, int position) {
        AlarmData alarm = alarmList.get(position);
        holder.bind(alarm, position);
    }

    @Override
    public int getItemCount() {
        return alarmList.size();
    }

    // アラーム項目を更新
    public void updateAlarm(int position, AlarmData alarm) {
        if (position >= 0 && position < alarmList.size()) {
            alarmList.set(position, alarm);
            notifyItemChanged(position);
        }
    }

    // アラーム項目を追加
    public void addAlarm(AlarmData alarm) {
        alarmList.add(alarm);
        notifyItemInserted(alarmList.size() - 1);
    }

    // アラーム項目を削除
    public void removeAlarm(int position) {
        if (position >= 0 && position < alarmList.size()) {
            alarmList.remove(position);
            notifyItemRemoved(position);
        }
    }

    // ViewHolder クラス
    public class AlarmViewHolder extends RecyclerView.ViewHolder {
        private TextView standardTimeText;
        private TextView fakeTimeText;
        private TextView forceModeText;
        private Button deleteButton;
        private Switch alarmSwitch;
        private View itemView;

        public AlarmViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            
            standardTimeText = itemView.findViewById(R.id.standard_time_text);
            fakeTimeText = itemView.findViewById(R.id.fake_time_text);
            forceModeText = itemView.findViewById(R.id.force_mode_text);
            deleteButton = itemView.findViewById(R.id.delete_button);
            alarmSwitch = itemView.findViewById(R.id.alarm_switch);
        }

        public void bind(AlarmData alarm, int position) {
            // 時間表示
            standardTimeText.setText(alarm.getStandardTimeString() + " : 規定時間");
            fakeTimeText.setText(alarm.getFakeTimeString() + " : フェイクタイム");
            
            // 強制モード表示
            forceModeText.setText("強制:" + (alarm.isForceModeEnabled() ? "ON" : "OFF"));

            // スイッチの状態設定
            alarmSwitch.setOnCheckedChangeListener(null); // 一時的にリスナーを無効化
            alarmSwitch.setChecked(alarm.isEnabled());
            alarmSwitch.setText(alarm.isEnabled() ? "ON" : "OFF");

            // スイッチのリスナー設定
            alarmSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                alarm.setEnabled(isChecked);
                alarmSwitch.setText(isChecked ? "ON" : "OFF");
                
                if (onAlarmToggleListener != null) {
                    onAlarmToggleListener.onAlarmToggle(alarm, position, isChecked);
                }
            });

            // 削除ボタンのリスナー設定
            deleteButton.setOnClickListener(v -> {
                if (onDeleteClickListener != null) {
                    onDeleteClickListener.onDeleteClick(alarm, position);
                }
            });

            // アイテムクリックリスナー設定
            itemView.setOnClickListener(v -> {
                if (onAlarmClickListener != null) {
                    onAlarmClickListener.onAlarmClick(alarm, position);
                }
            });

            // アラームが無効の場合はテキストを薄く表示
            float alpha = alarm.isEnabled() ? 1.0f : 0.5f;
            standardTimeText.setAlpha(alpha);
            fakeTimeText.setAlpha(alpha);
            forceModeText.setAlpha(alpha);
        }
    }
} 