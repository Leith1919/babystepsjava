package controllers.User;

public interface OAuthCompletedCallback {
    void oAuthCallback(OAuthAuthenticator authenticator);
}