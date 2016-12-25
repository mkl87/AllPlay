package eu.applabs.allplaytv.utils;

import eu.applabs.allplaytv.gui.PlaylistSearchFragment;

public class SearchRunnable implements Runnable {

    private PlaylistSearchFragment m_Playlist_SearchFragment = null;
    private String m_Query = null;

    public SearchRunnable(PlaylistSearchFragment fragment) {
        m_Playlist_SearchFragment = fragment;
    }

    public void setQuery(String query) {
        m_Query = query;
    }

    @Override
    public void run() {
        m_Playlist_SearchFragment.showResultsForQuery(m_Query);
    }
}
