package eu.applabs.allplaylibrary.data;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class MusicLibrary implements ServiceCategory.OnCategoryUpdateListener, ServicePlaylist.OnPlaylistUpdateListener {

    public interface OnMusicLibraryUpdateListener {
        void onMusicLibraryUpdate();
    }

    private static MusicLibrary mMusicLibrary;

    private List<ServiceLibrary> mServiceLibraryList;
    private List<OnMusicLibraryUpdateListener> mOnMusicLibraryUpdateListenerList;

    // Private (Singelton)
    private MusicLibrary() {}

    public static synchronized MusicLibrary getInstance() {
        if(MusicLibrary.mMusicLibrary == null) {
            MusicLibrary.mMusicLibrary = new MusicLibrary();
            MusicLibrary.mMusicLibrary.mServiceLibraryList = new ArrayList<>();
            MusicLibrary.mMusicLibrary.mOnMusicLibraryUpdateListenerList = new ArrayList<>();
        }

        return MusicLibrary.mMusicLibrary;
    }

    public void clearLibrary() {
        for(ServiceLibrary library : mServiceLibraryList) {
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
        mServiceLibraryList.add(library);
        notifyMLU();
    }

    public void removeMusicLibrary(ServiceLibrary library) {
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
