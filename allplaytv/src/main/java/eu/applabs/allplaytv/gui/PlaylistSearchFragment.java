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

import eu.applabs.allplaylibrary.data.ServiceLibrary;
import eu.applabs.allplaylibrary.data.ServiceCategory;
import eu.applabs.allplaylibrary.data.ServicePlaylist;
import eu.applabs.allplaylibrary.data.MusicLibrary;
import eu.applabs.allplaylibrary.player.Player;
import eu.applabs.allplaylibrary.player.Playlist;
import eu.applabs.allplaytv.R;
import eu.applabs.allplaytv.presenter.PlaylistPresenter;
import eu.applabs.allplaytv.utils.SearchRunnable;

public class PlaylistSearchFragment extends SearchFragment implements ServiceLibrary.OnServiceLibrarySearchResult, SearchFragment.SearchResultProvider, OnItemViewClickedListener {

    private static final int SEARCH_DELAY_MS = 300;

    private ProgressDialog m_LoadingDialog = null;
    private ArrayObjectAdapter m_RowAdapter = null;
    private Handler m_Handler = null;
    private SearchRunnable m_DelayedLoad = null;
    private MusicLibrary m_MusicLibrary = null;
    private Player m_Player = null;
    private ArrayList<ServicePlaylist> m_PlaylistList = null;
    private SearchActivity m_SearchActivity = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setOnItemViewClickedListener(this);

        m_PlaylistList = new ArrayList<>();

        m_Player = Player.getInstance();
        m_MusicLibrary = MusicLibrary.getInstance();

        if(m_MusicLibrary != null) {
            for(ServiceLibrary library : m_MusicLibrary.getLibraries()) {
                for(ServiceCategory category : library.getCategories()) {
                    for(ServicePlaylist playlist : category.getPlaylists()) {
                        m_PlaylistList.add(playlist);
                    }
                }
            }
        }

        m_RowAdapter = new ArrayObjectAdapter(new ListRowPresenter());

        setSearchResultProvider(this);
        setOnItemViewClickedListener(this);
        m_Handler = new Handler();
        m_DelayedLoad = new SearchRunnable(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Glide.get(getActivity()).clearMemory();
    }

    @Override
    public ObjectAdapter getResultsAdapter() {
        return m_RowAdapter;
    }

    @Override
    public boolean onQueryTextChange(String newQuery) {
        return false;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        if(query != null && query.compareTo("") != 0) {
            m_RowAdapter.clear();

            m_DelayedLoad.setQuery(query);
            m_Handler.removeCallbacks(m_DelayedLoad);
            m_Handler.postDelayed(m_DelayedLoad, SEARCH_DELAY_MS);

            return true;
        }

        return false;
    }

    @Override
    public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item, RowPresenter.ViewHolder rowViewHolder, Row row) {
        if(item instanceof ServicePlaylist) {
            if(m_SearchActivity != null) {
                ServicePlaylist imusiclibraryplaylist = (ServicePlaylist) item;
                Playlist playlist = m_Player.getPlaylist();
                playlist.clear();
                playlist.setPlaylist(imusiclibraryplaylist.getPlaylist());

                m_Player.play();

                // Start playlist
                Intent intent = new Intent(m_SearchActivity, PlaylistActivity.class);
                startActivity(intent);
            }
        }
    }

    public void setSearchActivity(SearchActivity activity) {
        m_SearchActivity = activity;
    }

    public void showResultsForQuery(String query) {
        m_LoadingDialog = ProgressDialog.show(getActivity(), "", getResources().getString(R.string.dialog_loading), true);
        m_MusicLibrary.search(query, this);
    }

    public void clear() {
        Glide.get(getActivity()).clearMemory();
        m_RowAdapter.clear();
    }

    @Override
    public void onSearchResult(List<ServiceCategory> list) {
        final List<ServiceCategory> result = list;

        getActivity().runOnUiThread(
            new Runnable() {
                @Override
                public void run() {
                    m_LoadingDialog.dismiss();

                    if(result != null && result.size() > 0) {
                        for(ServiceCategory category : result) {
                            ArrayObjectAdapter playlistAdapter = new ArrayObjectAdapter(new PlaylistPresenter());

                            for(ServicePlaylist playlist : category.getPlaylists()) {
                                playlistAdapter.add(playlist);
                            }

                            HeaderItem header = new HeaderItem(category.getCategoryName());
                            m_RowAdapter.add(new ListRow(header, playlistAdapter));
                        }
                    }
                }
            }
        );
    }
}
