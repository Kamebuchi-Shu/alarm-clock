package com.example.ogatafutoshikawa.alarm_clock;

import android.net.Uri;

public class AudioItem {
    private String name;
    private String path;  // Uriではなく、文字列（String）として保存

    // コンストラクタ（Uri受け取り）
    public AudioItem(String name, Uri uri) {
        this.name = name;
        this.path = uri.toString();  // Uri → String に変換
    }

    // コンストラクタ（String受け取り、Gson復元用）
    public AudioItem(String name, String path) {
        this.name = name;
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // Uriとして使うときのgetter
    public Uri getUri() {
        return Uri.parse(path);
    }

    public void setUri(Uri uri) {
        this.path = uri.toString();
    }

    // 保存時や表示用に使うpath getter
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
