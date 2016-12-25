package eu.applabs.allplaylibrary.services.gmusic;

import android.app.Activity;
import android.content.Intent;

import eu.applabs.allplaylibrary.data.Song;
import eu.applabs.allplaylibrary.player.IPlayer;
import eu.applabs.allplaylibrary.player.IPlayerListener;

public class GMusicPlayer implements IPlayer {

    private Activity m_Activity = null;

    @Override
    public void initialize(Activity activity) {
        m_Activity = activity;

        GMusicLoginDialog ld = new GMusicLoginDialog(m_Activity);
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
    public void registerListener(IPlayerListener listener) {

    }

    @Override
    public void unregisterListener(IPlayerListener listener) {

    }
}