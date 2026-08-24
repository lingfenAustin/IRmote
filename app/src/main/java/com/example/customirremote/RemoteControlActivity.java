package com.example.customirremote;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.customirremote.bean.KeyItem;
import com.example.customirremote.bean.RemoteConfig;
import com.example.customirremote.utils.ConfigStorageUtil;
import com.example.customirremote.utils.HexInvertUtil;
import com.example.customirremote.utils.IrTransmitUtil;
import com.example.customirremote.utils.WifiCommandUtil;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class RemoteControlActivity extends AppCompatActivity {
    private RemoteConfig config;
    private GridView gvKeys;
    private KeyAdapter adapter;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable repeatRunnable;
    private String currentFullCode;

    //顶部固定按钮
    private final Map<String,Integer> fixedBtnMap =
            new HashMap<String,Integer>(){{
                put("KEY_POWER", R.id.btn_power);
                put("KEY_UP", R.id.btn_up);
                put("KEY_DOWN", R.id.btn_down);
                put("KEY_LEFT", R.id.btn_left);
                put("KEY_RIGHT", R.id.btn_right);
                put("KEY_ENTER", R.id.btn_ok);
                put("KEY_VOLUMEUP", R.id.btn_vol_up);
                put("KEY_VOLUMEDOWN", R.id.btn_vol_down);
                put("KEY_CHANNELUP", R.id.btn_ch_up);
                put("KEY_CHANNELDOWN", R.id.btn_ch_down);
                put("KEY_BACK", R.id.btn_back);
                put("KEY_SL_FAC_FAC", R.id.btn_fac);
            }};

    static class KeyDisplayItem{
        String showName;
        String fullCode;
        KeyItem origin;
        KeyDisplayItem(String name, String code, KeyItem item){
            showName = name;
            fullCode = code;
            origin = item;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remote_control);
        Toolbar toolbar =
                findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(
                v -> finish()
        );
        gvKeys = findViewById(R.id.gv_keys);
        int index = getIntent().getIntExtra("index",-1);
        List<RemoteConfig> listAll = ConfigStorageUtil.loadAll(this);
        if(index < 0 || index >= listAll.size()){
            finish();
            return;
        }
        config = listAll.get(index);
        setTitle(config.getRemoteName());
        //绑定顶部固定按键
        bindFixedButtons();
        bindWifiButton();
        //下面动态按键
        List<KeyDisplayItem> list = buildGridItems();
        adapter = new KeyAdapter(this, list);
        gvKeys.setAdapter(adapter);
    }

    /**
     * 顶部固定按钮绑定
     */
    private void bindFixedButtons(){
        for(Map.Entry<String,Integer> entry : fixedBtnMap.entrySet()){
            String keyName = entry.getKey();
            MaterialButton btn = findViewById(entry.getValue());
            KeyItem item = findKeyByKeyName(keyName);
            if(item == null){
                btn.setEnabled(false);
                btn.setAlpha(0.3f);
                continue;
            }

            String fullCode = HexInvertUtil.getFullIrCode(item.getKeyRawCode(), config.getHeaderCode());
            bindButtonTouch(btn, fullCode);
        }
    }

    private void bindWifiButton(){
        MaterialButton btnWifi1 = findViewById(R.id.btn_wifi1);
        MaterialButton btnWifi2 = findViewById(R.id.btn_wifi2);
        if(btnWifi1 == null || btnWifi2 == null){
            return;
        }
        btnWifi1.setOnClickListener(v -> {
            WifiCommandUtil.sendWifi(RemoteControlActivity.this,1);
        });
        btnWifi2.setOnClickListener(v -> {
            WifiCommandUtil.sendWifi(RemoteControlActivity.this,2);
        });

    }

    /**
     * 查找按键
     * 兼容 KEY_POWER / POWER
     */
    private KeyItem findKeyByKeyName(String keyName){
        String target = normalizeKeyName(keyName);
        for(KeyItem item : config.getKeyList()){
            String name = normalizeKeyName(item.getKeyName());
            if(name.equals(target)){
                return item;
            }
        }
        return null;
    }

    private String normalizeKeyName(String name){
        name = name.toUpperCase()
                .replace("KEY_", "")
                .replace("TV_", "")
                .replace("_", "");
        //方向键兼容
        if(name.equals("KEY UP")){name = "UP";}
        if(name.equals("KEY DOWN")){name = "DOWN";}
        if(name.equals("KEY LEFT")){name = "LEFT";}
        if(name.equals("KEY RIGHT")){name = "RIGHT";}
        //确认键兼容
        if(name.equals("KEY ENTER")){name = "ENTER";}
        //音量兼容
        if(name.equals("VOL+")){name = "VOLUMEUP";}
        if(name.equals("VOL-")){name = "VOLUMEDOWN";}
        //频道兼容
        if(name.equals("CH+")){name = "CHANNELUP";}
        if(name.equals("CH-")){name = "CHANNELDOWN";}
        if(name.equals("KEY BACK")){name = "Exit";}
        if(name.equals("KEY SL_FAC_FAC")){name = "工厂";}
        return name;
    }

    /**
     * 生成下面GridView列表
     * 自动过滤顶部固定键
     */
    private List<KeyDisplayItem> buildGridItems(){
        List<KeyDisplayItem> result = new ArrayList<>();
        List<String> fixedNames = new ArrayList<>();
        for(String key : fixedBtnMap.keySet()){
            fixedNames.add(key.replace("KEY_", ""));
            fixedNames.add(key.replace("TV_", ""));
        }

        for(KeyItem item : config.getKeyList()){
            String name = item
                    .getKeyName()
                    .toUpperCase()
                    .replace("KEY_", "")
                    .replace("TV_", "");
            //过滤顶部已有按键
            if(fixedNames.contains(name)){
                continue;
            }
            String code = HexInvertUtil.getFullIrCode(item.getKeyRawCode(), config.getHeaderCode());
            if(name.equals("RED")){name = "红";}
            else if(name.equals("GREEN")){name = "绿";}
            else if(name.equals("YELLOW")){name = "黄";}
            else if(name.equals("BLUE")){name = "蓝";}
            else if(name.equals("SOUNDMODE")){name = "S.Mode";}
            else if(name.equals("PICTUREMODE")){name = "P.Mode";}
            else if(name.equals("MENU")){name = "菜单";}
            else if(name.equals("MUTE")){name = "🔕";}
            else if(name.equals("SUBTITLE")){name = "SUBT";}
            else if(name.equals("SL_FAC_BURN")){name = "老化";}
            else if(name.equals("SL_FAC_RESET")){name = "复位";}
            else if(name.equals("BLUETOOTH")){name = "蓝牙";}
            else if(name.equals("MOUSE")){name = "🖱";}
            else if(name.equals("PLAYPAUSE")){name = "⏯";}
            else if(name.equals("STOP")){name = "⏹";}
            else if(name.equals("NEXTSONG")){name = "⏭";}
            else if(name.equals("PREVIOUSSONG")){name = "⏮";}
            else if(name.equals("REWIND")){name = "⏪";}
            else if(name.equals("FASTFORWARD")){name = "⏩";}
            else if(name.equals("SLEEPMODE")){name = "SLEEP";}
            else if(name.equals("SUBCODE")){name = "SUB";}
            else if(name.equals("SL_FREEZE")){name = "FREEZE";}
            else if(name.equals("SCREENSHOT")){name = "截图";}
            result.add(new KeyDisplayItem(name, code, item));
        }
        return result;
    }

    /**
     * 按键按下发送
     * 支持长按
     */
    @SuppressLint("ClickableViewAccessibility")
    private void bindButtonTouch(MaterialButton btn, String code){
        btn.setOnTouchListener((v,event)->{
                    switch(event.getAction()){
                        case MotionEvent.ACTION_UP:
                            currentFullCode = code;
                            IrTransmitUtil.transmit(RemoteControlActivity.this, code);
//                            startRepeat();
                            v.setPressed(true);
                            return true;
                        case MotionEvent.ACTION_DOWN:
                        case MotionEvent.ACTION_CANCEL:
                            stopRepeat();
                            v.setPressed(false);
                            return true;
                    }
                    return false;
                });

    }

//    private void startRepeat(){
//        stopRepeat();
//        repeatRunnable = new Runnable(){
//                    @Override
//                    public void run(){
//                        if(currentFullCode != null){
//                            IrTransmitUtil.transmit(RemoteControlActivity.this, currentFullCode);
//                            handler.postDelayed(this, 110);
//                        }
//                    }
//                };
//        handler.postDelayed(repeatRunnable, 110);
//    }

    private void stopRepeat(){
        if(repeatRunnable != null){
            handler.removeCallbacks(repeatRunnable);
            repeatRunnable = null;
        }
        currentFullCode = null;
    }

    private class KeyAdapter extends BaseAdapter{
        private final Context context;
        private final List<KeyDisplayItem> list;
        KeyAdapter(Context c, List<KeyDisplayItem> l){
            context = c;
            list = l;
        }

        @Override
        public int getCount(){
            return list.size();
        }

        @Override
        public Object getItem(int position){
            return list.get(position);
        }

        @Override
        public long getItemId(int position){
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent){
            if(convertView == null){
                convertView = LayoutInflater
                                .from(context)
                                .inflate(R.layout.item_key, parent, false);
            }

            MaterialButton btn = convertView.findViewById(R.id.btn_item);
            KeyDisplayItem item = list.get(position);
            btn.setText(item.showName);
            bindButtonTouch(btn, item.fullCode);
            return convertView;
        }
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        stopRepeat();
    }
}