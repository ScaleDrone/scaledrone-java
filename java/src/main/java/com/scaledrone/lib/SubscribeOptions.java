package com.scaledrone.lib;

public class SubscribeOptions {
    private Integer historyCount;

    public SubscribeOptions(Integer historyCount) {
        this.historyCount = historyCount;
    }

    public void setHistoryCount(int historyCount) {
        this.historyCount = historyCount;
    }

    public Integer getHistoryCount() {
        return historyCount;
    }
}
