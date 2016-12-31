package eu.applabs.allplaylibrary.services.gmusic;

import android.app.Activity;
import android.content.Intent;

import javax.inject.Inject;

import eu.applabs.allplaylibrary.data.Song;
import eu.applabs.allplaylibrary.player.PlayerListener;
import eu.applabs.allplaylibrary.player.ServicePlayer;

public class GMusicPlayer implements ServicePlayer {

    @Inject
    protected Activity mActivity;

    public GMusicPlayer() {
        GMusicLoginDialog ld = new GMusicLoginDialog(mActivity);
        ld.show();
    }

    @Override
    public void clearPlayer() {

    }

    @Override
    public void login() {

    }

    @Override
    public void logout() {

    }

    @Override
    public boolean checkActivityResult(int requestCode, int resultCode, Intent intent) {
        return false;
    }

    @Override
    public State getPlayerState() {
        return null;
    }

    @Override
    public ServiceType getServiceType() {
        return null;
    }

    @Override
    public boolean play(Song song) {
        return false;
    }

    @Override
    public boolean pause(Song song) {
        return false;
    }

    @Override
    public boolean resume(Song song) {
        return false;
    }

    @Override
    public boolean stop(Song song) {
        return false;
    }

    @Override
    public void registerListener(PlayerListener listener) {

    }

    @Override
    public void unregisterListener(PlayerListener listener) {

    }
}