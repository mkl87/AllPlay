package eu.applabs.allplaylibrary.player;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import eu.applabs.allplaylibrary.data.MusicLibrary;
import eu.applabs.allplaylibrary.data.SettingsManager;
import eu.applabs.allplaylibrary.data.Song;
import eu.applabs.allplaylibrary.services.ServiceType;
import eu.applabs.allplaylibrary.services.deezer.DeezerPlayer;
import eu.applabs.allplaylibrary.services.spotify.SpotifyPlayer;

public class Player implements PlayerListener, NowPlayingPlaylist.OnPlaylistUpdateListener, AudioManager.OnAudioFocusChangeListener {

    private static final String TAG = Player.class.getSimpleName();

    private Activity mActivity;
    private SettingsManager mSettingsManager;

    private List<ServicePlayer> mServicePlayerList;
    private ServicePlayer mActiveServicePlayer;
    private boolean mMediaSessionCompatInitialized = false;
    private MediaSessionCompat mMediaSessionCompat;

    private List<PlayerListener> mPlayerListenerList;

    private NowPlayingPlaylist mNowPlayingPlaylist;

    public Player(Activity activity, MusicLibrary musicLibrary) {
        mActivity = activity;
        mServicePlayerList = new ArrayList<>();
        mPlayerListenerList = new ArrayList<>();

        mNowPlayingPlaylist = new NowPlayingPlaylist(mActivity, musicLibrary);
        mNowPlayingPlaylist.registerListener(this);

        mSettingsManager = SettingsManager.getInstance();
        mSettingsManager.initialize(mActivity);

        for(ServiceType serviceType : mSettingsManager.getConnectedServices()) {
            login(serviceType);
        }
    }

    public void clearPlayer() {
        mActivity = null;

        // Clear all initialized player
        if(mServicePlayerList != null) {
            for (ServicePlayer player : mServicePlayerList) {
                player.clearPlayer();
            }
        }

        mServicePlayerList.clear();
        mServicePlayerList = null;

        mNowPlayingPlaylist.unregisterListener(this);
        mNowPlayingPlaylist.clear();
        mNowPlayingPlaylist = null;

        mPlayerListenerList.clear();
        mPlayerListenerList = null;

        if(mMediaSessionCompat != null && mMediaSessionCompat.isActive()) {
            mMediaSessionCompat.setActive(false);
            mMediaSessionCompat = null;
        }
    }

    public boolean login(ServiceType type) {
        ServicePlayer player = null;

        switch(type) {
            case SPOTIFY:
                player = new SpotifyPlayer();
                break;
            case GOOGLE_MUSIC:
                return false;
            case DEEZER:
                player = new DeezerPlayer();
                break;
        }

        if(player != null) {
            player.registerListener(this);
            player.login();
            mServicePlayerList.add(player);
        }

        return true;
    }

    public boolean logout(ServiceType type) {
        ServicePlayer player = null;

        for(ServicePlayer iplayer : mServicePlayerList) {
            if(iplayer.getServiceType() == type) {
                player = iplayer;
                break;
            }
        }

        if(player != null) {
            List<ServiceType> connectedServices = mSettingsManager.getConnectedServices();
            connectedServices.remove(player.getServiceType());
            mSettingsManager.setConnectedServices(connectedServices);

            player.unregisterListener(this);
            player.logout();
            onLogoutSuccess(player.getServiceType());
            player.clearPlayer();
            mServicePlayerList.remove(player);
        }

        return true;
    }

    public void registerListener(PlayerListener listener) {
        mPlayerListenerList.add(listener);
    }

    public void unregisterListener(PlayerListener listener) {
        mPlayerListenerList.remove(listener);
    }

    public boolean checkActivityResult(int requestCode, int resultCode, Intent intent)
    {
        for(ServicePlayer player : mServicePlayerList) {
            if(player.checkActivityResult(requestCode, resultCode, intent)) {
                return true;
            }
        }

        return false;
    }

    public NowPlayingPlaylist getPlaylist() {
        return mNowPlayingPlaylist;
    }

    public ServicePlayer.State getPlayerState() {
        if(mActiveServicePlayer != null) {
            return mActiveServicePlayer.getPlayerState();
        }

        return ServicePlayer.State.Idle;
    }

    public void play() {
        Song song = mNowPlayingPlaylist.getCurrentSong();

        if(song != null) {
            for(ServicePlayer player : mServicePlayerList) {
                if(player.play(song)) {
                    // Save the active player
                    if(!mMediaSessionCompatInitialized) {
                        initializeMediaSession();
                    }

                    mActiveServicePlayer = player;
                    break;
                }
            }
        }
    }

    public void resume() {
        Song song = mNowPlayingPlaylist.getCurrentSong();

        if(mActiveServicePlayer != null && song != null) {
            mActiveServicePlayer.resume(song);
        }
    }

