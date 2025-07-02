package com.example.ogatafutoshikawa.alarm_clock;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.List;

public class AudioAdapter extends RecyclerView.Adapter<AudioAdapter.AudioViewHolder> {
    private static final String AUDIO_PREFS = "audio_prefs";
    private static final String AUDIO_LIST_KEY = "audio_list";
    private static final String SELECTED_AUDIO_KEY = "selected_audio";

    private Context context;
    private List<AudioItem> audioList;
    private AudioSelectActivity activity; // 追加: 親アクティビティへの参照
    private int selectedPosition = -1;
    private MediaPlayer mediaPlayer = null;
    private int currentlyPlayingPosition = -1;

    // コンストラクタ変更: activityを追加
    public AudioAdapter(Context context, List<AudioItem> audioList, AudioSelectActivity activity) {
        this.context = context;
        this.audioList = audioList;
        this.activity = activity; // 親アクティビティを保持
        loadSelectedPosition();
    }

    private void loadSelectedPosition() {
        SharedPreferences prefs = context.getSharedPreferences(AUDIO_PREFS, Context.MODE_PRIVATE);
        String selectedJson = prefs.getString(SELECTED_AUDIO_KEY, null);
        if (selectedJson != null) {
            AudioItem selectedItem = new Gson().fromJson(selectedJson, AudioItem.class);
            for (int i = 0; i < audioList.size(); i++) {
                if (audioList.get(i).getPath().equals(selectedItem.getPath())) {
                    selectedPosition = i;
                    break;
                }
            }
        }
    }

    @NonNull
    @Override
    public AudioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.audio_item, parent, false);
        return new AudioViewHolder(view);
    }

    private void saveAudioData() {
        SharedPreferences prefs = context.getSharedPreferences(AUDIO_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        // 音声リストを保存
        editor.putString(AUDIO_LIST_KEY, new Gson().toJson(audioList));

        // 選択された音声を保存
        if (selectedPosition != -1) {
            editor.putString(SELECTED_AUDIO_KEY,
                    new Gson().toJson(audioList.get(selectedPosition)));
        } else {
            editor.remove(SELECTED_AUDIO_KEY);
        }

        editor.apply();
    }

    @Override
    public void onBindViewHolder(@NonNull final AudioViewHolder holder, final int position) {
        AudioItem item = audioList.get(position);
        holder.textView.setText(item.getName());

        // 選択状態の表示更新
        holder.buttonSelect.setText(position == selectedPosition ? "選択中" : "選択");
        holder.buttonSelect.setEnabled(position != selectedPosition);

        // 再生状態の表示更新
        holder.buttonPlay.setText(position == currentlyPlayingPosition ? "停止" : "再生");

        holder.buttonSelect.setOnClickListener(v -> {
            int currentPos = holder.getAdapterPosition();
            if (currentPos != RecyclerView.NO_POSITION) {
                handleSelectClick(currentPos);
            }
        });

        holder.buttonPlay.setOnClickListener(v -> {
            int currentPos = holder.getAdapterPosition();
            if (currentPos != RecyclerView.NO_POSITION) {
                handlePlayClick(currentPos);
            }
        });

        holder.buttonDelete.setOnClickListener(v -> {
            int currentPos = holder.getAdapterPosition();
            if (currentPos != RecyclerView.NO_POSITION) {
                handleDeleteClick(currentPos);
            }
        });

        holder.textView.setOnClickListener(v -> {
            int currentPos = holder.getAdapterPosition();
            if (currentPos != RecyclerView.NO_POSITION) {
                showRenameDialog(currentPos);
            }
        });
    }

    private void handleSelectClick(int currentPos) {
        selectedPosition = currentPos;
        notifyDataSetChanged();
        saveAudioData();

        // 選択時に即座に結果を返して終了
        AudioItem selectedItem = audioList.get(currentPos);
        activity.finishWithResult(selectedItem.getName());

        Toast.makeText(context,
                selectedItem.getName() + " を選択しました",
                Toast.LENGTH_SHORT).show();
    }

    private void handlePlayClick(int position) {
        // 同じ項目が再生中の場合は停止
        if (currentlyPlayingPosition == position) {
            stopCurrentPlayback();
            return;
        }

        // 他の音声が再生中の場合は停止
        if (mediaPlayer != null) {
            stopCurrentPlayback();
        }

        // 新しい音声を再生
        startNewPlayback(position);
    }

    private void stopCurrentPlayback() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;

            int oldPosition = currentlyPlayingPosition;
            currentlyPlayingPosition = -1;

            if (oldPosition != RecyclerView.NO_POSITION) {
                notifyItemChanged(oldPosition);
            }
        }
    }

    private void startNewPlayback(int position) {
        Uri audioUri = audioList.get(position).getUri();
        mediaPlayer = MediaPlayer.create(context, audioUri);

        if (mediaPlayer != null) {
            mediaPlayer.setOnCompletionListener(mp -> {
                releaseMediaPlayer();
                int oldPosition = currentlyPlayingPosition;
                currentlyPlayingPosition = -1;
                if (oldPosition != RecyclerView.NO_POSITION) {
                    notifyItemChanged(oldPosition);
                }
            });

            mediaPlayer.start();
            currentlyPlayingPosition = position;
            notifyItemChanged(position);
        } else {
            Toast.makeText(context, "再生に失敗しました", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleDeleteClick(int position) {
        // 再生中の場合停止
        if (mediaPlayer != null && currentlyPlayingPosition == position) {
            stopCurrentPlayback();
        }

        // 選択状態の調整
        if (selectedPosition == position) {
            selectedPosition = -1;
        } else if (selectedPosition > position) {
            selectedPosition--;
        }

        // アイテム削除
        audioList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, audioList.size());

        saveAudioData();
    }

    private void showRenameDialog(final int position) {
        AudioItem currentItem = audioList.get(position);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("名前を変更");

        final EditText input = new EditText(context);
        input.setText(currentItem.getName());
        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> {
            currentItem.setName(input.getText().toString());
            notifyItemChanged(position);
            saveAudioData();
        });

        builder.setNegativeButton("キャンセル", null);
        builder.show();
    }

    private void releaseMediaPlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    public int getItemCount() {
        return audioList.size();
    }

    public AudioItem getSelectedItem() {
        return (selectedPosition != -1) ? audioList.get(selectedPosition) : null;
    }

    static class AudioViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        Button buttonSelect;
        Button buttonPlay;
        Button buttonDelete;

        public AudioViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.textViewName);
            buttonSelect = itemView.findViewById(R.id.buttonSelect);
            buttonPlay = itemView.findViewById(R.id.buttonPlay);
            buttonDelete = itemView.findViewById(R.id.buttonDelete);
        }
    }
}