package eu.applabs.allplaylibrary.player;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.session.MediaSession;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import eu.applabs.allplaylibrary.data.SettingsManager;
import eu.applabs.allplaylibrary.data.Song;
import eu.applabs.allplaylibrary.services.deezer.DeezerPlayer;
import eu.applabs.allplaylibrary.services.spotify.SpotifyPlayer;

public class Player implements PlayerListener, Playlist.OnPlaylistUpdateListener, AudioManager.OnAudioFocusChangeListener {

    private static final String CLASSNAME = Player.class.getSimpleName();

    private static Player m_Instance = null;
    private static boolean m_Initialized = false;

    private Activity m_Activity = null;
    private SettingsManager m_SettingsManager = null;

    private List<ServicePlayer> mM_ServicePlayerList = null;
    private ServicePlayer mM_ActiveServicePlayer = null;
    private boolean m_MediaSessionCompatInitialized = false;
    private MediaSessionCompat m_MediaSessionCompat = null;

    private List<PlayerListener> m_IPlayerListenerList = null;

    Playlist m_Playlist = null;

    // Private (Singelton)
    private Player() {}

    public static synchronized Player getInstance() {
        if(Player.m_Instance == null) {
            Player.m_Instance = new Player();
        }

        return Player.m_Instance;
    }

    public void initialize(Activity activity) {
        if(!Player.m_Initialized) {
            Player.m_Initialized = true;

            m_Activity = activity;
            mM_ServicePlayerList = new ArrayList<>();
            m_IPlayerListenerList = new ArrayList<>();

            m_Playlist = new Playlist(m_Activity);
            m_Playlist.registerListener(this);

            m_SettingsManager = SettingsManager.getInstance();
            m_SettingsManager.initialize(m_Activity);

            for(String s : m_SettingsManager.getConnectedServices()) {
                int service = Integer.valueOf(s);
                login(ServicePlayer.ServiceType.values()[service]);
            }
        }
    }

    public void clearPlayer() {
        Player.m_Initialized = false;
        m_Activity = null;

        // Clear all initialized player
        if(mM_ServicePlayerList != null) {
            for (ServicePlayer player : mM_ServicePlayerList) {
                player.clearPlayer();
            }
        }

        mM_ServicePlayerList.clear();
        mM_ServicePlayerList = null;

        m_Playlist.unregisterListener(this);
        m_Playlist.clear();
        m_Playlist = null;

        m_IPlayerListenerList.clear();
        m_IPlayerListenerList = null;

        if(m_MediaSessionCompat != null && m_MediaSessionCompat.isActive()) {
            m_MediaSessionCompat.setActive(false);
            m_MediaSessionCompat = null;
        }
    }

    public boolean login(ServicePlayer.ServiceType type) {
        return login(type, m_Activity);
    }

    public boolean login(ServicePlayer.ServiceType type, Activity activity) {
        ServicePlayer player = null;

        switch(type) {
            case Spotify:
                player = new SpotifyPlayer();
                break;
            case GoogleMusic:
                return false;
            case Deezer:
                player = new DeezerPlayer();
                break;
        }

        if(player != null) {
            player.registerListener(this);
            player.initialize(activity);
            player.login();
            mM_ServicePlayerList.add(player);
        }

        return true;
    }

    public boolean logout(ServicePlayer.ServiceType type) {
        ServicePlayer player = null;

        for(ServicePlayer iplayer : mM_ServicePlayerList) {
            if(iplayer.getServiceType() == type) {
                player = iplayer;
                break;
            }
        }

        if(player != null) {
            Set<String> connectedServices = m_SettingsManager.getConnectedServices();
            connectedServices.remove(String.valueOf(player.getServiceType().getValue()));
            m_SettingsManager.setConnectedServices(connectedServices);

            player.unregisterListener(this);
            player.logout();
            onLogoutSuccess(player.getServiceType());
            player.clearPlayer();
            mM_ServicePlayerList.remove(player);
        }

        return true;
    }

    public void registerListener(PlayerListener listener) {
        m_IPlayerListenerList.add(listener);
    }

    public void unregisterListener(PlayerListener listener) {
        m_IPlayerListenerList.remove(listener);
    }

    public boolean checkActivityResult(int requestCode, int resultCode, Intent intent)
    {
        for(ServicePlayer player : mM_ServicePlayerList) {
            if(player.checkActivityResult(requestCode, resultCode, intent)) {
                return true;
            }
        }

        return false;
    }

    public Playlist getPlaylist() {
        return m_Playlist;
    }

    public ServicePlayer.State getPlayerState() {
        if(mM_ActiveServicePlayer != null) {
            return mM_ActiveServicePlayer.getPlayerState();
        }

        return ServicePlayer.State.Idle;
    }

    public void play() {
        Song song = m_Playlist.getCurrentSong();

        if(song != null) {
            for(ServicePlayer player : mM_ServicePlayerList) {
                if(player.play(song)) {
                    // Save the active player
                    if(!m_MediaSessionCompatInitialized) {
                        initializeMediaSession();
                    }

                    mM_ActiveServicePlayer = player;
                    break;
                }
            }
        }
    }

    public void resume() {
        Song song = m_Playlist.getCurrentSong();

        if(mM_ActiveServicePlayer != null && song != null) {
            mM_ActiveServicePlayer.resume(song);
        }
    }

    public void pause() {
        Song song = m_Playlist.getCurrentSong();

        if(mM_ActiveServicePlayer != null && song != null) {
            mM_ActiveServicePlayer.pause(song);
        }
    }

