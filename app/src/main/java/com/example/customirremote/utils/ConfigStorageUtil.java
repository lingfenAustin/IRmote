package com.example.customirremote.utils;

import android.content.Context;
import com.example.customirremote.bean.RemoteConfig;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ConfigStorageUtil {
    private static final String FILE = "remotes.json";
    private static final Gson gson = new Gson();

    public static void saveAll(Context context, List<RemoteConfig> list) {
        try (FileOutputStream fos = context.openFileOutput(FILE, Context.MODE_PRIVATE)) {
            fos.write(gson.toJson(list).getBytes());
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static List<RemoteConfig> loadAll(Context context) {
        try (FileInputStream fis = context.openFileInput(FILE);
             BufferedReader br = new BufferedReader(new InputStreamReader(fis))) {
            Type type = new TypeToken<List<RemoteConfig>>(){}.getType();
            return gson.fromJson(br.readLine(), type);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
}