package eu.applabs.allplaytv.gui;


import android.animation.ObjectAnimator;
import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;

import eu.applabs.allplaylibrary.player.IPlayer;
import eu.applabs.allplaylibrary.player.IPlayerListener;
import eu.applabs.allplaylibrary.player.IPlaylistListener;
import eu.applabs.allplaylibrary.player.Player;
import eu.applabs.allplaylibrary.player.Playlist;
import eu.applabs.allplaytv.R;
import eu.applabs.allplaytv.adapter.PlaylistAdapter;

public class PlaylistActivity extends Activity implements IPlaylistListener, IPlayerListener, PlaylistAdapter.OnPositionSelectedListener {

    private Player m_Player = null;
    private Playlist m_Playlist = null;

    private ImageView m_Background = null;
    private RecyclerView m_RecyclerView = null;
    private LinearLayoutManager m_LinearLayoutManager = null;
    private PlaylistAdapter m_PlaylistAdapter = null;
    private ProgressBar m_ProgressBar = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_showplaylist);

        m_Background = (ImageView) findViewById(R.id.id_iv_ShowPlaylistActivity_Background);
        m_ProgressBar = (ProgressBar) findViewById(R.id.id_pb_ShowPlaylistActivity_ProgressBar);

        m_Player = Player.getInstance();
        m_Player.registerListener(this);
        m_Playlist = m_Player.getPlaylist();
        m_Playlist.registerListener(this);

        m_RecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        m_LinearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        m_LinearLayoutManager.scrollToPositionWithOffset(m_Playlist.getCurrentSongIndex(), 640);

        m_RecyclerView.setLayoutManager(m_LinearLayoutManager);

        m_PlaylistAdapter = new PlaylistAdapter(this, m_Background, Player.getInstance().getPlaylist().getPlaylistAsSongList(), this);
        m_RecyclerView.setAdapter(m_PlaylistAdapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        m_Player.unregisterListener(this);
        m_Playlist.unregisterListener(this);

        m_PlaylistAdapter.clearPlaylistAdapter();
        m_PlaylistAdapter = null;

        m_Background = null;
        m_ProgressBar = null;
        m_LinearLayoutManager = null;
        m_RecyclerView = null;

        Glide.get(this).clearMemory();
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
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                if(m_Player != null) {
                    m_Player.next();
                }

                return true;
            case KeyEvent.KEYCODE_DPAD_LEFT:
                if(m_Player != null) {
                    m_Player.prev();
                }

                return true;
            default:
                return super.onKeyUp(keyCode, event);
        }
    }

    @Override
    public void onPlaylistUpdate() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                m_PlaylistAdapter.updatePlaylist(m_Playlist.getPlaylistAsSongList());
                m_LinearLayoutManager.scrollToPositionWithOffset(m_Playlist.getCurrentSongIndex(), 640);
            }
        });
    }

    @Override
    public void onPlayerStateChanged(IPlayer.ServiceType type, IPlayer.State old_state, IPlayer.State new_state) {
        // Nothing to do
    }

    @Override
    public void onPlayerEvent(IPlayer.Event event) {
        // Nothing to do
    }

    @Override
    public void onLoginSuccess(IPlayer.ServiceType type) {
        // Nothing to do
    }

    @Override
    public void onLoginError(IPlayer.ServiceType type) {
        // Nothing to do
    }

    @Override
    public void onLogoutSuccess(IPlayer.ServiceType type) {
        // Nothing to do
    }

    @Override
    public void onLogoutError(IPlayer.ServiceType type) {
        // Nothing to do
    }

    @Override
    public void onPlayerPlaybackPositionChanged(final int position) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ObjectAnimator animator = ObjectAnimator.ofInt(m_ProgressBar, "progress", position);
                animator.setDuration(500); // 0.5 Sec
                animator.setInterpolator(new DecelerateInterpolator());
                animator.start();
            }
        });
    }

    @Override
    public void onPositionSelected(int position) {
        while (m_Player.getPlaylist().getCurrentSongIndex() != position) {
            int currentPosition = m_Player.getPlaylist().getCurrentSongIndex();

            if(currentPosition > position) {
                m_Player.prev();
            } else {
                m_Player.next();
            }
        }
    }
}
