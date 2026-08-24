package com.example.customirremote.utils;

import android.content.Context;
import android.hardware.ConsumerIrManager;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;

public class IrTransmitUtil {
    private static final int CARRIER = 38000;
    private static final int LEAD_HIGH = 9000;
    private static final int LEAD_LOW = 4500;
    private static final int BIT_HIGH = 560;
    private static final int BIT0_LOW = 560;
    private static final int BIT1_LOW = 1690;
    private static final int STOP_HIGH = 560;

    public static void transmit(Context context, String fullHexCode) {
        ConsumerIrManager ir = (ConsumerIrManager) context.getSystemService(Context.CONSUMER_IR_SERVICE);
        if (ir == null || !ir.hasIrEmitter()) {
            Toast.makeText(context, "本机无红外发射硬件", Toast.LENGTH_SHORT).show();
            return;
        }
        ir.transmit(CARRIER, hexToNecPattern(fullHexCode));
    }

    private static int[] hexToNecPattern(String hex) {
        List<Integer> list = new ArrayList<>();
        list.add(LEAD_HIGH);
        list.add(LEAD_LOW);

        byte[] bytes = hexToBytes(hex);
        for (byte b : bytes) {
            for (int i = 0; i < 8; i++) {
                int bit = (b >> i) & 0x01;
                list.add(BIT_HIGH);
                list.add(bit == 1 ? BIT1_LOW : BIT0_LOW);
            }
        }
        list.add(STOP_HIGH);

        int[] arr = new int[list.size()];
        for (int i = 0; i < list.size(); i++) arr[i] = list.get(i);
        return arr;
    }

    private static byte[] hexToBytes(String hex) {
        int len = hex.length() / 2;
        byte[] bytes = new byte[len];
        for (int i = 0; i < len; i++) {
            int idx = i * 2;
            int h = Character.digit(hex.charAt(idx), 16);
            int l = Character.digit(hex.charAt(idx + 1), 16);
            bytes[i] = (byte) ((h << 4) | l);
        }
        return bytes;
    }
}