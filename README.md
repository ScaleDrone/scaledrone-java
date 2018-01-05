![travis master branch build status](https://travis-ci.org/ScaleDrone/scaledrone-java.svg?branch=master)

# Scaledrone Java/Android WebSocket client

> Use the Scaledrone Java client to connect to the Scaledrone realtime messaging service

This project goes hand to hand with [Scaledrone documentation](https://www.scaledrone.com/docs). It's still a work in progress, pull requests and issues are very welcome.

## Installation

### Gradle
_TODO_
### Maven
_TODO_

## Getting started

```java
final Scaledrone drone = new Scaledrone("CHANNEL_ID_FROM_DASHBOARD");
drone.connect(new Listener() {
    @Override
    public void onOpen() {
        drone.subscribe("myroom", new RoomListener() {
            @Override
            public void onOpen(Room room) {
                room.publish("Hello world");
            }

            @Override
            public void onOpenFailure(Room room, Exception ex) {
                // This can happen when you don't have correct permissions
                System.out.println("Failed to subscribe to room: " + ex.getMessage());
            }

            @Override
            public void onMessage(Room room, JsonNode message) {
                System.out.println("Message: " + message.asText());
            }
        });
    }

    @Override
    public void onOpenFailure(Exception ex) {
        System.out.println("Failed to open connection: " + ex.getMessage());
    }

    @Override
    public void onFailure(Exception ex) {
        System.out.println("Unexcpected failure: " + ex.getMessage());
    }

    @Override
    public void onClosed(String reason) {
        System.out.println("Connection closed: " + reason);
    }
});
```

### Connection

Connecting to a new channel is done by creating a new instance of `Scaledrone` and calling the `connect()` method. To create your own `channel_id` create a Scaledrone account and a new channel from [Scaledrone dashboard](https://dashboard.scaledrone.com/channels).

```java
final Scaledrone drone = new Scaledrone("channel_id");
drone.connect(new Listener() {
    // overwrite the Listener methods
});
```

### Subscribing to messages

Subscribing to messages is done by subscribing to a room. Rooms are used to group users into groups, to which we can then publish messages. You can subscribe to multiple rooms.

A received message is of type `JsonNode`. Jackson `JsonNode` can easily be transferred into POJO or String.

```java
drone.subscribe("myroom", new RoomListener() {
    @Override
    public void onMessage(Room room, JsonNode message) {
        // parse as string
        System.out.println("Message: " + message.asText());

        // or parse as POJO
        try {
            ObjectMapper mapper = new ObjectMapper();
            Pojo pojo = mapper.treeToValue(message, Pojo.class);
            System.out.println("Message: " + pojo);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }
    // overwrite other methods
});
```

### Publishing messages

A published message will be converted to JSON using Jackson. You don't have to be subscribed to a room when publishing to it.

```java
drone.publish("roomName", data);
// or
room.publish(data);
```

## Authentication

Using authentication is optional, it can be enabled from the channel settings page. Scaledrone uses JSON Web Token for authentication.

A typical authentication flow happens after connecting to a channel. At that point the user would send the `drone.getClientID()` to your personal authentication server which returns a JSON Web Token. The token is then passed on to Scaledrone.

Use the [jwt.io debugger](https://jwt.io) to help you debug your JWT tokens.

```java
final Scaledrone drone = new Scaledrone("channel_id");
drone.connect(new Listener() {
    @Override
    public void onOpen() {
        final String jwt = requestJWTFromYourAuthServer(drone.getClientID());
        drone.authenticate(jwt, new AuthenticationListener() {
            @Override
            public void onAuthentication() {
                // user is now connected to the channel
            }

            @Override
            public void onAuthenticationFailure(Exception ex) {
                // something went wront, probably a problem with the JWT
            }
        });
    }
    // overwrite methods
```

See the Scaledrone [JWT authentication docs](https://www.scaledrone.com/docs/jwt-authentication) on what the token should look like.
See the [authentication tests](https://github.com/ScaleDrone/scaledrone-java/blob/master/src/test/java/com/scaledrone/AuthTest.java#L159-L178) to see how to generate a JWT on the server side.

## Observable rooms

Observable rooms act like regular rooms but provide additional events for keeping track of connected members.
Observable room names need to be prefixed with `observable-`.

```java
final Scaledrone drone = new Scaledrone("channel_id", data);
drone.connect(new Listener() {
    @Override
    public void onOpen() {
        Room room = drone.subscribe("observable-myroom", new RoomListener() {
            // Overwrite regular room listener methods
        });
        room.listenToObservableEvents(listener, new ObservableRoomListener() {
            @Override
            public void onMembers(Room room, ArrayList<Member> members) {
                // Emits an array of members that have joined the room. This event is only triggered once, right after the user has successfully connected to the observable room.
                // Keep in mind that the session user will also be part of this array, so the minimum size of the array is 1

                Member member = members.get(0);
                ObjectMapper mapper = new ObjectMapper();
                try {
                    Data d = mapper.treeToValue(member.getClientData(), Data.class);
                } catch (JsonProcessingException e) {

                }
            }

            @Override
            public void onMemberJoin(Room room, Member member) {
                // A new member joined the room.
            }

            @Override
            public void onMemberLeave(Room room, Member member) {
                // A member left the room (or disconnected)
            });
        });
    }
```
