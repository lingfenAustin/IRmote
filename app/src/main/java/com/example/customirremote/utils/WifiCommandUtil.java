package com.example.customirremote.utils;

import android.content.Context;

public class WifiCommandUtil {
    public static void sendWifi(Context context, int n){
        try {
            int delay = 120;
            // 6 6
            send(context, "41FB8E71");
            Thread.sleep(delay);
            send(context, "41FB8E71");
            Thread.sleep(delay);
            // 8 8 8 8 8 8
            for(int i = 0; i < 6; i++){
                send(context, "41FB916E");
                Thread.sleep(delay);
            }
            // 等待界面切换
            Thread.sleep(delay);
            // 返回
            send(context, "41FBCC33");
            Thread.sleep(delay);
            // 下
            send(context, "41FB9D62");
            Thread.sleep(delay);
            // 下
            send(context, "41FB9D62");
            Thread.sleep(delay);
            if(n == 1){
                send(context, "41FB9D62");
                Thread.sleep(delay);
            }
            // OK
            send(context, "41FB9A65");
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private static void send(Context context, String code){
        IrTransmitUtil.transmit(context, code);
    }
}