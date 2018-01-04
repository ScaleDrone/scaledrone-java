package com.scaledrone;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.scaledrone.messagetypes.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

interface Listener {
    void onOpen();
    void onOpenFailure(Exception ex);
    void onFailure(Exception ex);
    void onClosed(String reason);
}

interface AuthenticationListener {
    void onAuthentication();
    void onAuthenticationFailure(Exception ex);
}

interface CallbackHandler {
    void handleCallback(GenericCallback cb);
    void handleError(Exception ex);
}

public class Scaledrone extends WebSocketListener {
    private String channelID;
    private String clientID;
    private Object data;
    private WebSocket ws;
    private OkHttpClient client;
    private ArrayList<CallbackHandler> callbacks = new ArrayList<CallbackHandler>();
    private Map<String, Room> roomsMap = new HashMap<String, Room>();
    private Listener listener;
    private String url = "wss://api.scaledrone.com/v3/websocket";

    public Scaledrone(String channelID) {
        this.channelID = channelID;
    }

    public Scaledrone(String channelID, Object data) {
        this.channelID = channelID;
        this.data = data;
    }

    public void connect(Listener listener) {
        this.client = new OkHttpClient();
        this.listener = listener;
        Request request = new Request.Builder().url(url).build();
        this.ws = client.newWebSocket(request, this);
        client.dispatcher().executorService().shutdown();
    }

    @Override
    public void onOpen(WebSocket webSocket, Response response) {
        Handshake handshake = new Handshake(this.channelID, this.data, this.registerCallback(new CallbackHandler() {
            @Override
            public void handleCallback(GenericCallback cb) {
                Scaledrone.this.clientID = cb.getClientID();
                Scaledrone.this.listener.onOpen();
            }
            @Override
            public void handleError(Exception ex) {
                Scaledrone.this.listener.onOpenFailure(ex);
            }
        }));
        this.sendMessage(handshake);
    }

    private void sendMessage(Object data) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(data);
            this.ws.send(json);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    public void publish(String roomName, Object message) {
        this.sendMessage(new Publish(roomName, message));
    }

    @Override
    public void onMessage(WebSocket webSocket, String text) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            GenericCallback cb = mapper.readValue(text, GenericCallback.class);
            if (cb.getCallback() != null) {
                CallbackHandler handler = this.callbacks.get(cb.getCallback());
                this.callbacks.set(cb.getCallback(), null);
                if (cb.getError() == null) {
                    handler.handleCallback(cb);
                } else {
                    handler.handleError(new Exception(cb.getError()));
                }
            } else if (cb.getError() != null) {
                this.listener.onFailure(new Exception(cb.getError()));
            } else {
                Room room = this.roomsMap.get(cb.getRoom());
                Member member;
                switch (cb.getType()) {
                    case "publish":
                        room.getListener().onMessage(room, cb.getMessage());
                        break;
                    case "observable_members":
                        if (room.getObservableListener() == null) {
                            break;
                        }
                        ArrayList<Member> members = mapper.readValue(
                                mapper.treeAsTokens(cb.getData()),
                                mapper.getTypeFactory().constructType(new TypeReference<ArrayList<Member>>(){})
                        );
                        room.getObservableListener().onMembers(room, members);
                        break;
                    case "observable_member_join":
                        if (room.getObservableListener() == null) {
                            break;
                        }
                        member = mapper.treeToValue(cb.getData(), Member.class);
                        room.getObservableListener().onMemberJoin(room, member);
                        break;
                    case "observable_member_leave":
                        if (room.getObservableListener() == null) {
                            break;
                        }
                        member = mapper.treeToValue(cb.getData(), Member.class);
                        room.getObservableListener().onMemberLeave(room, member);
                        break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Room subscribe(String roomName, final RoomListener roomListener) {
        final Room room = new Room(roomName, roomListener, this);
        Subscribe subscribe = new Subscribe(roomName, this.registerCallback(new CallbackHandler() {
            @Override
            public void handleCallback(GenericCallback cb) {
                roomListener.onOpen(room);
            }
            @Override
            public void handleError(Exception ex) {
                roomListener.onOpenFailure(room, ex);
            }
        }));
        this.sendMessage(subscribe);
        this.roomsMap.put(roomName, room);
        return room;
    }

    public void unsubscribe(Room room) {
        Unsubscribe unsubscribe = new Unsubscribe(room.getName(), this.registerCallback(new CallbackHandler() {
            @Override
            public void handleCallback(GenericCallback cb) {

            }
            @Override
            public void handleError(Exception ex) {

            }
        }));
        this.sendMessage(unsubscribe);
    }

    public void authenticate(String jwt, final AuthenticationListener listener) {
        Authenticate authenticate = new Authenticate(jwt, this.registerCallback(new CallbackHandler() {
            @Override
            public void handleCallback(GenericCallback cb) {
                listener.onAuthentication();
            }
            @Override
            public void handleError(Exception ex) {
                listener.onAuthenticationFailure(ex);
            }
        }));
        this.sendMessage(authenticate);
    }

    private Integer registerCallback(CallbackHandler handler) {
        this.callbacks.add(handler);
        return this.callbacks.size() - 1;
    }

    @Override
    public void onMessage(WebSocket webSocket, ByteString bytes) {

    }

    @Override
    public void onClosing(WebSocket webSocket, int code, String reason) {
        System.out.println("Connection is closing, reason: " + reason);
    }

    @Override
    public void onClosed(WebSocket webSocket, int code, String reason) {
        System.out.println("Connection is closed, reason: " + reason);
        this.listener.onClosed(reason);
    }

    @Override
    public void onFailure(WebSocket webSocket, Throwable t, Response response) {
        System.out.println("Failure: " + t.getMessage());
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getClientID() {
        return clientID;
    }
}