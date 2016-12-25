package eu.applabs.allplaylibrary.data;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class MusicLibrary implements ServiceCategory.OnCategoryUpdateListener, ServicePlaylist.OnPlaylistUpdateListener {

    public interface OnMusicLibraryUpdateListener {
        void onMusicLibraryUpdate();
    }

    private static MusicLibrary s_Instance = null;

    private List<ServiceLibrary> mM_ServiceLibraryList = null;
    private List<OnMusicLibraryUpdateListener> mM_OnMusicLibraryUpdateListenerList = null;

    // Private (Singelton)
    private MusicLibrary() {}

    public static synchronized MusicLibrary getInstance() {
        if(MusicLibrary.s_Instance == null) {
            MusicLibrary.s_Instance = new MusicLibrary();
            MusicLibrary.s_Instance.mM_ServiceLibraryList = new ArrayList<>();
            MusicLibrary.s_Instance.mM_OnMusicLibraryUpdateListenerList = new ArrayList<>();
        }

        return MusicLibrary.s_Instance;
    }

    public void clearLibrary() {
        for(ServiceLibrary library : mM_ServiceLibraryList) {
            library.clearLibrary();
        }

        mM_ServiceLibraryList.clear();
    }

    public void search(String query, ServiceLibrary.OnServiceLibrarySearchResult callback) {
        if(query != null && callback != null) {
            QueryThread qt = new QueryThread(query, callback);
            qt.start();
        }
    }

    public List<ServiceLibrary> getLibraries() {
        return mM_ServiceLibraryList;
    }

    public void addMusicLibrary(ServiceLibrary library) {
        mM_ServiceLibraryList.add(library);
        notifyMLU();
    }

    public void removeMusicLibrary(ServiceLibrary library) {
        mM_ServiceLibraryList.remove(library);
        notifyMLU();
    }

    public void registerListener(OnMusicLibraryUpdateListener listener) {
        mM_OnMusicLibraryUpdateListenerList.add(listener);
    }

    public void unregisterListener(OnMusicLibraryUpdateListener listener) {
        mM_OnMusicLibraryUpdateListenerList.remove(listener);
    }

    private void notifyMLU() {
        for(OnMusicLibraryUpdateListener listener : mM_OnMusicLibraryUpdateListenerList) {
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

            for(ServiceLibrary library : mM_ServiceLibraryList) {
                List<ServiceCategory> list = library.search(m_Query);

                if(list != null && list.size() > 0) {
                    results.addAll(list);
                }
            }

            m_Callback.onSearchResult(results);
        }
    }
}
