package eu.applabs.allplaylibrary.services.spotify;

import android.util.Log;

import com.spotify.sdk.android.player.PlaybackState;
import com.spotify.sdk.android.player.Player;

import java.util.List;
import java.util.Observer;

import eu.applabs.allplaylibrary.event.PlayerEvent;
import eu.applabs.allplaylibrary.services.ServicePlayer;
import eu.applabs.allplaylibrary.services.ServiceType;

public class SpotifyPlaybackPositionChecker implements Runnable {

    private static final String TAG = SpotifyPlaybackPositionChecker.class.getSimpleName();
    private static final long SLEEP_TIME = 1000;

    private boolean mIsRunning = false;

    private SpotifyPlayer mSpotifyPlayer;
    private List<Observer> mObserverList;
    private Player mPlayer;

    public SpotifyPlaybackPositionChecker(SpotifyPlayer spotifyPlayer, List<Observer> observerList, Player player) {
        mSpotifyPlayer = spotifyPlayer;
        mObserverList = observerList;
        mPlayer = player;
    }

    public synchronized void updateObserverList(List<Observer> observerList) {
        mObserverList = observerList;
    }

    public synchronized void updatePlayer(Player player) {
        mPlayer = player;
    }

    public void setStopFlag() {
        mIsRunning = false;
    }

    private synchronized Player getPlayer() {
        return mPlayer;
    }

    private synchronized List<Observer> getObserverList() {
        return mObserverList;
    }

    @Override
    public void run() {
        Log.d(TAG, "run called");
        mIsRunning = true;

        while(mIsRunning) {
            try {
                Thread.sleep(SLEEP_TIME);
                update();
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "Exception while running");
                mIsRunning = false;
            }
        }
    }

    private void update() {
        PlaybackState playbackState = getPlayer().getPlaybackState();
        long durationInMs = getPlayer().getMetadata().currentTrack.durationMs;

        if(durationInMs == 0) {
            return;
        }

        int percent = (int) (playbackState.positionMs * 100) / (int) durationInMs;

        PlayerEvent playerEvent = new PlayerEvent(PlayerEvent.PlayerEventType.PLAYBACK_POSITION_CHANGED, ServiceType.SPOTIFY);
        playerEvent.setPlaybackPosition(percent);

        for(Observer observer : mObserverList) {
            observer.update(mSpotifyPlayer, playerEvent);
        }
    }
}