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

import eu.applabs.allplaylibrary.R;
import eu.applabs.allplaylibrary.data.MusicLibrary;
import eu.applabs.allplaylibrary.player.PlayerListener;
import eu.applabs.allplaylibrary.player.ServicePlayer;
import eu.applabs.allplaylibrary.data.Song;
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

    private static final String CLASSNAME = SpotifyPlayer.class.getSimpleName();

    private Activity m_Activity = null;
    private Player m_Player = null;
    private boolean m_TrackEndBroadcastEnabled = true;
    private State m_State = State.Idle;

    private static final int REQUEST_CODE = 1337;
    private UserPrivate m_User = null;
    private SpotifyService m_SpotifyService = null;

    private List<PlayerListener> m_IPlayerListenerList = null;

    private MusicLibrary m_MusicLibrary = null;
    private SpotifyServiceWrapper mM_SpotifyServiceWrapper = null;
    private SpotifyCategory m_SpotifyCategoryPlaylists = null;
    private SpotifyCategory m_SpotifyCategoryAlbums = null;
    private SpotifyCategory m_SpotifyCategoryArtists = null;
    private SpotifyCategory m_SpotifyCategorySongs = null;

    private Thread m_SpotifyPlaybackPositionCheckerThread = null;
    private SpotifyPlaybackPositionChecker m_SpotifyPlaybackPositionChecker = null;

    public SpotifyPlayer() {
        m_IPlayerListenerList = new CopyOnWriteArrayList<>();
    }

    @Override
    public void initialize(Activity activity) {
        if(activity != null) {
            m_Activity = activity;
        }
    }

    @Override
    public void clearPlayer() {
        if(m_Player != null) {
            m_Player.pause(new Player.OperationCallback() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onError(Error error) {

                }
            });
        }

        mM_SpotifyServiceWrapper.clearLibrary();
        m_MusicLibrary.removeMusicLibrary(mM_SpotifyServiceWrapper);

        Spotify.destroyPlayer(m_Activity);
    }

    @Override
    public void login() {
        AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(
                m_Activity.getResources().getString(R.string.spotify_client_id),
                AuthenticationResponse.Type.TOKEN,
                m_Activity.getResources().getString(R.string.spotify_redirect_uri));

        builder.setScopes(m_Activity.getResources().getStringArray(R.array.spotify_permissions));
        AuthenticationRequest request = builder.build();
        AuthenticationClient.openLoginActivity(m_Activity, REQUEST_CODE, request);
    }

    @Override
    public void logout() {
        if(m_MusicLibrary != null && mM_SpotifyServiceWrapper != null && m_Activity != null) {
            m_MusicLibrary.removeMusicLibrary(mM_SpotifyServiceWrapper);
            AuthenticationClient.stopLoginActivity(m_Activity, 0);
        }
    }

    @Override
    public boolean checkActivityResult(int requestCode, int resultCode, Intent intent) {
        if(requestCode == REQUEST_CODE) {
            final AuthenticationResponse authenticationresponse = AuthenticationClient.getResponse(resultCode, intent);

            if (authenticationresponse.getType() == AuthenticationResponse.Type.TOKEN) {
                Config playerConfig = new Config(m_Activity, authenticationresponse.getAccessToken(), m_Activity.getResources().getString(R.string.spotify_client_id));
                Spotify.getPlayer(playerConfig, this, new com.spotify.sdk.android.player.SpotifyPlayer.InitializationObserver() {
                    @Override
                    public void onInitialized(com.spotify.sdk.android.player.SpotifyPlayer spotifyPlayer) {
                        addMusicLibrary();
                        notifyLoginSuccess();

                        m_Player = spotifyPlayer;
                        m_SpotifyPlaybackPositionChecker = new SpotifyPlaybackPositionChecker(m_IPlayerListenerList, m_Player);
                        m_Player.addConnectionStateCallback(SpotifyPlayer.this);
                        m_Player.addNotificationCallback(SpotifyPlayer.this);

                        SpotifyApi api = new SpotifyApi();
                        api.setAccessToken(authenticationresponse.getAccessToken());
                        m_SpotifyService = api.getService();
                        mM_SpotifyServiceWrapper.setSpotifyService(m_SpotifyService);

                        m_SpotifyService.getMe(new Callback<UserPrivate>() {
                            @Override
                            public void success(UserPrivate user, Response response) {
                                m_User = user;
                                mM_SpotifyServiceWrapper.setSpotifyUser(user);
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
        return ServiceType.Spotify;
    }

    @Override
    public State getPlayerState() {
        return m_State;
    }

    @Override
    public boolean play(Song song) {
        if(song != null && song.getServiceType() == ServiceType.Spotify) {
            if(m_Player != null) {
                if(m_State == State.Playing || m_State == State.Paused) {
                    m_TrackEndBroadcastEnabled = false;
                }

                m_Player.playUri(new Player.OperationCallback() {
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
        if(song != null && song.getServiceType() == ServiceType.Spotify) {

            if(m_Player != null) {
                m_Player.resume(new Player.OperationCallback() {
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
        if(song != null && song.getServiceType() == ServiceType.Spotify) {

            if(m_Player != null) {
                m_Player.pause(new Player.OperationCallback() {
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
        if(song != null && song.getServiceType() == ServiceType.Spotify) {

            if(m_Player != null) {
                m_Player.pause(new Player.OperationCallback() {
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
        if(m_IPlayerListenerList != null) {
            m_IPlayerListenerList.add(listener);
        }
    }

    @Override
    public void unregisterListener(PlayerListener listener) {
        if(m_IPlayerListenerList != null) {
            m_IPlayerListenerList.remove(listener);
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
            m_TrackEndBroadcastEnabled = true;
        } else if (playerEvent == PlayerEvent.kSpPlaybackNotifyTrackDelivered && m_TrackEndBroadcastEnabled) {
            processEvent(Event.TrackEnd);
        } else if (playerEvent == PlayerEvent.kSpPlaybackNotifyPlay) {
            m_SpotifyPlaybackPositionChecker.updateIPlayerListenerList(m_IPlayerListenerList);
            m_SpotifyPlaybackPositionChecker.updatePlayer(m_Player);
            m_SpotifyPlaybackPositionCheckerThread = new Thread(m_SpotifyPlaybackPositionChecker);
            m_SpotifyPlaybackPositionCheckerThread.start();
        } else if (playerEvent == PlayerEvent.kSpPlaybackNotifyPause) {
            try {
                m_SpotifyPlaybackPositionChecker.setStopFlag();
                m_SpotifyPlaybackPositionCheckerThread.join();
            } catch(Exception e) {
                Log.e(CLASSNAME, "Wait for stop failed");
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
        State old_state = m_State;
        m_State = new_state;

        for(PlayerListener listener : m_IPlayerListenerList) {
            listener.onPlayerStateChanged(ServiceType.Spotify, old_state, new_state);
        }
    }

    private void processEvent(Event event) {
        for(PlayerListener listener : m_IPlayerListenerList) {
            listener.onPlayerEvent(event);
        }
    }

    private void notifyLoginSuccess() {
        for(PlayerListener listener : m_IPlayerListenerList) {
            listener.onLoginSuccess(ServiceType.Spotify);
        }
    }

    private void notifyLoginError() {
        for(PlayerListener listener : m_IPlayerListenerList) {
            listener.onLoginError(ServiceType.Spotify);
        }
    }

    private void addMusicLibrary() {
        m_MusicLibrary = MusicLibrary.getInstance();
        mM_SpotifyServiceWrapper = new SpotifyServiceWrapper(m_Activity);

        m_SpotifyCategoryPlaylists = new SpotifyCategory(m_Activity.getResources().getString(R.string.category_playlists));
        m_SpotifyCategoryAlbums = new SpotifyCategory(m_Activity.getResources().getString(R.string.category_albums));
        m_SpotifyCategoryArtists = new SpotifyCategory(m_Activity.getResources().getString(R.string.category_artists));
        m_SpotifyCategorySongs = new SpotifyCategory(m_Activity.getResources().getString(R.string.category_songs));

        mM_SpotifyServiceWrapper.addCategory(m_SpotifyCategoryPlaylists);
        mM_SpotifyServiceWrapper.addCategory(m_SpotifyCategoryAlbums);
        mM_SpotifyServiceWrapper.addCategory(m_SpotifyCategoryArtists);
        mM_SpotifyServiceWrapper.addCategory(m_SpotifyCategorySongs);

        m_MusicLibrary.addMusicLibrary(mM_SpotifyServiceWrapper);
    }

    private void loadPlaylists(final String userId, int offset) {
        final int LIMIT = 50;
        Map<String, Object> optionMap = new HashMap<>();
        optionMap.put(SpotifyService.OFFSET, offset);
        optionMap.put(SpotifyService.LIMIT, LIMIT);

        m_SpotifyService.getPlaylists(userId, optionMap, new Callback<Pager<PlaylistSimple>>() {
            @Override
            public void success(Pager<PlaylistSimple> playlistPager, Response response) {
                if (playlistPager != null && playlistPager.items != null) {

                    for (PlaylistSimple plist : playlistPager.items) {
                        SpotifyPlaylist spotifyPlaylist = new SpotifyPlaylist(m_SpotifyService, plist.name, plist.owner.id, plist.id, plist.images);
                        m_SpotifyService.getPlaylistTracks(plist.owner.id, plist.id, spotifyPlaylist.getCallbackPlaylistTracks());
                        m_SpotifyCategoryPlaylists.addPlaylist(spotifyPlaylist);
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

        m_SpotifyService.getMySavedTracks(optionMap, new Callback<Pager<SavedTrack>>() {
            @Override
            public void success(Pager<SavedTrack> savedTrackPager, Response response) {
                if (savedTrackPager != null && savedTrackPager.items != null) {
                    for (SavedTrack track : savedTrackPager.items) {
                        songs.add(track);

                        if (!albums.contains(track.track.album.id)) {
                            albums.add(track.track.album.id);

                            SpotifyPlaylist albumPlaylist = new SpotifyPlaylist(m_SpotifyService, track.track.album.name, "", "", track.track.album.images);
                            m_SpotifyCategoryAlbums.addPlaylist(albumPlaylist);
                            m_SpotifyService.getAlbum(track.track.album.id, albumPlaylist.getCallbackAlbum());
                        }

                        if (!artists.contains(track.track.artists.get(0).id)) {
                            artists.add(track.track.artists.get(0).id);

                            SpotifyPlaylist artistPlaylist = new SpotifyPlaylist(m_SpotifyService, track.track.artists.get(0).name, "", "", null);
                            m_SpotifyCategoryArtists.addPlaylist(artistPlaylist);

                            m_SpotifyService.getArtist(track.track.artists.get(0).id, artistPlaylist.getCallbackArtist());
                            m_SpotifyService.getArtistTopTrack(track.track.artists.get(0).id, m_User.country, artistPlaylist.getCallbackArtistTopTracks());
                        }
                    }

                    if (savedTrackPager.next != null) {
                        loadSongsAndAlbums(savedTrackPager.offset + LIMIT);
                    } else {
                        SpotifyPlaylist songsPlaylist;

                        if(songs.size() > 0) {
                            songsPlaylist = new SpotifyPlaylist(m_SpotifyService, m_Activity.getResources().getString(R.string.category_songs), "", "", songs.get(0).track.album.images);
                        } else {
                            songsPlaylist = new SpotifyPlaylist(m_SpotifyService, m_Activity.getResources().getString(R.string.category_songs), "", "", null);
                        }

                        for(SavedTrack track : songs) {
                            songsPlaylist.addSavedTrack(track);
                        }

                        m_SpotifyCategorySongs.addPlaylist(songsPlaylist);
                    }
                }
            }

            @Override
            public void failure(RetrofitError retrofitError) {

            }
        });
    }
}
