package eu.applabs.allplaylibrary.data;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import eu.applabs.allplaylibrary.AllPlayLibrary;

public class ServiceCategory {

    interface OnCategoryUpdateListener {
        void onCategoryUpdate();
    }

    @Inject
    protected MusicLibrary mMusicLibrary;

    private String mName;
    private List<OnCategoryUpdateListener> mOnCategoryUpdateListenerList = new ArrayList<>();
    private List<ServicePlaylist> mServicePlaylists = new ArrayList<>();

    public ServiceCategory(String name) {
        AllPlayLibrary.getInstance().component().inject(this);
        mName = name;
    }

    public String getCategoryName() {
        return mName;
    }

    public void clearCategory() {
        for(ServicePlaylist playlist : mServicePlaylists) {
            playlist.clearPlaylist();
        }

        mServicePlaylists.clear();
    }

    public void addPlaylist(@NonNull ServicePlaylist playlist) {
        mServicePlaylists.add(playlist);
        notifyListener();
    }

    public void removePlaylist(@NonNull ServicePlaylist playlist) {
        if(mServicePlaylists.contains(playlist)) {
            mServicePlaylists.remove(playlist);
            notifyListener();
        }
    }

    public List<ServicePlaylist> getPlaylists() {
        return mServicePlaylists;
    }

    public void registerListener(@NonNull OnCategoryUpdateListener listener) {
        if(!mOnCategoryUpdateListenerList.contains(listener)) {
            mOnCategoryUpdateListenerList.add(listener);
        }
    }

    public void unregisterListener(@NonNull OnCategoryUpdateListener listener) {
        if(mOnCategoryUpdateListenerList.contains(listener)) {
            mOnCategoryUpdateListenerList.remove(listener);
        }
    }

    public void notifyListener() {
        for(OnCategoryUpdateListener listener : mOnCategoryUpdateListenerList) {
            listener.onCategoryUpdate();
        }
    }
}
