package eu.applabs.allplaylibrary.data;

import java.util.List;

public interface IMusicLibraryCategory {
    void clearCategory();
    String getCategoryName();
    void addPlaylist(IMusicLibraryPlaylist playlist);
    void removePlaylist(IMusicLibraryPlaylist playlist);
    List<IMusicLibraryPlaylist> getPlaylists();

    void registerListener(IMusicLibraryCategoryUpdateListener listener);
    void unregisterListener(IMusicLibraryCategoryUpdateListener listener);
}
