package eu.applabs.allplaylibrary.services.deezer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.deezer.sdk.model.Album;
import com.deezer.sdk.model.Permissions;
import com.deezer.sdk.model.Playlist;
import com.deezer.sdk.model.Radio;
import com.deezer.sdk.model.Track;
import com.deezer.sdk.network.connect.DeezerConnect;
import com.deezer.sdk.network.connect.SessionStore;
import com.deezer.sdk.network.connect.event.DialogListener;
import com.deezer.sdk.network.request.DeezerRequest;
import com.deezer.sdk.network.request.DeezerRequestFactory;
import com.deezer.sdk.network.request.event.JsonRequestListener;
import com.deezer.sdk.player.DefaultPlayerFactory;
import com.deezer.sdk.player.event.OnPlayerErrorListener;
import com.deezer.sdk.player.event.OnPlayerProgressListener;
import com.deezer.sdk.player.event.OnPlayerStateChangeListener;
import com.deezer.sdk.player.event.PlayerState;
import com.deezer.sdk.player.networkcheck.WifiAndMobileNetworkStateChecker;

import java.util.ArrayList;
import java.util.List;

import eu.applabs.allplaylibrary.R;
import eu.applabs.allplaylibrary.data.MusicLibrary;
import eu.applabs.allplaylibrary.data.Song;
import eu.applabs.allplaylibrary.player.PlayerListener;
import eu.applabs.allplaylibrary.player.ServicePlayer;
import eu.applabs.allplaylibrary.player.Player;

public class DeezerPlayer implements ServicePlayer, OnPlayerErrorListener, OnPlayerProgressListener, OnPlayerStateChangeListener {

    private State mState;
    private DeezerService mDeezerService;
    private DeezerCategory mDeezerCategoryPlaylists;
    private DeezerCategory mDeezerCategoryAlbums;
    private DeezerCategory mDeezerCategoryOwnCharts;
    private MusicLibrary mMusicLibrary = MusicLibrary.getInstance();
    private Player mPlayer = Player.getInstance();
    private boolean mTrackEndBroadcastEnabled = true;

    private Activity mActivity;
    private DeezerConnect mDeezerConnect;
    private SessionStore mSessionStore = new SessionStore();
    private List<PlayerListener> mPlayerListenerList;
    private com.deezer.sdk.player.Player mDeezerPlayer;

    public DeezerPlayer() {
        mPlayerListenerList = new ArrayList<>();
        mState = State.Idle;
    }

    @Override
    public void initialize(Activity activity) {
        mActivity = activity;
        mDeezerConnect = new DeezerConnect(mActivity.getApplication(), mActivity.getString(R.string.deezer_application_id));
        mPlayer.initialize(mActivity);
    }

    @Override
    public void clearPlayer() {
        mDeezerService.clearLibrary();
        mMusicLibrary.removeMusicLibrary(mDeezerService);
        mDeezerPlayer.release();
    }

    @Override
    public void login() {
        // Check if a session was stored and request login if not
        if(!mSessionStore.restore(mDeezerConnect, mActivity.getApplication())) {
            String[] permissions = new String[]{
                    Permissions.BASIC_ACCESS,
                    Permissions.MANAGE_LIBRARY,
                    Permissions.LISTENING_HISTORY,
                    Permissions.OFFLINE_ACCESS,
            };

            DeezerDialogListener ddl = new DeezerDialogListener();
            mDeezerConnect.authorize(mActivity, permissions, ddl);
        } else {
            notifyLoginSuccess();
            loadUserPlaylists();
        }
    }

    @Override
    public void logout() {
        mDeezerConnect.logout(mActivity.getApplication());
        mSessionStore.clear(mActivity.getApplication());
    }

    @Override
    public boolean checkActivityResult(int requestCode, int resultCode, Intent intent) {
        return false;
    }

    @Override
    public State getPlayerState() {
        return mState;
    }

    @Override
    public ServiceType getServiceType() {
        return ServiceType.Deezer;
    }

    @Override
    public boolean play(Song song) {
        if(song != null && song.getServiceType() == ServiceType.Deezer) {
            if(mDeezerPlayer != null) {
                if(mState == State.Playing || mState == State.Paused) {
                    mDeezerPlayer.stop();
                    mTrackEndBroadcastEnabled = false;
                }

                mDeezerPlayer.init(Long.valueOf(song.getId()), song.getUri(), 0, 0);
            }

            return true;
        }

        return false;
    }

