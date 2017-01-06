package eu.applabs.allplaylibrary.services;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import eu.applabs.allplaylibrary.AllPlayLibrary;
import eu.applabs.allplaylibrary.MusicCatalog;
import eu.applabs.allplaylibrary.data.Observable;
import eu.applabs.allplaylibrary.event.CategoryEvent;
import eu.applabs.allplaylibrary.event.Event;

/**
 * Class which represents the category of a ServiceLibrary
 *
 * - Owns a list of ServicePlaylists
 * - Notifies the listener if an update occurs
 */
public class ServiceCategory extends Observable {

    @Inject
    protected MusicCatalog mMusicCatalog;

    private String mName;
    private ServiceType mServiceType;
    private List<ServicePlaylist> mServicePlaylists = new ArrayList<>();

    public ServiceCategory(String name, ServiceType serviceType) {
        AllPlayLibrary.getInstance().component().inject(this);
        mName = name;
        mServiceType = serviceType;

    }

    public String getCategoryName() {
        return mName;
    }

    public ServiceType getServiceType() {
        return mServiceType;
    }

    public void clearCategory() {
        for(ServicePlaylist playlist : mServicePlaylists) {
            playlist.clearPlaylist();
        }

        mServicePlaylists.clear();
    }

    public void addPlaylist(@NonNull ServicePlaylist playlist) {
        mServicePlaylists.add(playlist);
        notifyCategoryUpdate();
    }

    public void removePlaylist(@NonNull ServicePlaylist playlist) {
        if(mServicePlaylists.contains(playlist)) {
            mServicePlaylists.remove(playlist);
            notifyCategoryUpdate();
        }
    }

    public List<ServicePlaylist> getPlaylists() {
        return mServicePlaylists;
    }

    private void notifyCategoryUpdate() {
        CategoryEvent categoryEvent = new CategoryEvent(this);
        notifyObservers(categoryEvent);
    }
}
