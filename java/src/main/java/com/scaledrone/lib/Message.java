package com.scaledrone.lib;

import com.fasterxml.jackson.databind.JsonNode;

public class Message {
    private String ID;
    private JsonNode data;
    private long timestamp;
    private String clientID;
    private Member member;

    public Message(String ID, JsonNode data, long timestamp, String clientID, Member member) {
        this.ID = ID;
        this.data = data;
        this.timestamp = timestamp;
        this.clientID = clientID;
        this.member = member;
    }

    public String getID() {
        return ID;
    }

    public JsonNode getData() {
        return data;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getClientID() {
        return clientID;
    }

    public Member getMember() {
        return member;
    }

    @Override
    public String toString() {
        return "Message{" +
                "ID='" + ID + '\'' +
                ", data=" + data +
                ", timestamp=" + timestamp +
                ", clientID='" + clientID + '\'' +
                ", member=" + member +
                '}';
    }
}
