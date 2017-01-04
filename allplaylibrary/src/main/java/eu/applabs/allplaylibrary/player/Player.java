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
import java.util.Observer;

import javax.inject.Inject;

import eu.applabs.allplaylibrary.AllPlayLibrary;
import eu.applabs.allplaylibrary.data.MusicCatalog;
import eu.applabs.allplaylibrary.data.Observable;
import eu.applabs.allplaylibrary.data.SettingsManager;
import eu.applabs.allplaylibrary.data.Song;
import eu.applabs.allplaylibrary.event.Event;
import eu.applabs.allplaylibrary.event.PlayerEvent;
import eu.applabs.allplaylibrary.event.ServiceConnectionEvent;
import eu.applabs.allplaylibrary.services.ServicePlayer;
import eu.applabs.allplaylibrary.services.ServiceType;
import eu.applabs.allplaylibrary.services.deezer.DeezerPlayer;
import eu.applabs.allplaylibrary.services.spotify.SpotifyPlayer;

public class Player extends Observable implements Observer, AudioManager.OnAudioFocusChangeListener {

    private static final String TAG = Player.class.getSimpleName();

    @Inject
    protected Activity mActivity;

    @Inject
    protected MusicCatalog mMusicCatalog;

    @Inject
    protected SettingsManager mSettingsManager;

    @Inject
    protected NowPlayingPlaylist mNowPlayingPlaylist;

    private List<ServicePlayer> mServicePlayerList = new ArrayList<>();
    private ServicePlayer mActiveServicePlayer;
    private boolean mMediaSessionCompatInitialized = false;
    private MediaSessionCompat mMediaSessionCompat;

    public Player() {
        AllPlayLibrary.getInstance().component().inject(this);
        mNowPlayingPlaylist.addObserver(this);

        for(ServiceType serviceType : mSettingsManager.getConnectedServiceTypes()) {
            login(mActivity, serviceType);
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

        mNowPlayingPlaylist.deleteObserver(this);
        mNowPlayingPlaylist.clear();
        mNowPlayingPlaylist = null;

        if(mMediaSessionCompat != null && mMediaSessionCompat.isActive()) {
            mMediaSessionCompat.setActive(false);
            mMediaSessionCompat = null;
        }
    }

    public boolean login(Activity activity, ServiceType type) {
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
            player.addObserver(this);
            player.login(activity);
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
            List<ServiceType> connectedServices = mSettingsManager.getConnectedServiceTypes();
            connectedServices.remove(player.getServiceType());
            mSettingsManager.setConnectedServices(connectedServices);

            player.deleteObserver(this);
            player.logout();
            ServiceConnectionEvent serviceConnectionEvent = new ServiceConnectionEvent(ServiceConnectionEvent.ServiceConnectionEventType.DISCONNECTED, player.getServiceType());
            handleServiceConnectionEvent(serviceConnectionEvent);
            player.clearPlayer();
            mServicePlayerList.remove(player);
        }

        return true;
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

    public ServicePlayer.PlayerState getPlayerState() {
        if(mActiveServicePlayer != null) {
            return mActiveServicePlayer.getPlayerState();
        }

        return ServicePlayer.PlayerState.IDLE;
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
    public void onAudioFocusChange(int focusChange) {

    }

    @Override
    public void update(java.util.Observable observable, Object o) {
        if(o instanceof Event) {
            Event event = (Event) o;

            switch (event.getEventType()) {
                case SERVICE_PLAYLIST_UPDATE:
                    new NowPlayingCardUpdater(mNowPlayingPlaylist.getCurrentSong()).start();
                    break;
                case SERVICE_CONNECTION_EVENT:
                    ServiceConnectionEvent serviceConnectionEvent = (ServiceConnectionEvent) event;
                    handleServiceConnectionEvent(serviceConnectionEvent);
                    break;
                case PLAYER_EVENT:
                    PlayerEvent playerEvent = (PlayerEvent) event;
                    handlePlayerEvent(playerEvent);
                    break;
            }
        }
    }

    private void handleServiceConnectionEvent(ServiceConnectionEvent serviceConnectionEvent) {
        List<ServiceType> connectedServices = mSettingsManager.getConnectedServiceTypes();

        switch (serviceConnectionEvent.getServiceConnectionEventType()) {
            case CONNECTED:
                connectedServices.add(serviceConnectionEvent.getServiceType());
                mSettingsManager.setConnectedServices(connectedServices);
                break;
            case DISCONNECTED:
                connectedServices.remove(serviceConnectionEvent.getServiceType());
                mSettingsManager.setConnectedServices(connectedServices);
                break;
        }

        for(Observer observer : mObserverList) {
            observer.update(this, serviceConnectionEvent);
        }
    }

    private void handlePlayerEvent(PlayerEvent playerEvent) {
        switch (playerEvent.getPlayerEventType()) {
            case STATE_CHANGED:
                for (Observer observer : mObserverList) {
                    observer.update(this, playerEvent);
                }
                break;
            case PLAYBACK_POSITION_CHANGED:
                for (Observer observer : mObserverList) {
                    observer.update(this, playerEvent);
                }
                break;
            case TRACK_END:
                next();
                break;
            case ERROR:
                mNowPlayingPlaylist.remove(mNowPlayingPlaylist.getCurrentSong());
                next();
                break;
        }
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
