package com.example.customirremote.bean;

import java.util.List;

public class RemoteConfig {
    private String remoteName;
    private String headerCode;
    private List<KeyItem> keyList;

    public RemoteConfig(String remoteName, String headerCode, List<KeyItem> keyList) {
        this.remoteName = remoteName;
        this.headerCode = headerCode;
        this.keyList = keyList;
    }

    public String getRemoteName() { return remoteName; }
    public void setRemoteName(String remoteName) { this.remoteName = remoteName; }
    public String getHeaderCode() { return headerCode; }
    public void setHeaderCode(String headerCode) { this.headerCode = headerCode; }
    public List<KeyItem> getKeyList() { return keyList; }
    public void setKeyList(List<KeyItem> keyList) { this.keyList = keyList; }
}