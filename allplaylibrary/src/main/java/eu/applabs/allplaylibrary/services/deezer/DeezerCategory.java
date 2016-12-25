package eu.applabs.allplaylibrary.services.deezer;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import eu.applabs.allplaylibrary.data.ServiceCategory;
import eu.applabs.allplaylibrary.data.ServicePlaylist;
import eu.applabs.allplaylibrary.data.MusicLibrary;

public class DeezerCategory implements ServiceCategory {

    private String mName;
    private MusicLibrary mMusicLibrary = MusicLibrary.getInstance();
    private List<ServicePlaylist> mServicePlaylistList = new CopyOnWriteArrayList<>();
    private List<OnCategoryUpdateListener> mOnCategoryUpdateListenerList = new CopyOnWriteArrayList<>();

    public DeezerCategory(String name) {
        mName = name;
        registerListener(mMusicLibrary);
    }

    @Override
    public void clearCategory() {
        for(ServicePlaylist playlist : mServicePlaylistList) {
            playlist.clearPlaylist();
        }

        mServicePlaylistList.clear();
    }

    @Override
    public String getCategoryName() {
        return mName;
    }

    @Override
    public void addPlaylist(ServicePlaylist playlist) {
        mServicePlaylistList.add(playlist);
        notifyListener();
    }

    @Override
    public void removePlaylist(ServicePlaylist playlist) {
        mServicePlaylistList.remove(playlist);
        notifyListener();
    }

    @Override
    public List<ServicePlaylist> getPlaylists() {
        return mServicePlaylistList;
    }

    @Override
    public void registerListener(OnCategoryUpdateListener listener) {
        mOnCategoryUpdateListenerList.add(listener);
    }

    @Override
    public void unregisterListener(OnCategoryUpdateListener listener) {
        mOnCategoryUpdateListenerList.remove(listener);
    }

    private void notifyListener() {
        for(OnCategoryUpdateListener listener : mOnCategoryUpdateListenerList) {
            listener.onCategoryUpdate();
        }
    }
}
