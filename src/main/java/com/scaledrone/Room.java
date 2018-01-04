package com.scaledrone;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;

interface RoomListener {
    void onOpen(Room room);
    void onOpenFailure(Room room, Exception ex);
    void onMessage(Room room, JsonNode message);
}

interface ObservableRoomListener {
    void onMembers(Room room, ArrayList<Member> members);
    void onMemberJoin(Room room, Member member);
    void onMemberLeave(Room room, Member member);
}

public class Room {
    private String name;
    private RoomListener listener;
    private ObservableRoomListener observableListener;

    private Scaledrone scaledrone;

    public Room(String name, RoomListener listener, Scaledrone drone) {
        this.name = name;
        this.listener = listener;
        this.scaledrone = drone;
    }

    public void listenToObservableEvents(ObservableRoomListener listener) {
        this.observableListener = listener;
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

    public Scaledrone getScaledrone() {
        return scaledrone;
    }

    public void unsubscribe() {
        scaledrone.unsubscribe(this);
    }

}