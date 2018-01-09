package com.scaledrone.lib.messagetypes;

public class Unsubscribe {
    public final String type = "unsubscribe";
    public String room;
    public Integer callback;

    public Unsubscribe(String room, Integer callback) {
        this.room = room;
        this.callback = callback;
    }
}
