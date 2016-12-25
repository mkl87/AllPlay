package eu.applabs.allplaylibrary.services.spotify;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import eu.applabs.allplaylibrary.data.ServiceCategory;
import eu.applabs.allplaylibrary.data.ServicePlaylist;
import eu.applabs.allplaylibrary.data.MusicLibrary;

public class SpotifyCategory implements ServiceCategory {

    private MusicLibrary m_MusicLibrary = null;
    private String m_Name = null;
    private List<ServicePlaylist> mM_ServicePlaylistList = null;
    private List<OnCategoryUpdateListener> mM_OnCategoryUpdateListenerList = null;

    public SpotifyCategory(String name) {
        m_Name = name;
        mM_ServicePlaylistList = new CopyOnWriteArrayList<>();
        mM_OnCategoryUpdateListenerList = new CopyOnWriteArrayList<>();

        m_MusicLibrary = MusicLibrary.getInstance();
        registerListener(m_MusicLibrary);
    }

    public void addPlaylist(ServicePlaylist playlist) {
        mM_ServicePlaylistList.add(playlist);
        notifyListener();
    }

    public void removePlaylist(ServicePlaylist playlist) {
        mM_ServicePlaylistList.remove(playlist);
        notifyListener();
    }

    @Override
    public void clearCategory() {
        for(ServicePlaylist playlist : mM_ServicePlaylistList) {
            playlist.clearPlaylist();
        }

        mM_ServicePlaylistList.clear();
    }

    @Override
    public String getCategoryName() {
        return m_Name;
    }

    @Override
    public List<ServicePlaylist> getPlaylists() {
        return mM_ServicePlaylistList;
    }

    @Override
    public void registerListener(OnCategoryUpdateListener listener) {
        mM_OnCategoryUpdateListenerList.add(listener);
    }

    @Override
    public void unregisterListener(OnCategoryUpdateListener listener) {
        mM_OnCategoryUpdateListenerList.remove(listener);
    }

    private void notifyListener() {
        for(OnCategoryUpdateListener listener : mM_OnCategoryUpdateListenerList) {
            listener.onCategoryUpdate();
        }
    }
}