    @Override
    public boolean pause(Song song) {
        if(song != null && song.getServiceType() == ServiceType.Deezer) {

            if(mDeezerPlayer != null) {
                mDeezerPlayer.pause();
                changeState(State.Paused);
            }

            return true;
        }

        return false;
    }

    @Override
    public boolean resume(Song song) {
        if(song != null && song.getServiceType() == ServiceType.Deezer) {

            if(mDeezerPlayer != null) {
                mDeezerPlayer.play();
                changeState(State.Playing);
            }

            return true;
        }

        return false;
    }

    @Override
    public boolean stop(Song song) {
        if(song != null && song.getServiceType() == ServiceType.Deezer) {

            if(mDeezerPlayer != null) {
                mDeezerPlayer.stop();
                changeState(State.Idle);
            }

            return true;
        }

        return false;
    }

    @Override
    public void registerListener(PlayerListener listener) {
        mPlayerListenerList.add(listener);
    }

    @Override
    public void unregisterListener(PlayerListener listener) {
        mPlayerListenerList.remove(listener);
    }

    @Override
    public void onPlayerError(Exception e, long l) {
        Log.d("test", e.getMessage());
        e.printStackTrace();
        processEvent(Event.Error);
    }

    @Override
    public void onPlayerProgress(long l) {
        Song song = mPlayer.getPlaylist().getCurrentSong();

        if(song != null && song.getDuration() != 0) {
            int percent = (int) (((l/1000) * 100) /  song.getDuration());

            for(PlayerListener listener : mPlayerListenerList) {
                listener.onPlayerPlaybackPositionChanged(percent);
            }
        }
    }

    @Override
    public void onPlayerStateChange(PlayerState playerState, long l) {
        if(playerState == PlayerState.READY) {
            mDeezerPlayer.play();
        } else if(playerState == PlayerState.PLAYING) {
            changeState(State.Playing);
            mTrackEndBroadcastEnabled = true;
        } else if(playerState == PlayerState.PLAYBACK_COMPLETED && mTrackEndBroadcastEnabled) {
            mDeezerPlayer.stop();
            changeState(State.Idle);
            processEvent(Event.TrackEnd);
        }
    }

    private class DeezerDialogListener implements DialogListener {
        @Override
        public void onComplete(Bundle bundle) {
            notifyLoginSuccess();

            mSessionStore.save(mDeezerConnect, mActivity.getApplication());
            loadUserPlaylists();
        }

        @Override
        public void onCancel() {
            notifyLoginError();
        }

        @Override
        public void onException(Exception e) {
            notifyLoginError();
        }
    }

    private void changeState(State new_state) {
        State old_state = mState;
        mState = new_state;

        for(PlayerListener listener : mPlayerListenerList) {
            listener.onPlayerStateChanged(ServiceType.Deezer, old_state, new_state);
        }
    }

    private void processEvent(Event event) {
        for(PlayerListener listener : mPlayerListenerList) {
            listener.onPlayerEvent(event);
        }
    }

    private void notifyLoginSuccess() {
        for(PlayerListener listener : mPlayerListenerList) {
            listener.onLoginSuccess(ServiceType.Deezer);
        }
    }

    private void notifyLoginError() {
        for(PlayerListener listener : mPlayerListenerList) {
            listener.onLoginError(ServiceType.Deezer);
        }
    }

