package eu.applabs.allplaylibrary.services.spotify;

import android.app.Activity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import eu.applabs.allplaylibrary.R;
import eu.applabs.allplaylibrary.data.IMusicLibrary;
import eu.applabs.allplaylibrary.data.IMusicLibraryCategory;
import eu.applabs.allplaylibrary.data.IMusicLibraryPlaylist;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Album;
import kaaes.spotify.webapi.android.models.AlbumSimple;
import kaaes.spotify.webapi.android.models.AlbumsPager;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistSimple;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import kaaes.spotify.webapi.android.models.Playlist;
import kaaes.spotify.webapi.android.models.PlaylistSimple;
import kaaes.spotify.webapi.android.models.PlaylistsPager;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;
import kaaes.spotify.webapi.android.models.TracksPager;
import kaaes.spotify.webapi.android.models.UserPrivate;

public class SpotifyMusicLibrary implements IMusicLibrary {

    Activity m_Activity = null;

    SpotifyService m_SpotifyService = null;
    UserPrivate m_User = null;
    List<IMusicLibraryCategory> m_IMusicLibraryCategoryList = null;

    public SpotifyMusicLibrary(Activity activity) {
        m_Activity = activity;
        m_IMusicLibraryCategoryList = new CopyOnWriteArrayList<>();
    }

    @Override
    public void clearLibrary() {
        for(IMusicLibraryCategory category : m_IMusicLibraryCategoryList) {
            category.clearCategory();
        }

        m_IMusicLibraryCategoryList.clear();
    }

    @Override
    public List<IMusicLibraryCategory> getCategories() {
        return m_IMusicLibraryCategoryList;
    }

    public void setSpotifyService(SpotifyService service) {
        m_SpotifyService = service;
    }

    public void setSpotifyUser(UserPrivate user) {
        m_User = user;
    }

    public void addCategory(IMusicLibraryCategory category) {
        m_IMusicLibraryCategoryList.add(category);
    }

    public void removeCategory(IMusicLibraryCategory category) {
        m_IMusicLibraryCategoryList.remove(category);
    }

    @Override
    public List<IMusicLibraryCategory> search(String query) {
        if(m_SpotifyService != null) {
            List<IMusicLibraryCategory> result = new CopyOnWriteArrayList<>();

            ArtistsPager artistsPager = m_SpotifyService.searchArtists(query);

            if(artistsPager != null && artistsPager.artists != null && artistsPager.artists.items != null) {
                SpotifyCategory spotifyCategory = new SpotifyCategory(m_Activity.getResources().getString(R.string.category_artists));
                List<Artist> list = artistsPager.artists.items;

                for(Artist a : list) {
                    if(a != null && a.id != null && m_User != null) {
                        Tracks tracks = m_SpotifyService.getArtistTopTrack(a.id, m_User.country);

                        if (tracks != null) {
                            SpotifyPlaylist spotifyPlaylist = new SpotifyPlaylist(m_SpotifyService, a.name, "", "", a.images);
                            spotifyPlaylist.getCallbackArtistTopTracks().success(tracks, null);
                            spotifyCategory.addPlaylist(spotifyPlaylist);
                        }
                    }
                }

                if(spotifyCategory.getPlaylists() != null && spotifyCategory.getPlaylists().size() > 0) {
                    result.add(spotifyCategory);
                }
            }

            AlbumsPager albumsPager = m_SpotifyService.searchAlbums(query);

            if(albumsPager != null && albumsPager.albums != null && albumsPager.albums.items != null) {
                SpotifyCategory spotifyCategory = new SpotifyCategory(m_Activity.getResources().getString(R.string.category_albums));
                List<AlbumSimple> list = albumsPager.albums.items;

                for(AlbumSimple as : list) {
                    if(as != null && as.id != null) {
                        Album album = m_SpotifyService.getAlbum(as.id);

                        if (album != null) {
                            SpotifyPlaylist spotifyPlaylist = new SpotifyPlaylist(m_SpotifyService, album.name, "", "", album.images);
                            spotifyPlaylist.getCallbackAlbum().success(album, null);
                            spotifyCategory.addPlaylist(spotifyPlaylist);
                        }
                    }
                }

                if(spotifyCategory.getPlaylists() != null && spotifyCategory.getPlaylists().size() > 0) {
                    result.add(spotifyCategory);
                }
            }

            TracksPager trap = m_SpotifyService.searchTracks(query);

            if(trap != null && trap.tracks != null && trap.tracks.items != null) {
                SpotifyCategory spotifyCategory = new SpotifyCategory(m_Activity.getResources().getString(R.string.category_songs));
                List<Track> list = trap.tracks.items;

                for(Track t : list) {
                    SpotifyPlaylist spotifyPlaylist = new SpotifyPlaylist(m_SpotifyService, t.name, "", "", t.album.images);
                    spotifyPlaylist.addTrack(t);
                    spotifyCategory.addPlaylist(spotifyPlaylist);
                }

                if(spotifyCategory.getPlaylists() != null && spotifyCategory.getPlaylists().size() > 0) {
                    result.add(spotifyCategory);
                }
            }

            PlaylistsPager playlistsPager = m_SpotifyService.searchPlaylists(query);

            if(playlistsPager != null && playlistsPager.playlists != null && playlistsPager.playlists.items != null) {
                SpotifyCategory spotifyCategory = new SpotifyCategory(m_Activity.getResources().getString(R.string.category_playlists));
                List<PlaylistSimple> list = playlistsPager.playlists.items;

                for(PlaylistSimple ps : list) {
                    if(ps != null && ps.owner != null && ps.owner.id != null && ps.id != null) {
                        Playlist playlist = m_SpotifyService.getPlaylist(ps.owner.id, ps.id);

                        if (playlist != null) {
                            SpotifyPlaylist spotifyPlaylist = new SpotifyPlaylist(m_SpotifyService, playlist.name, ps.owner.id, ps.id, playlist.images);
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
