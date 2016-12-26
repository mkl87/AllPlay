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
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowPresenter;
import android.view.KeyEvent;
import android.view.View;

import com.bumptech.glide.Glide;

import eu.applabs.allplaylibrary.data.ServiceLibrary;
import eu.applabs.allplaylibrary.data.ServiceCategory;
import eu.applabs.allplaylibrary.data.ServicePlaylist;
import eu.applabs.allplaylibrary.data.MusicLibrary;
import eu.applabs.allplaylibrary.player.ServicePlayer;
import eu.applabs.allplaylibrary.player.Player;
import eu.applabs.allplaylibrary.player.Playlist;
import eu.applabs.allplaytv.R;
import eu.applabs.allplaytv.data.Action;
import eu.applabs.allplaytv.presenter.ActionPresenter;
import eu.applabs.allplaytv.presenter.PlaylistPresenter;

public class MainActivity extends Activity implements MusicLibrary.OnMusicLibraryUpdateListener,
                                                            OnItemViewClickedListener,
                                                            View.OnClickListener {

    private Activity mActivity;
    private Player mPlayer;
    private MusicLibrary mMusicLibrary = MusicLibrary.getInstance();

    private FragmentManager mFragmentManager;
    private BrowseFragment mBrowseFragment;

    private ArrayObjectAdapter mMusicLibraryAdapter;
    private ArrayObjectAdapter mActionAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mActivity = this;

        mFragmentManager = getFragmentManager();
        mBrowseFragment = (BrowseFragment) mFragmentManager.findFragmentById(R.id.id_frag_MainActivity);

        mBrowseFragment.setHeadersState(BrowseFragment.HEADERS_ENABLED);
        mBrowseFragment.setTitle("AllPlay");
        mBrowseFragment.setOnItemViewClickedListener(this);
        mBrowseFragment.setOnSearchClickedListener(this);
        mBrowseFragment.setSearchAffordanceColor(getResources().getColor(R.color.accent));

        BackgroundManager backgroundManager = BackgroundManager.getInstance(this);
        backgroundManager.attach(this.getWindow());
        backgroundManager.setDrawable(getResources().getDrawable(R.drawable.background, null));

        mMusicLibraryAdapter = new ArrayObjectAdapter(new ListRowPresenter());
        mBrowseFragment.setAdapter(mMusicLibraryAdapter);
        initializeActionAdapter();

        mMusicLibrary = MusicLibrary.getInstance();
        mMusicLibrary.registerListener(this);

        mPlayer = Player.getInstance();
        mPlayer.initialize(this);
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
                                    HeaderItem header = new HeaderItem(category.getCategoryName());
                                    mMusicLibraryAdapter.add(0, new ListRow(header, categoryAdapter));
                                } else {
                                    HeaderItem header = new HeaderItem(category.getCategoryName());
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
            ServicePlaylist imusiclibraryplaylist = (ServicePlaylist) item;

            if(imusiclibraryplaylist instanceof Playlist) {
                Intent intent = new Intent(this, PlaylistActivity.class);
                startActivity(intent);
                return;
            }

            Playlist playlist = mPlayer.getPlaylist();
            playlist.clear();
            playlist.setPlaylist(imusiclibraryplaylist.getPlaylist());

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

        Action voiceRecord = new Action();
        voiceRecord.setName("VoiceRecord");
        voiceRecord.setIcon(getResources().getDrawable(R.drawable.banner, null));
        voiceRecord.setIntent(new Intent(mActivity, SearchActivity.class));

        mActionAdapter.add(manageAccounts);
        mActionAdapter.add(voiceRecord);

        addActionAdapter();
    }

    private void addActionAdapter() {
        HeaderItem header = new HeaderItem(getResources().getString(R.string.mainactivity_header_settings));
        mMusicLibraryAdapter.add(new ListRow(header, mActionAdapter));
    }
}