    private void loadUserPlaylists() {
        try {
            mDeezerPlayer = new DefaultPlayerFactory(mActivity.getApplication(), mDeezerConnect, new WifiAndMobileNetworkStateChecker()).createPlayer();
            mDeezerPlayer.addOnPlayerErrorListener(this);
            mDeezerPlayer.addOnPlayerProgressListener(this);
            mDeezerPlayer.addOnPlayerStateChangeListener(this);
        } catch (Exception e) {
            e.printStackTrace();
        }

        mDeezerService = new DeezerService();
        mDeezerCategoryPlaylists = new DeezerCategory(mActivity.getString(R.string.category_playlists));
        mDeezerCategoryAlbums = new DeezerCategory(mActivity.getString(R.string.category_albums));
        mDeezerCategoryOwnCharts = new DeezerCategory(mActivity.getString(R.string.category_own_charts));

        mDeezerService.addCategory(mDeezerCategoryPlaylists);
        mDeezerService.addCategory(mDeezerCategoryAlbums);
        mDeezerService.addCategory(mDeezerCategoryOwnCharts);

        mMusicLibrary.addMusicLibrary(mDeezerService);

        AlbumsListener al = new AlbumsListener();
        DeezerRequest ar = DeezerRequestFactory.requestCurrentUserAlbums();
        ar.setId("ar");
        mDeezerConnect.requestAsync(ar, al);

        ChartsListener cl = new ChartsListener();
        DeezerRequest cr = DeezerRequestFactory.requestCurrentUserCharts();
        cr.setId("cr");
        mDeezerConnect.requestAsync(cr, cl);

        PlaylistsListener pl = new PlaylistsListener();
        DeezerRequest pr = DeezerRequestFactory.requestCurrentUserPlaylists();
        pr.setId("pr");
        mDeezerConnect.requestAsync(pr, pl);

        RadiosListener rl = new RadiosListener();
        DeezerRequest rr = DeezerRequestFactory.requestCurrentUserRadios();
        rr.setId("rr");
        mDeezerConnect.requestAsync(rr, rl);
    }

    private class AlbumListener extends JsonRequestListener {
        @Override
        public void onResult(Object result, Object requestId) {
            Album album = (Album) result;

            if(album != null) {
                DeezerPlaylist playlist = new DeezerPlaylist("");
                playlist.addSongs(album);
                mDeezerCategoryAlbums.addPlaylist(playlist);
            }
        }

        @Override
        public void onUnparsedResult(String s, Object o) {

        }

        @Override
        public void onException(Exception e, Object o) {

        }
    }

    private class AlbumsListener extends JsonRequestListener {
        @Override
        public void onResult(Object result, Object requestId) {
            List<Album> albums = (List<Album>) result;

            for(Album album : albums) {
                AlbumListener al = new AlbumListener();
                DeezerRequest ar = DeezerRequestFactory.requestAlbum(album.getId());
                ar.setId("al");
                mDeezerConnect.requestAsync(ar, al);
            }
        }

        @Override
        public void onUnparsedResult(String s, Object o) {

        }

        @Override
        public void onException(Exception e, Object o) {

        }
    }

    private class ChartsListener extends JsonRequestListener {
        @Override
        public void onResult(Object result, Object requestId) {
            List<Track> list = (List<Track>) result;

            DeezerPlaylist playlist = new DeezerPlaylist(mActivity.getString(R.string.category_own_charts));
            playlist.addSongs(list);
            mDeezerCategoryOwnCharts.addPlaylist(playlist);
        }

        @Override
        public void onUnparsedResult(String s, Object o) {

        }

        @Override
        public void onException(Exception e, Object o) {

        }
    }

    private class PlaylistListener extends JsonRequestListener {
        @Override
        public void onResult(Object result, Object requestId) {
            Playlist p = (Playlist) result;
            if(p != null) {
                DeezerPlaylist playlist = new DeezerPlaylist("");
                playlist.addSongs(p);
                mDeezerCategoryPlaylists.addPlaylist(playlist);
            }
        }

        @Override
        public void onUnparsedResult(String s, Object o) {

        }

        @Override
        public void onException(Exception e, Object o) {

        }
    }

    private class PlaylistsListener extends JsonRequestListener {
        @Override
        public void onResult(Object result, Object requestId) {
            List<Playlist> list = (List<Playlist>) result;
            for(Playlist p : list) {
                PlaylistListener pl = new PlaylistListener();
                DeezerRequest pr = DeezerRequestFactory.requestPlaylist(p.getId());
                pr.setId("pl");
                mDeezerConnect.requestAsync(pr, pl);
            }
        }

        @Override
        public void onUnparsedResult(String s, Object o) {

        }

        @Override
        public void onException(Exception e, Object o) {

        }
    }

    private class RadiosListener extends JsonRequestListener {
        @Override
        public void onResult(Object result, Object requestId) {
            List<Radio> list = (List<Radio>) result;

            for(Radio r : list) {
                r.getTitle();
            }
        }

        @Override
        public void onUnparsedResult(String s, Object o) {

        }

        @Override
        public void onException(Exception e, Object o) {

        }
    }
}