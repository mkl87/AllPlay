package eu.applabs.allplaylibrary.data;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class MusicLibrary implements IMusicLibraryCategoryUpdateListener, IMusicLibraryPlaylistUpdateListener {

    private static MusicLibrary s_Instance = null;

    private List<IMusicLibrary> m_IMusicLibraryList = null;
    private List<IMusicLibraryUpdateListener> m_IMusicLibraryUpdateListenerList = null;

    // Private (Singelton)
    private MusicLibrary() {}

    public static synchronized MusicLibrary getInstance() {
        if(MusicLibrary.s_Instance == null) {
            MusicLibrary.s_Instance = new MusicLibrary();
            MusicLibrary.s_Instance.m_IMusicLibraryList = new ArrayList<>();
            MusicLibrary.s_Instance.m_IMusicLibraryUpdateListenerList = new ArrayList<>();
        }

        return MusicLibrary.s_Instance;
    }

    public void clearLibrary() {
        for(IMusicLibrary library : m_IMusicLibraryList) {
            library.clearLibrary();
        }

        m_IMusicLibraryList.clear();
    }

    public void search(String query, IMusicLibrarySearchResultCallback callback) {
        if(query != null && callback != null) {
            QueryThread qt = new QueryThread(query, callback);
            qt.start();
        }
    }

    public List<IMusicLibrary> getLibraries() {
        return m_IMusicLibraryList;
    }

    public void addMusicLibrary(IMusicLibrary library) {
        m_IMusicLibraryList.add(library);
        notifyMLU();
    }

    public void removeMusicLibrary(IMusicLibrary library) {
        m_IMusicLibraryList.remove(library);
        notifyMLU();
    }

    public void registerListener(IMusicLibraryUpdateListener listener) {
        m_IMusicLibraryUpdateListenerList.add(listener);
    }

    public void unregisterListener(IMusicLibraryUpdateListener listener) {
        m_IMusicLibraryUpdateListenerList.remove(listener);
    }

    private void notifyMLU() {
        for(IMusicLibraryUpdateListener listener : m_IMusicLibraryUpdateListenerList) {
            if(listener != null) {
                listener.onMusicLibraryUpdate();
            }
        }
    }

    @Override
    public void onMusicLibraryCategoryUpdate() {
        notifyMLU();
    }

    @Override
    public void onMusicLibraryPlaylistUpdate() {
        notifyMLU();
    }

    private class QueryThread extends Thread {
        private String m_Query = null;
        private IMusicLibrarySearchResultCallback m_Callback = null;

        public QueryThread(String query, IMusicLibrarySearchResultCallback callback) {
            m_Query = query;
            m_Callback = callback;
        }

        @Override
        public void run() {
            super.run();

            List<IMusicLibraryCategory> results = new CopyOnWriteArrayList<>();

            for(IMusicLibrary library : m_IMusicLibraryList) {
                List<IMusicLibraryCategory> list = library.search(m_Query);

                if(list != null && list.size() > 0) {
                    results.addAll(list);
                }
            }

            m_Callback.onResult(results);
        }
    }
}
