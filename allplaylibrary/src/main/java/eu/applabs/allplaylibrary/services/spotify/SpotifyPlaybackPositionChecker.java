package eu.applabs.allplaylibrary.services.spotify;

import android.util.Log;

import com.spotify.sdk.android.player.PlaybackState;
import com.spotify.sdk.android.player.Player;

import java.util.List;

import eu.applabs.allplaylibrary.player.PlayerListener;

public class SpotifyPlaybackPositionChecker implements Runnable {

    private static final String TAG = SpotifyPlaybackPositionChecker.class.getSimpleName();
    private static final long SLEEP_TIME = 1000;

    private boolean mIsRunning = false;
    private List<PlayerListener> mPlayerListenerList;
    private Player mPlayer;

    public SpotifyPlaybackPositionChecker(List<PlayerListener> list, Player player) {
        mPlayerListenerList = list;
        mPlayer = player;
    }

    public synchronized void updateIPlayerListenerList(List<PlayerListener> list) {
        mPlayerListenerList = list;
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

    private synchronized List<PlayerListener> getIPlayerListenerList() {
        return mPlayerListenerList;
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

        for(PlayerListener listener : getIPlayerListenerList()) {
            if(listener != null) {
                listener.onPlayerPlaybackPositionChanged(percent);
            }
        }
    }
}