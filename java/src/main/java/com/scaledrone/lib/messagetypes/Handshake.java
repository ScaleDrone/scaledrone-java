package com.scaledrone.lib.messagetypes;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Handshake {
    public final String type = "handshake";
    public String channel;
    public Integer callback;
    @JsonProperty("client_data")
    public Object clientData; // should this be a string?

    public Handshake(String channel, Object clientData, Integer callback) {
        this.channel = channel;
        this.clientData = clientData;
        this.callback = callback;
    }
}
