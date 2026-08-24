package com.example.customirremote;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.example.customirremote.bean.KeyItem;
import com.example.customirremote.bean.RemoteConfig;
import com.example.customirremote.utils.ConfigStorageUtil;
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParseRemoteActivity extends AppCompatActivity {
    private EditText etName, etXml;

    // 正则：匹配<key>标签，提取value、name、注释
    private static final Pattern KEY_PATTERN = Pattern.compile(
            "<key\\s+value=\"(0x[0-9a-fA-F]+)\"\\s+name=\"([^\"]+)\"\\s*/>\\s*(<!--\\s*(.*?)\\s*-->)?"
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parse_remote);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        etName = findViewById(R.id.et_remote_name);
        etXml = findViewById(R.id.et_key_xml);
        MaterialButton btnSave = findViewById(R.id.btn_parse_save);

        btnSave.setOnClickListener(v -> parseAndSave());
    }

    private void parseAndSave() {
        String name = etName.getText().toString().trim();
        String xmlText = etXml.getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(this, "请输入遥控名称", Toast.LENGTH_SHORT).show();
            return;
        }
        if (xmlText.isEmpty()) {
            Toast.makeText(this, "请粘贴按键XML文本", Toast.LENGTH_SHORT).show();
            return;
        }

        List<KeyItem> keyList = new ArrayList<>();
        String headerCode = "";

        Matcher matcher = KEY_PATTERN.matcher(xmlText);
        while (matcher.find()) {
            String value = matcher.group(1); // 0x开头的8位十六进制
            String keyNameAttr = matcher.group(2); // name属性
//            String comment = matcher.group(4); // 注释内容

            // 去掉0x，转大写，取8位
            String hex = value.replace("0x", "").toUpperCase();
            if (hex.length() < 8) continue;

            // 拆分：前2位反码、3-4位原码、后4位头码
            String rawCode = hex.substring(2, 4); // 原码
            String tempHeader = hex.substring(4, 8); // 头码

            // 统一头码（取第一个的头码）
            if (headerCode.isEmpty()) headerCode = tempHeader;

            // 按键名：优先注释，没有就去掉KEY_前缀
            String keyName;
            keyName = keyNameAttr.replace("KEY_", "");

            keyList.add(new KeyItem(keyName, rawCode));
        }

        if (keyList.isEmpty()) {
            Toast.makeText(this, "未解析到有效按键，请检查文本格式", Toast.LENGTH_SHORT).show();
            return;
        }

        // 保存配置
        RemoteConfig config = new RemoteConfig(name, headerCode, keyList);
        List<RemoteConfig> allList = ConfigStorageUtil.loadAll(this);
        allList.add(config);
        ConfigStorageUtil.saveAll(this, allList);

        Toast.makeText(this, "解析成功，共" + keyList.size() + "个按键", Toast.LENGTH_SHORT).show();
        finish();
    }
}