package eu.applabs.allplaylibrary.data;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class MusicLibrary implements ServiceCategory.OnCategoryUpdateListener, ServicePlaylist.OnPlaylistUpdateListener {

    public interface OnMusicLibraryUpdateListener {
        void onMusicLibraryUpdate();
    }

    private List<ServiceLibrary> mServiceLibraryList = new ArrayList<>();
    private List<OnMusicLibraryUpdateListener> mOnMusicLibraryUpdateListenerList = new ArrayList<>();

    public MusicLibrary() {

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
        notifyMLU();
    }

    public void removeMusicLibrary(ServiceLibrary serviceLibrary) {
        unregisterFromAllEvents(serviceLibrary);
        mServiceLibraryList.remove(serviceLibrary);
        notifyMLU();
    }

    public void registerListener(OnMusicLibraryUpdateListener listener) {
        mOnMusicLibraryUpdateListenerList.add(listener);
    }

    public void unregisterListener(OnMusicLibraryUpdateListener listener) {
        mOnMusicLibraryUpdateListenerList.remove(listener);
    }

    @Override
    public void onCategoryUpdate() {
        notifyMLU();
    }

    @Override
    public void onPlaylistUpdate() {
        notifyMLU();
    }

    public void search(@NonNull String query, @NonNull ServiceLibrary.OnServiceLibrarySearchResult callback) {
        QueryThread qt = new QueryThread(query, callback);
        qt.start();
    }

    private void registerForAllEvents(@NonNull ServiceLibrary serviceLibrary) {
        for(ServiceCategory serviceCategory : serviceLibrary.getCategories()) {
            serviceCategory.registerListener(this);

            for(ServicePlaylist servicePlaylist : serviceCategory.getPlaylists()) {
                servicePlaylist.registerListener(this);
            }
        }
    }

    private void unregisterFromAllEvents(@NonNull ServiceLibrary serviceLibrary) {
        for(ServiceCategory serviceCategory : serviceLibrary.getCategories()) {
            serviceCategory.unregisterListener(this);

            for(ServicePlaylist servicePlaylist : serviceCategory.getPlaylists()) {
                servicePlaylist.unregisterListener(this);
            }
        }
    }

    private void notifyMLU() {
        for(OnMusicLibraryUpdateListener listener : mOnMusicLibraryUpdateListenerList) {
            if(listener != null) {
                listener.onMusicLibraryUpdate();
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
