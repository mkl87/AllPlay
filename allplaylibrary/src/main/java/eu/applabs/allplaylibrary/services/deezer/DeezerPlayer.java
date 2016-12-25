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

    private State m_State = null;
    private DeezerService mM_DeezerService = null;
    private DeezerCategory m_DeezerCategoryPlaylists = null;
    private DeezerCategory m_DeezerCategoryAlbums = null;
    private DeezerCategory m_DeezerCategoryOwnCharts = null;
    private MusicLibrary m_MusicLibrary = null;
    private Player m_Player = null;
    private boolean m_TrackEndBroadcastEnabled = true;

    private Activity m_Activity = null;
    private DeezerConnect m_DeezerConnect = null;
    private SessionStore m_SessionStore = null;
    private List<PlayerListener> m_IPlayerListenerList = null;
    private com.deezer.sdk.player.Player m_DeezerPlayer = null;

    public DeezerPlayer() {
        m_IPlayerListenerList = new ArrayList<>();
        m_State = State.Idle;
    }

    @Override
    public void initialize(Activity activity) {
        m_Activity = activity;
        m_DeezerConnect = new DeezerConnect(m_Activity.getApplication(), m_Activity.getString(R.string.deezer_application_id));
        m_SessionStore = new SessionStore();

        m_MusicLibrary = MusicLibrary.getInstance();

        m_Player = Player.getInstance();
        m_Player.initialize(m_Activity);
    }

    @Override
    public void clearPlayer() {
        mM_DeezerService.clearLibrary();
        m_MusicLibrary.removeMusicLibrary(mM_DeezerService);
        m_DeezerPlayer.release();
    }

    @Override
    public void login() {
        // Check if a session was stored and request login if not
        if(!m_SessionStore.restore(m_DeezerConnect, m_Activity.getApplication())) {
            String[] permissions = new String[]{
                    Permissions.BASIC_ACCESS,
                    Permissions.MANAGE_LIBRARY,
                    Permissions.LISTENING_HISTORY,
                    Permissions.OFFLINE_ACCESS,
            };

            DeezerDialogListener ddl = new DeezerDialogListener();
            m_DeezerConnect.authorize(m_Activity, permissions, ddl);
        } else {
            notifyLoginSuccess();
            loadUserPlaylists();
        }
    }

    @Override
    public void logout() {
        m_DeezerConnect.logout(m_Activity.getApplication());
        m_SessionStore.clear(m_Activity.getApplication());
    }

    @Override
    public boolean checkActivityResult(int requestCode, int resultCode, Intent intent) {
        return false;
    }

    @Override
    public State getPlayerState() {
        return m_State;
    }

    @Override
    public ServiceType getServiceType() {
        return ServiceType.Deezer;
    }

    @Override
    public boolean play(Song song) {
        if(song != null && song.getServiceType() == ServiceType.Deezer) {
            if(m_DeezerPlayer != null) {
                if(m_State == State.Playing || m_State == State.Paused) {
                    m_DeezerPlayer.stop();
                    m_TrackEndBroadcastEnabled = false;
                }

                m_DeezerPlayer.init(Long.valueOf(song.getId()), song.getUri(), 0, 0);
            }

            return true;
        }

        return false;
    }

    @Override
    public boolean pause(Song song) {
        if(song != null && song.getServiceType() == ServiceType.Deezer) {

            if(m_DeezerPlayer != null) {
                m_DeezerPlayer.pause();
                changeState(State.Paused);
            }

            return true;
        }

        return false;
    }

    @Override
    public boolean resume(Song song) {
        if(song != null && song.getServiceType() == ServiceType.Deezer) {

            if(m_DeezerPlayer != null) {
                m_DeezerPlayer.play();
                changeState(State.Playing);
            }

            return true;
        }

        return false;
    }

    @Override
    public boolean stop(Song song) {
        if(song != null && song.getServiceType() == ServiceType.Deezer) {

            if(m_DeezerPlayer != null) {
                m_DeezerPlayer.stop();
                changeState(State.Idle);
            }

            return true;
        }

        return false;
    }

    @Override
    public void registerListener(PlayerListener listener) {
        m_IPlayerListenerList.add(listener);
    }

    @Override
    public void unregisterListener(PlayerListener listener) {
        m_IPlayerListenerList.remove(listener);
    }

    @Override
    public void onPlayerError(Exception e, long l) {
        Log.d("test", e.getMessage());
        e.printStackTrace();
        processEvent(Event.Error);
    }

    @Override
    public void onPlayerProgress(long l) {
        Song song = m_Player.getPlaylist().getCurrentSong();

        if(song != null && song.getDuration() != 0) {
            int percent = (int) (((l/1000) * 100) /  song.getDuration());

            for(PlayerListener listener : m_IPlayerListenerList) {
                listener.onPlayerPlaybackPositionChanged(percent);
            }
        }
    }

    @Override
    public void onPlayerStateChange(PlayerState playerState, long l) {
        if(playerState == PlayerState.READY) {
            m_DeezerPlayer.play();
        } else if(playerState == PlayerState.PLAYING) {
            changeState(State.Playing);
            m_TrackEndBroadcastEnabled = true;
        } else if(playerState == PlayerState.PLAYBACK_COMPLETED && m_TrackEndBroadcastEnabled) {
            m_DeezerPlayer.stop();
            changeState(State.Idle);
            processEvent(Event.TrackEnd);
        }
    }

    private class DeezerDialogListener implements DialogListener {
        @Override
        public void onComplete(Bundle bundle) {
            notifyLoginSuccess();

            m_SessionStore.save(m_DeezerConnect, m_Activity.getApplication());
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
        State old_state = m_State;
        m_State = new_state;

        for(PlayerListener listener : m_IPlayerListenerList) {
            listener.onPlayerStateChanged(ServiceType.Deezer, old_state, new_state);
        }
    }

    private void processEvent(Event event) {
        for(PlayerListener listener : m_IPlayerListenerList) {
            listener.onPlayerEvent(event);
        }
    }

    private void notifyLoginSuccess() {
        for(PlayerListener listener : m_IPlayerListenerList) {
            listener.onLoginSuccess(ServiceType.Deezer);
        }
    }

    private void notifyLoginError() {
        for(PlayerListener listener : m_IPlayerListenerList) {
            listener.onLoginError(ServiceType.Deezer);
        }
    }

    private void loadUserPlaylists() {
        try {
            m_DeezerPlayer = new DefaultPlayerFactory(m_Activity.getApplication(), m_DeezerConnect, new WifiAndMobileNetworkStateChecker()).createPlayer();
            m_DeezerPlayer.addOnPlayerErrorListener(this);
            m_DeezerPlayer.addOnPlayerProgressListener(this);
            m_DeezerPlayer.addOnPlayerStateChangeListener(this);
        } catch (Exception e) {
            e.printStackTrace();
        }

        mM_DeezerService = new DeezerService();
        m_DeezerCategoryPlaylists = new DeezerCategory(m_Activity.getString(R.string.category_playlists));
        m_DeezerCategoryAlbums = new DeezerCategory(m_Activity.getString(R.string.category_albums));
        m_DeezerCategoryOwnCharts = new DeezerCategory(m_Activity.getString(R.string.category_own_charts));

        mM_DeezerService.addCategory(m_DeezerCategoryPlaylists);
        mM_DeezerService.addCategory(m_DeezerCategoryAlbums);
        mM_DeezerService.addCategory(m_DeezerCategoryOwnCharts);

        m_MusicLibrary.addMusicLibrary(mM_DeezerService);

        AlbumsListener al = new AlbumsListener();
        DeezerRequest ar = DeezerRequestFactory.requestCurrentUserAlbums();
        ar.setId("ar");
        m_DeezerConnect.requestAsync(ar, al);

        ChartsListener cl = new ChartsListener();
        DeezerRequest cr = DeezerRequestFactory.requestCurrentUserCharts();
        cr.setId("cr");
        m_DeezerConnect.requestAsync(cr, cl);

        PlaylistsListener pl = new PlaylistsListener();
        DeezerRequest pr = DeezerRequestFactory.requestCurrentUserPlaylists();
        pr.setId("pr");
        m_DeezerConnect.requestAsync(pr, pl);

        RadiosListener rl = new RadiosListener();
        DeezerRequest rr = DeezerRequestFactory.requestCurrentUserRadios();
        rr.setId("rr");
        m_DeezerConnect.requestAsync(rr, rl);
    }

    private class AlbumListener extends JsonRequestListener {
        @Override
        public void onResult(Object result, Object requestId) {
            Album album = (Album) result;

            if(album != null) {
                DeezerPlaylist playlist = new DeezerPlaylist("");
                playlist.addSongs(album);
                m_DeezerCategoryAlbums.addPlaylist(playlist);
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
                m_DeezerConnect.requestAsync(ar, al);
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

            DeezerPlaylist playlist = new DeezerPlaylist(m_Activity.getString(R.string.category_own_charts));
            playlist.addSongs(list);
            m_DeezerCategoryOwnCharts.addPlaylist(playlist);
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
                m_DeezerCategoryPlaylists.addPlaylist(playlist);
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
                m_DeezerConnect.requestAsync(pr, pl);
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