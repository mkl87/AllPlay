package eu.applabs.allplaytv.gui;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v17.leanback.app.BackgroundManager;
import android.support.v17.leanback.app.BrowseFragment;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.ListRowPresenter;
import android.support.v17.leanback.widget.OnItemViewClickedListener;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.PresenterSelector;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowPresenter;
import android.support.v4.content.ContextCompat;
import android.view.KeyEvent;
import android.view.View;

import com.bumptech.glide.Glide;

import java.util.Observable;
import java.util.Observer;

import eu.applabs.allplaylibrary.AllPlayLibrary;
import eu.applabs.allplaylibrary.event.CategoryEvent;
import eu.applabs.allplaylibrary.event.Event;
import eu.applabs.allplaylibrary.event.PlaylistEvent;
import eu.applabs.allplaylibrary.services.ServiceLibrary;
import eu.applabs.allplaylibrary.services.ServiceCategory;
import eu.applabs.allplaylibrary.services.ServicePlaylist;
import eu.applabs.allplaylibrary.MusicCatalog;
import eu.applabs.allplaylibrary.services.ServicePlayer;
import eu.applabs.allplaylibrary.Player;
import eu.applabs.allplaytv.R;
import eu.applabs.allplaytv.adapter.ServiceCategoryAdapter;
import eu.applabs.allplaytv.data.Action;
import eu.applabs.allplaytv.presenter.ActionPresenter;
import eu.applabs.allplaytv.presenter.IconHeaderItemPresenter;

public class MainActivity extends Activity implements Observer, OnItemViewClickedListener, View.OnClickListener {

    private Activity mActivity = this;

    private AllPlayLibrary mAllPlayLibrary;
    private MusicCatalog mMusicCatalog;
    private Player mPlayer;

    private FragmentManager mFragmentManager;
    private BrowseFragment mBrowseFragment;

    private ArrayObjectAdapter mMusicLibraryAdapter;
    private ArrayObjectAdapter mActionAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Init the library
        mAllPlayLibrary = AllPlayLibrary.getInstance();
        mAllPlayLibrary.init(this);

        mMusicCatalog = mAllPlayLibrary.getMusicLibrary();
        mPlayer = mAllPlayLibrary.getPlayer();

        mFragmentManager = getFragmentManager();
        mBrowseFragment = (BrowseFragment) mFragmentManager.findFragmentById(R.id.id_frag_MainActivity);

        mBrowseFragment.setHeadersState(BrowseFragment.HEADERS_ENABLED);
        mBrowseFragment.setHeaderPresenterSelector(new PresenterSelector() {
            @Override
            public Presenter getPresenter(Object item) {
                return new IconHeaderItemPresenter();
            }
        });

        mBrowseFragment.setTitle(getString(R.string.app_name));
        mBrowseFragment.setOnItemViewClickedListener(this);
        mBrowseFragment.setOnSearchClickedListener(this);
        mBrowseFragment.setSearchAffordanceColor(ContextCompat.getColor(this, R.color.accent));

        BackgroundManager backgroundManager = BackgroundManager.getInstance(this);
        backgroundManager.attach(this.getWindow());
        backgroundManager.setDrawable(ContextCompat.getDrawable(this, R.drawable.background));

        mMusicLibraryAdapter = new ArrayObjectAdapter(new ListRowPresenter());
        mBrowseFragment.setAdapter(mMusicLibraryAdapter);
        initializeActionAdapter();

        // Observe the MusicCatalog to update the UI after updates
        mMusicCatalog.addObserver(this);
        // Observe the Player to update the UI after updates
        mPlayer.addObserver(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(!mAllPlayLibrary.checkActivityResult(requestCode, resultCode, data)) {
            // Seems to be a result for another request
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(mMusicCatalog != null) {
            mMusicCatalog.deleteObserver(this);
            mMusicCatalog.clearLibrary();
        }

        if(mPlayer != null) {
            mPlayer.deleteObserver(this);
            mPlayer.clearPlayer();
        }

        AllPlayLibrary.getInstance().deinit();
        Glide.get(this).clearMemory();
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                if (mPlayer != null) {
                    ServicePlayer.PlayerState state = mPlayer.getPlayerState();

                    if(state == ServicePlayer.PlayerState.PLAYING) {
                        mPlayer.pause();
                    } else if(state == ServicePlayer.PlayerState.PAUSED) {
                        mPlayer.resume();
                    }
                }

                return true;
            case KeyEvent.KEYCODE_SEARCH:
                onClick(null);

                return true;
            default:
                return super.onKeyUp(keyCode, event);
        }
    }

