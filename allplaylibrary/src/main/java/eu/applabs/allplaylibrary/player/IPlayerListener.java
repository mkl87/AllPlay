package eu.applabs.allplaylibrary.player;

public interface IPlayerListener {
    void onPlayerStateChanged(IPlayer.ServiceType type, IPlayer.State old_state, IPlayer.State new_state);
    void onPlayerEvent(IPlayer.Event event);
    void onLoginSuccess(IPlayer.ServiceType type);
    void onLoginError(IPlayer.ServiceType type);
    void onLogoutSuccess(IPlayer.ServiceType type);
    void onLogoutError(IPlayer.ServiceType type);
    void onPlayerPlaybackPositionChanged(int position);
}
