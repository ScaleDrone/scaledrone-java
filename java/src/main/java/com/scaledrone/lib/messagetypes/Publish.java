package com.scaledrone.lib.messagetypes;

public class Publish {
    public final String type = "publish";
    public String room;
    public Object message;

    public Publish(String room, Object message) {
        this.room = room;
        this.message = message;
    }
}