    @Override
    public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item, RowPresenter.ViewHolder rowViewHolder, Row row) {
        if(item instanceof ServicePlaylist) {
            ServicePlaylist servicePlaylist = (ServicePlaylist) item;

            if(servicePlaylist == mPlayer.getServicePlaylist()) {
                Intent intent = new Intent(this, PlaylistActivity.class);
                startActivity(intent);
                return;
            }

            mPlayer.setServicePlaylist(servicePlaylist);

            mPlayer.play();

            Intent intent = new Intent(this, PlaylistActivity.class);
            startActivity(intent);
        } else if(item instanceof Action) {
            Action action = (Action) item;
            startActivity(action.getIntent());
        }
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(this, SearchActivity.class);
        startActivity(intent);
    }

    private void initializeActionAdapter() {
        mActionAdapter = new ArrayObjectAdapter(new ActionPresenter());

        Action manageAccounts = new Action();
        manageAccounts.setName(getResources().getString(R.string.mainactivity_action_manageaccounts));
        manageAccounts.setIcon(getResources().getDrawable(R.drawable.banner, null));
        manageAccounts.setIntent(new Intent(mActivity, ManageAccountsActivity.class));

        mActionAdapter.add(manageAccounts);

        addActionAdapter();
    }

    private void addActionAdapter() {
        IconHeaderItem header = new IconHeaderItem(getResources().getString(R.string.mainactivity_header_settings), ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_settings));
        mMusicLibraryAdapter.add(new ListRow(header, mActionAdapter));
    }

    private void addAllCategoriesToAdapter() {
        for (ServiceLibrary library : mMusicCatalog.getLibraries()) {
            for (ServiceCategory category : library.getCategories()) {
                ServiceCategoryAdapter serviceCategoryAdapter = new ServiceCategoryAdapter(category);

                if (category.getCategoryName().compareTo(getResources().getString(R.string.category_currentplayback)) == 0) {
                    IconHeaderItem header = new IconHeaderItem(category.getCategoryName(), ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_playing));
                    mMusicLibraryAdapter.add(0, new ListRow(header, serviceCategoryAdapter));
                } else {
                    IconHeaderItem header = new IconHeaderItem(getApplicationContext(), category.getCategoryName(), category.getServiceType());
                    mMusicLibraryAdapter.add(new ListRow(header, serviceCategoryAdapter));
                }
            }
        }
    }

    private void updateCategoryOfAdapter(ServiceCategory serviceCategory) {
        for (int i = 0; i < mMusicLibraryAdapter.size(); ++i) {
            ListRow categoryRow = ((ListRow) mMusicLibraryAdapter.get(i));
            if (categoryRow.getAdapter() instanceof ServiceCategoryAdapter) {
                ServiceCategoryAdapter serviceCategoryAdapter = ((ServiceCategoryAdapter) categoryRow.getAdapter());

                if (serviceCategory == serviceCategoryAdapter.getServiceCategory()) {
                    serviceCategoryAdapter.setServiceCategory(serviceCategory);
                }
            }
        }
    }

    private void updatePlaylistOfAdapter(ServicePlaylist servicePlaylist) {
        for (int i = 0; i < mMusicLibraryAdapter.size(); ++i) {
            ListRow categoryRow = ((ListRow) mMusicLibraryAdapter.get(i));
            if (categoryRow.getAdapter() instanceof ServiceCategoryAdapter) {
                ServiceCategoryAdapter serviceCategoryAdapter = ((ServiceCategoryAdapter) categoryRow.getAdapter());
                for (int p = 0; p < serviceCategoryAdapter.size(); ++p) {
                    if (serviceCategoryAdapter.get(p) instanceof ServicePlaylist) {
                        ServicePlaylist adapterPlaylist = (ServicePlaylist) serviceCategoryAdapter.get(p);

                        if (servicePlaylist == adapterPlaylist) {
                            serviceCategoryAdapter.notifyArrayItemRangeChanged(p, 1);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void update(Observable observable, final Object args) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (args instanceof CategoryEvent) {
                    CategoryEvent categoryEvent = (CategoryEvent) args;
                    ServiceCategory updatedServiceCategory = categoryEvent.getServiceCategory();
                    updateCategoryOfAdapter(updatedServiceCategory);
                } else if (args instanceof PlaylistEvent) {
                    PlaylistEvent playlistEvent = (PlaylistEvent) args;
                    ServicePlaylist updatedServicePlaylist = playlistEvent.getServicePlaylist();
                    updatePlaylistOfAdapter(updatedServicePlaylist);
                } else if (args instanceof Event) {
                    Event event = (Event) args;

                    if (event.getEventType() == Event.EventType.MUSIC_CATALOG_EVENT) {
                        mMusicLibraryAdapter.clear();
                        addAllCategoriesToAdapter();
                        addActionAdapter();
                    }
                }
            }
        });
    }
}
