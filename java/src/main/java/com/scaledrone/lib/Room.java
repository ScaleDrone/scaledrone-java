package com.scaledrone.lib;

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