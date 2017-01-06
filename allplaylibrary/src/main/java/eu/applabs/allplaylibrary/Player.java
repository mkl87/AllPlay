package eu.applabs.allplaylibrary;

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

import eu.applabs.allplaylibrary.data.Observable;
import eu.applabs.allplaylibrary.data.SettingsManager;
import eu.applabs.allplaylibrary.data.Song;
import eu.applabs.allplaylibrary.event.Event;
import eu.applabs.allplaylibrary.event.PlayerEvent;
import eu.applabs.allplaylibrary.event.ServiceConnectionEvent;
import eu.applabs.allplaylibrary.services.ServicePlayer;
import eu.applabs.allplaylibrary.services.ServicePlaylist;
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
    protected Playlist mPlaylist;

    private List<ServicePlayer> mServicePlayerList = new ArrayList<>();
    private ServicePlayer mActiveServicePlayer;

    private boolean mMediaSessionCompatInitialized = false;
    private MediaSessionCompat mMediaSessionCompat;

    public Player() {
        AllPlayLibrary.getInstance().component().inject(this);
        mPlaylist.addObserver(this);

        for(ServiceType serviceType : mSettingsManager.getConnectedServiceTypes()) {
            connectServiceType(mActivity, serviceType);
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

        mPlaylist.deleteObserver(this);
        mPlaylist.clear();
        mPlaylist = null;

        if(mMediaSessionCompat != null && mMediaSessionCompat.isActive()) {
            mMediaSessionCompat.setActive(false);
            mMediaSessionCompat = null;
        }
    }

    public void setServicePlaylist(ServicePlaylist servicePlaylist) {
        mPlaylist.setServicePlaylist(servicePlaylist);
    }

    public ServicePlaylist getServicePlaylist() {
        return mPlaylist.getServicePlaylist();
    }

    /**
     * Method to connect a ServiceType
     *
     * Access level not specified to provide package only access !
     *
     * @param activity Activity (Used to connect the ServiceType)
     * @param serviceType ServiceType
     *
     * @return boolean (true = Connect started)
     */
    boolean connectServiceType(Activity activity, ServiceType serviceType) {
        ServicePlayer player = null;

        switch(serviceType) {
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

    /**
     * Method to disconnect a ServiceType
     *
     * Access level not specified to provide package only access !
     *
     * @param serviceType ServiceType
     *
     * @return boolean (true = Disconnect started)
     */
    boolean disconnectServiceType(ServiceType serviceType) {
        ServicePlayer player = null;

        for(ServicePlayer iplayer : mServicePlayerList) {
            if(iplayer.getServiceType() == serviceType) {
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

    /**
     * Method to check if the login was successful
     *
     * Access level not specified to provide package only access !
     *
     * @param requestCode Integer (Code of the request)
     * @param resultCode Integer (Code of the result)
     * @param intent Intent (Provides addition data)
     *
     * @return boolean (true = Event handled)
     */
    boolean checkActivityResult(int requestCode, int resultCode, Intent intent)
    {
        for(ServicePlayer player : mServicePlayerList) {
            if(player.checkActivityResult(requestCode, resultCode, intent)) {
                return true;
            }
        }

        return false;
    }

    public Playlist getPlaylist() {
        return mPlaylist;
    }

    public ServicePlayer.PlayerState getPlayerState() {
        if(mActiveServicePlayer != null) {
            return mActiveServicePlayer.getPlayerState();
        }

        return ServicePlayer.PlayerState.IDLE;
    }

    public void play() {
        Song song = mPlaylist.getCurrentSong();

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
        Song song = mPlaylist.getCurrentSong();

        if(mActiveServicePlayer != null && song != null) {
            mActiveServicePlayer.resume(song);
        }
    }

    public void pause() {
        Song song = mPlaylist.getCurrentSong();

        if(mActiveServicePlayer != null && song != null) {
            mActiveServicePlayer.pause(song);
        }
    }

    public void stop() {
        Song song = mPlaylist.getCurrentSong();

        if(mActiveServicePlayer != null && song != null) {
            mActiveServicePlayer.stop(song);
        }
    }

    public void next() {
        Song song = mPlaylist.getNextSong();

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
        Song song = mPlaylist.getPrevSong();

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
                new NowPlayingCardUpdater(mPlaylist.getCurrentSong()).start();
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
                case PLAYLIST_EVENT:
                    new NowPlayingCardUpdater(mPlaylist.getCurrentSong()).start();
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
                mPlaylist.remove(mPlaylist.getCurrentSong());
                next();
                break;
        }
    }

    private class NowPlayingCardUpdater extends Thread {

        private Song mSong = null;

        public NowPlayingCardUpdater(Song song) {
            mSong = song;
        }

        @Override
        public void run() {
            super.run();

            if(mSong != null) {
                MediaMetadataCompat.Builder metadataBuilder = new MediaMetadataCompat.Builder();
                metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_TITLE, mSong.getTitle());
                metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_ARTIST, mSong.getArtist());

                try {
                    Bitmap bitmap = Glide.with(mActivity).load(mSong.getCoverBig()).asBitmap().into(800, 800).get();
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
