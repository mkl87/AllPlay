package eu.applabs.allplaylibrary.services.deezer;

import com.deezer.sdk.model.Album;
import com.deezer.sdk.model.Playlist;
import com.deezer.sdk.model.Track;

import java.util.List;

import javax.inject.Inject;

import eu.applabs.allplaylibrary.AllPlayLibrary;
import eu.applabs.allplaylibrary.MusicCatalog;
import eu.applabs.allplaylibrary.event.Event;
import eu.applabs.allplaylibrary.event.PlaylistEvent;
import eu.applabs.allplaylibrary.services.ServicePlaylist;
import eu.applabs.allplaylibrary.data.Song;
import eu.applabs.allplaylibrary.services.ServiceType;

public class DeezerPlaylist extends ServicePlaylist {

    private String mName;
    private String mCover;

    @Inject
    protected MusicCatalog mMusicCatalog;

    public DeezerPlaylist(String name) {
        AllPlayLibrary.getInstance().component().inject(this);
        mName = name;
        addObserver(mMusicCatalog);
    }

    public void addSongs(Playlist playlist) {
        if(playlist != null) {
            mName = playlist.getTitle();
            mCover = playlist.getPictureUrl();

            for(Track t : playlist.getTracks()) {
                Song song = new Song();
                song.setTitle(t.getTitle());
                song.setAlbum(mName);
                song.setArtist(t.getArtist().getName());
                song.setCoverSmall(mCover);
                song.setId(String.valueOf(t.getId()));
                song.setServiceType(ServiceType.DEEZER);
                song.setUri(t.getPreviewUrl());
                song.setDuration((long) t.getDuration());

                mSongList.add(song);
            }

            notifyPlaylistUpdate();
        }
    }

    public void addSongs(Album album) {
        if(album != null) {
            mName = album.getTitle();
            mCover = album.getCoverUrl();

            for(Track t : album.getTracks()) {
                Song song = new Song();
                song.setTitle(t.getTitle());
                song.setAlbum(mName);
                song.setArtist(t.getArtist().getName());
                song.setCoverSmall(mCover);
                song.setId(String.valueOf(t.getId()));
                song.setServiceType(ServiceType.DEEZER);
                song.setUri(t.getPreviewUrl());
                song.setDuration((long) t.getDuration());

                mSongList.add(song);
            }

            notifyPlaylistUpdate();
        }
    }

    public void addSongs(List<Track> tracks) {
        if(tracks != null && tracks.size() > 0) {
            mCover = tracks.get(0).getAlbum().getCoverUrl(); // Maybe just show a star?!
        }

        for(Track t : tracks) {
            Song song = new Song();
            song.setTitle(t.getTitle());
            song.setAlbum(mName);
            song.setArtist(t.getArtist().getName());
            song.setCoverSmall(mCover);
            song.setId(String.valueOf(t.getId()));
            song.setServiceType(ServiceType.DEEZER);
            song.setUri(t.getPreviewUrl());
            song.setDuration((long) t.getDuration());

            mSongList.add(song);
        }

        notifyPlaylistUpdate();
    }

    @Override
    public String getPlaylistName() {
        return mName;
    }

    @Override
    public String getCoverUrl() {
        return mCover;
    }

    private void notifyPlaylistUpdate() {
        PlaylistEvent playlistEvent = new PlaylistEvent(this);

        notifyObservers(playlistEvent);
    }
}
