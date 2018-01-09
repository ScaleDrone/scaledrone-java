package com.scaledrone.lib;

public interface Listener {
    void onOpen();
    void onOpenFailure(Exception ex);
    void onFailure(Exception ex);
    void onClosed(String reason);
}
