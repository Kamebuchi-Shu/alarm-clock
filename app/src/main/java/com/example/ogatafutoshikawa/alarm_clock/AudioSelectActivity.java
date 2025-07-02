package com.example.ogatafutoshikawa.alarm_clock;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.util.ArrayList;

public class AudioSelectActivity extends AppCompatActivity {

    public static final String EXTRA_AUDIO_NAME = "audio_name";

    private static final int PICK_AUDIO_REQUEST = 1001;
    private static final String AUDIO_PREFS = "audio_prefs";
    private static final String AUDIO_LIST_KEY = "audio_list";
    private static final String SELECTED_AUDIO_KEY = "selected_audio";
    private static final String DEFAULT_UNNAMED_FILE = "未命名ファイル";
    private static final String DEFAULT_AUDIO_NAME = "デフォルト音声";

    private ArrayList<AudioItem> audioList;
    private AudioAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_select);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("音声選択");
        }

        initializeUI();
        loadAudioList();
    }

    private void initializeUI() {
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        audioList = new ArrayList<>();
        adapter = new AudioAdapter(this, audioList, this); // thisを渡す
        recyclerView.setAdapter(adapter);

        Button uploadButton = findViewById(R.id.buttonUpload);
        uploadButton.setOnClickListener(v -> pickAudioFile());

        Button backButton = findViewById(R.id.buttonBack);
        backButton.setOnClickListener(v -> returnWithSelectedAudio());
    }

    // 戻るボタン処理（アクションバーと物理ボタン）
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            returnWithSelectedAudio();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        returnWithSelectedAudio();
    }

    // 選択結果を返して終了
    private void returnWithSelectedAudio() {
        AudioItem selectedItem = adapter.getSelectedItem();
        String audioName = (selectedItem != null) ? selectedItem.getName() : DEFAULT_AUDIO_NAME;
        returnSelectionResult(audioName);
        finish();
    }

    private void pickAudioFile() {
        try {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.setType("audio/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
            startActivityForResult(intent, PICK_AUDIO_REQUEST);
        } catch (Exception e) {
            Toast.makeText(this, "音声ファイルを開けませんでした", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_AUDIO_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri audioUri = data.getData();
            if (audioUri != null) {
                handleSelectedAudio(audioUri);
            }
        }
    }

    private void handleSelectedAudio(Uri audioUri) {
        try {
            String fileName = getFileName(audioUri);
            if (fileName == null || fileName.isEmpty()) {
                fileName = DEFAULT_UNNAMED_FILE;
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                getContentResolver().takePersistableUriPermission(
                        audioUri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                );
            }

            AudioItem newItem = new AudioItem(fileName, audioUri.toString());
            addAudioItem(newItem);

        } catch (SecurityException e) {
            Toast.makeText(this, "ファイルへのアクセス権限がありません", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "音声ファイルの処理中にエラーが発生しました", Toast.LENGTH_SHORT).show();
        }
    }

    private void addAudioItem(AudioItem item) {
        audioList.add(item);
        saveAudioList();
        adapter.notifyItemInserted(audioList.size() - 1);
        Toast.makeText(this, item.getName() + " を追加しました", Toast.LENGTH_SHORT).show();
    }

    private String getFileName(Uri uri) {
        String result = null;

        if ("content".equals(uri.getScheme())) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (index != -1) {
                        result = cursor.getString(index);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return result != null ? result : uri.getLastPathSegment();
    }

    private void saveAudioList() {
        SharedPreferences prefs = getSharedPreferences(AUDIO_PREFS, MODE_PRIVATE);
        prefs.edit()
                .putString(AUDIO_LIST_KEY, new Gson().toJson(audioList))
                .apply();
    }

    private void loadAudioList() {
        SharedPreferences prefs = getSharedPreferences(AUDIO_PREFS, MODE_PRIVATE);
        String json = prefs.getString(AUDIO_LIST_KEY, null);

        if (json != null) {
            try {
                ArrayList<AudioItem> savedList = new Gson().fromJson(
                        json,
                        new TypeToken<ArrayList<AudioItem>>(){}.getType()
                );

                if (savedList != null) {
                    audioList.clear();
                    audioList.addAll(savedList);
                    adapter.notifyDataSetChanged();
                }
            } catch (Exception e) {
                prefs.edit().remove(AUDIO_LIST_KEY).apply();
            }
        }
    }

    public static AudioItem getSelectedAudioItem(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(AUDIO_PREFS, Context.MODE_PRIVATE);
        String json = prefs.getString(SELECTED_AUDIO_KEY, null);
        if (json != null) {
            return new Gson().fromJson(json, AudioItem.class);
        }
        return null;
    }

    public static void setSelectedAudioItem(Context context, AudioItem item) {
        SharedPreferences prefs = context.getSharedPreferences(AUDIO_PREFS, Context.MODE_PRIVATE);
        prefs.edit()
                .putString(SELECTED_AUDIO_KEY, new Gson().toJson(item))
                .apply();
    }

    // 選択結果を返すメソッド（publicに変更）
    public void returnSelectionResult(String audioName) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra(EXTRA_AUDIO_NAME, audioName);
        setResult(RESULT_OK, resultIntent);
    }

    // 外部から呼び出せる終了メソッド
    public void finishWithResult(String audioName) {
        returnSelectionResult(audioName);
        finish();
    }
}