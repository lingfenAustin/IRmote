package com.example.customirremote.bean;

public class KeyItem {
    private String keyName;
    private String keyRawCode;

    public KeyItem(String keyName, String keyRawCode) {
        this.keyName = keyName;
        this.keyRawCode = keyRawCode;
    }

    public String getKeyName() { return keyName; }
    public void setKeyName(String keyName) { this.keyName = keyName; }
    public String getKeyRawCode() { return keyRawCode; }
    public void setKeyRawCode(String keyRawCode) { this.keyRawCode = keyRawCode; }
}