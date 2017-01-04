package eu.applabs.allplaylibrary.event;

import eu.applabs.allplaylibrary.services.ServicePlayer;
import eu.applabs.allplaylibrary.services.ServiceType;

public class PlayerEvent extends Event {

    public enum PlayerEventType {
        STATE_CHANGED,
        PLAYBACK_POSITION_CHANGED,
        TRACK_END,
        ERROR
    }

    private PlayerEventType mPlayerEventType;
    private ServiceType mServiceType;

    private int mPlaybackPosition = 0;
    private ServicePlayer.PlayerState mNewPlayerState;
    private ServicePlayer.PlayerState mOldPlayerState;

    public PlayerEvent(PlayerEventType playerEventType, ServiceType serviceType) {
        super(EventType.PLAYER_EVENT);

        mPlayerEventType = playerEventType;
        mServiceType = serviceType;
    }

    public PlayerEventType getPlayerEventType() {
        return mPlayerEventType;
    }

    public void setPlayerEventType(PlayerEventType playerEventType) {
        mPlayerEventType = playerEventType;
    }

    public ServiceType getServiceType() {
        return mServiceType;
    }

    public void setServiceType(ServiceType serviceType) {
        mServiceType = serviceType;
    }

    public int getPlaybackPosition() {
        return mPlaybackPosition;
    }

    public void setPlaybackPosition(int playbackPosition) {
        mPlaybackPosition = playbackPosition;
    }

    public PlayerEvent(EventType eventType) {
        super(eventType);
    }

    public ServicePlayer.PlayerState getNewPlayerState() {
        return mNewPlayerState;
    }

    public void setNewPlayerState(ServicePlayer.PlayerState newPlayerState) {
        mNewPlayerState = newPlayerState;
    }

    public ServicePlayer.PlayerState getOldPlayerState() {
        return mOldPlayerState;
    }

    public void setOldPlayerState(ServicePlayer.PlayerState oldPlayerState) {
        mOldPlayerState = oldPlayerState;
    }
}
