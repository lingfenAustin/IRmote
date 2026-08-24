package com.example.customirremote;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.customirremote.bean.KeyItem;
import com.example.customirremote.bean.RemoteConfig;
import com.example.customirremote.utils.ConfigStorageUtil;
import com.example.customirremote.utils.HexInvertUtil;
import java.util.ArrayList;
import java.util.List;

public class EditRemoteActivity extends AppCompatActivity {
    private EditText etName, etHeader;
    private LinearLayout llKeys;
    private int index;
    private List<RemoteConfig> allConfigs;
    private List<KeyItem> keyList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_remote);

        etName = findViewById(R.id.et_remote_name);
        etHeader = findViewById(R.id.et_header_code);
        llKeys = findViewById(R.id.ll_keys_container);
        Button btnAddKey = findViewById(R.id.btn_add_key);
        Button btnSave = findViewById(R.id.btn_save);

        index = getIntent().getIntExtra("index", -1);
        allConfigs = ConfigStorageUtil.loadAll(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        // 编辑模式加载数据
        if (index >= 0 && index < allConfigs.size()) {
            RemoteConfig config = allConfigs.get(index);
            etName.setText(config.getRemoteName());
            etHeader.setText(config.getHeaderCode());
            keyList.addAll(config.getKeyList());
            for (KeyItem key : keyList) addKeyView(key);
        }

        // 头码输入过滤
        etHeader.addTextChangedListener(new HexWatcher(etHeader, 4));

        btnAddKey.setOnClickListener(v -> {
            KeyItem key = new KeyItem("", "00");
            keyList.add(key);
            addKeyView(key);
        });

        btnSave.setOnClickListener(v -> saveConfig());
    }

    private void addKeyView(KeyItem key) {
        View view = LayoutInflater.from(this).inflate(R.layout.item_key_edit, llKeys, false);
        EditText etName = view.findViewById(R.id.et_key_name);
        EditText etCode = view.findViewById(R.id.et_key_code);
        Button btnDel = view.findViewById(R.id.btn_del_key);

        etName.setText(key.getKeyName());
        etCode.setText(key.getKeyRawCode());
        etCode.addTextChangedListener(new HexWatcher(etCode, 2));

        etName.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                key.setKeyName(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        etCode.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                key.setKeyRawCode(HexInvertUtil.filterHex(s.toString(), 2));
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        btnDel.setOnClickListener(v -> {
            keyList.remove(key);
            llKeys.removeView(view);
        });

        llKeys.addView(view);
    }

    private void saveConfig() {
        String name = etName.getText().toString().trim();
        String header = etHeader.getText().toString().trim();
        if (name.isEmpty()) name = "未命名遥控";
        if (header.isEmpty()) header = "0000";

        RemoteConfig config = new RemoteConfig(name, header, new ArrayList<>(keyList));
        if (index >= 0) {
            allConfigs.set(index, config);
        } else {
            allConfigs.add(config);
        }
        ConfigStorageUtil.saveAll(this, allConfigs);
        finish();
    }

    // 十六进制输入过滤器
    static class HexWatcher implements TextWatcher {
        private final EditText et;
        private final int maxLen;
        HexWatcher(EditText et, int maxLen) { this.et = et; this.maxLen = maxLen; }

        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
        @Override
        public void afterTextChanged(Editable s) {
            String filtered = HexInvertUtil.filterHex(s.toString(), maxLen);
            if (!filtered.equals(s.toString())) {
                et.setText(filtered);
                et.setSelection(filtered.length());
            }
        }
    }
}