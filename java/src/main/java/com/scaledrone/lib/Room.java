package com.scaledrone.lib;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Room {
    private String name;
    private RoomListener listener;
    private ObservableRoomListener observableListener;
    private HistoryRoomListener historyListener;
    private Map<String, Member> members = new HashMap<String, Member>();
    private Scaledrone scaledrone;

    private SubscribeOptions options;
    private int historyReceivedCount;
    private int historyNextIndex;
    private Message[] historyReceivedMessages;

    public Room(String name, RoomListener listener, Scaledrone drone, SubscribeOptions options) {
        this.name = name;
        this.listener = listener;
        this.scaledrone = drone;
        this.options = options;
        if (options.getHistoryCount() > 0) {
            this.historyReceivedMessages = new Message[options.getHistoryCount()];
        }
    }

    public void handleHistoryMessage(Message message, int index) {
        this.historyReceivedMessages[index] = message;
        Message nextMessage = this.historyReceivedMessages[this.historyNextIndex];
        if (nextMessage != null) {
            this.historyNextIndex++;
            if (this.getHistoryListener() != null) {
                this.getHistoryListener().onHistoryMessage(this, nextMessage);
            }
        }
    }

    public void listenToObservableEvents(ObservableRoomListener listener) {
        this.observableListener = listener;
    }

    public void listenToHistoryEvents(HistoryRoomListener listener) {
        this.historyListener = listener;
    }

    public void publish(Object message) {
        this.scaledrone.publish(this.getName(), message);
    }

    public String getName() {
        return name;
    }

    public RoomListener getListener() {
        return listener;
    }

    public ObservableRoomListener getObservableListener() {
        return observableListener;
    }

    public HistoryRoomListener getHistoryListener() {
        return historyListener;
    }

    public Scaledrone getScaledrone() {
        return scaledrone;
    }

    public Map<String, Member> getMembers() {
        return members;
    }

    public void unsubscribe() {
        scaledrone.unsubscribe(this);
    }

}