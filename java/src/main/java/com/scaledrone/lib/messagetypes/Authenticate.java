package com.scaledrone.lib.messagetypes;

public class Authenticate {
    public final String type = "authenticate";
    public String token;
    public Integer callback;

    public Authenticate(String token, Integer callback) {
        this.token = token;
        this.callback = callback;
    }
}