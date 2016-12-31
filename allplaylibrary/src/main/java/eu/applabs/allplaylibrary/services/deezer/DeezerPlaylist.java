package eu.applabs.allplaylibrary.services.deezer;

import com.deezer.sdk.model.Album;
import com.deezer.sdk.model.Playlist;
import com.deezer.sdk.model.Track;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.inject.Inject;

import eu.applabs.allplaylibrary.AllPlayLibrary;
import eu.applabs.allplaylibrary.data.ServicePlaylist;
import eu.applabs.allplaylibrary.data.MusicLibrary;
import eu.applabs.allplaylibrary.data.Song;
import eu.applabs.allplaylibrary.player.ServicePlayer;

public class DeezerPlaylist extends ServicePlaylist {

    private String mName;
    private String mCover;

    @Inject
    protected MusicLibrary mMusicLibrary;

    private List<OnPlaylistUpdateListener> mOnPlaylistUpdateListenerList = new ArrayList<>();

    public DeezerPlaylist(String name) {
        AllPlayLibrary.getInstance().component().inject(this);
        mName = name;
        registerListener(mMusicLibrary);
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
                song.setServiceType(ServicePlayer.ServiceType.Deezer);
                song.setUri(t.getPreviewUrl());
                song.setDuration((long) t.getDuration());

                mSongList.add(song);
            }

            notifyListener();
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
                song.setServiceType(ServicePlayer.ServiceType.Deezer);
                song.setUri(t.getPreviewUrl());
                song.setDuration((long) t.getDuration());

                mSongList.add(song);
            }

            notifyListener();
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
            song.setServiceType(ServicePlayer.ServiceType.Deezer);
            song.setUri(t.getPreviewUrl());
            song.setDuration((long) t.getDuration());

            mSongList.add(song);
        }

        notifyListener();
    }

    @Override
    public String getPlaylistName() {
        return mName;
    }

    @Override
    public String getCoverUrl() {
        return mCover;
    }
}
