package com.example.customirremote;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.customirremote.bean.KeyItem;
import com.example.customirremote.bean.RemoteConfig;
import com.example.customirremote.utils.ConfigStorageUtil;
import com.example.customirremote.utils.HexInvertUtil;
import com.example.customirremote.utils.WifiCommandUtil;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.List;
import androidx.appcompat.app.AlertDialog;

public class MainActivity extends AppCompatActivity {
    private List<RemoteConfig> configList;
    private RemoteAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView rv = findViewById(R.id.rv_remote_list);
        rv.setLayoutManager(new LinearLayoutManager(this));
        configList = ConfigStorageUtil.loadAll(this);
        adapter = new RemoteAdapter();
        rv.setAdapter(adapter);
        bindWifiButton();

        FloatingActionButton fab = findViewById(R.id.fab_add);
        fab.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("新建遥控")
                    .setItems(new String[]{"解析遥控配置", "手动新建", "测试按键"}, (dialog, which) -> {
                        switch (which) {
                            case 0:
                                // 解析遥控配置
                                startActivity(new Intent(this, ParseRemoteActivity.class));
                                break;
                            case 1:
                                // 手动新建，跳转原编辑页
                                Intent intent = new Intent(this, EditRemoteActivity.class);
                                intent.putExtra("index", -1);
                                startActivity(intent);
                                break;
                            case 2:
                                // 测试按键
                                startActivity(new Intent(this, TestKeyActivity.class));
                                break;
                        }
                    })
                    .show();
        });
    }

    private void bindWifiButton(){
        MaterialButton btnWifi = findViewById(R.id.btn_wifi);
        if(btnWifi == null){
            return;
        }

        btnWifi.setOnClickListener(v -> {
            WifiCommandUtil.sendWifi(MainActivity.this);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        configList = ConfigStorageUtil.loadAll(this);
        adapter.notifyDataSetChanged();
    }

    class RemoteAdapter extends RecyclerView.Adapter<RemoteAdapter.VH> {
        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(MainActivity.this).inflate(R.layout.item_remote, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int position) {
            RemoteConfig config = configList.get(position);
            h.tvName.setText(config.getRemoteName());
            h.tvHeader.setText("头码：" + config.getHeaderCode());

            h.btnEnter.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, RemoteControlActivity.class);
                intent.putExtra("index", position);
                startActivity(intent);
            });

            h.btnEdit.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, EditRemoteActivity.class);
                intent.putExtra("index", position);
                startActivity(intent);
            });

            h.btnDelete.setOnClickListener(v -> {
                configList.remove(position);
                ConfigStorageUtil.saveAll(MainActivity.this, configList);
                notifyItemRemoved(position);
            });
        }

        @Override
        public int getItemCount() { return configList.size(); }

        class VH extends RecyclerView.ViewHolder {
            TextView tvName, tvHeader;
            Button btnEnter, btnEdit, btnDelete;
            VH(View v) {
                super(v);
                tvName = v.findViewById(R.id.tv_name);
                tvHeader = v.findViewById(R.id.tv_header);
                btnEnter = v.findViewById(R.id.btn_enter);
                btnEdit = v.findViewById(R.id.btn_edit);
                btnDelete = v.findViewById(R.id.btn_delete);
            }
        }
    }
}