package com.scaledrone.lib;

import net.jodah.concurrentunit.Waiter;
import org.junit.Test;

import java.util.Timer;
import java.util.TimerTask;

public class CloseTest {

    final String channel = System.getenv("AUTHLESS_CHANNEL");

    @Test
    public void test() throws Exception {
        final Waiter waiter = new Waiter();
        final Scaledrone drone = new Scaledrone(channel);
        waiter.assertEquals(null, drone.getClientID());
        drone.connect(new Listener() {
            @Override
            public void onOpen() {
                waiter.assertNotNull(drone.getClientID());
                drone.close();
                Timer t = new Timer();
                t.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        drone.publish("some-room", "some message");
                    }
                }, 100);
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
                waiter.resume();
            }
        });

        waiter.await(10000);
    }
}