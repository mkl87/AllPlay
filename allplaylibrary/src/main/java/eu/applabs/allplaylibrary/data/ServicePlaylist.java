package eu.applabs.allplaylibrary.data;

import java.util.ArrayList;
import java.util.List;

public abstract class ServicePlaylist {

    public interface OnPlaylistUpdateListener {
        void onPlaylistUpdate();
    }

    protected List<Song> mSongList = new ArrayList<>();
    private List<OnPlaylistUpdateListener> mOnPlaylistUpdateListenerList = new ArrayList<>();

    public abstract String getPlaylistName();
    public abstract String getCoverUrl();

    public void clearPlaylist() {
        mSongList.clear();
    }

    public int getSize() {
        return mSongList.size();
    }

    public List<Song> getPlaylist() {
        return mSongList;
    }

    public void registerListener(OnPlaylistUpdateListener listener) {
        if(!mOnPlaylistUpdateListenerList.contains(listener)) {
            mOnPlaylistUpdateListenerList.add(listener);
        }
    }

    public void unregisterListener(OnPlaylistUpdateListener listener) {
        if(mOnPlaylistUpdateListenerList.contains(listener)) {
            mOnPlaylistUpdateListenerList.remove(listener);
        }
    }

    protected void notifyListener() {
        for(OnPlaylistUpdateListener listener : mOnPlaylistUpdateListenerList) {
            listener.onPlaylistUpdate();
        }
    }
}
