package eu.applabs.allplaylibrary.services;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import eu.applabs.allplaylibrary.AllPlayLibrary;
import eu.applabs.allplaylibrary.data.MusicCatalog;
import eu.applabs.allplaylibrary.data.Observable;
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
        notifyObservers(new Event(Event.EventType.SERVICE_CATEGORY_UPDATE));
    }

    public void removePlaylist(@NonNull ServicePlaylist playlist) {
        if(mServicePlaylists.contains(playlist)) {
            mServicePlaylists.remove(playlist);
            notifyObservers(new Event(Event.EventType.SERVICE_CATEGORY_UPDATE));
        }
    }

    public List<ServicePlaylist> getPlaylists() {
        return mServicePlaylists;
    }
}
