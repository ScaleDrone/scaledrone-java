package com.scaledrone.lib.messagetypes;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GenericCallback {
    // common fields
    private String type;
    private Integer callback;
    private String error;

    // message specific fields
    @JsonProperty("id")
    private String ID; // message ID
    private long timestamp; // unix timestamp of when the message was sent
    @JsonProperty("client_id")
    private String clientID; // the ID of the client who sent the message
    private String room; // the room to which the message was sent
    private JsonNode message; // the data content of the message
    private JsonNode data; // JSON object or array of objects
    private int index; // index of the message in history

    public String getType() {
        return type;
    }

    public Integer getCallback() {
        return callback;
    }

    public String getError() {
        return error;
    }

    public String getClientID() {
        return clientID;
    }

    public String getRoom() {
        return room;
    }

    public JsonNode getMessage() {
        return message;
    }

    public JsonNode getData() {
        return data;
    }

    public String getID() {
        return ID;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public int getIndex() {
        return index;
    }

    @Override
    public String toString() {
        return "GenericCallback{" +
                "type='" + type + '\'' +
                ", callback=" + callback +
                ", error='" + error + '\'' +
                ", ID='" + ID + '\'' +
                ", timestamp=" + timestamp +
                ", clientID='" + clientID + '\'' +
                ", room='" + room + '\'' +
                ", message=" + message +
                ", data=" + data +
                ", index=" + index +
                '}';
    }
}