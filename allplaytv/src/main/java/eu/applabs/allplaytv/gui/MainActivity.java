package eu.applabs.allplaytv.gui;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v17.leanback.app.BackgroundManager;
import android.support.v17.leanback.app.BrowseFragment;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.HeaderItem;
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

import eu.applabs.allplaylibrary.AllPlayLibrary;
import eu.applabs.allplaylibrary.data.ServiceLibrary;
import eu.applabs.allplaylibrary.data.ServiceCategory;
import eu.applabs.allplaylibrary.data.ServicePlaylist;
import eu.applabs.allplaylibrary.data.MusicLibrary;
import eu.applabs.allplaylibrary.player.NowPlayingPlaylist;
import eu.applabs.allplaylibrary.player.ServicePlayer;
import eu.applabs.allplaylibrary.player.Player;
import eu.applabs.allplaytv.R;
import eu.applabs.allplaytv.data.Action;
import eu.applabs.allplaytv.presenter.ActionPresenter;
import eu.applabs.allplaytv.presenter.IconHeaderItemPresenter;
import eu.applabs.allplaytv.presenter.PlaylistPresenter;

public class MainActivity extends Activity implements MusicLibrary.OnMusicLibraryUpdateListener,
                                                            OnItemViewClickedListener,
                                                            View.OnClickListener {

    private Activity mActivity = this;
    private Player mPlayer;
    private MusicLibrary mMusicLibrary;

    private FragmentManager mFragmentManager;
    private BrowseFragment mBrowseFragment;

    private ArrayObjectAdapter mMusicLibraryAdapter;
    private ArrayObjectAdapter mActionAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Init the library
        AllPlayLibrary library = AllPlayLibrary.getInstance();
        library.init(this);
        mPlayer = library.getPlayer();
        mMusicLibrary = library.getMusicLibrary();

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

        mMusicLibrary.registerListener(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(!mPlayer.checkActivityResult(requestCode, resultCode, data)) {
            // Seems to be a result for another request
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(mMusicLibrary != null) {
            mMusicLibrary.unregisterListener(this);
            mMusicLibrary.clearLibrary();
        }

        if(mPlayer != null) {
            mPlayer.clearPlayer();
        }

        Glide.get(this).clearMemory();
    }

    @Override
    public void onMusicLibraryUpdate() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mMusicLibraryAdapter.clear();

                for (ServiceLibrary library : mMusicLibrary.getLibraries()) {
                    if (library != null) {
                        for (ServiceCategory category : library.getCategories()) {
                            if (category != null) {
                                ArrayObjectAdapter categoryAdapter = new ArrayObjectAdapter(new PlaylistPresenter());

                                for (ServicePlaylist playlist : category.getPlaylists()) {
                                    if (playlist != null) {
                                        categoryAdapter.add(playlist);
                                    }
                                }

                                if (category.getCategoryName().compareTo(getResources().getString(R.string.category_currentplayback)) == 0) {
                                    IconHeaderItem header = new IconHeaderItem(category.getCategoryName(), ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_playing));
                                    mMusicLibraryAdapter.add(0, new ListRow(header, categoryAdapter));
                                } else {
                                    IconHeaderItem header = null;
                                    switch (library.getServiceType()) {
                                        case SPOTIFY:
                                            header = new IconHeaderItem(category.getCategoryName(), ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_spotify));
                                            break;
                                        case DEEZER:
                                            header = new IconHeaderItem(category.getCategoryName(), ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_deezer));
                                            break;
                                        case GOOGLE_MUSIC:
                                            header = new IconHeaderItem(category.getCategoryName(), ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_googlemusic));
                                            break;
                                        default:
                                            header = new IconHeaderItem(category.getCategoryName(), ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_default));
                                            break;
                                    }
                                    mMusicLibraryAdapter.add(new ListRow(header, categoryAdapter));
                                }
                            }
                        }
                    }
                }

                addActionAdapter();
            }
        });
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                if (mPlayer != null) {
                    ServicePlayer.State state = mPlayer.getPlayerState();

                    if(state == ServicePlayer.State.Playing) {
                        mPlayer.pause();
                    } else if(state == ServicePlayer.State.Paused) {
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

            if(servicePlaylist instanceof NowPlayingPlaylist) {
                Intent intent = new Intent(this, PlaylistActivity.class);
                startActivity(intent);
                return;
            }

            NowPlayingPlaylist nowPlayingPlaylist = mPlayer.getPlaylist();
            nowPlayingPlaylist.clear();
            nowPlayingPlaylist.setPlaylist(servicePlaylist.getPlaylist());

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
}
