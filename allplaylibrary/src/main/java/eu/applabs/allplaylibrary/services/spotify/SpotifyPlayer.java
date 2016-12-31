package eu.applabs.allplaylibrary.services.spotify;


import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Error;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.PlayerEvent;
import com.spotify.sdk.android.player.Spotify;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.inject.Inject;

import eu.applabs.allplaylibrary.AllPlayLibrary;
import eu.applabs.allplaylibrary.R;
import eu.applabs.allplaylibrary.data.MusicLibrary;
import eu.applabs.allplaylibrary.player.PlayerListener;
import eu.applabs.allplaylibrary.player.ServicePlayer;
import eu.applabs.allplaylibrary.data.Song;
import eu.applabs.allplaylibrary.services.ServiceType;
import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Pager;
import kaaes.spotify.webapi.android.models.PlaylistSimple;
import kaaes.spotify.webapi.android.models.SavedTrack;
import kaaes.spotify.webapi.android.models.UserPrivate;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class SpotifyPlayer implements ServicePlayer, Player.NotificationCallback, ConnectionStateCallback {

    private static final String TAG = SpotifyPlayer.class.getSimpleName();

    private Player mPlayer;
    private boolean mTrackEndBroadcastEnabled = true;
    private State mState = State.Idle;

    private static final int REQUEST_CODE = 1337;
    private UserPrivate mUser;
    private SpotifyService mSpotifyService;

    private List<PlayerListener> mIPlayerListenerList = new CopyOnWriteArrayList<>();

    @Inject
    protected MusicLibrary mMusicLibrary;

    @Inject
    protected Activity mActivity;

    private SpotifyServiceWrapper mSpotifyServiceWrapper;
    private SpotifyCategory mSpotifyCategoryPlaylists;
    private SpotifyCategory mSpotifyCategoryAlbums;
    private SpotifyCategory mSpotifyCategoryArtists;
    private SpotifyCategory mSpotifyCategorySongs;

    private Thread m_SpotifyPlaybackPositionCheckerThread;
    private SpotifyPlaybackPositionChecker m_SpotifyPlaybackPositionChecker;

    public SpotifyPlayer() {
        AllPlayLibrary.getInstance().component().inject(this);
    }

    @Override
    public void clearPlayer() {
        if(mPlayer != null) {
            mPlayer.pause(new Player.OperationCallback() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onError(Error error) {

                }
            });
        }

        mSpotifyServiceWrapper.clearLibrary();
        mMusicLibrary.removeMusicLibrary(mSpotifyServiceWrapper);

        Spotify.destroyPlayer(mActivity);
    }

    @Override
    public void login() {
        AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(
                mActivity.getResources().getString(R.string.spotify_client_id),
                AuthenticationResponse.Type.TOKEN,
                mActivity.getResources().getString(R.string.spotify_redirect_uri));

        builder.setScopes(mActivity.getResources().getStringArray(R.array.spotify_permissions));
        AuthenticationRequest request = builder.build();
        AuthenticationClient.openLoginActivity(mActivity, REQUEST_CODE, request);
    }

    @Override
    public void logout() {
        if(mMusicLibrary != null && mSpotifyServiceWrapper != null && mActivity != null) {
            mMusicLibrary.removeMusicLibrary(mSpotifyServiceWrapper);
            AuthenticationClient.stopLoginActivity(mActivity, 0);
        }
    }

    @Override
    public boolean checkActivityResult(int requestCode, int resultCode, Intent intent) {
        if(requestCode == REQUEST_CODE) {
            final AuthenticationResponse authenticationresponse = AuthenticationClient.getResponse(resultCode, intent);

            if (authenticationresponse.getType() == AuthenticationResponse.Type.TOKEN) {
                Config playerConfig = new Config(mActivity, authenticationresponse.getAccessToken(), mActivity.getResources().getString(R.string.spotify_client_id));
                Spotify.getPlayer(playerConfig, this, new com.spotify.sdk.android.player.SpotifyPlayer.InitializationObserver() {
                    @Override
                    public void onInitialized(com.spotify.sdk.android.player.SpotifyPlayer spotifyPlayer) {
                        addMusicLibrary();
                        notifyLoginSuccess();

                        mPlayer = spotifyPlayer;
                        m_SpotifyPlaybackPositionChecker = new SpotifyPlaybackPositionChecker(mIPlayerListenerList, mPlayer);
                        mPlayer.addConnectionStateCallback(SpotifyPlayer.this);
                        mPlayer.addNotificationCallback(SpotifyPlayer.this);

                        SpotifyApi api = new SpotifyApi();
                        api.setAccessToken(authenticationresponse.getAccessToken());
                        mSpotifyService = api.getService();
                        mSpotifyServiceWrapper.setSpotifyService(mSpotifyService);

                        mSpotifyService.getMe(new Callback<UserPrivate>() {
                            @Override
                            public void success(UserPrivate user, Response response) {
                                mUser = user;
                                mSpotifyServiceWrapper.setSpotifyUser(user);
                                loadPlaylists(user.id, 0);
                                loadSongsAndAlbums(0);
                            }

                            @Override
                            public void failure(RetrofitError retrofitError) {
                                int i = 0;
                                ++i;
                            }
                        });
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        notifyLoginError();
                    }
                });
            }

            return true;
        }

        return false;
    }

    @Override
    public ServiceType getServiceType() {
        return ServiceType.SPOTIFY;
    }

    @Override
    public State getPlayerState() {
        return mState;
    }

    @Override
    public boolean play(Song song) {
        if(song != null && song.getServiceType() == ServiceType.SPOTIFY) {
            if(mPlayer != null) {
                if(mState == State.Playing || mState == State.Paused) {
                    mTrackEndBroadcastEnabled = false;
                }

                mPlayer.playUri(new Player.OperationCallback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError(Error error) {

                    }
                }, song.getUri(), 0, 0);
                changeState(State.Playing);
            }

            return true;
        }

        return false;
    }

    @Override
    public boolean resume(Song song) {
        if(song != null && song.getServiceType() == ServiceType.SPOTIFY) {

            if(mPlayer != null) {
                mPlayer.resume(new Player.OperationCallback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError(Error error) {

                    }
                });
                changeState(State.Playing);
            }

            return true;
        }

        return false;
    }

    @Override
    public boolean pause(Song song) {
        if(song != null && song.getServiceType() == ServiceType.SPOTIFY) {

            if(mPlayer != null) {
                mPlayer.pause(new Player.OperationCallback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError(Error error) {

                    }
                });
                changeState(State.Paused);
            }

            return true;
        }

        return false;
    }

    @Override
    public boolean stop(Song song) {
        if(song != null && song.getServiceType() == ServiceType.SPOTIFY) {

            if(mPlayer != null) {
                mPlayer.pause(new Player.OperationCallback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError(Error error) {

                    }
                });
                changeState(State.Idle);
            }

            return true;
        }

        return false;
    }

    @Override
    public void registerListener(PlayerListener listener) {
        if(mIPlayerListenerList != null) {
            mIPlayerListenerList.add(listener);
        }
    }

    @Override
    public void unregisterListener(PlayerListener listener) {
        if(mIPlayerListenerList != null) {
            mIPlayerListenerList.remove(listener);
        }
    }

    @Override
    public void onLoggedIn() {

    }

    @Override
    public void onLoggedOut() {

    }

    @Override
    public void onLoginFailed(Error error) {

    }

    @Override
    public void onTemporaryError() {

    }

    @Override
    public void onConnectionMessage(String s) {

    }

    @Override
    public void onPlaybackEvent(PlayerEvent playerEvent) {
        if(playerEvent == PlayerEvent.kSpPlaybackNotifyContextChanged) {
            mTrackEndBroadcastEnabled = true;
        } else if (playerEvent == PlayerEvent.kSpPlaybackNotifyTrackDelivered && mTrackEndBroadcastEnabled) {
            processEvent(Event.TrackEnd);
        } else if (playerEvent == PlayerEvent.kSpPlaybackNotifyPlay) {
            m_SpotifyPlaybackPositionChecker.updateIPlayerListenerList(mIPlayerListenerList);
            m_SpotifyPlaybackPositionChecker.updatePlayer(mPlayer);
            m_SpotifyPlaybackPositionCheckerThread = new Thread(m_SpotifyPlaybackPositionChecker);
            m_SpotifyPlaybackPositionCheckerThread.start();
        } else if (playerEvent == PlayerEvent.kSpPlaybackNotifyPause) {
            try {
                m_SpotifyPlaybackPositionChecker.setStopFlag();
                m_SpotifyPlaybackPositionCheckerThread.join();
            } catch(Exception e) {
                Log.e(TAG, "Wait for stop failed");
            }
        }
    }

    @Override
    public void onPlaybackError(Error error) {
        if(error == Error.kSpErrorCorruptTrack) {
            processEvent(Event.Error);
        }
    }

    private void changeState(State new_state) {
        State old_state = mState;
        mState = new_state;

        for(PlayerListener listener : mIPlayerListenerList) {
            listener.onPlayerStateChanged(ServiceType.SPOTIFY, old_state, new_state);
        }
    }

    private void processEvent(Event event) {
        for(PlayerListener listener : mIPlayerListenerList) {
            listener.onPlayerEvent(event);
        }
    }

    private void notifyLoginSuccess() {
        for(PlayerListener listener : mIPlayerListenerList) {
            listener.onLoginSuccess(ServiceType.SPOTIFY);
        }
    }

    private void notifyLoginError() {
        for(PlayerListener listener : mIPlayerListenerList) {
            listener.onLoginError(ServiceType.SPOTIFY);
        }
    }

    private void addMusicLibrary() {
        mSpotifyServiceWrapper = new SpotifyServiceWrapper(mActivity);

        mSpotifyCategoryPlaylists = new SpotifyCategory(mActivity.getResources().getString(R.string.category_playlists));
        mSpotifyCategoryAlbums = new SpotifyCategory(mActivity.getResources().getString(R.string.category_albums));
        mSpotifyCategoryArtists = new SpotifyCategory(mActivity.getResources().getString(R.string.category_artists));
        mSpotifyCategorySongs = new SpotifyCategory(mActivity.getResources().getString(R.string.category_songs));

        mSpotifyServiceWrapper.addCategory(mSpotifyCategoryPlaylists);
        mSpotifyServiceWrapper.addCategory(mSpotifyCategoryAlbums);
        mSpotifyServiceWrapper.addCategory(mSpotifyCategoryArtists);
        mSpotifyServiceWrapper.addCategory(mSpotifyCategorySongs);

        mMusicLibrary.addMusicLibrary(mSpotifyServiceWrapper);
    }

    private void loadPlaylists(final String userId, int offset) {
        final int LIMIT = 50;
        Map<String, Object> optionMap = new HashMap<>();
        optionMap.put(SpotifyService.OFFSET, offset);
        optionMap.put(SpotifyService.LIMIT, LIMIT);

        mSpotifyService.getPlaylists(userId, optionMap, new Callback<Pager<PlaylistSimple>>() {
            @Override
            public void success(Pager<PlaylistSimple> playlistPager, Response response) {
                if (playlistPager != null && playlistPager.items != null) {

                    for (PlaylistSimple plist : playlistPager.items) {
                        SpotifyPlaylist spotifyPlaylist = new SpotifyPlaylist(mSpotifyService, plist.name, plist.owner.id, plist.id, plist.images);
                        mSpotifyService.getPlaylistTracks(plist.owner.id, plist.id, spotifyPlaylist.getCallbackPlaylistTracks());
                        mSpotifyCategoryPlaylists.addPlaylist(spotifyPlaylist);
                    }

                    if (playlistPager.next != null) {
                        loadPlaylists(userId, playlistPager.offset + LIMIT);
                    }
                }
            }

            @Override
            public void failure(RetrofitError retrofitError) {
            }
        });
    }

    private void loadSongsAndAlbums(int offset) {
        final int LIMIT = 50;
        Map<String, Object> optionMap = new HashMap<>();
        optionMap.put(SpotifyService.OFFSET, offset);
        optionMap.put(SpotifyService.LIMIT, LIMIT);

        final ArrayList<String> albums = new ArrayList<>();
        final ArrayList<String> artists = new ArrayList<>();
        final ArrayList<SavedTrack> songs = new ArrayList<>();

        mSpotifyService.getMySavedTracks(optionMap, new Callback<Pager<SavedTrack>>() {
            @Override
            public void success(Pager<SavedTrack> savedTrackPager, Response response) {
                if (savedTrackPager != null && savedTrackPager.items != null) {
                    for (SavedTrack track : savedTrackPager.items) {
                        songs.add(track);

                        if (!albums.contains(track.track.album.id)) {
                            albums.add(track.track.album.id);

                            SpotifyPlaylist albumPlaylist = new SpotifyPlaylist(mSpotifyService, track.track.album.name, "", "", track.track.album.images);
                            mSpotifyCategoryAlbums.addPlaylist(albumPlaylist);
                            mSpotifyService.getAlbum(track.track.album.id, albumPlaylist.getCallbackAlbum());
                        }

                        if (!artists.contains(track.track.artists.get(0).id)) {
                            artists.add(track.track.artists.get(0).id);

                            SpotifyPlaylist artistPlaylist = new SpotifyPlaylist(mSpotifyService, track.track.artists.get(0).name, "", "", null);
                            mSpotifyCategoryArtists.addPlaylist(artistPlaylist);

                            mSpotifyService.getArtist(track.track.artists.get(0).id, artistPlaylist.getCallbackArtist());
                            mSpotifyService.getArtistTopTrack(track.track.artists.get(0).id, mUser.country, artistPlaylist.getCallbackArtistTopTracks());
                        }
                    }

                    if (savedTrackPager.next != null) {
                        loadSongsAndAlbums(savedTrackPager.offset + LIMIT);
                    } else {
                        SpotifyPlaylist songsPlaylist;

                        if(songs.size() > 0) {
                            songsPlaylist = new SpotifyPlaylist(mSpotifyService, mActivity.getResources().getString(R.string.category_songs), "", "", songs.get(0).track.album.images);
                        } else {
                            songsPlaylist = new SpotifyPlaylist(mSpotifyService, mActivity.getResources().getString(R.string.category_songs), "", "", null);
                        }

                        for(SavedTrack track : songs) {
                            songsPlaylist.addSavedTrack(track);
                        }

                        mSpotifyCategorySongs.addPlaylist(songsPlaylist);
                    }
                }
            }

            @Override
            public void failure(RetrofitError retrofitError) {

            }
        });
    }
}
