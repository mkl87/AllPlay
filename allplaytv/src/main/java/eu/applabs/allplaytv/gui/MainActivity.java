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

import eu.applabs.allplaylibrary.data.IMusicLibrary;
import eu.applabs.allplaylibrary.data.IMusicLibraryCategory;
import eu.applabs.allplaylibrary.data.IMusicLibraryPlaylist;
import eu.applabs.allplaylibrary.data.IMusicLibraryUpdateListener;
import eu.applabs.allplaylibrary.data.MusicLibrary;
import eu.applabs.allplaylibrary.player.IPlayer;
import eu.applabs.allplaylibrary.player.Player;
import eu.applabs.allplaylibrary.player.Playlist;
import eu.applabs.allplaytv.R;
import eu.applabs.allplaytv.data.Action;
import eu.applabs.allplaytv.presenter.ActionPresenter;
import eu.applabs.allplaytv.presenter.PlaylistPresenter;

public class MainActivity extends Activity implements IMusicLibraryUpdateListener,
                                                            OnItemViewClickedListener,
                                                            View.OnClickListener {

    private Activity m_Activity = null;
    private Player m_Player = null;
    private MusicLibrary m_MusicLibrary = null;

    private FragmentManager m_FragmentManager = null;
    private BrowseFragment m_BrowseFragment = null;

    private ArrayObjectAdapter m_MusicLibraryAdapter = null;
    private ArrayObjectAdapter m_ActionAdapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        m_Activity = this;

        m_FragmentManager = getFragmentManager();
        m_BrowseFragment = (BrowseFragment) m_FragmentManager.findFragmentById(R.id.id_frag_MainActivity);

        m_BrowseFragment.setHeadersState(BrowseFragment.HEADERS_ENABLED);
        m_BrowseFragment.setTitle("AllPlay");
        m_BrowseFragment.setOnItemViewClickedListener(this);
        m_BrowseFragment.setOnSearchClickedListener(this);
        m_BrowseFragment.setSearchAffordanceColor(getResources().getColor(R.color.accent));

        BackgroundManager backgroundManager = BackgroundManager.getInstance(this);
        backgroundManager.attach(this.getWindow());
        backgroundManager.setDrawable(getResources().getDrawable(R.drawable.background, null));

        m_MusicLibraryAdapter = new ArrayObjectAdapter(new ListRowPresenter());
        m_BrowseFragment.setAdapter(m_MusicLibraryAdapter);
        initializeActionAdapter();

        m_MusicLibrary = MusicLibrary.getInstance();
        m_MusicLibrary.registerListener(this);

        m_Player = Player.getInstance();
        m_Player.initialize(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(!m_Player.checkActivityResult(requestCode, resultCode, data)) {
            // Seems to be a result for another request
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(m_MusicLibrary != null) {
            m_MusicLibrary.unregisterListener(this);
            m_MusicLibrary.clearLibrary();
        }

        if(m_Player != null) {
            m_Player.clearPlayer();
        }

        Glide.get(this).clearMemory();
    }

    @Override
    public void onMusicLibraryUpdate() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                m_MusicLibraryAdapter.clear();

                for (IMusicLibrary library : m_MusicLibrary.getLibraries()) {
                    if (library != null) {
                        for (IMusicLibraryCategory category : library.getCategories()) {
                            if (category != null) {
                                ArrayObjectAdapter categoryAdapter = new ArrayObjectAdapter(new PlaylistPresenter());

                                for (IMusicLibraryPlaylist playlist : category.getPlaylists()) {
                                    if (playlist != null) {
                                        categoryAdapter.add(playlist);
                                    }
                                }

                                if (category.getCategoryName().compareTo(getResources().getString(R.string.category_currentplayback)) == 0) {
                                    HeaderItem header = new HeaderItem(category.getCategoryName());
                                    m_MusicLibraryAdapter.add(0, new ListRow(header, categoryAdapter));
                                } else {
                                    HeaderItem header = new HeaderItem(category.getCategoryName());
                                    m_MusicLibraryAdapter.add(new ListRow(header, categoryAdapter));
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
                if (m_Player != null) {
                    IPlayer.State state = m_Player.getPlayerState();

                    if(state == IPlayer.State.Playing) {
                        m_Player.pause();
                    } else if(state == IPlayer.State.Paused) {
                        m_Player.resume();
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
        if(item instanceof IMusicLibraryPlaylist) {
            IMusicLibraryPlaylist imusiclibraryplaylist = (IMusicLibraryPlaylist) item;

            if(imusiclibraryplaylist instanceof Playlist) {
                Intent intent = new Intent(this, PlaylistActivity.class);
                startActivity(intent);
                return;
            }

            Playlist playlist = m_Player.getPlaylist();
            playlist.clear();
            playlist.setPlaylist(imusiclibraryplaylist.getPlaylist());

            m_Player.play();

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
        m_ActionAdapter = new ArrayObjectAdapter(new ActionPresenter());

        Action manageAccounts = new Action();
        manageAccounts.setName(getResources().getString(R.string.mainactivity_action_manageaccounts));
        manageAccounts.setIcon(getResources().getDrawable(R.drawable.banner, null));
        manageAccounts.setIntent(new Intent(m_Activity, ManageAccountsActivity.class));

        Action voiceRecord = new Action();
        voiceRecord.setName("VoiceRecord");
        voiceRecord.setIcon(getResources().getDrawable(R.drawable.banner, null));
        voiceRecord.setIntent(new Intent(m_Activity, SearchActivity.class));

        m_ActionAdapter.add(manageAccounts);
        m_ActionAdapter.add(voiceRecord);

        addActionAdapter();
    }

    private void addActionAdapter() {
        HeaderItem header = new HeaderItem(getResources().getString(R.string.mainactivity_header_settings));
        m_MusicLibraryAdapter.add(new ListRow(header, m_ActionAdapter));
    }
}
