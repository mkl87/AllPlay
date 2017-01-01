package eu.applabs.allplaylibrary.player;

import android.content.Intent;

import eu.applabs.allplaylibrary.data.Song;
import eu.applabs.allplaylibrary.services.ServiceType;

public interface ServicePlayer {

    enum State {
        Undefined,
        Idle,
        Playing,
        Paused
    }

    enum Event {
        Undefined,
        TrackEnd,
        Error
    }

    void clearPlayer();

    // Methods to login
    void login();
    void logout();

    /**
     * Method to check if the method was triggered by the authentication request
     *
     * @param requestCode Request Code
     * @param resultCode Result Code
     * @param intent Intent with information
     * @return bool (true = Result of authentication request)
     */
    boolean checkActivityResult(int requestCode, int resultCode, Intent intent);

    State getPlayerState();
    ServiceType getServiceType();

    // Playback methods
    boolean play(Song song);
    boolean pause(Song song);
    boolean resume(Song song);
    boolean stop(Song song);

    // Listener methods
    void registerListener(PlayerListener listener);
    void unregisterListener(PlayerListener listener);
}

