package eu.applabs.allplaylibrary.services.spotify;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eu.applabs.allplaylibrary.data.ServicePlaylist;
import eu.applabs.allplaylibrary.data.MusicLibrary;
import eu.applabs.allplaylibrary.data.Song;
import eu.applabs.allplaylibrary.player.ServicePlayer;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Album;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistSimple;
import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Pager;
import kaaes.spotify.webapi.android.models.Playlist;
import kaaes.spotify.webapi.android.models.PlaylistTrack;
import kaaes.spotify.webapi.android.models.SavedTrack;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.TrackSimple;
import kaaes.spotify.webapi.android.models.Tracks;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class SpotifyPlaylist implements ServicePlaylist {

    private MusicLibrary m_MusicLibrary = null;
    private SpotifyService m_SpotifyService = null;
    private String m_Name = null;
    private String m_CoverUrl = null;
    private String m_Owner = null;
    private String m_Id = null;
    private List<Song> m_SongList = null;
    private List<OnPlaylistUpdateListener> mM_OnPlaylistUpdateListenerList = null;

    private CallbackAlbum m_CallbackAlbum = null;
    private CallbackPlaylist m_CallbackPlaylist = null;
    private CallbackPlaylistTracks m_CallbackPlaylistTracks = null;
    private CallbackArtist m_CallbackArtist = null;
    private CallbackArtistTopTracks m_CallbackArtistTopTracks = null;

    public SpotifyPlaylist(SpotifyService service, String name, String owner, String id, List<Image> images) {
        m_CallbackAlbum = new CallbackAlbum();
        m_CallbackPlaylist = new CallbackPlaylist();
        m_CallbackArtist = new CallbackArtist();
        m_CallbackArtistTopTracks = new CallbackArtistTopTracks();
        m_CallbackPlaylistTracks = new CallbackPlaylistTracks();

        m_SpotifyService = service;
        m_Name = name;
        m_Owner = owner;
        m_Id = id;
        m_SongList = new ArrayList<>();
        mM_OnPlaylistUpdateListenerList = new ArrayList<>();

        if(images != null && images.size() > 0) {
            m_CoverUrl = images.get(0).url;
        } else {
            m_CoverUrl = "";
        }

        m_MusicLibrary = MusicLibrary.getInstance();
        registerListener(m_MusicLibrary);
    }

    public void addSavedTrack(SavedTrack savedtrack) {
        if(savedtrack != null && savedtrack.track != null) {
            Song song = new Song();

            song.setTitle(savedtrack.track.name);
            song.setUri(savedtrack.track.uri);
            song.setServiceType(ServicePlayer.ServiceType.Spotify);

            List<ArtistSimple> artists = savedtrack.track.artists;
            if (artists.size() > 0) {
                song.setArtist(artists.get(0).name);
                song.setArtistUri(artists.get(0).uri);
            }

            for(Image image : savedtrack.track.album.images) {
                switch(image.width) {
                    case 640:
                        song.setCoverBig(image.url);
                        break;
                    case 300:
                        song.setCoverSmall(image.url);
                        break;
                }
            }

            m_SongList.add(song);
            notifyListener();
        }
    }

    public void addTrack(Track track) {
        if(track != null) {
            Song song = new Song();

            song.setTitle(track.name);
            song.setUri(track.uri);
            song.setServiceType(ServicePlayer.ServiceType.Spotify);

            List<ArtistSimple> artists = track.artists;
            if (artists.size() > 0) {
                song.setArtist(artists.get(0).name);
                song.setArtistUri(artists.get(0).uri);
            }

            for(Image image : track.album.images) {
                switch(image.width) {
                    case 640:
                        song.setCoverBig(image.url);
                        break;
                    case 300:
                        song.setCoverSmall(image.url);
                        break;
                }
            }

            m_SongList.add(song);
            notifyListener();
        }
    }

    public CallbackAlbum getCallbackAlbum() {
        return m_CallbackAlbum;
    }

    public CallbackPlaylist getCallbackPlaylist() {
        return m_CallbackPlaylist;
    }

    public CallbackArtist getCallbackArtist() {
        return m_CallbackArtist;
    }

    public CallbackArtistTopTracks getCallbackArtistTopTracks() {
        return m_CallbackArtistTopTracks;
    }

    public CallbackPlaylistTracks getCallbackPlaylistTracks() {
        return m_CallbackPlaylistTracks;
    }

    @Override
    public void clearPlaylist() {
        m_SongList.clear();
    }

    @Override
    public String getPlaylistName() {
        return m_Name;
    }

    @Override
    public int getSize() {
        if(m_SongList != null) {
            return m_SongList.size();
        }

        return 0;
    }

    @Override
    public String getCoverUrl() {
        return m_CoverUrl;
    }

    @Override
    public List<Song> getPlaylist() {
        return m_SongList;
    }

    @Override
    public void registerListener(OnPlaylistUpdateListener listener) {
        mM_OnPlaylistUpdateListenerList.add(listener);
    }

    @Override
    public void unregisterListener(OnPlaylistUpdateListener listener) {
        mM_OnPlaylistUpdateListenerList.remove(listener);
    }

    public class CallbackPlaylistTracks implements Callback<Pager<PlaylistTrack>> {

        @Override
        public void success(Pager<PlaylistTrack> playlistTrackPager, Response response) {
            if(playlistTrackPager != null && playlistTrackPager.items != null) {
                for(PlaylistTrack ptrack : playlistTrackPager.items) {
                    Song song = new Song();
                    Track track = ptrack.track;

                    song.setTitle(track.name);
                    song.setUri(track.uri);
                    song.setServiceType(ServicePlayer.ServiceType.Spotify);

                    List<ArtistSimple> artists = track.artists;
                    if (artists.size() > 0) {
                        song.setArtist(artists.get(0).name);
                        song.setArtistUri(artists.get(0).uri);
                    }

                    for (Image image : track.album.images) {
                        switch (image.width) {
                            case 640:
                                song.setCoverBig(image.url);
                                break;
                            case 300:
                                song.setCoverSmall(image.url);
                                break;
                        }
                    }

                    m_SongList.add(song);
                }

                if(playlistTrackPager.next != null && m_Id != null && m_Owner != null) {
                    Map<String, Object> optionMap = new HashMap<>();
                    optionMap.put(SpotifyService.OFFSET, m_SongList.size());
                    optionMap.put(SpotifyService.LIMIT, 100);

                    m_SpotifyService.getPlaylistTracks(m_Owner, m_Id, optionMap, getCallbackPlaylistTracks());
                } else {
                    notifyListener();
                }
            }
        }

        @Override
        public void failure(RetrofitError retrofitError) {

        }
    }

    public class CallbackPlaylist implements Callback<Playlist> {
        @Override
        public void success(Playlist playlist, Response response) {
            if(playlist != null && playlist.tracks != null && playlist.tracks.items != null) {
                for (PlaylistTrack ptrack : playlist.tracks.items) {
                    Song song = new Song();
                    Track track = ptrack.track;

                    song.setTitle(track.name);
                    song.setUri(track.uri);
                    song.setServiceType(ServicePlayer.ServiceType.Spotify);

                    List<ArtistSimple> artists = track.artists;
                    if (artists.size() > 0) {
                        song.setArtist(artists.get(0).name);
                        song.setArtistUri(artists.get(0).uri);
                    }

                    for(Image image : track.album.images) {
                        switch(image.width) {
                            case 640:
                                song.setCoverBig(image.url);
                                break;
                            case 300:
                                song.setCoverSmall(image.url);
                                break;
                        }
                    }

                    m_SongList.add(song);
                }

                notifyListener();
            }
        }

        @Override
        public void failure(RetrofitError retrofitError) {

        }
    }

    public class CallbackAlbum implements Callback<Album> {
        @Override
        public void success(Album album, Response response) {
            if(album != null && album.tracks != null) {
                for(TrackSimple strack : album.tracks.items) {
                    Song song = new Song();

                    song.setTitle(strack.name);
                    song.setUri(strack.uri);
                    song.setServiceType(ServicePlayer.ServiceType.Spotify);

                    List<ArtistSimple> artists = strack.artists;
                    if (artists.size() > 0) {
                        song.setArtist(artists.get(0).name);
                        song.setArtistUri(artists.get(0).uri);
                    }

                    for(Image image : album.images) {
                        switch(image.width) {
                            case 640:
                                song.setCoverBig(image.url);
                                break;
                            case 300:
                                song.setCoverSmall(image.url);
                                break;
                        }
                    }

                    m_SongList.add(song);
                }

                notifyListener();
            }
        }

        @Override
        public void failure(RetrofitError retrofitError) {

        }
    }

    public class CallbackArtist implements Callback<Artist> {
        @Override
        public void success(Artist artist, Response response) {
            if(artist != null && artist.images != null) {
                for(Image image : artist.images) {
                    switch(image.width) {
                        case 200:
                            m_CoverUrl = image.url;
                            break;
                    }
                }
            }
        }

        @Override
        public void failure(RetrofitError retrofitError) {

        }
    }

    public class CallbackArtistTopTracks implements Callback<Tracks> {
        @Override
        public void success(Tracks tracks, Response response) {
            if(tracks != null && tracks.tracks != null) {
                for (Track track : tracks.tracks) {
                    Song song = new Song();

                    song.setTitle(track.name);
                    song.setUri(track.uri);
                    song.setServiceType(ServicePlayer.ServiceType.Spotify);

                    List<ArtistSimple> artists = track.artists;
                    if (artists.size() > 0) {
                        song.setArtist(artists.get(0).name);
                        song.setArtistUri(artists.get(0).uri);
                    }

                    for (Image image : track.album.images) {
                        switch (image.width) {
                            case 640:
                                song.setCoverBig(image.url);
                                break;
                            case 300:
                                song.setCoverSmall(image.url);
                                break;
                        }
                    }

                    m_SongList.add(song);
                }

                notifyListener();
            }
        }

        @Override
        public void failure(RetrofitError retrofitError) {

        }
    }

    private void notifyListener() {
        for (OnPlaylistUpdateListener listener : mM_OnPlaylistUpdateListenerList) {
            if(listener != null) {
                listener.onPlaylistUpdate();
            }
        }
    }
}
