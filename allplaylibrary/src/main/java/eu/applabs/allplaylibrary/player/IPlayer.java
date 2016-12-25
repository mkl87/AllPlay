package eu.applabs.allplaylibrary.player;

import android.app.Activity;
import android.content.Intent;

import eu.applabs.allplaylibrary.data.Song;

public interface IPlayer {

    enum ServiceType {
        Undefined(0),
        Spotify(1),
        GoogleMusic(2),
        Deezer(3);

        private int m_Value = 0;
        private ServiceType(int value) {
            this.m_Value = value;
        }

        public int getValue() {
            return m_Value;
        }
    }

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

    // Methods for initialization
    void initialize(Activity activity);
    void clearPlayer();

    // Methods to login
    void login();
    void logout();

    /**
     * Method to check if the method was triggered by the authentication request
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
    void registerListener(IPlayerListener listener);
    void unregisterListener(IPlayerListener listener);
}

