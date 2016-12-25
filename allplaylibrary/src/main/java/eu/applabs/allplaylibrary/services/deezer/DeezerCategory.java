package eu.applabs.allplaylibrary.services.deezer;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import eu.applabs.allplaylibrary.data.IMusicLibraryCategory;
import eu.applabs.allplaylibrary.data.IMusicLibraryCategoryUpdateListener;
import eu.applabs.allplaylibrary.data.IMusicLibraryPlaylist;
import eu.applabs.allplaylibrary.data.MusicLibrary;

public class DeezerCategory implements IMusicLibraryCategory {
    private String m_Name = null;
    private MusicLibrary m_MusicLibrary = null;
    private List<IMusicLibraryPlaylist> m_IMusicLibraryPlaylistList = null;
    private List<IMusicLibraryCategoryUpdateListener> m_IMusicLibraryCategoryUpdateListenerList = null;

    public DeezerCategory(String name) {
        m_Name = name;

        m_IMusicLibraryPlaylistList = new CopyOnWriteArrayList<>();
        m_IMusicLibraryCategoryUpdateListenerList = new CopyOnWriteArrayList<>();

        m_MusicLibrary = MusicLibrary.getInstance();
        registerListener(m_MusicLibrary);
    }

    @Override
    public void clearCategory() {
        for(IMusicLibraryPlaylist playlist : m_IMusicLibraryPlaylistList) {
            playlist.clearPlaylist();
        }

        m_IMusicLibraryPlaylistList.clear();
    }

    @Override
    public String getCategoryName() {
        return m_Name;
    }

    @Override
    public void addPlaylist(IMusicLibraryPlaylist playlist) {
        m_IMusicLibraryPlaylistList.add(playlist);
        notifyListener();
    }

    @Override
    public void removePlaylist(IMusicLibraryPlaylist playlist) {
        m_IMusicLibraryPlaylistList.remove(playlist);
        notifyListener();
    }

    @Override
    public List<IMusicLibraryPlaylist> getPlaylists() {
        return m_IMusicLibraryPlaylistList;
    }

    @Override
    public void registerListener(IMusicLibraryCategoryUpdateListener listener) {
        m_IMusicLibraryCategoryUpdateListenerList.add(listener);
    }

    @Override
    public void unregisterListener(IMusicLibraryCategoryUpdateListener listener) {
        m_IMusicLibraryCategoryUpdateListenerList.remove(listener);
    }

    private void notifyListener() {
        for(IMusicLibraryCategoryUpdateListener listener : m_IMusicLibraryCategoryUpdateListenerList) {
            listener.onMusicLibraryCategoryUpdate();
        }
    }
}