    public void stop() {
        Song song = m_Playlist.getCurrentSong();

        if(mM_ActiveServicePlayer != null && song != null) {
            mM_ActiveServicePlayer.stop(song);
        }
    }

    public void next() {
        Song song = m_Playlist.getNextSong();

        if(song != null) {
            for(ServicePlayer player : mM_ServicePlayerList) {
                if(player.play(song)) {
                    // Save the active player
                    if(!m_MediaSessionCompatInitialized) {
                        initializeMediaSession();
                    }

                    mM_ActiveServicePlayer = player;
                    break;
                }
            }
        }
    }

    public void prev() {
        Song song = m_Playlist.getPrevSong();

        if(song != null) {
            for(ServicePlayer player : mM_ServicePlayerList) {
                if(player.play(song)) {
                    // Save the active player
                    if(!m_MediaSessionCompatInitialized) {
                        initializeMediaSession();
                    }

                    mM_ActiveServicePlayer = player;
                    break;
                }
            }
        }
    }

    @Override
    public void onPlayerStateChanged(ServicePlayer.ServiceType type, ServicePlayer.State old_state, ServicePlayer.State new_state) {
        // Just inform the client if we got an update from the current player
        if(mM_ActiveServicePlayer != null && mM_ActiveServicePlayer.getServiceType() == type) {
            for(PlayerListener listener : m_IPlayerListenerList) {
                listener.onPlayerStateChanged(type, old_state, new_state);
            }
        }
    }

    @Override
    public void onPlayerEvent(ServicePlayer.Event event) {
        if(event == ServicePlayer.Event.TrackEnd) {
            next(); // Play the next song
        } else if(event == ServicePlayer.Event.Error) {
            m_Playlist.remove(m_Playlist.getCurrentSong());
            next();
        }
    }

    @Override
    public void onLoginSuccess(ServicePlayer.ServiceType type) {
        Set<String> connectedServices = m_SettingsManager.getConnectedServices();
        connectedServices.add(String.valueOf(type.getValue()));

        m_SettingsManager.setConnectedServices(connectedServices);

        for(PlayerListener listener : m_IPlayerListenerList) {
            listener.onLoginSuccess(type);
        }
    }

    @Override
    public void onLoginError(ServicePlayer.ServiceType type) {
        Set<String> connectedServices = m_SettingsManager.getConnectedServices();
        connectedServices.remove(String.valueOf(type.getValue()));

        m_SettingsManager.setConnectedServices(connectedServices);

        for(PlayerListener listener : m_IPlayerListenerList) {
            listener.onLoginError(type);
        }
    }

    @Override
    public void onLogoutSuccess(ServicePlayer.ServiceType type) {
        for(PlayerListener listener : m_IPlayerListenerList) {
            listener.onLogoutSuccess(type);
        }
    }

    @Override
    public void onLogoutError(ServicePlayer.ServiceType type) {
        for(PlayerListener listener : m_IPlayerListenerList) {
            listener.onLogoutError(type);
        }
    }

    @Override
    public void onPlayerPlaybackPositionChanged(int position) {
        if(m_IPlayerListenerList != null) {
            for (PlayerListener listener : m_IPlayerListenerList) {
                if (listener != null) {
                    listener.onPlayerPlaybackPositionChanged(position);
                }
            }
        }
    }

    private void initializeMediaSession() {
        m_MediaSessionCompatInitialized = true;

        ComponentName cn = new ComponentName("eu.applabs.allplay", "eu.applabs.allplay.player.Player");
        Intent intent = new Intent(m_Activity, m_Activity.getClass());
        PendingIntent pi = PendingIntent.getActivity(m_Activity, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        m_MediaSessionCompat = new MediaSessionCompat(m_Activity, CLASSNAME, cn, pi);
        m_MediaSessionCompat.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

        AudioManager am = (AudioManager) m_Activity.getSystemService(Context.AUDIO_SERVICE);

        // Request audio focus for playback
        int result = am.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            if(m_MediaSessionCompat != null) {
                PlaybackStateCompat.Builder stateBuilder = new PlaybackStateCompat.Builder().setActions(getAvailableActions());
                stateBuilder.setState(0, 100, 1.0f);

                m_MediaSessionCompat.setPlaybackState(stateBuilder.build());
                new NowPlayingCardUpdater(m_Playlist.getCurrentSong()).start();
                m_MediaSessionCompat.setActive(true);
            }
        }
    }

    private long getAvailableActions() {
        return PlaybackStateCompat.ACTION_PLAY |
                PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID |
                PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH;
    }

    @Override
    public void onPlaylistUpdate() {
        new NowPlayingCardUpdater(m_Playlist.getCurrentSong()).start();
    }

    @Override
    public void onAudioFocusChange(int focusChange) {

    }

    private class NowPlayingCardUpdater extends Thread {

        private Song m_Song = null;

        public NowPlayingCardUpdater(Song song) {
            m_Song = song;
        }

        @Override
        public void run() {
            super.run();

            if(m_Song != null) {
                MediaMetadataCompat.Builder metadataBuilder = new MediaMetadataCompat.Builder();
                metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_TITLE, m_Song.getTitle());
                metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_ARTIST, m_Song.getArtist());

                try {
                    Bitmap bitmap = Glide.with(m_Activity).load(m_Song.getCoverBig()).asBitmap().into(800, 800).get();
                    metadataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_ART, bitmap);
                } catch (Exception e) {
                    Log.e(CLASSNAME, "Error during cover load");
                }

                if(m_MediaSessionCompat != null) {
                    m_MediaSessionCompat.setMetadata(metadataBuilder.build());
                }
            }
        }
    }
}
