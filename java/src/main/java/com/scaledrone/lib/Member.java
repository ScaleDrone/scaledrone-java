package com.scaledrone.lib;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Member {

    private String id;
    private JsonNode authData;
    private JsonNode clientData;

    public String getId() {
        return id;
    }

    public JsonNode getAuthData() {
        return authData;
    }

    public JsonNode getClientData() {
        return clientData;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "Member{" +
                "id='" + id + '\'' +
                ", authData=" + authData +
                ", clientData=" + clientData +
                '}';
    }
}
