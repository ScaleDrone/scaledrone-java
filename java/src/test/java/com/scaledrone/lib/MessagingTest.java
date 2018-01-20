package com.scaledrone.lib;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.jodah.concurrentunit.Waiter;
import org.junit.Test;

public class MessagingTest {

    final String channel = System.getenv("AUTHLESS_CHANNEL");

    @Test
    public void stringMessage() throws Exception {
        final Waiter waiter = new Waiter();
        final Scaledrone drone = new Scaledrone(channel);
        drone.connect(new Listener() {
            @Override
            public void onOpen() {
                drone.subscribe("room1", new RoomListener() {
                    @Override
                    public void onOpen(Room room) {
                        room.publish("Hello there");
                    }

                    @Override
                    public void onOpenFailure(Room room, Exception ex) {
                        waiter.fail(ex.getMessage());
                    }

                    @Override
                    public void onMessage(Room room, JsonNode message, Member member) {
                        waiter.assertEquals("Hello there", message.asText());
                        waiter.assertEquals(drone.getClientID(), member.getId());
                        waiter.resume();
                    }
                });
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

    @Test
    public void objectMessage() throws Exception {
        final Waiter waiter = new Waiter();
        final Data data = new Data("bob", "red");
        final Scaledrone drone = new Scaledrone(channel);
        drone.connect(new Listener() {
            @Override
            public void onOpen() {
                drone.subscribe("room2", new RoomListener() {
                    @Override
                    public void onOpen(Room room) {
                        room.publish(data);
                    }

                    @Override
                    public void onOpenFailure(Room room, Exception ex) {
                        waiter.fail(ex.getMessage());
                    }

                    @Override
                    public void onMessage(Room room, JsonNode message, Member member) {
                        ObjectMapper mapper = new ObjectMapper();
                        try {
                            waiter.assertEquals(data, mapper.treeToValue(message, Data.class));
                            waiter.resume();
                        } catch (JsonProcessingException e) {
                            e.printStackTrace();
                        }
                    }
                });
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
