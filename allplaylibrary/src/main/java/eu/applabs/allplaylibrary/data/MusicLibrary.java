package eu.applabs.allplaylibrary.data;

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
        for(ServiceLibrary library : mServiceLibraryList) {

            // Unregister from category and playlist updates
            for(ServiceCategory serviceCategory : library.getCategories()) {
                serviceCategory.unregisterListener(this);

                for(ServicePlaylist servicePlaylist : serviceCategory.getPlaylists()) {
                    servicePlaylist.unregisterListener(this);
                }
            }

            library.clearLibrary();
        }

        mServiceLibraryList.clear();
    }

    public void search(String query, ServiceLibrary.OnServiceLibrarySearchResult callback) {
        if(query != null && callback != null) {
            QueryThread qt = new QueryThread(query, callback);
            qt.start();
        }
    }

    public List<ServiceLibrary> getLibraries() {
        return mServiceLibraryList;
    }

    public void addMusicLibrary(ServiceLibrary library) {

        // Register for category and playlist updates
        for(ServiceCategory serviceCategory : library.getCategories()) {
            serviceCategory.registerListener(this);

            for(ServicePlaylist servicePlaylist : serviceCategory.getPlaylists()) {
                servicePlaylist.registerListener(this);
            }
        }

        mServiceLibraryList.add(library);
        notifyMLU();
    }

    public void removeMusicLibrary(ServiceLibrary library) {

        // Unregister from category and playlist updates
        for(ServiceCategory serviceCategory : library.getCategories()) {
            serviceCategory.unregisterListener(this);

            for(ServicePlaylist servicePlaylist : serviceCategory.getPlaylists()) {
                servicePlaylist.unregisterListener(this);
            }
        }

        mServiceLibraryList.remove(library);
        notifyMLU();
    }

    public void registerListener(OnMusicLibraryUpdateListener listener) {
        mOnMusicLibraryUpdateListenerList.add(listener);
    }

    public void unregisterListener(OnMusicLibraryUpdateListener listener) {
        mOnMusicLibraryUpdateListenerList.remove(listener);
    }

    private void notifyMLU() {
        for(OnMusicLibraryUpdateListener listener : mOnMusicLibraryUpdateListenerList) {
            if(listener != null) {
                listener.onMusicLibraryUpdate();
            }
        }
    }

    @Override
    public void onCategoryUpdate() {
        notifyMLU();
    }

    @Override
    public void onPlaylistUpdate() {
        notifyMLU();
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
