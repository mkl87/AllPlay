package eu.applabs.allplaylibrary.services.spotify;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import eu.applabs.allplaylibrary.AllPlayLibrary;
import eu.applabs.allplaylibrary.event.Event;
import eu.applabs.allplaylibrary.event.PlaylistEvent;
import eu.applabs.allplaylibrary.services.ServicePlaylist;
import eu.applabs.allplaylibrary.MusicCatalog;
import eu.applabs.allplaylibrary.data.Song;
import eu.applabs.allplaylibrary.services.ServiceType;
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

public class SpotifyPlaylist extends ServicePlaylist {

    @Inject
    protected MusicCatalog mMusicCatalog;

    private SpotifyService mSpotifyService;
    private String mName;
    private String mCoverUrl;
    private String mOwner;
    private String mId;

    private CallbackAlbum mCallbackAlbum = new CallbackAlbum();
    private CallbackPlaylist mCallbackPlaylist = new CallbackPlaylist();
    private CallbackPlaylistTracks mCallbackPlaylistTracks = new CallbackPlaylistTracks();
    private CallbackArtist mCallbackArtist = new CallbackArtist();
    private CallbackArtistTopTracks mCallbackArtistTopTracks = new CallbackArtistTopTracks();

    public SpotifyPlaylist(SpotifyService service, String name, String owner, String id, List<Image> images) {
        AllPlayLibrary.getInstance().component().inject(this);

        mSpotifyService = service;
        mName = name;
        mOwner = owner;
        mId = id;

        if(images != null && images.size() > 0) {
            mCoverUrl = images.get(0).url;
        } else {
            mCoverUrl = "";
        }

        addObserver(mMusicCatalog);
    }

    public void addSavedTrack(SavedTrack savedtrack) {
        if(savedtrack != null && savedtrack.track != null) {
            Song song = new Song();

            song.setTitle(savedtrack.track.name);
            song.setUri(savedtrack.track.uri);
            song.setServiceType(ServiceType.SPOTIFY);

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

            mSongList.add(song);
            notifyPlaylistUpdate();
        }
    }

    public void addTrack(Track track) {
        if(track != null) {
            Song song = new Song();

            song.setTitle(track.name);
            song.setUri(track.uri);
            song.setServiceType(ServiceType.SPOTIFY);

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

            mSongList.add(song);
            notifyPlaylistUpdate();
        }
    }

    public CallbackAlbum getCallbackAlbum() {
        return mCallbackAlbum;
    }

    public CallbackPlaylist getCallbackPlaylist() {
        return mCallbackPlaylist;
    }

    public CallbackArtist getCallbackArtist() {
        return mCallbackArtist;
    }

    public CallbackArtistTopTracks getCallbackArtistTopTracks() {
        return mCallbackArtistTopTracks;
    }

    public CallbackPlaylistTracks getCallbackPlaylistTracks() {
        return mCallbackPlaylistTracks;
    }

    @Override
    public String getPlaylistName() {
        return mName;
    }

    @Override
    public String getCoverUrl() {
        return mCoverUrl;
    }

    private void notifyPlaylistUpdate() {
        PlaylistEvent playlistEvent = new PlaylistEvent(SpotifyPlaylist.this);
        notifyObservers(playlistEvent);
    }

    public class CallbackPlaylistTracks implements Callback<Pager<PlaylistTrack>> {

        @Override
        public void success(Pager<PlaylistTrack> playlistTrackPager, Response response) {
            if(playlistTrackPager != null && playlistTrackPager.items != null) {
                for(PlaylistTrack ptrack : playlistTrackPager.items) {
                    Track track = ptrack.track;

                    if(track != null) {
                        Song song = new Song();

                        song.setTitle(track.name);
                        song.setUri(track.uri);
                        song.setServiceType(ServiceType.SPOTIFY);

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

                        mSongList.add(song);
                    }
                }

                if(playlistTrackPager.next != null && mId != null && mOwner != null) {
                    Map<String, Object> optionMap = new HashMap<>();
                    optionMap.put(SpotifyService.OFFSET, mSongList.size());
                    optionMap.put(SpotifyService.LIMIT, 100);

                    mSpotifyService.getPlaylistTracks(mOwner, mId, optionMap, getCallbackPlaylistTracks());
                } else {
                    notifyPlaylistUpdate();
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
                    song.setServiceType(ServiceType.SPOTIFY);

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

                    mSongList.add(song);
                }

                notifyPlaylistUpdate();
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
                    song.setServiceType(ServiceType.SPOTIFY);

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

                    mSongList.add(song);
                }

                notifyPlaylistUpdate();
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
                            mCoverUrl = image.url;
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
                    song.setServiceType(ServiceType.SPOTIFY);

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

                    mSongList.add(song);
                }

                notifyPlaylistUpdate();
            }
        }

        @Override
        public void failure(RetrofitError retrofitError) {

        }
    }
}
