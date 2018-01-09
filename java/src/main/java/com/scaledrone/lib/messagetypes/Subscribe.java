package com.scaledrone.lib.messagetypes;

public class Subscribe {
    public final String type = "subscribe";
    public String room;
    public Integer callback;

    public Subscribe(String room, Integer callback) {
        this.room = room;
        this.callback = callback;
    }
}