    public void pause() {
        Song song = mNowPlayingPlaylist.getCurrentSong();

        if(mActiveServicePlayer != null && song != null) {
            mActiveServicePlayer.pause(song);
        }
    }

    public void stop() {
        Song song = mNowPlayingPlaylist.getCurrentSong();

        if(mActiveServicePlayer != null && song != null) {
            mActiveServicePlayer.stop(song);
        }
    }

    public void next() {
        Song song = mNowPlayingPlaylist.getNextSong();

        if(song != null) {
            for(ServicePlayer player : mServicePlayerList) {
                if(player.play(song)) {
                    // Save the active player
                    if(!mMediaSessionCompatInitialized) {
                        initializeMediaSession();
                    }

                    mActiveServicePlayer = player;
                    break;
                }
            }
        }
    }

    public void prev() {
        Song song = mNowPlayingPlaylist.getPrevSong();

        if(song != null) {
            for(ServicePlayer player : mServicePlayerList) {
                if(player.play(song)) {
                    // Save the active player
                    if(!mMediaSessionCompatInitialized) {
                        initializeMediaSession();
                    }

                    mActiveServicePlayer = player;
                    break;
                }
            }
        }
    }

    @Override
    public void onPlayerStateChanged(ServiceType type, ServicePlayer.State old_state, ServicePlayer.State new_state) {
        // Just inform the client if we got an update from the current player
        if(mActiveServicePlayer != null && mActiveServicePlayer.getServiceType() == type) {
            for(PlayerListener listener : mPlayerListenerList) {
                listener.onPlayerStateChanged(type, old_state, new_state);
            }
        }
    }

    @Override
    public void onPlayerEvent(ServicePlayer.Event event) {
        if(event == ServicePlayer.Event.TrackEnd) {
            next(); // Play the next song
        } else if(event == ServicePlayer.Event.Error) {
            mNowPlayingPlaylist.remove(mNowPlayingPlaylist.getCurrentSong());
            next();
        }
    }

    @Override
    public void onLoginSuccess(ServiceType type) {
        List<ServiceType> connectedServices = mSettingsManager.getConnectedServices();
        connectedServices.add(type);
        mSettingsManager.setConnectedServices(connectedServices);

        for(PlayerListener listener : mPlayerListenerList) {
            listener.onLoginSuccess(type);
        }
    }

    @Override
    public void onLoginError(ServiceType type) {
        List<ServiceType> connectedServices = mSettingsManager.getConnectedServices();
        connectedServices.remove(type);
        mSettingsManager.setConnectedServices(connectedServices);

        for(PlayerListener listener : mPlayerListenerList) {
            listener.onLoginError(type);
        }
    }

    @Override
    public void onLogoutSuccess(ServiceType type) {
        for(PlayerListener listener : mPlayerListenerList) {
            listener.onLogoutSuccess(type);
        }
    }

    @Override
    public void onLogoutError(ServiceType type) {
        for(PlayerListener listener : mPlayerListenerList) {
            listener.onLogoutError(type);
        }
    }

    @Override
    public void onPlayerPlaybackPositionChanged(int position) {
        if(mPlayerListenerList != null) {
            for (PlayerListener listener : mPlayerListenerList) {
                if (listener != null) {
                    listener.onPlayerPlaybackPositionChanged(position);
                }
            }
        }
    }

    private void initializeMediaSession() {
        mMediaSessionCompatInitialized = true;

        ComponentName cn = new ComponentName("eu.applabs.allplay", "eu.applabs.allplay.player.Player");
        Intent intent = new Intent(mActivity, mActivity.getClass());
        PendingIntent pi = PendingIntent.getActivity(mActivity, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mMediaSessionCompat = new MediaSessionCompat(mActivity, TAG, cn, pi);
        mMediaSessionCompat.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

        AudioManager am = (AudioManager) mActivity.getSystemService(Context.AUDIO_SERVICE);

        // Request audio focus for playback
        int result = am.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            if(mMediaSessionCompat != null) {
                PlaybackStateCompat.Builder stateBuilder = new PlaybackStateCompat.Builder().setActions(getAvailableActions());
                stateBuilder.setState(0, 100, 1.0f);

                mMediaSessionCompat.setPlaybackState(stateBuilder.build());
                new NowPlayingCardUpdater(mNowPlayingPlaylist.getCurrentSong()).start();
                mMediaSessionCompat.setActive(true);
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
        new NowPlayingCardUpdater(mNowPlayingPlaylist.getCurrentSong()).start();
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
                    Bitmap bitmap = Glide.with(mActivity).load(m_Song.getCoverBig()).asBitmap().into(800, 800).get();
                    metadataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_ART, bitmap);
                } catch (Exception e) {
                    Log.e(TAG, "Error during cover load");
                }

                if(mMediaSessionCompat != null) {
                    mMediaSessionCompat.setMetadata(metadataBuilder.build());
                }
            }
        }
    }
}
