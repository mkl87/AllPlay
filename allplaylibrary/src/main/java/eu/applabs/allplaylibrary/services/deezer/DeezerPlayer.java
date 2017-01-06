package eu.applabs.allplaylibrary.services.deezer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

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
import com.deezer.sdk.player.networkcheck.WifiAndMobileNetworkStateChecker;

import java.util.List;
import java.util.Observer;

import javax.inject.Inject;

import eu.applabs.allplaylibrary.AllPlayLibrary;
import eu.applabs.allplaylibrary.R;
import eu.applabs.allplaylibrary.MusicCatalog;
import eu.applabs.allplaylibrary.event.PlayerEvent;
import eu.applabs.allplaylibrary.event.ServiceConnectionEvent;
import eu.applabs.allplaylibrary.services.ServiceCategory;
import eu.applabs.allplaylibrary.data.Song;
import eu.applabs.allplaylibrary.services.ServicePlayer;
import eu.applabs.allplaylibrary.Player;
import eu.applabs.allplaylibrary.services.ServiceType;

public class DeezerPlayer extends ServicePlayer implements OnPlayerErrorListener, OnPlayerProgressListener, OnPlayerStateChangeListener {

    private ServicePlayer.PlayerState mPlayerState;
    private DeezerService mDeezerService = new DeezerService();
    private ServiceCategory mDeezerCategoryPlaylists;
    private ServiceCategory mDeezerCategoryAlbums;
    private ServiceCategory mDeezerCategoryOwnCharts;
    private boolean mTrackEndBroadcastEnabled = true;
    private DeezerConnect mDeezerConnect;
    private SessionStore mSessionStore = new SessionStore();
    private com.deezer.sdk.player.Player mDeezerPlayer;

    @Inject
    protected MusicCatalog mMusicCatalog;

    @Inject
    protected Player mPlayer;

    @Inject
    protected Activity mActivity;

    public DeezerPlayer() {
        AllPlayLibrary.getInstance().component().inject(this);

        mPlayerState = PlayerState.IDLE;
        mDeezerConnect = new DeezerConnect(mActivity.getApplication(), mActivity.getString(R.string.deezer_application_id));
    }

    @Override
    public void clearPlayer() {
        mDeezerService.clearLibrary();
        mMusicCatalog.removeServiceLibrary(mDeezerService);
        mDeezerPlayer.release();
    }

