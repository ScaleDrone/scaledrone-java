package com.scaledrone.lib;

import com.fasterxml.jackson.databind.JsonNode;

public interface RoomListener {
    void onOpen(Room room);
    void onOpenFailure(Room room, Exception ex);
    void onMessage(Room room, JsonNode message, Member member);
}
