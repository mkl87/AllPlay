package eu.applabs.allplaytv.utils;

import eu.applabs.allplaytv.gui.PlaylistSearchFragment;

public class SearchRunnable implements Runnable {

    private PlaylistSearchFragment mPlaylistSearchFragment;
    private String mQuery;

    public SearchRunnable(PlaylistSearchFragment fragment) {
        mPlaylistSearchFragment = fragment;
    }

    public void setQuery(String query) {
        mQuery = query;
    }

    @Override
    public void run() {
        mPlaylistSearchFragment.showResultsForQuery(mQuery);
    }

}
