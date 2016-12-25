package eu.applabs.allplaylibrary.player;


public interface PlayerListener {
    void onPlayerStateChanged(ServicePlayer.ServiceType type, ServicePlayer.State old_state, ServicePlayer.State new_state);
    void onPlayerEvent(ServicePlayer.Event event);
    void onLoginSuccess(ServicePlayer.ServiceType type);
    void onLoginError(ServicePlayer.ServiceType type);
    void onLogoutSuccess(ServicePlayer.ServiceType type);
    void onLogoutError(ServicePlayer.ServiceType type);
    void onPlayerPlaybackPositionChanged(int position);
}
