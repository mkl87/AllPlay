package eu.applabs.allplaylibrary.services.spotify;

import android.util.Log;

import com.spotify.sdk.android.player.PlaybackState;
import com.spotify.sdk.android.player.Player;

import java.util.List;

import eu.applabs.allplaylibrary.player.IPlayerListener;

public class SpotifyPlaybackPositionChecker implements Runnable {

    private static final String s_Classname = SpotifyPlaybackPositionChecker.class.getSimpleName();
    private static final long s_SleepTime = 1000;

    private boolean m_Running = false;
    private List<IPlayerListener> m_IPlayerListenerList = null;
    private Player m_Player = null;

    public SpotifyPlaybackPositionChecker(List<IPlayerListener> list, Player player) {
        m_IPlayerListenerList = list;
        m_Player = player;
    }

    public synchronized void updateIPlayerListenerList(List<IPlayerListener> list) {
        m_IPlayerListenerList = list;
    }

    public synchronized void updatePlayer(Player player) {
        m_Player = player;
    }

    public void setStopFlag() {
        m_Running = false;
    }

    private synchronized Player getPlayer() {
        return m_Player;
    }

    private synchronized List<IPlayerListener> getIPlayerListenerList() {
        return m_IPlayerListenerList;
    }

    @Override
    public void run() {
        Log.d(s_Classname, "run called");
        m_Running = true;

        while(m_Running) {
            try {
                Thread.sleep(s_SleepTime);
                update();
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(s_Classname, "Exception while running");
                m_Running = false;
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

        for(IPlayerListener listener : getIPlayerListenerList()) {
            if(listener != null) {
                listener.onPlayerPlaybackPositionChanged(percent);
            }
        }
    }
}