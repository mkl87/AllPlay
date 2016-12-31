package eu.applabs.allplaylibrary.player;


import eu.applabs.allplaylibrary.services.ServiceType;

public interface PlayerListener {
    void onPlayerStateChanged(ServiceType type, ServicePlayer.State old_state, ServicePlayer.State new_state);
    void onPlayerEvent(ServicePlayer.Event event);
    void onLoginSuccess(ServiceType type);
    void onLoginError(ServiceType type);
    void onLogoutSuccess(ServiceType type);
    void onLogoutError(ServiceType type);
    void onPlayerPlaybackPositionChanged(int position);
}
