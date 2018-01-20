package com.scaledrone.lib;

import com.auth0.jwt.JWTSigner;
import com.fasterxml.jackson.databind.JsonNode;
import net.jodah.concurrentunit.Waiter;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

class ScaledronePermission {
    private boolean publish;
    private boolean subscribe;

    public ScaledronePermission(boolean publish, boolean subscribe) {
        this.publish = publish;
        this.subscribe = subscribe;
    }

    public boolean getPublish() {
        return publish;
    }

    public boolean getSubscribe() {
        return subscribe;
    }
}

public class AuthTest {

    final String channel = System.getenv("AUTH_CHANNEL");
    final String secret = System.getenv("SECRET");

    @Test
    public void connectWithoutAuth() throws Exception {
        final Waiter waiter = new Waiter();
        final Scaledrone drone = new Scaledrone(channel);
        drone.connect(new Listener() {
            @Override
            public void onOpen() {
                Room room = drone.subscribe("room1", new RoomListener() {
                    @Override
                    public void onOpen(Room room) {
                        waiter.fail();
                    }

                    @Override
                    public void onOpenFailure(Room room, Exception ex) {
                        waiter.assertEquals("Unauthorized", ex.getMessage());
                        waiter.resume();
                    }

                    @Override
                    public void onMessage(Room room, JsonNode message, Member member) {
                        waiter.fail();
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
    public void connectWithWrongJWT() throws Exception {
        final Waiter waiter = new Waiter();
        final Scaledrone drone = new Scaledrone(channel);
        drone.connect(new Listener() {
            @Override
            public void onOpen() {
                drone.authenticate("invalid jwt string", new AuthenticationListener() {
                    @Override
                    public void onAuthentication() {
                        waiter.fail();
                    }

                    @Override
                    public void onAuthenticationFailure(Exception ex) {
                        waiter.assertEquals("JWT does not have 3 parts", ex.getMessage());
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
    public void connectWithCorrectJWT() throws Exception {
        final Waiter waiter = new Waiter();
        final Scaledrone drone = new Scaledrone(channel);
        drone.connect(new Listener() {
            @Override
            public void onOpen() {
                final String jwt = generateJWT(drone.getClientID());
                drone.authenticate(jwt, new AuthenticationListener() {
                    @Override
                    public void onAuthentication() {
                        waiter.resume();
                    }

                    @Override
                    public void onAuthenticationFailure(Exception ex) {
                        waiter.fail(ex);
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

    String generateJWT(String clientID) {
        final long iat = System.currentTimeMillis() / 1000L;
        final long exp = iat + 10 * 60L; // the token expires in 10 minutes

        final Map<String, ScaledronePermission> permissionMap = new HashMap<String, ScaledronePermission>() {
            {
                put("*.", new ScaledronePermission(true, true));
                put("^main-room$", new ScaledronePermission(false, false));
            }
        };

        final JWTSigner signer = new JWTSigner(secret);
        final Map<String, Object> claims = new HashMap<String, Object>();
        claims.put("client", clientID);
        claims.put("exp", exp);
        claims.put("channel", channel);
        claims.put("permissions", permissionMap);

        return signer.sign(claims);
    }


}
