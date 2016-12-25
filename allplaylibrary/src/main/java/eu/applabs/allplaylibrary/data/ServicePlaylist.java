package eu.applabs.allplaylibrary.data;

import java.util.List;

public interface ServicePlaylist {

    interface OnPlaylistUpdateListener {
        void onPlaylistUpdate();
    }

    void clearPlaylist();
    String getPlaylistName();
    String getCoverUrl();
    int getSize();
    List<Song> getPlaylist();

    void registerListener(OnPlaylistUpdateListener listener);
    void unregisterListener(OnPlaylistUpdateListener listener);
}
