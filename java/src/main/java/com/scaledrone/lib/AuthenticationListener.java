package com.scaledrone.lib;

public interface AuthenticationListener {
    void onAuthentication();
    void onAuthenticationFailure(Exception ex);
}
