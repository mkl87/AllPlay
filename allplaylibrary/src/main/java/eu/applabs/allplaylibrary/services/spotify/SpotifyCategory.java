package eu.applabs.allplaylibrary.services.spotify;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import eu.applabs.allplaylibrary.data.ServiceCategory;
import eu.applabs.allplaylibrary.data.ServicePlaylist;
import eu.applabs.allplaylibrary.data.MusicLibrary;

public class SpotifyCategory implements ServiceCategory {

    private MusicLibrary mMusicLibrary = MusicLibrary.getInstance();
    private String mName = null;
    private List<ServicePlaylist> mM_ServicePlaylistList = new CopyOnWriteArrayList<>();
    private List<OnCategoryUpdateListener> mM_OnCategoryUpdateListenerList = new CopyOnWriteArrayList<>();

    public SpotifyCategory(String name) {
        mName = name;
        registerListener(mMusicLibrary);
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
        return mName;
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
