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

import java.util.Observable;
import java.util.Observer;

import butterknife.BindView;
import butterknife.ButterKnife;
import eu.applabs.allplaylibrary.AllPlayLibrary;
import eu.applabs.allplaylibrary.Playlist;
import eu.applabs.allplaylibrary.event.Event;
import eu.applabs.allplaylibrary.event.PlayerEvent;
import eu.applabs.allplaylibrary.Player;
import eu.applabs.allplaylibrary.services.ServicePlayer;
import eu.applabs.allplaytv.R;
import eu.applabs.allplaytv.adapter.PlaylistAdapter;

public class PlaylistActivity extends Activity implements Observer, PlaylistAdapter.OnPositionSelectedListener {

    private Player mPlayer = AllPlayLibrary.getInstance().getPlayer();
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

        mPlayer.addObserver(this);
        mPlaylist = mPlayer.getPlaylist();
        mPlaylist.addObserver(this);

        mLinearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mLinearLayoutManager.scrollToPositionWithOffset(mPlaylist.getCurrentSongIndex(), 640);

        mRecyclerView.setLayoutManager(mLinearLayoutManager);

        mPlaylistAdapter = new PlaylistAdapter(mBackground, mPlayer.getPlaylist().getPlaylist(), this);
        mRecyclerView.setAdapter(mPlaylistAdapter);
    }

    @Override
    protected void onDestroy() {
        mPlayer.deleteObserver(this);
        mPlaylist.deleteObserver(this);

        mPlaylistAdapter.clearImages();
        mPlaylistAdapter = null;

        mBackground = null;
        mProgressBar = null;
        mLinearLayoutManager = null;
        mRecyclerView = null;

        Glide.get(this).clearMemory();

        super.onDestroy();
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                if (mPlayer != null) {
                    ServicePlayer.PlayerState state = mPlayer.getPlayerState();

                    if (state == ServicePlayer.PlayerState.PLAYING) {
                        mPlayer.pause();
                    } else if (state == ServicePlayer.PlayerState.PAUSED) {
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

    @Override
    public void update(Observable observable, Object o) {
        if(o instanceof Event) {
            Event event = (Event) o;

            switch (event.getEventType()) {
                case PLAYLIST_EVENT:
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mPlaylistAdapter.updatePlaylist(mPlaylist.getPlaylist());
                            mLinearLayoutManager.scrollToPositionWithOffset(mPlaylist.getCurrentSongIndex(), 640);
                        }
                    });
                    break;
                case PLAYER_EVENT:
                    final PlayerEvent playerEvent = (PlayerEvent) event;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ObjectAnimator animator = ObjectAnimator.ofInt(mProgressBar, "progress", playerEvent.getPlaybackPosition());
                            animator.setDuration(500); // 0.5 Sec
                            animator.setInterpolator(new DecelerateInterpolator());
                            animator.start();
                        }
                    });
                    break;
            }
        }
    }
}