    @Override
    public void login(Activity activity) {
        // Check if a session was stored and request connectServiceType if not
        if(!mSessionStore.restore(mDeezerConnect, mActivity.getApplication())) {
            String[] permissions = new String[]{
                    Permissions.BASIC_ACCESS,
                    Permissions.MANAGE_LIBRARY,
                    Permissions.LISTENING_HISTORY,
                    Permissions.OFFLINE_ACCESS,
            };

            DeezerDialogListener ddl = new DeezerDialogListener();
            mDeezerConnect.authorize(activity, permissions, ddl);
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
    public PlayerState getPlayerState() {
        return mPlayerState;
    }

    @Override
    public ServiceType getServiceType() {
        return ServiceType.DEEZER;
    }

    @Override
    public boolean play(Song song) {
        if(song != null && song.getServiceType() == ServiceType.DEEZER) {
            if(mDeezerPlayer != null) {
                if(mPlayerState == PlayerState.PLAYING || mPlayerState == PlayerState.PAUSED) {
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
        if(song != null && song.getServiceType() == ServiceType.DEEZER) {

            if(mDeezerPlayer != null) {
                mDeezerPlayer.pause();
                changeState(PlayerState.PAUSED);
            }

            return true;
        }

        return false;
    }

    @Override
    public boolean resume(Song song) {
        if(song != null && song.getServiceType() == ServiceType.DEEZER) {

            if(mDeezerPlayer != null) {
                mDeezerPlayer.play();
                changeState(PlayerState.PLAYING);
            }

            return true;
        }

        return false;
    }

    @Override
    public boolean stop(Song song) {
        if(song != null && song.getServiceType() == ServiceType.DEEZER) {

            if(mDeezerPlayer != null) {
                mDeezerPlayer.stop();
                changeState(PlayerState.IDLE);
            }

            return true;
        }

        return false;
    }

    @Override
    public void onPlayerError(Exception e, long l) {
        PlayerEvent playerEvent = new PlayerEvent(PlayerEvent.PlayerEventType.ERROR, ServiceType.DEEZER);
        processPlayerEvent(playerEvent);
    }

    @Override
    public void onPlayerProgress(long l) {
        Song song = mPlayer.getPlaylist().getCurrentSong();

        if(song != null && song.getDuration() != 0) {
            int percent = (int) (((l/1000) * 100) /  song.getDuration());

            PlayerEvent playerEvent = new PlayerEvent(PlayerEvent.PlayerEventType.PLAYBACK_POSITION_CHANGED, ServiceType.DEEZER);
            playerEvent.setPlaybackPosition(percent);

            for(Observer observer : mObserverList) {
                observer.update(this, playerEvent);
            }
        }
    }

    @Override
    public void onPlayerStateChange(com.deezer.sdk.player.event.PlayerState playerState, long l) {
        if(playerState == com.deezer.sdk.player.event.PlayerState.READY) {
            mDeezerPlayer.play();
        } else if(playerState == com.deezer.sdk.player.event.PlayerState.PLAYING) {
            changeState(PlayerState.PLAYING);
            mTrackEndBroadcastEnabled = true;
        } else if(playerState == com.deezer.sdk.player.event.PlayerState.PLAYBACK_COMPLETED && mTrackEndBroadcastEnabled) {
            mDeezerPlayer.stop();
            changeState(PlayerState.IDLE);

            PlayerEvent playerEvent = new PlayerEvent(PlayerEvent.PlayerEventType.TRACK_END, ServiceType.DEEZER);
            processPlayerEvent(playerEvent);
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

    private void changeState(PlayerState new_state) {
        PlayerState old_state = mPlayerState;
        mPlayerState = new_state;

        PlayerEvent playerEvent = new PlayerEvent(PlayerEvent.PlayerEventType.STATE_CHANGED, ServiceType.DEEZER);
        playerEvent.setOldPlayerState(old_state);
        playerEvent.setNewPlayerState(mPlayerState);

        for(Observer observer : mObserverList) {
            observer.update(this, playerEvent);
        }
    }

    private void processPlayerEvent(PlayerEvent playerEvent) {
        for(Observer observer : mObserverList) {
            observer.update(this, playerEvent);
        }
    }

    private void processServiceConnectionEvent(ServiceConnectionEvent serviceConnectionEvent) {
        for(Observer observer : mObserverList) {
            observer.update(this, serviceConnectionEvent);
        }
    }

    private void notifyLoginSuccess() {
        ServiceConnectionEvent serviceConnectionEvent =
                new ServiceConnectionEvent(ServiceConnectionEvent.ServiceConnectionEventType.CONNECTED, ServiceType.DEEZER);
        processServiceConnectionEvent(serviceConnectionEvent);
    }

    private void notifyLoginError() {
        ServiceConnectionEvent serviceConnectionEvent =
                new ServiceConnectionEvent(ServiceConnectionEvent.ServiceConnectionEventType.ERROR, ServiceType.DEEZER);
        processServiceConnectionEvent(serviceConnectionEvent);
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

        mDeezerCategoryPlaylists = new ServiceCategory(mActivity.getString(R.string.category_playlists), ServiceType.DEEZER);
        mDeezerCategoryAlbums = new ServiceCategory(mActivity.getString(R.string.category_albums), ServiceType.DEEZER);
        mDeezerCategoryOwnCharts = new ServiceCategory(mActivity.getString(R.string.category_own_charts), ServiceType.DEEZER);

        mDeezerService.addCategory(mDeezerCategoryPlaylists);
        mDeezerService.addCategory(mDeezerCategoryAlbums);
        mDeezerService.addCategory(mDeezerCategoryOwnCharts);

        mMusicCatalog.addServiceLibrary(mDeezerService);

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