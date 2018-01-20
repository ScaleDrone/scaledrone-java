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
    @JsonProperty("client_id")
    private String clientID;
    private String room;
    private JsonNode message;
    private JsonNode data; // JSON object or array of objects

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

    @Override
    public String toString() {
        return "GenericCallback{" +
                "type='" + type + '\'' +
                ", callback=" + callback +
                ", error='" + error + '\'' +
                ", clientID='" + clientID + '\'' +
                ", room='" + room + '\'' +
                ", message=" + message +
                ", data=" + data +
                '}';
    }
}