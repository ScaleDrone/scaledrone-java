[![travis master branch build status](https://travis-ci.org/ScaleDrone/scaledrone-java.svg?branch=master)](https://travis-ci.org/ScaleDrone/scaledrone-java)

# Scaledrone Java/Android WebSocket client

> Use the Scaledrone Java client to connect to the Scaledrone realtime messaging service

This project goes hand to hand with [Scaledrone documentation](https://www.scaledrone.com/docs). It's still a work in progress, pull requests and issues are very welcome.

## Installation

[ ![Download](https://api.bintray.com/packages/scaledrone/scaledrone/scaledrone-java/images/download.svg) ](https://bintray.com/scaledrone/scaledrone/scaledrone-java/_latestVersion)

The library is hosted on the [Jcenter repository](https://bintray.com/scaledrone/scaledrone/scaledrone-java), so you need to ensure that the repo is referenced also; IDEs will typically include this by default:

```
repositories {
	jcenter()
}
```

### Maven

```xml
<dependency>
  <groupId>com.scaledrone</groupId>
  <artifactId>scaledrone-java</artifactId>
  <version>0.7.0</version>
  <type>pom</type>
</dependency>
```

### Gradle

```
compile 'com.scaledrone:scaledrone-java:0.7.0'
```

## Android

If you are using this library on Android make sure you add the [INTERNET](https://developer.android.com/training/basics/network-ops/connecting.html) permission to the manifest file.
```xml
<uses-permission android:name="android.permission.INTERNET" />
```

## Getting started

### Basic example

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
            public void onMessage(Room room, Message message) {
                System.out.println("Message: " + message.getData().asText());
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
    public void onMessage(Room room, Message message) {
        // parse as string
        System.out.println("Message: " + message.getData().asText());

        // or parse as POJO
        try {
            ObjectMapper mapper = new ObjectMapper();
            Pojo pojo = mapper.treeToValue(message.getData(), Pojo.class);
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

## Message history

When creating a Scaledrone room you can supply the number of messages to recieve from that room's history. The messages will arrive, in reverse chronological order and one by one, in `scaledroneRoomDidReceiveMessage`, just like real-time messages.

Pass the `SubscribeOptions` as the second parameter to the `subscribe` method to define how many messages you would like to receive.

You can then listen to the history messages using `room.listenToHistoryEvents()`.

In order to recieve message history messages, this feature needs to be enabled in the [Scaledrone dashboard](http://dashboard.scaledrone.com). You can learn more about Message History and its limitations in [Scaledrone docs](https://www.scaledrone.com/docs/message-history).

```java

Room room = drone.subscribe(roomName, new RoomListener() {
    // implement the default RoomListener methods here
}, new SubscribeOptions(50)); // ask for 50 messages from the history

room.listenToHistoryEvents(new HistoryRoomListener() {
    @Override
    public void onHistoryMessage(Room room, Message message) {
        System.out.println('Received a message from the past ' + message.getData().asText());
    }
});
```

## Checking if the messages was sent by the user itself

```java
// inside a room listener
@Override
public void onMessage(Room room, Message message) {
    if (message.getClientID() == scaledrone.getClientID()) {
        // message is sent by session user
    }
}
```

## Reconnecting
The currect version of the Java library doesn't reconnect automatically after a possible disconnect. This feature will be added in the future.
To handle reconnection you can listen to the `onFailure` event.
```java
@Override
public void onFailure(Exception ex) {
    tryReconnecting(0);
}

private void tryReconnecting(final int reconnectAttempt) {
    Timer timer = new Timer();
    timer.schedule(new TimerTask() {
        @Override
        public void run() {
            final Scaledrone drone = new Scaledrone("channel-id");
            drone.connect(new Listener() {
                @Override
                public void onFailure(Exception ex) {
                    tryReconnecting(reconnectAttempt + 1);
                }
            });
            // set everything up again..
        }
    }, reconnectAttempt * 1000);
}
```

## Troubleshooting
```
PKIX path building failed: sun.security.provider.certpath.SunCertPathBuilderException: unable to find valid certification path to requested target
```
This likely means that your Java doesn't support Let's Encrypt Certificates. Upgrade Java7 to 7u111 or newer or Java 8 to 8u101 or newer. [Read more..](https://stackoverflow.com/questions/34110426/does-java-support-lets-encrypt-certificates/35454903)

## Publishing a new version to JCenter

* Update the version 1.2.3 in `build.gradle`
* Run `./gradlew java:assembleRelease` to generate the new release files
* Go to https://bintray.com/scaledrone/scaledrone/scaledrone-java and create a new version
* Click "Upload Files"
* Type in `com/scaledrone/scaledrone-java/1.2.3` into "Target Repository Path" ensuring the correct version is included.
* Upload all the files from `java/build/release/1.2.3/com/scaledrone/scaledrone-java/1.2.3`
* You will see a notice "You have 8 unpublished item(s) for this version", click "Publish". It might take a few minutes
* Update the README with the correct version

## Changelog

* `0.7.0` - Fixed parsing 'id' field from JSON message.
* `0.6.0` - Added message history features. Created a `Message` class that wraps the sent data, member as well as new properties such as message ID, timestamp and clientID.
* `0.5.0` - Add up `close()` method.
* `0.4.0` - Hook up `onFailure` listener. This can be used for reconnecting.
* `0.3.0` - Add `member` parameter to `onMessage` listener method.
