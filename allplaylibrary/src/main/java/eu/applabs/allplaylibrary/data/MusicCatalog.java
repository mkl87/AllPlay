package eu.applabs.allplaylibrary.data;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Observer;
import java.util.concurrent.CopyOnWriteArrayList;

import eu.applabs.allplaylibrary.event.Event;
import eu.applabs.allplaylibrary.services.ServiceCategory;
import eu.applabs.allplaylibrary.services.ServiceLibrary;
import eu.applabs.allplaylibrary.services.ServicePlaylist;

/**
 * Class which provides access to all available service libraries
 *
 * - Notifies the listener if the list was updated
 */
public class MusicCatalog extends Observable implements Observer {

    private List<ServiceLibrary> mServiceLibraryList = new ArrayList<>();

    public MusicCatalog() {

    }

    public void clearLibrary() {
        for(ServiceLibrary serviceLibrary : mServiceLibraryList) {
            unregisterFromAllEvents(serviceLibrary);
            serviceLibrary.clearLibrary();
        }

        mServiceLibraryList.clear();
    }

    public List<ServiceLibrary> getLibraries() {
        return mServiceLibraryList;
    }

    public void addMusicLibrary(ServiceLibrary serviceLibrary) {
        registerForAllEvents(serviceLibrary);
        mServiceLibraryList.add(serviceLibrary);
        notifyObservers(new Event(Event.EventType.MUSIC_CATALOG_UPDATE));
    }

    public void removeMusicLibrary(ServiceLibrary serviceLibrary) {
        unregisterFromAllEvents(serviceLibrary);
        mServiceLibraryList.remove(serviceLibrary);
        notifyObservers(new Event(Event.EventType.MUSIC_CATALOG_UPDATE));
    }

    @Override
    public void update(java.util.Observable observable, Object o) {
        if(o instanceof Event) {
            Event event = (Event) o;

            if(event.getEventType() == Event.EventType.SERVICE_CATEGORY_UPDATE || event.getEventType() == Event.EventType.SERVICE_PLAYLIST_UPDATE) {
                notifyObservers(new Event(Event.EventType.MUSIC_CATALOG_UPDATE));
            }
        }
    }

    public void search(@NonNull String query, @NonNull ServiceLibrary.OnServiceLibrarySearchResult callback) {
        QueryThread qt = new QueryThread(query, callback);
        qt.start();
    }

    private void registerForAllEvents(@NonNull ServiceLibrary serviceLibrary) {
        for(ServiceCategory serviceCategory : serviceLibrary.getCategories()) {
            serviceCategory.addObserver(this);

            for(ServicePlaylist servicePlaylist : serviceCategory.getPlaylists()) {
                servicePlaylist.addObserver(this);
            }
        }
    }

    private void unregisterFromAllEvents(@NonNull ServiceLibrary serviceLibrary) {
        for(ServiceCategory serviceCategory : serviceLibrary.getCategories()) {
            serviceCategory.deleteObserver(this);

            for(ServicePlaylist servicePlaylist : serviceCategory.getPlaylists()) {
                servicePlaylist.deleteObserver(this);
            }
        }
    }

    private class QueryThread extends Thread {
        private String m_Query = null;
        private ServiceLibrary.OnServiceLibrarySearchResult m_Callback = null;

        public QueryThread(String query, ServiceLibrary.OnServiceLibrarySearchResult callback) {
            m_Query = query;
            m_Callback = callback;
        }

        @Override
        public void run() {
            super.run();

            List<ServiceCategory> results = new CopyOnWriteArrayList<>();

            for(ServiceLibrary library : mServiceLibraryList) {
                List<ServiceCategory> list = library.search(m_Query);

                if(list != null && list.size() > 0) {
                    results.addAll(list);
                }
            }

            m_Callback.onSearchResult(results);
        }
    }
}
