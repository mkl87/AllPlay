package eu.applabs.allplaylibrary.services.spotify;

import android.app.Activity;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import eu.applabs.allplaylibrary.R;
import eu.applabs.allplaylibrary.data.ServiceLibrary;
import eu.applabs.allplaylibrary.data.ServiceCategory;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Album;
import kaaes.spotify.webapi.android.models.AlbumSimple;
import kaaes.spotify.webapi.android.models.AlbumsPager;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import kaaes.spotify.webapi.android.models.Playlist;
import kaaes.spotify.webapi.android.models.PlaylistSimple;
import kaaes.spotify.webapi.android.models.PlaylistsPager;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;
import kaaes.spotify.webapi.android.models.TracksPager;
import kaaes.spotify.webapi.android.models.UserPrivate;

public class SpotifyServiceWrapper extends ServiceLibrary {

    Activity mActivity = null;

    SpotifyService mSpotifyService = null;
    UserPrivate mUser = null;

    public SpotifyServiceWrapper(Activity activity) {
        mActivity = activity;
    }

    public void setSpotifyService(SpotifyService service) {
        mSpotifyService = service;
    }

    public void setSpotifyUser(UserPrivate user) {
        mUser = user;
    }

    @Override
    public List<ServiceCategory> search(String query) {
        if(mSpotifyService != null) {
            List<ServiceCategory> result = new CopyOnWriteArrayList<>();

            ArtistsPager artistsPager = mSpotifyService.searchArtists(query);

            if(artistsPager != null && artistsPager.artists != null && artistsPager.artists.items != null) {
                SpotifyCategory spotifyCategory = new SpotifyCategory(mActivity.getResources().getString(R.string.category_artists));
                List<Artist> list = artistsPager.artists.items;

                for(Artist a : list) {
                    if(a != null && a.id != null && mUser != null) {
                        Tracks tracks = mSpotifyService.getArtistTopTrack(a.id, mUser.country);

                        if (tracks != null) {
                            SpotifyPlaylist spotifyPlaylist = new SpotifyPlaylist(mSpotifyService, a.name, "", "", a.images);
                            spotifyPlaylist.getCallbackArtistTopTracks().success(tracks, null);
                            spotifyCategory.addPlaylist(spotifyPlaylist);
                        }
                    }
                }

                if(spotifyCategory.getPlaylists() != null && spotifyCategory.getPlaylists().size() > 0) {
                    result.add(spotifyCategory);
                }
            }

            AlbumsPager albumsPager = mSpotifyService.searchAlbums(query);

            if(albumsPager != null && albumsPager.albums != null && albumsPager.albums.items != null) {
                SpotifyCategory spotifyCategory = new SpotifyCategory(mActivity.getResources().getString(R.string.category_albums));
                List<AlbumSimple> list = albumsPager.albums.items;

                for(AlbumSimple as : list) {
                    if(as != null && as.id != null) {
                        Album album = mSpotifyService.getAlbum(as.id);

                        if (album != null) {
                            SpotifyPlaylist spotifyPlaylist = new SpotifyPlaylist(mSpotifyService, album.name, "", "", album.images);
                            spotifyPlaylist.getCallbackAlbum().success(album, null);
                            spotifyCategory.addPlaylist(spotifyPlaylist);
                        }
                    }
                }

                if(spotifyCategory.getPlaylists() != null && spotifyCategory.getPlaylists().size() > 0) {
                    result.add(spotifyCategory);
                }
            }

            TracksPager trap = mSpotifyService.searchTracks(query);

            if(trap != null && trap.tracks != null && trap.tracks.items != null) {
                SpotifyCategory spotifyCategory = new SpotifyCategory(mActivity.getResources().getString(R.string.category_songs));
                List<Track> list = trap.tracks.items;

                for(Track t : list) {
                    SpotifyPlaylist spotifyPlaylist = new SpotifyPlaylist(mSpotifyService, t.name, "", "", t.album.images);
                    spotifyPlaylist.addTrack(t);
                    spotifyCategory.addPlaylist(spotifyPlaylist);
                }

                if(spotifyCategory.getPlaylists() != null && spotifyCategory.getPlaylists().size() > 0) {
                    result.add(spotifyCategory);
                }
            }

            PlaylistsPager playlistsPager = mSpotifyService.searchPlaylists(query);

            if(playlistsPager != null && playlistsPager.playlists != null && playlistsPager.playlists.items != null) {
                SpotifyCategory spotifyCategory = new SpotifyCategory(mActivity.getResources().getString(R.string.category_playlists));
                List<PlaylistSimple> list = playlistsPager.playlists.items;

                for(PlaylistSimple ps : list) {
                    if(ps != null && ps.owner != null && ps.owner.id != null && ps.id != null) {
                        Playlist playlist = mSpotifyService.getPlaylist(ps.owner.id, ps.id);

                        if (playlist != null) {
                            SpotifyPlaylist spotifyPlaylist = new SpotifyPlaylist(mSpotifyService, playlist.name, ps.owner.id, ps.id, playlist.images);
                            spotifyPlaylist.getCallbackPlaylist().success(playlist, null);
                            spotifyCategory.addPlaylist(spotifyPlaylist);
                        }
                    }
                }

                if(spotifyCategory.getPlaylists() != null && spotifyCategory.getPlaylists().size() > 0) {
                    result.add(spotifyCategory);
                }
            }

            return result;
        }

        return null;
    }
}
