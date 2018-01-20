package com.scaledrone.lib;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.jodah.concurrentunit.Waiter;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Objects;

public class ObservableRoomsTest {

    private Scaledrone friend;
    final String channel = System.getenv("AUTHLESS_CHANNEL");

    @Test
    public void testPublish() throws Exception {
        final Waiter waiter = new Waiter();

        final Data data = new Data("Android", "#ff0000");
        final Scaledrone drone = new Scaledrone(channel, data);
        drone.connect(new Listener() {
            @Override
            public void onOpen() {
                drone.subscribe("observable-room1", new RoomListener() {
                    @Override
                    public void onOpen(Room room) {
                        drone.publish("observable-room1", data);
                    }

                    @Override
                    public void onOpenFailure(Room room, Exception ex) {

                    }

                    @Override
                    public void onMessage(Room room, JsonNode message, Member member) {
                        waiter.assertEquals(drone.getClientID(), member.getId());
                        ObjectMapper mapper = new ObjectMapper();
                        try {
                            Data d = mapper.treeToValue(member.getClientData(), Data.class);
                            waiter.assertEquals(data, d);
                            waiter.resume();
                        } catch (JsonProcessingException e) {
                            waiter.fail(e);
                        }
                    }
                });
            }

            @Override
            public void onOpenFailure(Exception ex) {

            }

            @Override
            public void onFailure(Exception ex) {

            }

            @Override
            public void onClosed(String reason) {

            }
        });

        waiter.await(10000, 1);
    }

    @Test
    public void testEvents() throws Exception {
        final Waiter waiter = new Waiter();

        final ObservableRoomListener silentListener = new ObservableRoomListener() {

            @Override
            public void onMembers(Room room, ArrayList<Member> members) {

            }

            @Override
            public void onMemberJoin(Room room, Member member) {

            }

            @Override
            public void onMemberLeave(Room room, Member member) {

            }
        };

        final Data data = new Data("Android", "#ff0000");
        connectNewScaledroneInstance(data, waiter, new ObservableRoomListener() {
            @Override
            public void onMembers(Room room, ArrayList<Member> members) {
                friend = connectNewScaledroneInstance(new Data("Java", "#00ff00"), waiter, silentListener);

                waiter.assertEquals(1, members.size());
                Member member = members.get(0);
                waiter.assertEquals(room.getScaledrone().getClientID(), member.getId());
                waiter.assertNotNull(member.getClientData());
                waiter.assertNull(member.getAuthData());
                ObjectMapper mapper = new ObjectMapper();
                try {
                    Data d = mapper.treeToValue(member.getClientData(), Data.class);
                    waiter.assertEquals(data, d);
                    waiter.resume();
                } catch (JsonProcessingException e) {
                    waiter.fail(e);
                }
            }

            @Override
            public void onMemberJoin(Room room, Member member) {
                friend.unsubscribe(room);
                waiter.assertTrue(room.getScaledrone().getClientID() != member.getId());
                waiter.assertNotNull(member.getClientData());
                waiter.assertNull(member.getAuthData());
                ObjectMapper mapper = new ObjectMapper();
                try {
                    Data d = mapper.treeToValue(member.getClientData(), Data.class);
                    waiter.assertEquals("Java", d.getName());
                    waiter.assertEquals("#00ff00", d.getColor());
                    waiter.resume();
                } catch (JsonProcessingException e) {
                    waiter.fail(e);
                }
            }

            @Override
            public void onMemberLeave(Room room, Member member) {
                waiter.assertEquals(friend.getClientID(), member.getId());
                waiter.resume();
            }
        });

        waiter.await(10000, 3);
    }

    private Scaledrone connectNewScaledroneInstance(Data data, final Waiter waiter, final ObservableRoomListener listener) {
        final Scaledrone drone = new Scaledrone(channel, data);
        waiter.assertEquals(null, drone.getClientID());
        drone.connect(new Listener() {
            @Override
            public void onOpen() {
                waiter.assertNotNull(drone.getClientID());
                Room room = drone.subscribe("observable-room2", new RoomListener() {
                    @Override
                    public void onOpen(Room room) {

                    }

                    @Override
                    public void onOpenFailure(Room room, Exception ex) {
                        waiter.fail(ex.getMessage());
                    }

                    @Override
                    public void onMessage(Room room, JsonNode message, Member member) {

                    }
                });
                room.listenToObservableEvents(listener);
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
        return drone;
    }
}

class Data {
    private String name;
    private String color;

    public Data() {

    }

    public Data(String name, String color) {
        this.name = name;
        this.color = color;
    }

    public String getName() {
        return name;
    }

    public String getColor() {
        return color;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Data data = (Data) o;
        return Objects.equals(name, data.name) && Objects.equals(color, data.color);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, color);
    }

    @Override
    public String toString() {
        return "Data{" +
                "name='" + name + '\'' +
                ", color='" + color + '\'' +
                '}';
    }
}
