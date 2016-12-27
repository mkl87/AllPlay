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

import butterknife.BindView;
import butterknife.ButterKnife;
import eu.applabs.allplaylibrary.player.Player;
import eu.applabs.allplaylibrary.player.PlayerListener;
import eu.applabs.allplaylibrary.player.Playlist;
import eu.applabs.allplaylibrary.player.ServicePlayer;
import eu.applabs.allplaytv.R;
import eu.applabs.allplaytv.adapter.PlaylistAdapter;

public class PlaylistActivity extends Activity implements Playlist.OnPlaylistUpdateListener, PlayerListener, PlaylistAdapter.OnPositionSelectedListener {

    private Player mPlayer;
    private Playlist mPlaylist;

    private LinearLayoutManager mLinearLayoutManager;
    private PlaylistAdapter mPlaylistAdapter;

    @BindView(R.id.id_iv_ShowPlaylistActivity_Background)
    ImageView mBackground;
    @BindView(R.id.id_pb_ShowPlaylistActivity_ProgressBar)
    ProgressBar mProgressBar;
    @BindView(R.id.my_recycler_view)
    RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_showplaylist);
        ButterKnife.bind(this);

        mPlayer = Player.getInstance();
        mPlayer.registerListener(this);
        mPlaylist = mPlayer.getPlaylist();
        mPlaylist.registerListener(this);

        mLinearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mLinearLayoutManager.scrollToPositionWithOffset(mPlaylist.getCurrentSongIndex(), 640);

        mRecyclerView.setLayoutManager(mLinearLayoutManager);

        mPlaylistAdapter = new PlaylistAdapter(this, mBackground, Player.getInstance().getPlaylist().getPlaylistAsSongList(), this);
        mRecyclerView.setAdapter(mPlaylistAdapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mPlayer.unregisterListener(this);
        mPlaylist.unregisterListener(this);

        mPlaylistAdapter.clearPlaylistAdapter();
        mPlaylistAdapter = null;

        mBackground = null;
        mProgressBar = null;
        mLinearLayoutManager = null;
        mRecyclerView = null;

        Glide.get(this).clearMemory();
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                if (mPlayer != null) {
                    ServicePlayer.State state = mPlayer.getPlayerState();

                    if (state == ServicePlayer.State.Playing) {
                        mPlayer.pause();
                    } else if (state == ServicePlayer.State.Paused) {
                        mPlayer.resume();
                    }
                }

                return true;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                if (mPlayer != null) {
                    mPlayer.next();
                }

                return true;
            case KeyEvent.KEYCODE_DPAD_LEFT:
                if (mPlayer != null) {
                    mPlayer.prev();
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
                mPlaylistAdapter.updatePlaylist(mPlaylist.getPlaylistAsSongList());
                mLinearLayoutManager.scrollToPositionWithOffset(mPlaylist.getCurrentSongIndex(), 640);
            }
        });
    }

    @Override
    public void onPlayerStateChanged(ServicePlayer.ServiceType type, ServicePlayer.State old_state, ServicePlayer.State new_state) {
        // Nothing to do
    }

    @Override
    public void onPlayerEvent(ServicePlayer.Event event) {
        // Nothing to do
    }

    @Override
    public void onLoginSuccess(ServicePlayer.ServiceType type) {
        // Nothing to do
    }

    @Override
    public void onLoginError(ServicePlayer.ServiceType type) {
        // Nothing to do
    }

    @Override
    public void onLogoutSuccess(ServicePlayer.ServiceType type) {
        // Nothing to do
    }

    @Override
    public void onLogoutError(ServicePlayer.ServiceType type) {
        // Nothing to do
    }

    @Override
    public void onPlayerPlaybackPositionChanged(final int position) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ObjectAnimator animator = ObjectAnimator.ofInt(mProgressBar, "progress", position);
                animator.setDuration(500); // 0.5 Sec
                animator.setInterpolator(new DecelerateInterpolator());
                animator.start();
            }
        });
    }

    @Override
    public void onPositionSelected(int position) {
        while (mPlayer.getPlaylist().getCurrentSongIndex() != position) {
            int currentPosition = mPlayer.getPlaylist().getCurrentSongIndex();

            if (currentPosition > position) {
                mPlayer.prev();
            } else {
                mPlayer.next();
            }
        }
    }
}
