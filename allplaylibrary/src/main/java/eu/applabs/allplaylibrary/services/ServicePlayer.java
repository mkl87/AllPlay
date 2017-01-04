package eu.applabs.allplaylibrary.services;

import android.app.Activity;
import android.content.Intent;

import eu.applabs.allplaylibrary.data.Observable;
import eu.applabs.allplaylibrary.data.Song;
import eu.applabs.allplaylibrary.player.Player;

public abstract class ServicePlayer extends Observable {

    public enum PlayerState {
        IDLE,
        PLAYING,
        PAUSED
    }

    public abstract void clearPlayer();

    // Methods to login
    public abstract void login(Activity activity);
    public abstract void logout();

    /**
     * Method to check if the method was triggered by the authentication request
     *
     * @param requestCode Request Code
     * @param resultCode Result Code
     * @param intent Intent with information
     * @return bool (true = Result of authentication request)
     */
    public abstract boolean checkActivityResult(int requestCode, int resultCode, Intent intent);

    public abstract PlayerState getPlayerState();
    public abstract ServiceType getServiceType();

    // Playback methods
    public abstract boolean play(Song song);
    public abstract boolean pause(Song song);
    public abstract boolean resume(Song song);
    public abstract boolean stop(Song song);
}

