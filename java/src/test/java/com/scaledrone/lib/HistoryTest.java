package com.scaledrone.lib;

import net.jodah.concurrentunit.Waiter;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HistoryTest {

    final String channel = System.getenv("AUTHLESS_CHANNEL");

    @Test
    public void testHistory() throws Exception {
        final Waiter waiter = new Waiter();
        final Scaledrone drone = new Scaledrone(channel);
        final String roomName = "myHistoryRoom";
        final List<String> messages = new ArrayList<String>();
        messages.addAll(Arrays.asList("first", "second", "third"));
        drone.connect(new Listener() {
            @Override
            public void onOpen() {
                try {
                    for (String message : messages) {
                        drone.publish(roomName, message);
                        Thread.sleep(100);
                    }
                    Room room = drone.subscribe(roomName, new RoomListener() {
                        @Override
                        public void onOpen(Room room) {

                        }

                        @Override
                        public void onOpenFailure(Room room, Exception ex) {
                            waiter.fail(ex.getMessage());
                        }

                        @Override
                        public void onMessage(Room room, Message message) {

                        }
                    }, new SubscribeOptions(messages.size()));
                    room.listenToHistoryEvents(new HistoryRoomListener() {
                        @Override
                        public void onHistoryMessage(Room room, Message message) {
                            waiter.assertEquals(messages.get(0), message.getData().asText());
                            messages.remove(0);
                            if (messages.isEmpty()) {
                                waiter.resume();
                            }
                        }
                    });
                } catch (InterruptedException e) {
                    waiter.fail(e.getMessage());
                }
            }

            @Override
            public void onOpenFailure(Exception ex) {
                waiter.fail(ex.getMessage());
            }

            @Override
            public void onFailure(Exception ex) {
                waiter.fail(ex.getMessage());
            }

            @Override
            public void onClosed(String reason) {
                waiter.fail(reason);
            }
        });

        waiter.await(10000);
    }
}
