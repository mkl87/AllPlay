package eu.applabs.allplaylibrary.data;

import java.util.List;

public interface IMusicLibraryPlaylist {
    void clearPlaylist();
    String getPlaylistName();
    String getCoverUrl();
    int getSize();
    List<Song> getPlaylist();

    void registerListener(IMusicLibraryPlaylistUpdateListener listener);
    void unregisterListener(IMusicLibraryPlaylistUpdateListener listener);
}
