package com.scaledrone.lib.messagetypes;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Subscribe {
    public final String type = "subscribe";
    public String room;
    public Integer callback;
    @JsonProperty("history_count")
    public Integer historyCount;
    @JsonProperty("history")
    public Integer history; // legacy property

    public Subscribe(String room, Integer historyCount, Integer callback) {
        this.room = room;
        this.historyCount = this.history = historyCount;
        this.callback = callback;
    }
}
