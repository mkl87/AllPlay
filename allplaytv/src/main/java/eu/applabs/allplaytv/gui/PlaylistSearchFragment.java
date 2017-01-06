package eu.applabs.allplaytv.gui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v17.leanback.app.SearchFragment;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.HeaderItem;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.ListRowPresenter;
import android.support.v17.leanback.widget.ObjectAdapter;
import android.support.v17.leanback.widget.OnItemViewClickedListener;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowPresenter;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import eu.applabs.allplaylibrary.AllPlayLibrary;
import eu.applabs.allplaylibrary.Playlist;
import eu.applabs.allplaylibrary.services.ServiceLibrary;
import eu.applabs.allplaylibrary.services.ServiceCategory;
import eu.applabs.allplaylibrary.services.ServicePlaylist;
import eu.applabs.allplaylibrary.MusicCatalog;
import eu.applabs.allplaylibrary.Player;
import eu.applabs.allplaytv.R;
import eu.applabs.allplaytv.presenter.PlaylistPresenter;
import eu.applabs.allplaytv.utils.SearchRunnable;

public class PlaylistSearchFragment extends SearchFragment implements ServiceLibrary.OnServiceLibrarySearchResult, SearchFragment.SearchResultProvider, OnItemViewClickedListener {

    private static final int SEARCH_DELAY_MS = 300;

    private ProgressDialog mLoadingDialog;
    private ArrayObjectAdapter mRowAdapter;
    private Handler mHandler;
    private SearchRunnable mDelayedLoad;
    private AllPlayLibrary mLibrary = AllPlayLibrary.getInstance();
    private MusicCatalog mMusicCatalog = mLibrary.getMusicLibrary();
    private Player mPlayer = mLibrary.getPlayer();
    private ArrayList<ServicePlaylist> mPlaylistList = new ArrayList<>();
    private SearchActivity mSearchActivity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setOnItemViewClickedListener(this);

        if(mMusicCatalog != null) {
            for(ServiceLibrary library : mMusicCatalog.getLibraries()) {
                for(ServiceCategory category : library.getCategories()) {
                    for(ServicePlaylist playlist : category.getPlaylists()) {
                        mPlaylistList.add(playlist);
                    }
                }
            }
        }

        mRowAdapter = new ArrayObjectAdapter(new ListRowPresenter());

        setSearchResultProvider(this);
        setOnItemViewClickedListener(this);
        mHandler = new Handler();
        mDelayedLoad = new SearchRunnable(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Glide.get(getActivity()).clearMemory();
    }

    @Override
    public ObjectAdapter getResultsAdapter() {
        return mRowAdapter;
    }

    @Override
    public boolean onQueryTextChange(String newQuery) {
        return false;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        if(query != null && query.compareTo("") != 0) {
            mRowAdapter.clear();

            mDelayedLoad.setQuery(query);
            mHandler.removeCallbacks(mDelayedLoad);
            mHandler.postDelayed(mDelayedLoad, SEARCH_DELAY_MS);

            return true;
        }

        return false;
    }

    @Override
    public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item, RowPresenter.ViewHolder rowViewHolder, Row row) {
        if(item instanceof ServicePlaylist) {
            if(mSearchActivity != null) {
                ServicePlaylist servicePlaylist = (ServicePlaylist) item;
                mPlayer.setServicePlaylist(servicePlaylist);
                mPlayer.play();

                // Start playlist
                Intent intent = new Intent(mSearchActivity, PlaylistActivity.class);
                startActivity(intent);
            }
        }
    }

    public void setSearchActivity(SearchActivity activity) {
        mSearchActivity = activity;
    }

    public void showResultsForQuery(String query) {
        mLoadingDialog = ProgressDialog.show(getActivity(), "", getResources().getString(R.string.dialog_loading), true);
        mMusicCatalog.search(query, this);
    }

    public void clear() {
        Glide.get(getActivity()).clearMemory();
        mRowAdapter.clear();
    }

    @Override
    public void onSearchResult(List<ServiceCategory> list) {
        final List<ServiceCategory> result = list;

        getActivity().runOnUiThread(
            new Runnable() {
                @Override
                public void run() {
                    mLoadingDialog.dismiss();

                    if(result != null && result.size() > 0) {
                        for(ServiceCategory category : result) {
                            ArrayObjectAdapter playlistAdapter = new ArrayObjectAdapter(new PlaylistPresenter());

                            for(ServicePlaylist playlist : category.getPlaylists()) {
                                playlistAdapter.add(playlist);
                            }

                            HeaderItem header = new HeaderItem(category.getCategoryName());
                            mRowAdapter.add(new ListRow(header, playlistAdapter));
                        }
                    }
                }
            }
        );
    }
}
