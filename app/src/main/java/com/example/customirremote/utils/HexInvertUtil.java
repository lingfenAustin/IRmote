package com.example.customirremote.utils;

import android.util.Log;

import java.util.HashMap;
import java.util.Map;

public class HexInvertUtil {
    private static final Map<Character, Character> INVERT_MAP = new HashMap<>();
    static {
        INVERT_MAP.put('0', 'F'); INVERT_MAP.put('1', 'E');
        INVERT_MAP.put('2', 'D'); INVERT_MAP.put('3', 'C');
        INVERT_MAP.put('4', 'B'); INVERT_MAP.put('5', 'A');
        INVERT_MAP.put('6', '9'); INVERT_MAP.put('7', '8');
        INVERT_MAP.put('8', '7'); INVERT_MAP.put('9', '6');
        INVERT_MAP.put('A', '5'); INVERT_MAP.put('B', '4');
        INVERT_MAP.put('C', '3'); INVERT_MAP.put('D', '2');
        INVERT_MAP.put('E', '1'); INVERT_MAP.put('F', '0');
    }

    // 计算2位按键原始码的反码
    public static String getKeyInvertCode(String rawCode) {
        rawCode = rawCode.toUpperCase().trim();
        StringBuilder sb = new StringBuilder();
        for (char c : rawCode.toCharArray()) {
            sb.append(INVERT_MAP.get(c));
        }
        return sb.toString();
    }

    // 组装完整8位发射码：反码(2位) + 原始按键(2位) + 头码(4位)
    public static String getFullIrCode(String rawKeyCode, String headerCode) {
        String header = headerCode.toUpperCase().trim();
        // 头码4位：前后两位对调（高低字节交换）
        String swappedHeader = header.length() >= 4
                ? header.substring(2) + header.substring(0, 2)
                : header;
        String rawKey = rawKeyCode.toUpperCase().trim();
        String invertKey = getKeyInvertCode(rawKey);
        return swappedHeader + rawKey + invertKey;
    }

    public static String getFullIrCode(String rawKeyCode) {
        String header = "FB41";
        // 头码4位：前后两位对调（高低字节交换）
        String swappedHeader = header.length() >= 4
                ? header.substring(2) + header.substring(0, 2)
                : header;
        String rawKey = rawKeyCode.toUpperCase().trim();
        String invertKey = getKeyInvertCode(rawKey);
        Log.d("TAG", "swappedHeader + rawKey + invertKey:" + swappedHeader + rawKey + invertKey);
        return swappedHeader + rawKey + invertKey;
    }

    // 过滤十六进制输入，限制长度
    public static String filterHex(String input, int maxLen) {
        StringBuilder sb = new StringBuilder();
        for (char c : input.toUpperCase().toCharArray()) {
            if ((c >= '0' && c <= '9') || (c >= 'A' && c <= 'F')) {
                sb.append(c);
            }
        }
        return sb.length() > maxLen ? sb.substring(0, maxLen) : sb.toString();
    }


}